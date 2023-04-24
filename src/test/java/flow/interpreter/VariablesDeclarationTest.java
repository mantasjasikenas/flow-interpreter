package flow.interpreter;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class VariablesDeclarationTest {

    @Test
    void declare_and_print_variable() {
        String program = """
                int a = 5;            
                print(a);       
                """;

        String expected = """
                5
                """;

        String actual = FlowInterpreter.execute(program);

        assertEquals(expected, actual);
    }

    @Test
    void undeclared_variable_throws_exception() {
        String program = """
                a = 5;            
                print(a);       
                """;

        assertThrows(RuntimeException.class,
                () -> {
                    FlowInterpreter.execute(program);
                });
    }
}
