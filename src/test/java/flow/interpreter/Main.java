package flow.interpreter;

import flow.interpreter.util.Helpers;

public class Main {

    public static void main(String[] args) {
        Character character = Helpers.readFromConsole();
        System.out.println("You entered: " + character);

        String string = Helpers.readLnFromConsole();
        System.out.println("You entered: " + string);

    }
}
