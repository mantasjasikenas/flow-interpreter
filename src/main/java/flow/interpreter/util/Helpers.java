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


    // FIXME not working together with readFromConsole
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
