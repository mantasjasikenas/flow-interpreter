package flow.interpreter.scope;

import flow.interpreter.exception.FlowException;

import java.util.Objects;

public class Symbol {

    private Scope scope;
    private final String name;
    private final String type;

    private boolean isMutable = false;
    private Object value;


    public Symbol(String name, Object value, String type, boolean isMutable) {
        this.name = name;
        this.value = value;
        this.type = type;
        this.isMutable = isMutable;
    }


    public Object getValue() {
        return value;
    }

    public String getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public void setValue(Object value) {
        if (!isMutable())
            throw new FlowException("Cannot assign value to variable: `" + name + "`, variable is immutable");

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


    public boolean isMutable() {
        return isMutable;
    }

    public void setMutable(boolean mutable) {
        isMutable = mutable;
    }
}
