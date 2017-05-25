package com.coradec.coralog.model.impl;

import com.coradec.coracore.annotation.Nullable;
import com.coradec.coracore.model.Origin;
import com.coradec.coralog.model.LogLevel;
import com.coradec.coralog.model.StringProblemLogEntry;

import java.util.Optional;

/**
 * ​​Basic implementation of a string log entry.
 */
@SuppressWarnings("ClassHasNoToStringMethod")
public class BasicStringProblemLogEntry extends BasicLogEntry implements StringProblemLogEntry {

    private final Throwable problem;
    private final @Nullable String text;
    private final @Nullable Object[] textArgs;

    public BasicStringProblemLogEntry(final Origin origin, final LogLevel level,
                                      final Throwable problem, final @Nullable String text,
                                      final @Nullable Object... textArgs) {
        super(origin, level);
        this.problem = problem;
        this.text = text;
        this.textArgs = textArgs;
    }

    @Override public Optional<String> getTemplate() {
        return Optional.ofNullable(text);
    }

    @Override public Throwable getProblem() {
        return problem;
    }

    @Override public Optional<String> getExplanation() {
        return getTemplate().map(t -> String.format(t, Optional.ofNullable(textArgs).orElse(new Object[0])));
    }
}
