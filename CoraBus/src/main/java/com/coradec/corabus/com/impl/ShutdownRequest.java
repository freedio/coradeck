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

import com.coradec.corabus.com.BusControlRequest;
import com.coradec.corabus.model.Bus;
import com.coradec.coracom.model.Recipient;
import com.coradec.coracom.model.Sender;
import com.coradec.coracore.annotation.Inject;
import com.coradec.corasession.model.Session;

import java.util.Collections;

/**
 * ​​Request to shut down the bus system.
 */
public class ShutdownRequest extends NetworkRequest implements BusControlRequest {

    @Inject private static Bus BUS;

    /**
     * Initializes a new instance of ShutdownRequest with the specified sender and list of
     * recipients in the context of the specified session.
     *
     * @param session    the session context.
     * @param sender     the sender.
     * @param recipients the list of recipients
     */
    public ShutdownRequest(final Session session, final Sender sender,
            final Recipient... recipients) {
        super(session, "SHUTDOWN", Collections.emptyMap(), null, sender, BUS.recipient("/"));
    }

}
