package flow.interpreter.visitor;


import flow.FlowBaseVisitor;
import flow.FlowParser;
import flow.interpreter.exception.FlowException;
import flow.interpreter.scope.*;

import static flow.interpreter.util.Helpers.getClassName;
import static flow.interpreter.util.Helpers.getObjectDefaultValue;

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
        symbolTable.popScope();

        return SYSTEM_OUT.toString();
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

        FlowParser.MethodArgsContext objectArgs = ctx.methodArgs();
        ClassDeclaration classDeclaration = symbolTable.getClassDeclaration(className);

        if (classDeclaration == null) {
            throw new FlowException("Class " + className + " does not exist.");
        }

        Scope currentScope = symbolTable.currentScope();

        if (currentScope.resolve(objectName) != null) {
            throw new FlowException("Object " + objectName + " already exists.");
        }

        Symbol objectSymbol = new Symbol(objectName, classDeclaration, className);
        symbolTable.defineCurrentScopeValue(objectSymbol);

        Scope classMembersScope = symbolTable.pushScope();
        objectSymbol.setScope(classMembersScope);
        classDeclaration.getClassFieldsContext().forEach(this::visit);

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

        symbolTable.pushScope();

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

                symbolTable.defineCurrentScopeValue(new Symbol(argName, argValue, argType));
            }
        }

        visit(ctx.controlStructureBody());
        symbolTable.popScope();

    }

    @Override
    public Object visitControlStructureBody(FlowParser.ControlStructureBodyContext ctx) {
        //TODO: new scope for if/else/while/for ...

        symbolTable.pushScope();
        Object o = super.visitControlStructureBody(ctx);
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

        if (ctx.TYPE() == null && ctx.expression() == null) {
            throw new FlowException("Variable `" + varName + "` must have a type or an expression.");
        }

        Object value = ctx.expression() != null ? visit(ctx.expression()) : null;
        String type = value == null ? ctx.TYPE().getText() : getClassName(value);

        if (type != null && value == null) {
            value = getObjectDefaultValue(type);
        }

        Scope currentScope = symbolTable.currentScope();

        if (currentScope.resolve(varName) != null) {
            throw new FlowException("Variable `" + varName + "` is already declared.");
        }

        symbolTable.defineCurrentScopeValue(new Symbol(varName, value, type));

        return null;
    }


    @Override
    public Object visitMethodInvocation(FlowParser.MethodInvocationContext ctx) {

        String objectName;
        String methodName;

        int size = ctx.ID().size();
        FlowParser.MethodArgsContext args = ctx.methodArgs();

        if (size == 1) {
            objectName = "this";
            methodName = ctx.ID(0).getText();
        } else {
            objectName = ctx.ID(0).getText();
            methodName = ctx.ID(1).getText();
        }


        // global method
        if (objectName.equals("this")) {
            MethodDeclaration methodDeclaration = symbolTable.getGlobalMethod(methodName);
            FlowParser.MethodDeclarationContext context = methodDeclaration.getMethodDeclarationContext();

            if (methodDeclaration == null) {
                throw new FlowException("Method " + methodName + " does not exist.");
            }

            Scope methodScope = symbolTable.pushScope();
            methodScope.setParent(symbolTable.getScope(0));

            if (args != null) {
                visitMethodArgs(args, methodDeclaration.getMethodDeclarationContext());
            }
            Object returnValue = visit(context.methodStructureBody());

            if (returnValue != null) {
                if (!context.TYPE().getText().equals(getClassName(returnValue))) {
                    throw new FlowException("Return type is not the same as method return type.");
                }
            }

            symbolTable.popScope();

            return returnValue;

        }


        // class method
        Scope currentScope = symbolTable.currentScope();
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


        Scope classScope = object.getScope();
        Scope methodScope = symbolTable.pushScope();
        methodScope.setParent(classScope);

        if (args != null) {
            visitMethodArgs(args, methodDeclaration);
        }

        Object returnValue = visit(methodDeclaration.methodStructureBody());

        if (returnValue != null) {
            if (!methodDeclaration.TYPE().getText().equals(getClassName(returnValue))) {
                throw new FlowException("Return type is not the same as method return type.");
            }
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

        for (FlowParser.MethodBodyStatementContext methodBodyContext : ctx.methodBodyStatement()) {
            if (methodBodyContext.returnStatement() != null) {
                return visit(methodBodyContext.returnStatement().expression());
            }
            visit(methodBodyContext);
        }

        return null;
    }

    @Override
    public Object visitReturnStatement(FlowParser.ReturnStatementContext ctx) {
        return super.visitReturnStatement(ctx);
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

            symbolTable.defineCurrentScopeValue(new Symbol(argName, argValue, argType));
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

        return symbol.getValue();
    }

    @Override
    public Object visitForStatement(FlowParser.ForStatementContext ctx) {

        String name = ctx.ID().getText();


        return null;
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
    public Object visitIntAddOpExpression(FlowParser.IntAddOpExpressionContext ctx) {
        Object val1 = visit(ctx.expression(0));
        Object val2 = visit(ctx.expression(1));
        return switch (ctx.intAddOp().getText()) {
            case "+" -> (Integer) val1 + (Integer) val2;
            case "-" -> (Integer) val1 - (Integer) val2;
            default -> null;
        };
    }

    @Override
    public Object visitIntMultiOpExpression(FlowParser.IntMultiOpExpressionContext ctx) {
        Object val1 = visit(ctx.expression(0));
        Object val2 = visit(ctx.expression(1));
        //TODO - validation etc
        return switch (ctx.intMultiOp().getText()) {
            case "*" -> (Integer) val1 * (Integer) val2;
            case "/" -> (Integer) val1 / (Integer) val2;
            case "%" -> (Integer) val1 % (Integer) val2;
            default -> null;
        };
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
}
