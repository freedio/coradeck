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

import com.coradec.corabus.com.NetworkRequest;
import com.coradec.coracom.model.Recipient;
import com.coradec.coracom.model.impl.BasicSessionRequest;
import com.coradec.coracore.annotation.Implementation;
import com.coradec.coracore.model.Origin;
import com.coradec.corasession.model.Session;

import java.util.Map;

/**
 * ​​Basic implementation of a network request.
 */
@SuppressWarnings("ClassHasNoToStringMethod")
@Implementation
public class BasicNetworkRequest extends BasicSessionRequest implements NetworkRequest {

    private final String command;

    /**
     * Initializes a new instance of BasicRequest with the specified sender, recipient and command.
     *
     * @param sender    the sender.
     * @param recipient the recipient of the request
     * @param command   the command.
     */
    public BasicNetworkRequest(final Session session, final Origin sender,
            final Recipient recipient, final String command) {
        super(session, sender, recipient);
        this.command = command;
    }

    /**
     * Initializes a new instance of BasicNetworkRequest from the specified property map.
     *
     * @param properties the property map.
     */
    public BasicNetworkRequest(final Map<String, Object> properties) {
        super(properties);
        this.command = get(String.class, PROP_COMMAND);
    }

    /**
     * Returns the command.
     *
     * @return the command.
     */
    @Override public String getCommand() {
        return command;
    }

    @Override protected void collect() {
        super.collect();
        set(PROP_COMMAND, command);
    }

}
