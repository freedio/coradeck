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

import com.coradec.coratext.model.Text;

import java.util.Optional;

/**
 * ​A log entry conveying a problem and an optional explanation for the problem.
 */
public interface ProblemLogEntry extends LogEntry {

    /**
     * Returns the explanatory text literal.
     *
     * @return the explanatory text literal, if any.
     */
    Optional<Text> getText();

    /**
     * Returns the problem.
     *
     * @return the problem.
     */
    Throwable getProblem();

    /**
     * Returns the explanation.
     *
     * @return the explanation, if present
     */
    Optional<String> getExplanation();

}
