package com.coradec.coracom.trouble;

/**
 * ​​Base class of all queue exceptions.
 */
public class QueueException extends CoraComException {

    /**
     * Creates a new instance of QueueException.
     */
    public QueueException() {
    }

    /**
     * Creates a new instance of QueueException with the specified underlying problem.
     *
     * @param problem the underlying problem.
     */
    public QueueException(final Exception problem) {
        super(problem);
    }

}
