package flow.interpreter.visitor;


import flow.FlowBaseVisitor;
import flow.FlowParser;
import flow.interpreter.exception.FlowException;
import flow.interpreter.scope.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static flow.interpreter.util.Helpers.*;

public class InterpreterVisitor extends FlowBaseVisitor<Object> {

    protected final StringBuilder SYSTEM_OUT = new StringBuilder();
    protected final SymbolTable symbolTable;
    private final IfStatementVisitor ifStatementVisitor;
    private final IoStatementVisitor ioStatementVisitor;


    public InterpreterVisitor(SymbolTable symbolTable) {
        this.symbolTable = symbolTable;
        this.ifStatementVisitor = new IfStatementVisitor(this);
        this.ioStatementVisitor = new IoStatementVisitor(this);
    }

    @Override
    public Object visitProgram(FlowParser.ProgramContext ctx) {
        super.visitProgram(ctx);
        symbolTable.clear();

        return SYSTEM_OUT.toString();
    }

    @Override
    public Object visitGlobalStatement(FlowParser.GlobalStatementContext ctx) {
        return super.visitGlobalStatement(ctx);
    }

    @Override
    public Object visitClassDeclaration(FlowParser.ClassDeclarationContext ctx) {
        symbolTable.defineClass(new ClassDeclaration(ctx.ID().getText(), ctx));
        return null;
    }

    @Override
    public Object visitObjectDeclaration(FlowParser.ObjectDeclarationContext ctx) {

        String objectName = ctx.ID().get(0).getText();
        String className = ctx.ID().get(1).getText();
        boolean isMutable = Objects.equals(ctx.VARIABLE().getText(), "var");

        FlowParser.MethodArgsContext objectArgs = ctx.methodArgs();
        ClassDeclaration classDeclaration = symbolTable.getClassDeclaration(className);

        if (classDeclaration == null) {
            throw new FlowException("Class " + className + " does not exist.");
        }

        Scope currentScope = symbolTable.currentScope();

        if (currentScope.resolve(objectName) != null) {
            throw new FlowException("Object " + objectName + " already exists.");
        }

        Symbol objectSymbol = new Symbol(objectName, classDeclaration, className, isMutable);
        symbolTable.defineCurrentScopeValue(objectSymbol);

        ClassScope classMembersScope = symbolTable.pushClassScope();
        classMembersScope.setScopeName(objectName);
        objectSymbol.setScope(classMembersScope);
        classDeclaration.getClassFieldsContext().forEach(this::visit);

        List<FlowParser.MethodDeclarationContext> methodsContext = classDeclaration.getMethodDeclarationContext();
        methodsContext.forEach(methodContext -> {
            String methodName = methodContext.ID().getText();
            String methodType = methodContext.TYPE() == null ?
                    methodContext.UNIT().getText() :
                    methodContext.TYPE().getText();
            classMembersScope.defineMethod(new MethodDeclaration(methodName, methodType, methodContext));
        });


        FlowParser.ClassConstructorContext classConstructorContext = classDeclaration.getConstructorDeclarationContext();
        visitClassConstructor(classConstructorContext, objectArgs);

        symbolTable.popScope();

        return null;
    }


    @Override
    public Object visitClassConstructor(FlowParser.ClassConstructorContext ctx) {
        return null;
    }

    public void visitClassConstructor(FlowParser.ClassConstructorContext ctx, FlowParser.MethodArgsContext objectArgs) {

        if (ctx == null && objectArgs != null && !objectArgs.expression().isEmpty()) {
            throw new FlowException("Expected no arguments but got " + objectArgs.expression().size() + ".");
        }

        if (ctx == null) {
            return;
        }

        symbolTable.pushLocalScope();

        if (objectArgs == null) {
            if (!ctx.methodParams().ID().isEmpty()) {
                throw new FlowException("Wrong number of arguments.");
            }
        } else {
            if (objectArgs.expression().size() != ctx.methodParams().ID().size()) {
                throw new FlowException("Wrong number of arguments.");
            }

            for (int i = 0; i < objectArgs.expression().size(); i++) {
                String argName = ctx.methodParams().ID(i).getText();
                String argType = ctx.methodParams().TYPE(i).getText();
                Object argValue = visit(objectArgs.expression(i));

                // check if argType is same as argValue type
                if (!argType.equals("Unit") && !argType.equals(getClassName(argValue))) {
                    throw new FlowException("Wrong argument " + argName + " type. Expected " + argType + " but got " + argValue.getClass().getSimpleName() + ".");
                }

                symbolTable.defineCurrentScopeValue(new Symbol(argName, argValue, argType, true));
            }
        }

        visit(ctx.controlStructureBody());
        symbolTable.popScope();

    }

