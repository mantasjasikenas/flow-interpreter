package flow.interpreter.util;

// declare static class

import flow.interpreter.exception.FlowException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;

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

    public static Object getObjectDefaultValue(String type) {
        return switch (type) {
            case "Int" -> 0;
            case "Double" -> 0.0;
            case "Char" -> '\u0000';
            case "String" -> "";
            case "Boolean" -> false;
            default -> null;
        };
    }

    public static boolean resolveCondition(Object left, Object right, String relOp) {

        String leftClassName = left.getClass().getSimpleName();
        String rightClassName = right.getClass().getSimpleName();

        if (!leftClassName.equals(rightClassName)) {
            throw new FlowException("Cannot compare values of different types: " + leftClassName + " and " + rightClassName);
        }

        return switch (relOp) {
            case "==" -> left.equals(right);
            case "!=" -> !left.equals(right);
            case "<" -> ((Comparable<Object>) left).compareTo(right) < 0;
            case ">" -> ((Comparable<Object>) left).compareTo(right) > 0;
            case "<=" -> ((Comparable<Object>) left).compareTo(right) <= 0;
            case ">=" -> ((Comparable<Object>) left).compareTo(right) >= 0;
            default -> throw new FlowException("Unknown relation operator: " + relOp);
        };
    }

    public static Object getDoubleOpResult(String op, Double val1, Double val2) {
        return switch (op) {
            case "+" -> val1 + val2;
            case "-" -> val1 - val2;
            case "*" -> val1 * val2;
            case "/" -> val1 / val2;
            case "%" -> val1 % val2;
            default -> null;
        };
    }

    public static Object getIntOpResult(String op, Integer val1, Integer val2) {
        return switch (op) {
            case "+" -> val1 + val2;
            case "-" -> val1 - val2;
            case "*" -> val1 * val2;
            case "/" -> val1 / val2;
            case "%" -> val1 % val2;
            default -> null;
        };
    }

    public static String getStringOpResult(String op, String val1, String val2) {
        if (op.equals("+")) {
            return val1 + val2;
        }

        throw new FlowException("Wrong operator " + op + " for String type.");
    }

    public static boolean isNumber(String type) {
        return type.equals("Int") || type.equals("Double");
    }

    public static String readFromFile(String path) {
        try {
            return Files.readString(Paths.get(path));
        } catch (IOException e) {
            throw new FlowException("Cannot read from file: " + path);
        }
    }

    public static void writeToFile(String path, String content) {
        try {
            Files.writeString(Paths.get(path), content);
        } catch (IOException e) {
            throw new FlowException("Cannot write to file: " + path);
        }
    }

    public static String readLnFromConsole() {
        try {
            Scanner scanner = new Scanner(System.in);
            String s = scanner.nextLine();

            scanner.close();

            return s;
        } catch (Exception e) {
            throw new FlowException("Cannot read from console");
        }
    }

    public static Character readFromConsole() {
        try {
            Scanner scanner = new Scanner(System.in);
            char s = scanner.next().charAt(0);

            scanner.close();

            return s;
        } catch (Exception e) {
            throw new FlowException("Cannot read from console");
        }
    }
}
