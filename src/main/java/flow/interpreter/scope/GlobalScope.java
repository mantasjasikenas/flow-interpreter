package flow.interpreter.scope;

import java.util.HashMap;

public class GlobalScope extends Scope {
    private final HashMap<String, ClassDeclaration> classTable = new HashMap<>();
    private final HashMap<String, MethodDeclaration> globalMethodsTable = new HashMap<>();


    public GlobalScope() {
        super(ScopeType.GLOBAL, 0, null);
    }

    public void addClassDeclaration(ClassDeclaration classDeclaration) {
        classTable.put(classDeclaration.getClassName(), classDeclaration);
    }

    public ClassDeclaration getClassDeclaration(String className) {
        return classTable.get(className);
    }

    public void addGlobalMethod(MethodDeclaration methodDeclaration) {
        globalMethodsTable.put(methodDeclaration.getMethodName(), methodDeclaration);
    }

    public MethodDeclaration getGlobalMethod(String methodName) {
        return globalMethodsTable.get(methodName);
    }
}
