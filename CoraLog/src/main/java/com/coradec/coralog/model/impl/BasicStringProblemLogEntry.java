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

    public BasicStringProblemLogEntry(final Origin origin, final LogLevel level, final Throwable
            problem, final @Nullable String text,
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
