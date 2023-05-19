package flow.interpreter.scope;

import flow.FlowParser;

import java.util.HashMap;
import java.util.Map;

public class MethodDeclaration {


    private String methodName;
    private String returnType;
    private final Map<String, String> parameters = new HashMap<>();
    private FlowParser.MethodDeclarationContext methodDeclarationContext;

    public MethodDeclaration(String functionName, String returnType, FlowParser.MethodDeclarationContext methodDeclarationContext) {
        this.methodName = functionName;
        this.returnType = returnType;
        this.methodDeclarationContext = methodDeclarationContext;
    }

    public void setMethodDeclarationContext(FlowParser.MethodDeclarationContext methodDeclarationContext) {
        this.methodDeclarationContext = methodDeclarationContext;
    }

    public void addParameter(String parameterName, String parameterType) {
        parameters.put(parameterName, parameterType);
    }

    public String getMethodName() {
        return methodName;
    }

    public String getReturnType() {
        return returnType;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public FlowParser.MethodDeclarationContext getMethodDeclarationContext() {
        return methodDeclarationContext;
    }
}