    @Override
    public Object visitControlStructureBody(FlowParser.ControlStructureBodyContext ctx) {

        Object o = null;

        symbolTable.pushLocalScope();

        for (FlowParser.StatementContext statementContext : ctx.statement()) {
            o = visit(statementContext);

            if (o != null) {
                symbolTable.popScope();
                return o;
            }
        }

        symbolTable.popScope();

        return o;
    }

    @Override
    public Object visitClassMember(FlowParser.ClassMemberContext ctx) {
        return super.visitClassMember(ctx);
    }


    @Override
    public Object visitVariableDeclaration(FlowParser.VariableDeclarationContext ctx) {
        String varName = ctx.ID().getText();
        boolean isMutable = Objects.equals(ctx.VARIABLE().getText(), "var");

        if (ctx.TYPE() == null && ctx.expression() == null) {
            throw new FlowException("Variable `" + varName + "` must have a type or an expression.");
        }

        Object value = ctx.expression() != null ? visit(ctx.expression()) : null;
        String type = value == null ? ctx.TYPE().getText() : getClassName(value);

        // QS Removed default value for type
/*        if (type != null && value == null) {
            value = getObjectDefaultValue(type);
        }*/

        Scope currentScope = symbolTable.currentScope();

        if (currentScope.resolve(varName) != null) {
            throw new FlowException("Variable `" + varName + "` is already declared.");
        }

        symbolTable.defineCurrentScopeValue(new Symbol(varName, value, type, isMutable));

        return null;
    }


    @Override
    public Object visitMethodInvocation(FlowParser.MethodInvocationContext ctx) {

        String objectName;
        String methodName;

        int size = ctx.ID().size();

        FlowParser.MethodArgsContext argsContext = ctx.methodArgs();
        Scope currentScope = symbolTable.currentScope();


        if (size == 2) {
            objectName = ctx.ID(0).getText();
            methodName = ctx.ID(1).getText();
        } else {

            // get class scope
            Scope classScope = currentScope;
            while (classScope != null && classScope.getType() != ScopeType.CLASS) {
                classScope = classScope.getParent();
            }

            if ((classScope != null ? classScope.getType() : null) == ScopeType.CLASS) {
                objectName = classScope.getScopeName();
            } else {
                objectName = null;
            }

            methodName = ctx.ID(0).getText();
        }


        // global method
        if (objectName == null) {
            MethodDeclaration methodDeclaration = symbolTable.getGlobalMethod(methodName);

            if (methodDeclaration == null) {
                throw new FlowException("Method `" + methodName + "` does not exist.");
            }

            FlowParser.MethodDeclarationContext context = methodDeclaration.getMethodDeclarationContext();

            List<Object> args = (List<Object>) visitMethodArgs(argsContext);

            Scope methodScope = symbolTable.pushLocalScope();
            methodScope.setParent(symbolTable.getScope(0));

            visitMethodParams(methodDeclaration.getMethodDeclarationContext().methodParams(), args);


            return getMethodReturnValue(methodName, context);
        }


        // class method
        Symbol object = currentScope.resolve(objectName);

        // check if object exists
        if (object == null) {
            throw new FlowException("Object " + objectName + " does not exist.");
        }

        ClassDeclaration classDeclaration = symbolTable.getClassDeclaration(object.getType());

        if (classDeclaration == null) {
            throw new FlowException("Class " + object.getType() + " does not exist.");
        }

        FlowParser.MethodDeclarationContext methodDeclaration = classDeclaration.getMethodDeclarationContext(methodName);

        if (methodDeclaration == null) {
            throw new FlowException("Method " + methodName + " does not exist.");
        }

        List<Object> args = (List<Object>) visitMethodArgs(argsContext);

        Scope classScope = object.getScope();
        Scope methodScope = symbolTable.pushLocalScope();
        methodScope.setParent(classScope);

        visitMethodParams(methodDeclaration.methodParams(), args);


        return getMethodReturnValue(methodName, methodDeclaration);
    }

    @Override
    public Object visitMethodArgs(FlowParser.MethodArgsContext ctx) {

        List<Object> args = new ArrayList<>();

        if (ctx == null || ctx.expression() == null) {
            return args;
        }

        for (FlowParser.ExpressionContext expressionContext : ctx.expression()) {
            Object result = visit(expressionContext);
            args.add(result);
        }

        return args;
    }


