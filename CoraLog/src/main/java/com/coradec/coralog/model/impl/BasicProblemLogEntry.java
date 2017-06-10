/*
 * Copyright ⓒ 2017 by Coradec GmbH.
 *
 * This file is part of the Coradeck.
 *
 * Coradeck is free software: you can redistribute it under the the terms of the GNU General
 * Public License as published by the Free Software Foundation, either version 3 of the License,
 * or any later version.
 *
 * Coradeck is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR ANY PARTICULAR PURPOSE.  See the
 * GNU General Public License for further details.
 *
 * The GNU General Public License is available from <http://www.gnu.org/licenses/>.
 *
 * @license GPL-3.0+ <http://spdx.org/licenses/GPL-3.0+>
 * @author Dominik Wezel <dom@coradec.com>
 *
 */

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
public class BasicProblemLogEntry extends BasicLogEntry<String> implements ProblemLogEntry {

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

    @Override public String getContent() {
        return getExplanation().map(s -> getProblem().getLocalizedMessage() + ": " + s)
                               .orElse(String.valueOf(getProblem()));
    }
}
