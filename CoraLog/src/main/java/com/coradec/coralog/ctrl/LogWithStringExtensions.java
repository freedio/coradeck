package com.coradec.coralog.ctrl;

import com.coradec.coracore.model.Origin;
import com.coradec.coralog.model.LogLevel;

/**
 * Extension of a standard log disclosing string extensions.
 */
public interface LogWithStringExtensions extends Log {

    /**
     * Logs the specified problem from the specified origin along with the specified explanation
     * (with optional arguments) at log level {@link LogLevel#ERROR}.
     *
     * @param origin  the origin.
     * @param problem the problem.
     * @param text    the explanation.
     * @param args    arguments to the explanation (as needed).
     */
    void error(Origin origin, Throwable problem, String text, Object... args);

    /**
     * Logs the specified error text with optional arguments at log level ERROR.
     *
     * @param text the text.
     * @param args arguments to the text (as needed).
     */
    void error(Origin origin, String text, Object... args);

    /**
     * Logs the specified text with the specified optional arguments at log level {@link
     * LogLevel#INFORMATION}.
     *
     * @param text the text.
     * @param args arguments to the text (as needed).
     */
    void info(Origin origin, String text, Object... args);

}
