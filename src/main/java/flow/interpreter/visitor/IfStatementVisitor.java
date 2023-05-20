package flow.interpreter.visitor;


import flow.FlowBaseVisitor;
import flow.FlowParser;
import org.antlr.v4.runtime.tree.TerminalNode;

import static flow.interpreter.util.Helpers.resolveCondition;

public class IfStatementVisitor extends FlowBaseVisitor<Object> {

    private final InterpreterVisitor parent;

    public IfStatementVisitor(InterpreterVisitor parent) {
        this.parent = parent;
    }

    @Override
    public Object visitIfStatement(FlowParser.IfStatementContext ctx) {

        Object left = parent.visit(ctx.expression(0));
        Object right = parent.visit(ctx.expression(1));

        TerminalNode relOpNode = (TerminalNode) ctx.relationOp().getChild(0);
        String relOp = relOpNode.getText();

        if (resolveCondition(left, right, relOp)) {
            return parent.visit(ctx.controlStructureBody(0));
        } else {

            if (ctx.controlStructureBody().size() == 1) {
                return null;
            }

            return parent.visit(ctx.controlStructureBody(1));
        }
    }

}
