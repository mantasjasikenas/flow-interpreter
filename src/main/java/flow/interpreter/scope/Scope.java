package flow.interpreter.scope;

import java.util.HashMap;
import java.util.Map;

public class Scope {

    public final int genId;

    public ScopeType type;
    private Scope parent;
    private final Map<String, Symbol> symbolTable = new HashMap<>();

    public Scope(ScopeType type, final int genId, Scope parent) {
        this.type = type;
        this.genId = genId;
        this.parent = parent;
    }

    public Map<String, Symbol> resolveAll() {
        return symbolTable;
    }

    public Symbol resolve(String name) {
        Symbol obj = symbolTable.get(name);
        if (obj != null) {
            return obj;
        }

        if (parent != null) return parent.resolve(name);
        return null;
    }

    public void define(Symbol symbol) throws IllegalArgumentException {
        if (symbolTable.containsKey(symbol.getName())) {
            throw new IllegalArgumentException("Duplicate symbol " + symbol.getName());
        }

        symbol.setScope(this);
        symbolTable.put(symbol.getName(), symbol);
    }

    public Scope getParent() {
        return parent;
    }

    public void setParent(Scope classScope) {
        this.parent = classScope;
    }
}
