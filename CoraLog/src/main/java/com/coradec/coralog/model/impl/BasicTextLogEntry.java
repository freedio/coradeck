package com.coradec.coralog.model.impl;

import com.coradec.coracore.annotation.Nullable;
import com.coradec.coracore.annotation.ToString;
import com.coradec.coracore.model.Origin;
import com.coradec.coralog.model.LogLevel;
import com.coradec.coralog.model.TextLogEntry;
import com.coradec.coratext.model.Text;

/**
 * Basic implementation of a text log entry.
 */
@SuppressWarnings("ClassHasNoToStringMethod")
public class BasicTextLogEntry extends BasicLogEntry implements TextLogEntry {

    private final Text text;
    private final Object[] textArgs;

    /**
     * Initializes a new instance of BasicTextLogEntry from the specified origin with the specified
     * l;og level, text and optional text arguments.
     *
     * @param origin   the origin.
     * @param level    the log level.
     * @param text     the log text
     * @param textArgs arguments to the text (optional).
     */
    public BasicTextLogEntry(final Origin origin, final LogLevel level, final Text text,
                             final @Nullable Object... textArgs) {
        super(origin, level);
        this.text = text;
        this.textArgs = textArgs;
    }

    @Override @ToString public Text getTextLiteral() {
        return text;
    }

    @Override @ToString public String getText() {
        return text.resolve(textArgs);
    }

}
