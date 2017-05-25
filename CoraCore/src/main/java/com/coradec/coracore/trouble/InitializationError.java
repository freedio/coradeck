package com.coradec.coracore.trouble;

/**
 * ​​Indicates an error while initializing a class.
 */
public class InitializationError extends BasicException {

    /**
     * Initializes a new instance of InitializationError with the specified explanation and
     * underlying problem.
     *
     * @param explanation the explanation.
     * @param problem     the underlying problem.
     */
    public InitializationError(final String explanation, final Throwable problem) {
        super(explanation, problem);
    }

    /**
     * Initializes a new instance of InitializationError with the specified explanation.
     *
     * @param explanation the explanation.
     */
    public InitializationError(final String explanation) {
        super(explanation);
    }

    public InitializationError(final Throwable problem) {
        super(problem);
    }
}