    public Object visitMethodParams(FlowParser.MethodParamsContext ctx, List<Object> args) {
        if (ctx == null || ctx.ID() == null) {
            return null;
        }

        if (ctx.ID().size() != args.size()) {
            throw new FlowException("Wrong number of arguments. Expected " + ctx.ID().size() + " but got " + args.size() + ".");
        }

        for (int i = 0; i < ctx.ID().size(); i++) {
            String paramName = ctx.ID(i).getText();
            String paramType = ctx.TYPE(i).getText();
            Object argValue = args.get(i);

            // check if argType is same as argValue type
            if (!paramType.equals("Unit") && !paramType.equals(getClassName(argValue))) {
                throw new FlowException("Wrong argument " + paramName + " type. Expected " + paramType + " but got " + argValue.getClass().getSimpleName() + ".");
            }

            symbolTable.defineCurrentScopeValue(new Symbol(paramName, argValue, paramType, true));
        }


        return null;
    }

    private Object getMethodReturnValue(String methodName, FlowParser.MethodDeclarationContext methodDeclaration) {
        Object returnValue = visit(methodDeclaration.methodStructureBody());
        String returnType = methodDeclaration.TYPE() == null ?
                methodDeclaration.UNIT().getText() :
                methodDeclaration.TYPE().getText();

        List<FlowParser.MethodBodyStatementContext> bodyStatements = methodDeclaration
                .methodStructureBody()
                .methodBodyStatement();

        FlowParser.MethodBodyStatementContext lastStatement = bodyStatements.get(bodyStatements.size() - 1);
        FlowParser.ReturnStatementContext returnStatementContext = lastStatement.statement().returnStatement();

        if (!returnType.equals("Unit") && returnStatementContext == null) {
            throw new FlowException("Missing return statement in `" + methodName + "` method. Expected return type `" + returnType + "`.");
        }

        if (returnValue != null) {
            if (!returnType.equals(getClassName(returnValue))) {
                throw new FlowException("Return type is not the same as method return type. Expected " + returnType + " but got " + getClassName(returnValue) + ".");
            }
        } else if (!returnType.equals("Unit")) {
            throw new FlowException("Missing return statement. Expected " + returnType + ".");
        }


        symbolTable.popScope();

        return returnValue;
    }

    @Override
    public Object visitMethodDeclaration(FlowParser.MethodDeclarationContext ctx) {
        String methodName = ctx.ID().getText();
        String methodType = ctx.TYPE() != null ? ctx.TYPE().getText() : "Unit";

        symbolTable.defineGlobalMethod(new MethodDeclaration(methodName, methodType, ctx));
        return null;
    }

    @Override
    public Object visitMethodStructureBody(FlowParser.MethodStructureBodyContext ctx) {

        Object returnedValue = null;

        for (FlowParser.MethodBodyStatementContext methodBodyContext : ctx.methodBodyStatement()) {
            if (methodBodyContext.statement().returnStatement() != null) {
                if (methodBodyContext.statement().returnStatement().expression() == null) {
                    return null;
                }
                return visit(methodBodyContext.statement().returnStatement().expression());
            }
            returnedValue = visit(methodBodyContext);

            if (returnedValue != null) {
                return returnedValue;
            }

        }

        return returnedValue;
    }

    @Override
    public Object visitReturnStatement(FlowParser.ReturnStatementContext ctx) {
        if (ctx.expression() == null) {
            return null;
        }

        return visit(ctx.expression());
    }

    @Override
    public Object visitClassObjectVariableSetter(FlowParser.ClassObjectVariableSetterContext ctx) {

        String objectName = ctx.ID(0).getText();
        String variableName = ctx.ID(1).getText();
        Object value = visit(ctx.expression());

        Scope currentScope = symbolTable.currentScope();
        Symbol object = currentScope.resolve(objectName);

        if (object == null) {
            throw new FlowException("Object `" + objectName + "` is not declared.");
        }

        // get object class scope
        Scope classScope = object.getScope();
        Symbol variable = classScope.resolve(variableName);

        if (variable == null) {
            throw new FlowException("Variable `" + variableName + "` is not declared.");
        }

        if (!variable.getType().equals(getClassName(value))) {
            throw new FlowException("Wrong type of variable `" + variableName + "`. Expected " + variable.getType() + " but got " + getClassName(value) + ".");
        }

        variable.setValue(value);


        return null;
    }

