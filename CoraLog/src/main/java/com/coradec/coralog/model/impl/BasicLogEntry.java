/*
 * Copyright ⓒ 2017 by Coradec GmbH.
 *
 * This file is part of the Coradeck.
 *
 * Coradeck is free software: you can redistribute it under the the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or any later version.
 *
 * Coradeck is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR ANY PARTICULAR PURPOSE.  See the GNU General Public License for further details.
 *
 * The GNU General Public License is available from <http://www.gnu.org/licenses/>.
 *
 * @license GPL-3.0+ <http://spdx.org/licenses/GPL-3.0+>
 *
 * @author Dominik Wezel <dom@coradec.com>
 */

package com.coradec.coralog.model.impl;

import com.coradec.coracore.annotation.ToString;
import com.coradec.coracore.model.Origin;
import com.coradec.coracore.util.ClassUtil;
import com.coradec.coralog.model.LogEntry;
import com.coradec.coralog.model.LogLevel;

import java.time.LocalDateTime;

/**
 * ​​Basic implementation of a log entry.
 */
public class BasicLogEntry implements LogEntry {

    private final LocalDateTime timestamp;
    private final Origin origin;
    private final LogLevel level;

    public BasicLogEntry(final Origin origin, final LogLevel level) {
        this.timestamp = LocalDateTime.now();
        this.origin = origin;
        this.level = level;
    }

    @Override @ToString public Origin getOrigin() {
        return this.origin;
    }

    @Override @ToString public LogLevel getLevel() {
        return this.level;
    }

    @Override @ToString public LocalDateTime getTimestamp() {
        return timestamp;
    }

    @Override public String toString() {
        return ClassUtil.toString(this);
    }
}
