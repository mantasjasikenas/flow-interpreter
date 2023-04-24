package flow.interpreter;


import java.util.HashMap;
import java.util.Map;

public class SymbolTable {
    private final Map<String, Object> table;

    public SymbolTable() {
        table = new HashMap<>();
    }

    public void put(String name, Object value) {
        table.put(name, value);
    }

    public Object get(String name) {
        return table.get(name);
    }

    public boolean contains(String name) {
        return table.containsKey(name);
    }
}
