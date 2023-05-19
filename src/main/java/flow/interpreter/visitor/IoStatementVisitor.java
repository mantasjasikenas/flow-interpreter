package flow.interpreter.visitor;

import flow.FlowBaseVisitor;
import flow.FlowParser;
import flow.interpreter.scope.Scope;
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
        return Helpers.readFromConsole();
    }

    @Override
    public Object visitReadLineStatement(FlowParser.ReadLineStatementContext ctx) {
        return Helpers.readLnFromConsole();
    }

    @Override
    public Object visitReadFileStatement(FlowParser.ReadFileStatementContext ctx) {
        return Helpers.readFromFile(ctx.STRING().getText());
    }

    @Override
    public Object visitWriteFileStatement(FlowParser.WriteFileStatementContext ctx) {
        Scope scope = parent.symbolTable.currentScope();

        Object path = ctx.STRING(0) != null ?
                ctx.STRING(0).getText().substring(1, ctx.STRING(0).getText().length() - 1) :
                scope.resolve(ctx.ID(0).getText()).getValue();

        Object content = ctx.STRING(1) != null ?
                ctx.STRING(1).getText().substring(1, ctx.STRING(1).getText().length() - 1) :
                scope.resolve(ctx.ID(1).getText()).getValue();

        Helpers.writeToFile(path.toString(), content.toString());

        return null;
    }
}
