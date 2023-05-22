package flow.interpreter.visitor;

import flow.FlowBaseVisitor;
import flow.FlowParser;
import flow.interpreter.exception.FlowException;
import flow.interpreter.scope.*;

import java.util.List;
import java.util.Objects;

import static flow.interpreter.util.Helpers.getClassName;

public class ClassVisitor extends FlowBaseVisitor<Object> {

    private final InterpreterVisitor parent;
    private final SymbolTable symbolTable;


    public ClassVisitor(InterpreterVisitor parent) {
        this.parent = parent;
        this.symbolTable = parent.symbolTable;
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
        boolean isMutable;

        // means assignment
        if (ctx.VARIABLE() == null) {
            Symbol symbol = symbolTable.resolve(objectName);
            if (symbol == null) {
                throw new FlowException("Cannot assign to undeclared variable " + objectName);
            }

            if (symbol.isMutable()) {
                isMutable = true;
            } else {
                throw new FlowException("Cannot assign to immutable variable " + objectName);
            }

            symbolTable.remove(objectName);
        } else {
            isMutable = Objects.equals(ctx.VARIABLE().getText(), "var");
        }


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
        classDeclaration.getClassFieldsContext().forEach(parent::visit);

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
                Object argValue = parent.visit(objectArgs.expression(i));

                // check if argType is same as argValue type
                if (!argType.equals("Unit") && !argType.equals(getClassName(argValue))) {
                    throw new FlowException("Wrong argument " + argName + " type. Expected " + argType + " but got " + argValue.getClass().getSimpleName() + ".");
                }

                symbolTable.defineCurrentScopeValue(new Symbol(argName, argValue, argType, true));
            }
        }

        parent.visit(ctx.controlStructureBody());
        symbolTable.popScope();

    }

    @Override
    public Object visitClassMember(FlowParser.ClassMemberContext ctx) {
        return super.visitClassMember(ctx);
    }

    @Override
    public Object visitClassObjectVariableSetter(FlowParser.ClassObjectVariableSetterContext ctx) {

        String objectName = ctx.ID(0).getText();
        String variableName = ctx.ID(1).getText();
        Object value = parent.visit(ctx.expression());

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


}
