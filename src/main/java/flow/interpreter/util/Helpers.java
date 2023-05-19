package flow.interpreter.util;

// declare static class

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

    public static String readFromFile(String path) {
        try {
            return Files.readString(Paths.get(path));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void writeToFile(String path, String content) {
        try {
            Files.writeString(Paths.get(path), content);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static String readLnFromConsole() {
        try {
            Scanner scanner = new Scanner(System.in);
            String s = scanner.nextLine();

            scanner.close();

            return s;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Character readFromConsole() {
        try {
            Scanner scanner = new Scanner(System.in);
            char s = scanner.next().charAt(0);

            scanner.close();

            return s;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
