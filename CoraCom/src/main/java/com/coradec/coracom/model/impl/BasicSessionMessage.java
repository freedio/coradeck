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

import com.coradec.coracom.model.Message;
import com.coradec.coracom.model.Recipient;
import com.coradec.coracom.model.SessionMessage;
import com.coradec.coracore.annotation.Implementation;
import com.coradec.coracore.model.Origin;
import com.coradec.corasession.model.Session;

import java.util.Map;
import java.util.UUID;

/**
 * ​​Basic implementation of a session message.
 */
@SuppressWarnings("ClassHasNoToStringMethod")
@Implementation
public class BasicSessionMessage extends BasicMessage implements SessionMessage {

    private final Session session;

    /**
     * Initializes a new instance of BasicSessionMessage with the specified sender and recipient in
     * the context of the specified session.
     *
     * @param sender    the sender.
     * @param recipient the recipient.
     */
    public BasicSessionMessage(final Session session, final Origin sender,
            final Recipient recipient) {
        super(sender, recipient);
        this.session = session;
    }

    /**
     * Initializes a new instance of BasicSessionMessage from the specified property map.
     *
     * @param properties the property map.
     */
    public BasicSessionMessage(final Map<String, Object> properties) {
        super(properties);
        session = Session.get(get(UUID.class, PROP_SESSION));
    }

    /**
     * Initializes a new instanceof of BasicSessionMessage from the specified message in the context
     * of the specified session.
     *
     * @param session the session context.
     * @param message the message.
     */
    public BasicSessionMessage(final Session session, Message message) {
        super(message.getProperties());
        this.session = session;
    }

    @Override protected void collect() {
        set(PROP_SESSION, session.getId());
        super.collect();
    }

    /**
     * Returns the session context.
     *
     * @return the session context.
     */
    @Override public Session getSession() {
        return session;
    }
}
