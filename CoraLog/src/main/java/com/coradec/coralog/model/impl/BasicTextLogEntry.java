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
