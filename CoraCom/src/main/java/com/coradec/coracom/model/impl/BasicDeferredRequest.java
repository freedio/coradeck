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

package com.coradec.coracom.model.impl;

import com.coradec.coracom.model.Deferred;
import com.coradec.coracom.model.Recipient;
import com.coradec.coracore.annotation.ToString;
import com.coradec.coracore.model.Origin;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * ​​Basic implementation of a deferred request.
 */
@SuppressWarnings("ClassHasNoToStringMethod")
public class BasicDeferredRequest extends BasicRequest implements Deferred {

    private final long executionTime;

    /**
     * Initializes a new instance of BasicDeferredRequest with the specified delay, sender and
     * recipient.
     *
     * @param amount    the amount of delay.
     * @param unit      the time unit of delay.
     * @param sender    the sender.
     * @param recipient the recipient.
     */
    public BasicDeferredRequest(final long amount, final TimeUnit unit, final Origin sender,
            final Recipient recipient) {
        super(sender, recipient);
        executionTime = System.currentTimeMillis() + unit.toMillis(amount);
    }

    /**
     * Initializes a new instance of BasicDeferredRequest from the specified property map.
     *
     * @param properties the property map.
     */
    private BasicDeferredRequest(final Map<String, Object> properties) {
        super(properties);
        executionTime = get(Long.class, PROP_EXECUTION_TIME);
    }

    @Override public long getExecutionTime() {
        return executionTime;
    }

    @ToString public LocalDateTime getExecutionTimeStamp() {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(getExecutionTime()),
                ZoneId.systemDefault());
    }

    @Override protected void collect() {
        super.collect();
        set(PROP_EXECUTION_TIME, executionTime);
    }

}