    @Override
    public Object visitClassObjectVariableGetter(FlowParser.ClassObjectVariableGetterContext ctx) {

        String objectName = ctx.ID(0).getText();
        String variableName = ctx.ID(1).getText();

        Scope currentScope = symbolTable.currentScope();
        Symbol object = currentScope.resolve(objectName);

        if (object == null) {
            throw new FlowException("Object `" + objectName + "` is not declared.");
        }

        Scope classScope = object.getScope();
        Symbol variable = classScope.resolve(variableName);

        if (variable == null) {
            throw new FlowException("Variable `" + variableName + "` is not declared.");
        }

        return variable.getValue();
    }

    public void visitMethodArgs(FlowParser.MethodArgsContext ctx, FlowParser.MethodDeclarationContext methodDeclaration) {

        if (ctx.expression().size() != methodDeclaration.methodParams().ID().size()) {
            throw new FlowException("Wrong number of arguments.");
        }

        for (int i = 0; i < ctx.expression().size(); i++) {
            String argName = methodDeclaration.methodParams().ID(i).getText();
            String argType = methodDeclaration.methodParams().TYPE(i).getText();
            Object argValue = visit(ctx.expression(i));

            // check if argType is same as argValue type
            if (!argType.equals("Unit") && !argType.equals(getClassName(argValue))) {
                throw new FlowException("Wrong argument " + argName + " type. Expected " + argType + " but got " + argValue.getClass().getSimpleName() + ".");
            }

            symbolTable.defineCurrentScopeValue(new Symbol(argName, argValue, argType, true));
        }
    }

    @Override
    public Object visitVariableAssignment(FlowParser.VariableAssignmentContext ctx) {
        String varName = ctx.ID().getText();
        Object value = visit(ctx.expression());

        Scope currentScope = symbolTable.currentScope();
        Symbol symbol = currentScope.resolve(varName);


        if (symbol == null) {
            throw new FlowException("Undeclared variable " + varName + ".");
        }

        if (!symbol.getType().equals(getClassName(value))) {
            throw new FlowException("Wrong type of variable `" + varName + "`. Expected " + symbol.getType() + " but got " + getClassName(value) + ".");
        }

        symbol.setValue(value);

        return null;
    }

    @Override
    public Object visitIdExpression(FlowParser.IdExpressionContext ctx) {
        Scope currentScope = symbolTable.currentScope();
        Symbol symbol = currentScope.resolve(ctx.ID().getText());

        if (symbol == null) {
            throw new FlowException("Undeclared variable " + ctx.ID().getText() + ".");
        }

        Object value = symbol.getValue();

        // QS default value for variables
        if (value == null) {
            throw new FlowException("Variable " + ctx.ID().getText() + " is not initialized.");
        }

        return value;
    }

    @Override
    public Object visitIntExpression(FlowParser.IntExpressionContext ctx) {
        return Integer.parseInt(ctx.INT().getText());
    }

    @Override
    public Object visitStringExpression(FlowParser.StringExpressionContext ctx) {
        String value = ctx.STRING().getText();
        return value.substring(1, value.length() - 1);
    }

    @Override
    public Object visitCharExpression(FlowParser.CharExpressionContext ctx) {
        return ctx.CHAR().getText().charAt(1);
    }

    @Override
    public Object visitBooleanExpression(FlowParser.BooleanExpressionContext ctx) {
        return Boolean.parseBoolean(ctx.BOOLEAN().getText());
    }

    @Override
    public Object visitDoubleExpression(FlowParser.DoubleExpressionContext ctx) {
        return Double.parseDouble(ctx.DOUBLE().getText());
    }

    @Override
    public Object visitParenthesesExpression(FlowParser.ParenthesesExpressionContext ctx) {
        return visit(ctx.expression());
    }

    @Override
    public Object visitNumberOpExpression(FlowParser.NumberOpExpressionContext ctx) {
        Object val1 = visit(ctx.expression(0));
        Object val2 = visit(ctx.expression(1));
        String op = ctx.numbersOp().getText();

        // get type of val1 and val2
        String type1 = getClassName(val1);
        String type2 = getClassName(val2);

        if (type1.equals("String") || type2.equals("String")) {
            return getStringOpResult(op, val1.toString(), val2.toString());
        }

        // check if types are same
        if (!type1.equals(type2)) {
            throw new FlowException("Wrong type of arguments in expression. Expected " + type1 + " but got " + type2 + ".");
        }

        if (type1.equals("Int")) {
            return getIntOpResult(op, (Integer) val1, (Integer) val2);
        }

        if (type1.equals("Double")) {
            return getDoubleOpResult(op, (Double) val1, (Double) val2);
        }

        throw new FlowException("Wrong type of arguments in expression. Expected String, Int or Double but got " + type1 + ".");
    }


