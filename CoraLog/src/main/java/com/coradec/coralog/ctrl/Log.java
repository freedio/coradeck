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
