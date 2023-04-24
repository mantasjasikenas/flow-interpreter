package flow.interpreter;



import flow.FlowBaseVisitor;
import flow.FlowParser;
import org.antlr.v4.runtime.tree.TerminalNode;

public class IfStatementVisitor extends FlowBaseVisitor<Object> {

    private final InterpreterVisitor parent;

    public IfStatementVisitor(InterpreterVisitor parent) {
        this.parent = parent;
    }

@Override
public Object visitIfStatement(FlowParser.IfStatementContext ctx) {
    // Get the left and right expressions from the context
    Object left = parent.visit(ctx.expression(0));
    Object right = parent.visit(ctx.expression(1));

    // Get the relation operator from the context
    TerminalNode relOpNode = (TerminalNode) ctx.relationOp().getChild(0);
    String relOp = relOpNode.getText();

    // Resolve the condition and execute the appropriate statement
    if (resolveCondition(left, right, relOp)) {
        return parent.visit(ctx.expression(1));
    } else {
        return parent.visit(ctx.expression(1));
    }
}

    private static boolean resolveCondition(Object left, Object right, String relOp) {
        return switch (relOp) {
            case "==" -> left == right;
            case "!=" -> left != right;
            default -> throw new RuntimeException("Unsupported operator.");
        };
    }
}
