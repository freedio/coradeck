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

package com.coradec.coracore.time;

import com.coradec.coracore.model.Factory;
import com.coradec.coracore.model.GenericFactory;

import java.util.concurrent.TimeUnit;

/**
 * ​Representation of a duration with a time unit and an amount.
 */
public interface Duration {

    Factory<Duration> DURATION = new GenericFactory<>(Duration.class);

    /**
     * Creates a suitable duration with the specified amount of the specified time unit.
     *
     * @param amount the amount.
     * @param unit   the time unit.
     */
    static Duration of(long amount, TimeUnit unit) {
        return DURATION.create(amount, unit);
    }

    /**
     * Returns the amount.
     *
     * @return the amount.
     */
    long getAmount();

    /**
     * Returns the time unit.
     *
     * @return the time unit.
     */
    TimeUnit getUnit();

    /**
     * Returns this duration in milliseconds.
     */
    long toMillis();
}
