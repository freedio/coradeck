package com.coradec.coralog.model;

/**
 * ​​​A log entry containing a text literal.
 */
public interface StringLogEntry extends LogEntry {

    /**
     * Returns the text template.
     *
     * @return the text template.
     */
    String getTemplate();

    /**
     * Returns the resolved text.
     *
     * @return the resolved text.
     */
    String getText();

}
