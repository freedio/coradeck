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

package com.coradec.coracore.model.impl;

import com.coradec.coracore.annotation.Implementation;
import com.coradec.coracore.model.Timer;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * ​​BNasic implementation of a timer.
 */
@SuppressWarnings("ClassHasNoToStringMethod")
@Implementation
public class BasicTimer implements Timer {

    private Instant start = null, stop = null, lap = null;

    public BasicTimer() {
        this.start = lap();
    }

    @Override public Timer start() {
        start = lap();
        return this;
    }

    @Override public Timer stop() {
        stop = lap = lap();
        return this;
    }

    private Instant lap() {
        return Instant.now();
    }

    @Override public long get(final ChronoUnit unit) {
        return unit.between(start, stop != null ? stop : lap != null ? lap : Instant.now());
    }

}
