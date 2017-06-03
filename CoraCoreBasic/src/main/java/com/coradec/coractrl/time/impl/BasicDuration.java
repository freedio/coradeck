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

package com.coradec.coractrl.time.impl;

import com.coradec.coracore.annotation.Implementation;
import com.coradec.coracore.time.Duration;

import java.util.concurrent.TimeUnit;

/**
 * ​​Basic implementation of a duration.
 */
@Implementation
public class BasicDuration implements Duration {

    private final long amount;
    private final TimeUnit unit;

    public BasicDuration(long amount, TimeUnit unit) {
        this.amount = amount;
        this.unit = unit;
    }

    public long getAmount() {
        return amount;
    }

    public TimeUnit getUnit() {
        return unit;
    }

    @Override public String toString() {
        return String.format("%d %s", getAmount(), getUnit());
    }
}
