package com.coradec.coralog.ctrl;

import com.coradec.coracore.annotation.Nullable;
import com.coradec.coracore.model.Origin;
import com.coradec.coralog.model.LogEntry;
import com.coradec.coralog.model.LogLevel;
import com.coradec.coratext.model.Text;

/**
 * ​A facility processing log entries.
 */
public interface Log {

    /**
     * Checks if this log logs at least at the specified log level.
     *
     * @param level the log level threshold.
     * @return {@code true} if the log logs at least at the specified log level, otherwise {@code
     * false}.
     */
    boolean logsAt(LogLevel level);

    /**
     * Writes the specified log entry to the log.
     *
     * @param entry the entry.
     */
    void log(LogEntry entry);

    /**
     * Returns the reference log level.
     *
     * @return the reference log level.
     */
    LogLevel getLevel();

    /**
     * Logs the specified problem from the specified origin along with the specified explanatory
     * text at log level WARNING.
     *
     * @param origin   the origin.
     * @param problem  the problem to getLog().
     * @param text     the explanatory text (optional).
     * @param textArgs arguments for the explanatory text (optional).
     */
    void warn(final Origin origin, final @Nullable Throwable problem, final @Nullable Text text,
              final @Nullable Object... textArgs);

    /**
     * Logs the specified problem from the specified origin at log level WARNING.
     *
     * @param origin  the origin.
     * @param problem the problem to getLog().
     */
    void warn(Origin origin, Throwable problem);

    /**
     * Logs the specified problem from the specified origin along with the specified explanatory
     * text at log level ERROR.
     *
     * @param origin   the origin.
     * @param problem  the problem to getLog().
     * @param text     the explanatory text.
     * @param textArgs arguments to fit into the template text.
     */
    void error(final Origin origin, final @Nullable Throwable problem, final @Nullable Text text,
               final Object... textArgs);

    /**
     * Logs the specified problem from the specified origin at log level ERROR.
     *
     * @param origin  the origin.
     * @param problem the problem to getLog().
     */
    void error(Origin origin, Throwable problem);

    /**
     * Logs the specified text from the specified origin at log level DEBUG.
     *
     * @param origin   the origin.
     * @param text     the text.
     * @param textArgs optional arguments to fit into the template text.
     */
    void debug(final Origin origin, final String text, final Object... textArgs);

    LogWithStringExtensions discloseStringExtensions();
}
