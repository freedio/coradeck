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
import com.coradec.coracom.model.Sender;
import com.coradec.coracom.model.impl.BasicSessionEvent;
import com.coradec.coracore.annotation.Nullable;
import com.coradec.coracore.annotation.ToString;
import com.coradec.corasession.model.Session;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * ​​Implementation of an event across the wire.
 */
@SuppressWarnings("ClassHasNoToStringMethod")
public class NetworkEvent extends BasicSessionEvent implements NetworkInformation {

    private final Map<String, String> attributes;
    private final @Nullable byte[] body;

    /**
     * Initializes a new instance of NetworkEvent from the specified sender with the specified
     * attributes and body (if any) in the context of the specified session.
     *
     * @param session    the session context.
     * @param attributes the attributes.
     * @param body       the body (optional).
     * @param sender     the sender.
     */
    public NetworkEvent(final Session session, final Map<String, String> attributes,
            final @Nullable byte[] body, final Sender sender) {
        super(session, sender);
        this.attributes = new HashMap<>(attributes);
        this.body = body == null ? null : body.clone();
    }

    @Override @ToString public Map<String, String> getAttributes() {
        return Collections.unmodifiableMap(attributes);
    }

    @Override @ToString public @Nullable byte[] getBody() {
        return body == null ? null : body.clone();
    }

}