package flow.interpreter.scope;

import java.util.HashMap;

public class ClassScope extends Scope {

    private final HashMap<String, MethodDeclaration> methodsTable = new HashMap<>();

    public ClassScope(ScopeType type, int genId, Scope parent) {
        super(type, genId, parent);
    }

    public ClassScope(int genId, Scope parent) {
        super(ScopeType.CLASS, genId, parent);
    }

    public void defineMethod(MethodDeclaration method) {
        methodsTable.put(method.getMethodName(), method);
    }

    public MethodDeclaration resolveMethod(String name) {
        return methodsTable.get(name);
    }


}
