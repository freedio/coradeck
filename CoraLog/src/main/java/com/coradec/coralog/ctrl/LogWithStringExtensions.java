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
