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

package com.coradec.corajet.cldr;

import com.coradec.coracore.annotation.Nullable;

import java.io.PrintStream;
import java.time.LocalTime;
import java.util.Optional;

/**
 * ​​A simple static log facility for the boot loader.
 */
@SuppressWarnings("UseOfSystemOutOrSystemErr")
public final class Syslog {

    private static final Syslog ME = new Syslog();

    private static final PrintStream NORMAL = System.out;
    private static final PrintStream ALERT = System.err;

    /**
     * Sets the syslog level.
     *
     * @param level the new level.
     * @throws IllegalArgumentException if the log level is invalid.
     */
    public static void setLevel(final String level) throws IllegalArgumentException {
        SYSLOG_LEVEL = LogLevel.valueOf(level);
        System.out.printf("syslog.level is now %s%n", SYSLOG_LEVEL);
        System.out.flush();
    }

    @SuppressWarnings("ClassHasNoToStringMethod")
    enum LogLevel {
        TRACE("TRACE"),
        DEBUG("DEBUG"),
        INFORMATION("INFO "),
        ALERT("ALERT"),
        WARNING("WARN "),
        ERROR("ERROR");

        private final String tag;

        LogLevel(final String tag) {
            this.tag = tag;
        }

        public String getTag() {
            return this.tag;
        }

        boolean below(final LogLevel level) {
            return ordinal() < level.ordinal();
        }
    }

    private static final String SYSLOG_LEVEL$;
    private static LogLevel SYSLOG_LEVEL;

    static {
        SYSLOG_LEVEL$ =
                Optional.ofNullable(System.getProperty("syslog.level")).orElse("INFORMATION");
        try {
            SYSLOG_LEVEL = LogLevel.valueOf(SYSLOG_LEVEL$);
        } catch (IllegalArgumentException e) {
            SYSLOG_LEVEL = LogLevel.INFORMATION;
            System.err.printf("Invalid syslog.level system property: %s!%n", SYSLOG_LEVEL$);
        }
        System.out.printf("syslog.level is %s%n", SYSLOG_LEVEL);
        System.out.flush();
    }

    private static void log(final PrintStream out, final LogLevel level,
                            final @Nullable Throwable problem, final @Nullable String text) {
        if (!level.below(SYSLOG_LEVEL)) {
            LocalTime time = LocalTime.now();
            final StackTraceElement ste = Thread.currentThread().getStackTrace()[3];
            out.printf("%-13s%-5s %s(%d)%n", time, level.getTag(),
                    String.format("%s.%s", ste.getClassName(), ste.getMethodName()),
                    ste.getLineNumber());
            if (text != null) out.printf("                   %s%n", text);
            if (problem != null) problem.printStackTrace(out);
            out.flush();
        }
    }

    private Syslog() {
    }

    public static void debug(final String text, final Object... args) {
        log(NORMAL, LogLevel.DEBUG, null, String.format(text, args));
    }

    public static void info(final Throwable problem, final String text, final Object... args) {
        log(NORMAL, LogLevel.INFORMATION, problem, String.format(text, args));
    }

    public static void info(final String text, final Object... args) {
        log(NORMAL, LogLevel.INFORMATION, null, String.format(text, args));
    }

    public static void error(final Throwable problem) {
        log(ALERT, LogLevel.ERROR, problem, null);
    }

    public static void error(final String text, final Object... args) {
        log(ALERT, LogLevel.ERROR, null, String.format(text, args));
    }

    public static void warn(final String text, final Object... args) {
        log(ALERT, LogLevel.WARNING, null, String.format(text, args));
    }

    public static void warn(final Throwable problem) {
        log(ALERT, LogLevel.WARNING, problem, null);
    }

    public static void info(final Throwable problem) {
        log(NORMAL, LogLevel.INFORMATION, problem, null);
    }

    public static void trace(final String text, final Object... args) {
        log(NORMAL, LogLevel.TRACE, null, String.format(text, args));
    }

}
