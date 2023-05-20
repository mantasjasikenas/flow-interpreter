package flow.interpreter.visitor;

import flow.FlowBaseVisitor;
import flow.FlowParser;
import flow.interpreter.exception.FlowException;
import flow.interpreter.util.Helpers;

public class IoStatementVisitor extends FlowBaseVisitor<Object> {

    private final InterpreterVisitor parent;

    public IoStatementVisitor(InterpreterVisitor parent) {
        this.parent = parent;
    }

    @Override
    public Object visitIOStatement(FlowParser.IOStatementContext ctx) {
        return super.visitIOStatement(ctx);
    }

    @Override
    public Object visitReadStatement(FlowParser.ReadStatementContext ctx) {
        return super.visitReadStatement(ctx);
    }

    @Override
    public Object visitWriteStatement(FlowParser.WriteStatementContext ctx) {
        return super.visitWriteStatement(ctx);
    }

    @Override
    public Object visitReadConsoleStatement(FlowParser.ReadConsoleStatementContext ctx) {
        return Helpers.readFromConsole();
    }

    @Override
    public Object visitReadLineConsoleStatement(FlowParser.ReadLineConsoleStatementContext ctx) {
        return Helpers.readLnFromConsole();
    }

    @Override
    public Object visitReadFileStatement(FlowParser.ReadFileStatementContext ctx) {

        String fromFile = "";

        if (ctx.STRING() != null) {
            fromFile = Helpers.readFromFile(ctx.STRING().getText());
        } else if (ctx.expression() != null) {
            Object obj = parent.visit(ctx.expression());

            if (obj == null)
                throw new FlowException("Cannot read from file: " + ctx.expression().getText() + " is null");

            if (!(obj instanceof String))
                throw new FlowException("Cannot read from file: " + ctx.expression().getText() + " is not a string");

            fromFile = Helpers.readFromFile(obj.toString());
        }

        return fromFile;
    }

    @Override
    public Object visitWriteFileStatement(FlowParser.WriteFileStatementContext ctx) {

        Object pathExpr = ctx.expression(0) != null ? parent.visit(ctx.expression(0)) : null;
        Object contentExpr = ctx.expression(1) != null ? parent.visit(ctx.expression(1)) : null;

        Object path = ctx.STRING(0) != null ?
                ctx.STRING(0).getText().substring(1, ctx.STRING(0).getText().length() - 1) :
                pathExpr;

        Object content = ctx.STRING(1) != null ?
                ctx.STRING(1).getText().substring(1, ctx.STRING(1).getText().length() - 1) :
                contentExpr;

        if (path == null)
            throw new FlowException("Cannot write to file: path is null");

        if (content == null)
            throw new FlowException("Cannot write to file: content is null");


        Helpers.writeToFile(path.toString(), content.toString());

        return null;
    }

    @Override
    public Object visitPrintStatement(FlowParser.PrintStatementContext ctx) {
        FlowParser.ExpressionContext expressionContext = ctx.expression();
        String text;

        if (expressionContext != null) {
            Object obj = parent.visit(expressionContext);
            text = obj != null ? obj.toString() : "";
        } else {
            text = "";
        }

        if (ctx.PRINTLN() != null) {
            parent.SYSTEM_OUT.append("\n");
        }

        parent.SYSTEM_OUT.append(text);

        return null;
    }
}
