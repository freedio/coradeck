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

import com.coradec.corabus.model.Bus;
import com.coradec.coracom.model.Recipient;
import com.coradec.coracore.annotation.Inject;
import com.coradec.coracore.model.Origin;
import com.coradec.corasession.model.Session;

import java.util.Map;

/**
 * ​​Request to shut down the bus system.
 */
public class ShutdownRequest extends BasicNetworkRequest {

    @Inject private static Bus BUS;

    /**
     * Initializes a new instance of ShutdownRequest with the specified sender and recipients in the
     * context of the specified session.
     *
     * @param session   the session context.
     * @param sender    the sender.
     * @param recipient the recipient.
     */
    public ShutdownRequest(final Session session, final Origin sender, final Recipient recipient) {
        super(session, sender, recipient, "SHUTDOWN");
    }

    /**
     * Initializes a new instance of ShutdownRequest from the specified property map.
     *
     * @param properties the property map.
     */
    public ShutdownRequest(final Map<String, Object> properties) {
        super(properties);
    }

}
