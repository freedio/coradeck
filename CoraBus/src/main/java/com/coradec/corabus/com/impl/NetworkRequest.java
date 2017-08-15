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

import com.coradec.coracom.model.NetworkInformation;
import com.coradec.coracom.model.Recipient;
import com.coradec.coracom.model.Sender;
import com.coradec.coracom.model.impl.BasicSessionRequest;
import com.coradec.coracore.annotation.Nullable;
import com.coradec.coracore.annotation.ToString;
import com.coradec.corasession.model.Session;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * ​​Implementation of a request across the wire.
 */
@SuppressWarnings("ClassHasNoToStringMethod")
public class NetworkRequest extends BasicSessionRequest implements NetworkInformation {

    private final String command;
    private final Map<String, String> attributes;
    private final @Nullable byte[] body;

    /**
     * Initializes a new instance of NetworkRequest from the specified sender to the specified
     * recipient(s) with the specified additional headers and the specified body (if any) in the
     * context of the specified session.
     *
     * @param session    the session context.
     * @param attributes the additional header attributes.
     * @param body       the body (optional).
     * @param sender     the sender.
     * @param recipients the recipient(s).
     */
    public NetworkRequest(final Session session, final String command,
            final Map<String, String> attributes, final @Nullable byte[] body, final Sender sender,
            final Recipient... recipients) {
        super(session, sender, recipients);
        this.command = command;
        this.attributes = new HashMap<>(attributes);
        this.body = body == null ? null : body.clone();
    }

    @ToString public String getCommand() {
        return command;
    }

    @Override @ToString public Map<String, String> getAttributes() {
        return Collections.unmodifiableMap(attributes);
    }

    @Override @ToString @Nullable public byte[] getBody() {
        return body == null ? null : body.clone();
    }
}
