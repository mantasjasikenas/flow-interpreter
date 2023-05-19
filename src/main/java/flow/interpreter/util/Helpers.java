package flow.interpreter.util;

// declare static class

public class Helpers {
    public static String getClassName(Object object) {
        String simpleName = object.getClass().getSimpleName();

        return switch (simpleName) {
            case "Integer" -> "Int";
            case "Double" -> "Double";
            case "Boolean" -> "Boolean";
            case "String" -> "String";
            case "Character" -> "Char";
            case "Void" -> "Unit";
            default -> simpleName;
        };

    }
}
