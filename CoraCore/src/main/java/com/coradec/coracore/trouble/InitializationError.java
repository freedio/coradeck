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

package com.coradec.coracore.trouble;

/**
 * ​​Indicates an error while initializing a class.
 */
public class InitializationError extends BasicException {

    /**
     * Initializes a new instance of InitializationError with the specified explanation and
     * underlying problem.
     *
     * @param explanation the explanation.
     * @param problem     the underlying problem.
     */
    public InitializationError(final String explanation, final Throwable problem) {
        super(explanation, problem);
    }

    /**
     * Initializes a new instance of InitializationError with the specified explanation.
     *
     * @param explanation the explanation.
     */
    public InitializationError(final String explanation) {
        super(explanation);
    }

    public InitializationError(final Throwable problem) {
        super(problem);
    }
}
