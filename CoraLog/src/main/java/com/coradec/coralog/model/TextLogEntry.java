package com.coradec.coralog.model;

import com.coradec.coratext.model.Text;

/**
 * ​​​A log entry containing a text literal.
 */
public interface TextLogEntry extends LogEntry {

    /**
     * Returns the text literal.
     *
     * @return the text literal.
     */
    Text getTextLiteral();

    /**
     * Returns the resolved text.
     *
     * @return the resolved text.
     */
    String getText();
}
