package flow.interpreter.scope;

import java.util.Objects;

public class Symbol {

    private Scope scope;
    private final String name;
    private final String type;
    private Object value;

    public String getName() {
        return name;
    }

    public Symbol(String name, Object value, String type) {
        this.name = name;
        this.value = value;
        this.type = type;
    }

    public Symbol(String name, Object value, String type, Scope scope) {
        this.name = name;
        this.value = value;
        this.type = type;
        this.scope = scope;
    }

    public Object getValue() {
        return value;
    }

    public String getType() {
        return type;
    }

    public void setValue(Object value) {
        if (Objects.equals(type, "val"))
            throw new RuntimeException("Cannot change value of val.");

        this.value = value;
    }

    public Scope getScope() {
        return scope;
    }

    public void setScope(Scope scope) {
        this.scope = scope;
    }

    @Override
    public int hashCode() {

        if (value == null) {
            return 0;
        }

        return this.value.hashCode();
    }

    @Override
    public boolean equals(Object o) {

        if (value == o) {
            return true;
        }

        if (value == null || o == null || o.getClass() != value.getClass()) {
            return false;
        }

        Symbol that = (Symbol) o;

        return this.value.equals(that.value);
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }


}
