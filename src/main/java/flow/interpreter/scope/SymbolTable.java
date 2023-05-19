package flow.interpreter.scope;


import java.util.ArrayList;
import java.util.Stack;


public class SymbolTable {

    private final Stack<Scope> scopeStack = new Stack<>();
    protected ArrayList<Scope> allScopes = new ArrayList<>();
    private final GlobalScope globalScope = new GlobalScope();
    private int genId = 0; // for generating unique symbol names


    public SymbolTable() {
        scopeStack.push(globalScope);
        allScopes.add(globalScope);
    }

    public Scope pushScope() {
        Scope parent = scopeStack.peek();
        Scope scope = new Scope(ScopeType.LOCAL, nextGenId(), parent);

        scopeStack.push(scope);
        allScopes.add(scope);

        return scope;
    }

    public Scope pushScope(Scope scope) {
        scopeStack.push(scope);

        return scope;
    }

    public void popScope() {
        scopeStack.pop();
    }

    public Scope currentScope() {
        if (scopeStack.size() > 0) {
            return scopeStack.peek();
        }

        return allScopes.get(0);
    }

    public Scope getScope(int genId) {
        for (Scope scope : scopeStack) {
            if (scope.genId == genId) return scope;
        }
        return null;
    }

    public void defineCurrentScopeValue(Symbol symbol) {
        currentScope().define(symbol);
    }

    public void defineGlobalScopeValue(Symbol symbol) {
        globalScope.define(symbol);
    }

    public void defineClass(ClassDeclaration classDeclaration) {
        globalScope.addClassDeclaration(classDeclaration);
    }

    public void defineGlobalMethod(MethodDeclaration methodDeclaration) {
        globalScope.addGlobalMethod(methodDeclaration);
    }

    public Symbol resolve(String name) {
        return currentScope().resolve(name);
    }

    private int nextGenId() {
        genId++;
        return genId;
    }

    public ClassDeclaration getClassDeclaration(String className) {
        return globalScope.getClassDeclaration(className);
    }

    public MethodDeclaration getGlobalMethod(String methodName) {
        return globalScope.getGlobalMethod(methodName);
    }

}
