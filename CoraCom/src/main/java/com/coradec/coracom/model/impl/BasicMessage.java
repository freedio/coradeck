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

import static com.coradec.coracom.state.QueueState.*;

import com.coradec.coracom.model.Message;
import com.coradec.coracom.model.Recipient;
import com.coradec.coracom.model.Sender;
import com.coradec.coracore.annotation.Implementation;
import com.coradec.coracore.annotation.ToString;
import com.coradec.coracore.model.State;
import com.coradec.coracore.util.ClassUtil;
import com.coradec.coracore.util.CollectionUtil;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * ​​Basic implementation of a message.
 */
@SuppressWarnings("WeakerAccess")
@Implementation
public class BasicMessage extends BasicEvent implements Message {

    private final Set<Recipient> recipients;
    private int deliveries;  // number of remaining deliveries

    /**
     * Initializes a new instance of BasicMessage with the specified sender and set of recipients.
     *
     * @param sender     the sender.
     * @param recipients the recipients.
     */
    private BasicMessage(final Sender sender, final Set<Recipient> recipients) {
        super(sender);
        this.recipients = new HashSet<>(recipients);
        deliveries = 0; // not yet dispatched
    }

    /**
     * Initializes a new instance of BasicMessage with the specified sender and list of recipients.
     *
     * @param sender     the sender.
     * @param recipients the list of recipients
     */
    public BasicMessage(final Sender sender, Recipient... recipients) {
        this(sender, CollectionUtil.setOf(recipients));
    }

    @Override @ToString public Collection<Recipient> getRecipients() {
        return Collections.unmodifiableCollection(recipients);
    }

    @Override public Recipient[] getRecipientList() {
        return recipients.toArray(new Recipient[recipients.size()]);
    }

    @Override public void onDeliver() throws IllegalStateException {
        final State state = getState();
        if (state != ENQUEUED) throw new IllegalStateException(
                String.format("Message %s has illegal state %s (should be ENQUEUED)", this,
                        state.name()));
        if (--deliveries == 0) onDelivered();
    }

    @Override public void onDelivered() {
        final State state = getState();
        if (state != ENQUEUED) throw new IllegalStateException(
                String.format("Message %s has illegal state %s (should be ENQUEUED)", this,
                        state.name()));
        setState(DELIVERED);
    }

    @Override public void setDeliveries(final int recipients) {
        deliveries = recipients;
    }

    @Override public String toString() {
        return ClassUtil.toString(this);
    }

    @Override @ToString public Sender getSender() {
        return (Sender)getOrigin();
    }

    @Override @ToString public boolean isUrgent() {
        return false;
    }

}
