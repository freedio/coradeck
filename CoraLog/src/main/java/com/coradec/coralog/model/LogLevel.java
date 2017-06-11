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

package com.coradec.coralog.model;

import static com.coradec.coralog.model.LogSeverity.*;

import com.coradec.coracore.annotation.ToString;
import com.coradec.coracore.model.Representable;
import com.coradec.coracore.util.ClassUtil;

import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * ​Enumeration of log levels.
 */
@SuppressWarnings("HardCodedStringLiteral")
public enum LogLevel implements Representable {
    ALL(SUBLIMINAL, 'A', "ANY"),
    CHAT(SUBLIMINAL, 'C', "CHAT"),
    DEBUG(SUBLIMINAL, 'D', "DEBUG"),
    DETAIL(SUBLIMINAL, 'L', "DTAIL"),
    INFORMATION(INFORMATIONAL, 'I', "INFO"),
    ALERT(INFORMATIONAL, 'A', "ALERT"),
    WARNING(SEVERE, 'W', "WARN"),
    ERROR(SEVERE, 'E', "ERROR"),
    ABORT(CRITICAL, 'X', "ABORT"),
    NONE(CRITICAL, 'N', "NONE");

    private final LogSeverity severity;
    private final char initial;
    private final String shortHand;

    /**
     * Returns a list of severe log levels.
     *
     * @return the severe log levels.
     */
    public static LogLevel[] getSevereLogLevels() {
        return Stream.of(LogLevel.values())
                     .filter(LogLevel::isSevere)
                     .collect(Collectors.toList())
                     .toArray(new LogLevel[0]);
    }

    /**
     * Returns a list of non-severe log levels.
     *
     * @return the non-severe log levels.
     */
    public static LogLevel[] getNonSevere() {
        return Stream.of(LogLevel.values())
                     .filter(logLevel -> !logLevel.isSevere())
                     .collect(Collectors.toList())
                     .toArray(new LogLevel[0]);
    }

    @ToString
    public LogSeverity getSeverity() {
        return this.severity;
    }

    @ToString
    public char getInitial() {
        return this.initial;
    }

    @ToString
    public String getShortHand() {
        return this.shortHand;
    }

    LogLevel(final LogSeverity severity, final char initial, final String shortHand) {
        this.severity = severity;
        this.initial = initial;
        this.shortHand = shortHand;
    }

    /**
     * Checks if the log level is severe (severity is SEVERE or above)
     *
     * @return {@code true} if the log level is severe, {@code false} if not.
     */
    public boolean isSevere() {
        return getSeverity().ordinal() >= SEVERE.ordinal();
    }

    /**
     * Checks if this log level is a t least as severe as the specified log level.
     *
     * @param logLevel the log level to compare against.
     * @return {@code true} if this log levle is equal or higher.
     */
    public boolean atLeast(final LogLevel logLevel) {
        return ordinal() >= logLevel.ordinal();
    }

    @Override public String toString() {
        return ClassUtil.toString(this);
    }

    @Override public String represent() {
        return getShortHand();
    }
}
