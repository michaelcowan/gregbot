package io.blt.gregbot.core.template;

/**
 * Thrown when a variable key is unknown
 */
public class UnknownVariableException extends Exception {

    private final String variable;

    public UnknownVariableException(String variable) {
        super("Unknown variable key '" + variable + "'");
        this.variable = variable;
    }

    public String variable() {
        return variable;
    }

}
