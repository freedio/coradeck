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
import com.coradec.coracom.model.Sender;
import com.coradec.coracore.annotation.Attribute;
import com.coradec.coracore.annotation.ToString;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.concurrent.TimeUnit;

/**
 * ​​Abstract implementation of a deferred command.
 */
public abstract class BasicDeferredCommand extends BasicCommand implements Deferred {

    private final long executionTime;

    /**
     * Initializes a new instance of AbstractDeferred Command with the specified delay, sender and
     * list of recipients.
     *
     * @param amount     the amount of delay.
     * @param unit       the delay time unit.
     * @param sender     the sender.
     * @param recipients the list of recipients
     */
    protected BasicDeferredCommand(final long amount, final TimeUnit unit, final Sender sender,
            final Recipient... recipients) {
        super(sender, recipients);
        executionTime = System.currentTimeMillis() + unit.toMillis(amount);
    }

    @Override public long getExecutionTime() {
        return executionTime;
    }

    @ToString @Attribute("On") public LocalDateTime getExecutionTimeStamp() {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(executionTime), ZoneId.systemDefault());
    }

}
