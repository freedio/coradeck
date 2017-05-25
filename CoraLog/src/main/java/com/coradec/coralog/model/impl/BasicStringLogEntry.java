package com.coradec.coralog.model.impl;

import com.coradec.coracore.annotation.ToString;
import com.coradec.coracore.model.Origin;
import com.coradec.coralog.model.LogLevel;
import com.coradec.coralog.model.StringLogEntry;

/**
 * Basic implementation of a string log entry.
 */
public class BasicStringLogEntry extends BasicLogEntry implements StringLogEntry {

    private final String text;
    private final Object[] textArgs;

    /**
     * Initializes a new instance of BasicTextLogEntry from the specified origin with the specified
     * l;og level, text and optional text arguments.
     *
     * @param origin   the origin.
     * @param level    the log level.
     * @param text     the log text.
     * @param textArgs arguments to the text (optional).
     */
    public BasicStringLogEntry(final Origin origin, final LogLevel level, final String text,
                               final Object... textArgs) {
        super(origin, level);
        this.text = text;
        this.textArgs = textArgs;
    }

    @Override @ToString public String getTemplate() {
        return text;
    }

    @Override @ToString public String getText() {
        return String.format(text, textArgs);
    }

}
