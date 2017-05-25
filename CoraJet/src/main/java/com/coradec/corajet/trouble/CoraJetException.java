package com.coradec.corajet.trouble;

import com.coradec.coracore.trouble.BasicException;

/**
 * ​​Base class of all exceptions thrown in the CoreJet environment.
 */
public class CoraJetException extends BasicException {

    protected CoraJetException(final String explanation) {
        super(explanation);
    }

    public CoraJetException() {
    }
}
