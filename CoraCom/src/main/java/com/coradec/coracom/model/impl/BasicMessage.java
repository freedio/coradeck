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

import com.coradec.coracom.ctrl.RecipientResolver;
import com.coradec.coracom.model.Message;
import com.coradec.coracom.model.Recipient;
import com.coradec.coracom.model.SessionInformation;
import com.coradec.coracore.annotation.Implementation;
import com.coradec.coracore.annotation.ToString;
import com.coradec.coracore.model.Origin;
import com.coradec.coracore.model.State;
import com.coradec.corasession.model.Session;

import java.util.Map;
import java.util.UUID;

/**
 * ​​Basic implementation of a message.
 */
@SuppressWarnings({"WeakerAccess", "ClassHasNoToStringMethod"})
@Implementation
public class BasicMessage extends BasicEvent implements Message {

    private final boolean urgent;
    private Recipient recipient;
    private RecipientResolver resolver;

    /**
     * Initializes a new instance of BasicMessage with the specified sender and recipient.
     *
     * @param sender    the sender.
     * @param recipient the recipient.
     */
    public BasicMessage(final Origin sender, final Recipient recipient) {
        super(sender);
        this.recipient = recipient;
        this.urgent = false;
    }

    /**
     * Initializes a new instance of BasicMessage from the specified property map.
     *
     * @param properties the property map.
     */
    public BasicMessage(final Map<String, Object> properties) {
        super(properties);
        final Session session = Session.get(
                UUID.fromString((String)properties.get(SessionInformation.PROP_SESSION)));
        this.recipient =
                RecipientResolver.resolveRecipient(session, get(String.class, PROP_RECIPIENT));
        this.urgent = get(Boolean.class, PROP_URGENT, false);
    }

    @Override @ToString public Recipient getRecipient() {
        return recipient;
    }

    @Override public void setRecipent(final Recipient recipient) {
        this.recipient = recipient;
    }

    @Override public void onDeliver() throws IllegalStateException {
        final State state = getState();
        if (state != ENQUEUED) throw new IllegalStateException(
                String.format("Message %s has illegal state %s (should be ENQUEUED)", this,
                        state.name()));
    }

    @Override @ToString public boolean isUrgent() {
        return urgent;
    }

    @Override protected void collect() {
        super.collect();
        set(PROP_RECIPIENT, recipient.getRecipientId());
        set(PROP_URGENT, urgent);
    }

}
