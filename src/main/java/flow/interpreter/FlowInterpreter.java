package flow.interpreter;

import flow.FlowLexer;
import flow.FlowParser;
import flow.interpreter.scope.SymbolTable;
import flow.interpreter.visitor.InterpreterVisitor;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.antlr.v4.runtime.tree.ParseTree;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Objects;

public class FlowInterpreter {
    public static void main(String[] args) {
        // Initialize variables to hold parsed arguments
        String filename = null;
        boolean isInteractiveMode = false;

        // Loop through program arguments
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "-f" -> {
                    // If the -f flag is provided, check if there is a filename argument after it
                    if (i + 1 < args.length) {
                        // If there is a filename argument, store it and skip the next iteration of the loop
                        filename = args[i + 1];
                        i++;
                    } else {
                        // If there is no filename argument, print an error message and the help information and exit the program
                        System.err.println("Error: Missing filename argument for -f flag.");
                        printHelp();
                        System.exit(1);
                    }
                }
                case "-i" ->
                    // If the -i flag is provided, enable interactive mode
                        isInteractiveMode = true;
                case "-h" -> {
                    // If the -h flag is provided, print the help information and exit the program
                    printHelp();
                    System.exit(0);
                }
                case "-c" -> {
                    // allow user to enter file name in command line
                    System.out.println("Enter file name: ");
                    BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
                    try {
                        filename = reader.readLine();
                        if (!filename.startsWith("samples")) {
                            filename = "samples/" + filename;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
                case "-m" -> {
                    // allow user to enter file name in command line
                    try {
                        processMultipleFilesInput();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                default -> {
                    // If an invalid argument is provided, print an error message and the help information and exit the program
                    System.err.println("Error: Invalid argument: " + args[i]);
                    printHelp();
                    System.exit(1);
                }
            }
        }

        try {
            if (isInteractiveMode) {
                processInteractiveInput();
            } else {

                if (filename == null) {
                    return;
                }

                processFile(filename);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static void printHelp() {
        System.out.println("Usage: java ArgumentParser [-f filename] [-i] [-h]");
        System.out.println("-f filename\tPass a file as an argument");
        System.out.println("-i\t\tEnable interactive mode");
        System.out.println("-h\t\tDisplay help information");
    }

    private static void processInteractiveInput() throws IOException {
        SymbolTable symbolTable = new SymbolTable();

        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String input = "";
        while (true) {
            System.out.print("> ");
            String line = reader.readLine();
            if (Objects.equals(line, "exit")) {
                break;
            }
            input += line + "\n";
            try {
                String output = executeCode(symbolTable, CharStreams.fromString(input));
                if (output != null) {
                    input = "";
                    if (!output.equals("")) {
                        System.out.println(output);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("<ERROR> " + e.getMessage());
                input = "";
            }
        }
    }

    private static void processMultipleFilesInput() throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String filePath = "";

        System.out.println("Execute multiple files from samples folder. Enter 'exit' to exit.\n");

        while (true) {
            System.out.println("Enter file name:");
            System.out.print("> ");

            filePath = reader.readLine();

            if (Objects.equals(filePath, "exit")) {
                break;
            }

            try {
                if (!filePath.startsWith("samples")) {
                    filePath = "samples/" + filePath;
                }

                if (!filePath.endsWith(".flow")) {
                    filePath = filePath + ".flow";
                }

                processFile(filePath);
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("<ERROR> " + e.getMessage());
            }

            System.out.println();
        }
    }


    public static void processFile(String filename) {
        SymbolTable symbolTable = new SymbolTable();
        try {
            String output = executeCode(symbolTable, CharStreams.fromFileName(filename));
            System.out.println("\u001B[45m" + "[PROGRAM OUTPUT]" + "\u001B[0m");
            System.out.println(output);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("\n\u001B[31m" + "[ERROR] " + e.getMessage() + "\u001B[0m");
        }
    }

    public static String execute(String program) {
        return executeCode(new SymbolTable(), CharStreams.fromString(program));
    }

    private static String executeCode(SymbolTable symbolTable, CharStream input) {
        FlowLexer lexer = new FlowLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        FlowParser parser = new FlowParser(tokens);

        parser.removeErrorListeners();
        FlowErrorListener errorListener = new FlowErrorListener();
        parser.addErrorListener(errorListener);


        ParseTree tree = parser.program();

        if (errorListener.isHasSyntaxError()) {
            throw new ParseCancellationException(errorListener.getErrorMsg());
        }
        if (errorListener.isPartialTree()) {
            return null;
        }

        InterpreterVisitor interpreter = new InterpreterVisitor(symbolTable);
        return (String) interpreter.visit(tree);
    }
}