    @Override
    public Object visitIfStatement(FlowParser.IfStatementContext ctx) {
        return this.ifStatementVisitor.visitIfStatement(ctx);
    }

    @Override
    public Object visitIOStatement(FlowParser.IOStatementContext ctx) {
        return this.ioStatementVisitor.visitIOStatement(ctx);
    }

    @Override
    public Object visitReadConsoleStatement(FlowParser.ReadConsoleStatementContext ctx) {
        return this.ioStatementVisitor.visitReadConsoleStatement(ctx);
    }

    @Override
    public Object visitReadLineConsoleStatement(FlowParser.ReadLineConsoleStatementContext ctx) {
        return this.ioStatementVisitor.visitReadLineConsoleStatement(ctx);
    }

    @Override
    public Object visitReadFileStatement(FlowParser.ReadFileStatementContext ctx) {
        return this.ioStatementVisitor.visitReadFileStatement(ctx);
    }

    @Override
    public Object visitWriteFileStatement(FlowParser.WriteFileStatementContext ctx) {
        return this.ioStatementVisitor.visitWriteFileStatement(ctx);
    }

    @Override
    public Object visitPrintStatement(FlowParser.PrintStatementContext ctx) {
        return this.ioStatementVisitor.visitPrintStatement(ctx);
    }

    @Override
    public Object visitReadStatement(FlowParser.ReadStatementContext ctx) {
        return this.ioStatementVisitor.visitReadStatement(ctx);
    }

    @Override
    public Object visitWriteStatement(FlowParser.WriteStatementContext ctx) {
        return this.ioStatementVisitor.visitWriteStatement(ctx);
    }

    @Override
    public Object visitForStatement(FlowParser.ForStatementContext ctx) {

        List<Integer> range = (List<Integer>) visitRangeExpression(ctx.rangeExpression());

        if (range == null) {
            throw new FlowException("Range expression must return list of integers.");
        }

        symbolTable.pushLocalScope();
        Symbol cycleVariable = new Symbol(ctx.ID().getText(), null, "Int", true);
        symbolTable.defineCurrentScopeValue(cycleVariable);

        for (Integer i : range) {
            cycleVariable.setValue(i);

            Object o = visit(ctx.controlStructureBody());
            if (o != null) {
                return o;
            }
        }

        symbolTable.popScope();

        return null;
    }

    @Override
    public Object visitWhileStatement(FlowParser.WhileStatementContext ctx) {

        while ((Boolean) visit(ctx.expression())) {
            Object o = visit(ctx.controlStructureBody());
            if (o != null) {
                return o;
            }
        }

        return null;
    }

    @Override
    public Object visitRangeExpression(FlowParser.RangeExpressionContext ctx) {

        Object startInclusive = visit(ctx.expression(0));
        Object endExclusive = visit(ctx.expression(1));

        return IntStream
                .range((Integer) startInclusive, (Integer) endExclusive)
                .boxed()
                .collect(Collectors.toList());
    }

    @Override
    public Object visitRelationOpExpression(FlowParser.RelationOpExpressionContext ctx) {

        Object val1 = visit(ctx.expression(0));
        Object val2 = visit(ctx.expression(1));
        String relationOp = ctx.relationOp().getText();

        return resolveCondition(val1, val2, relationOp);
    }

    @Override
    public Object visitConvertToStringExpression(FlowParser.ConvertToStringExpressionContext ctx) {

        Object value = visit(ctx.expression());

        if (value == null) {
            throw new FlowException("Cannot convert value to string.");
        }

        return value.toString();
    }

    @Override
    public Object visitTryStatement(FlowParser.TryStatementContext ctx) {

        try {
            Object o = visit(ctx.controlStructureBody(0));
            if (o != null) {
                return o;
            }
        } catch (FlowException e) {
            if (ctx.controlStructureBody().size() > 1) {

                String variableName = ctx.ID().getText();
                String variableType = ctx.TYPE().getText();
                Object value = e.getMessage();

                if (!variableType.equals("String")) {
                    throw new FlowException("Wrong type of arguments in catch expression. Expected String but got " + variableType + ".");
                }

                Symbol variable = new Symbol(variableName, value, variableType, true);
                symbolTable.defineCurrentScopeValue(variable);

                Object o = visit(ctx.controlStructureBody(1));
                if (o != null) {
                    return o;
                }
            } else {
                SYSTEM_OUT.append("\n\u001B[31mFlowException caught: ")
                        .append(e.getMessage())
                        .append("\u001B[0m");
            }
        }

        return null;
    }
}
