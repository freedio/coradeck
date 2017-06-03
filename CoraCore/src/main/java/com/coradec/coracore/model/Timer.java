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

package com.coradec.coracore.model;

import java.time.temporal.ChronoUnit;

/**
 * ​Representation of a t9mer.
 */
@SuppressWarnings("UnusedReturnValue")
public interface Timer {

    /**
     * Starts the timer.
     *
     * @return this timer, for method chaining.
     */
    Timer start();

    /**
     * Stops the timer and registers the time.
     *
     * @return this timer, for method chaining.
     */
    Timer stop();

    /**
     * Returns the time of the last lap in the specified time unit.
     *
     * @param unit the time unit.
     * @return the amount of time since the last lap in the specified time unit.
     */
    long get(ChronoUnit unit);

}
