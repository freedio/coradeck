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

package com.coradec.corabus.com.impl;

import com.coradec.corabus.com.OutboundMessage;
import com.coradec.coracom.model.Message;
import com.coradec.coracom.model.Recipient;
import com.coradec.coracom.model.Request;
import com.coradec.coracom.model.SessionMessage;
import com.coradec.coracom.model.SessionRequest;
import com.coradec.coracom.model.impl.BasicSessionMessage;
import com.coradec.coracore.annotation.ToString;
import com.coradec.coradir.model.Path;
import com.coradec.corasession.model.Session;

/**
 * ​​Basic implementation of an outbound message.
 */
@SuppressWarnings("ClassHasNoToStringMethod")
public class BasicOutboundMessage extends BasicSessionMessage implements OutboundMessage {

    private final SessionMessage message;
    private final Path path;

    public BasicOutboundMessage(final Session session, final Message message, final Path path,
            final Recipient recipient) {
        super(session, message.getOrigin(), recipient);
        this.message = toSessionMessage(message);
        this.path = path;
    }

    private SessionMessage toSessionMessage(final Message message) {
        if (message instanceof SessionMessage) return (SessionMessage)message;
        if (message instanceof Request) return SessionRequest.wrap(getSession(), (Request)message);
        return SessionMessage.wrap(getSession(), message);
    }

    @Override @ToString public SessionMessage getContent() {
        return message;
    }

    @Override @ToString public Path getPath() {
        return path;
    }

}
