package flow.interpreter;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PrintTest {

    @Test
    void print_integer() {
        String program = """
                         print(5);                     
                         """;

        String expected = """
                          5
                          """;

        String actual = FlowInterpreter.execute(program);

        assertEquals(expected, actual);
    }
}