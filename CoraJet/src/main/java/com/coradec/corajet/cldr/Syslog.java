package com.coradec.corajet.cldr;

import java.io.PrintStream;
import java.time.LocalTime;

/**
 * ​​A simple static log facility for the boot loader.
 */
public final class Syslog {

    private static final Syslog ME = new Syslog();

    private static final PrintStream NORMAL = System.out;
    private static final PrintStream ALERT = System.err;

    private static final String ERROR = "ERROR";
    private static final String WARNING = "WARN ";
    private static final String INFO = "INFO ";
    private static final String DEBUG = "DEBUG";

    private static void log(final PrintStream out, final String level, final Throwable problem,
                            final String text) {
        LocalTime time = LocalTime.now();
        final StackTraceElement ste = Thread.currentThread().getStackTrace()[3];
        out.printf("%-13s%-5s %s%n", time, level,
                String.format("%s.%s", ste.getClassName(), ste.getMethodName()));
        out.printf("                   %s%n", text);
        if (problem != null) problem.printStackTrace(out);
        out.flush();
    }

    private Syslog() {
    }

    public static void debug(final String text, final Object... args) {
        log(NORMAL, DEBUG, null, String.format(text, args));
    }

    public static void info(final String text, final Object... args) {
        log(NORMAL, INFO, null, String.format(text, args));
    }

    public static void error(final Throwable problem) {
        log(ALERT, ERROR, problem, null);
    }

    public static void error(final String text, final Object... args) {
        log(ALERT, ERROR, null, String.format(text, args));
    }

    public static void warn(final String text, final Object... args) {
        log(ALERT, WARNING, null, String.format(text, args));
    }
}
