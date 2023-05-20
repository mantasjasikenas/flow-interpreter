package flow.interpreter;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.DefaultErrorStrategy;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;

public class FlowErrorListener extends BaseErrorListener {
    private String errorMsg = null;
    private boolean isPartialTree = false;


    @Override
    public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
        //TODO bad. Override DefaultErrorStrategy instead? https://stackoverflow.com/a/18139305
        if (msg.startsWith("mismatched input '<EOF>'") || msg.matches("missing '.*' at '<EOF>'")) {
            isPartialTree = true;
        } else {
            errorMsg = "Syntax error at line " + line + ", position " + charPositionInLine + ": " + msg;
        }
    }

    public boolean isHasSyntaxError() {
        return errorMsg != null;
    }

    public boolean isPartialTree() {
        return isPartialTree;
    }

    public String getErrorMsg() {
        return errorMsg;
    }
}