package com.coradec.coracom.trouble;

import com.coradec.coracore.trouble.BasicException;

/**
 * ​​Base class of all communication related exceptions.
 */
public class CoraComException extends BasicException {

    protected CoraComException(final Throwable problem) {
        super(problem);
    }

    protected CoraComException() {
    }
}
