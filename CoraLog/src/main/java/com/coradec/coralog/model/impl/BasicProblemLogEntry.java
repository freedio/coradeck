package com.coradec.coralog.model.impl;

import com.coradec.coracore.annotation.Nullable;
import com.coradec.coracore.annotation.ToString;
import com.coradec.coracore.model.Origin;
import com.coradec.coracore.util.ClassUtil;
import com.coradec.coralog.model.LogLevel;
import com.coradec.coralog.model.ProblemLogEntry;
import com.coradec.coratext.model.Text;

import java.util.Optional;

/**
 * ​​Basic implementation of a problem log entry.
 */
public class BasicProblemLogEntry extends BasicLogEntry implements ProblemLogEntry {

    private final @Nullable Text text;
    private final Throwable problem;
    private final @Nullable Object[] textArgs;

    /**
     * Initializes a new instance of BasicProblemLogEntry from the specified origin with the
     * specified log level regarding the specified problem with the specified optional explanatory
     * text with the specified optional text arguments.
     *
     * @param origin   the origin of the log entry.
     * @param level    the log level.
     * @param problem  the problem to log.
     * @param text     the explanatory text (optional).
     * @param textArgs arguments to the explanatory text (optional).
     */
    public BasicProblemLogEntry(final Origin origin, final LogLevel level, final Throwable problem,
                                final @Nullable Text text, final @Nullable Object... textArgs) {
        super(origin, level);
        this.text = text;
        this.problem = problem;
        this.textArgs = textArgs;
    }

    @Override @ToString public Optional<Text> getText() {
        return Optional.ofNullable(this.text);
    }

    @Override @ToString public Throwable getProblem() {
        return this.problem;
    }

    @Override @ToString public Optional<String> getExplanation() {
        return getText().map(text -> text.resolve(textArgs));
    }

    @Override public String toString() {
        return ClassUtil.toString(this);
    }

}
