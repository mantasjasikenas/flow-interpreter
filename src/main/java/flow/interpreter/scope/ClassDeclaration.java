package flow.interpreter.scope;

import flow.FlowParser;

import java.util.List;
import java.util.Objects;

public class ClassDeclaration {
    private final String className;
    private final FlowParser.ClassDeclarationContext classDeclarationContext;


    public ClassDeclaration(String className, FlowParser.ClassDeclarationContext classDeclarationContext) {
        this.className = className;
        this.classDeclarationContext = classDeclarationContext;
    }

    public String getClassName() {
        return className;
    }

    public FlowParser.ClassDeclarationContext getClassDeclarationContext() {
        return classDeclarationContext;
    }

    // Tested, works
    public FlowParser.MethodDeclarationContext getMethodDeclarationContext(String methodName) {
        return classDeclarationContext
                .classMember()
                .stream()
                .map(FlowParser.ClassMemberContext::methodDeclaration)
                .filter(obj -> Objects.nonNull(obj) && obj.ID().getText().equals(methodName))
                .findFirst()
                .orElse(null);
    }

    public List<FlowParser.MethodDeclarationContext> getMethodDeclarationContext() {
        return classDeclarationContext
                .classMember()
                .stream()
                .map(FlowParser.ClassMemberContext::methodDeclaration)
                .filter(Objects::nonNull)
                .toList();
    }

    public FlowParser.ClassConstructorContext getConstructorDeclarationContext() {
        return classDeclarationContext
                .classMember()
                .stream()
                .map(FlowParser.ClassMemberContext::classConstructor)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);

    }

    public List<FlowParser.DeclarationContext> getClassFieldsContext() {
        return classDeclarationContext
                .classMember()
                .stream()
                .map(FlowParser.ClassMemberContext::declaration)
                .filter(Objects::nonNull)
                .toList();
    }


}
