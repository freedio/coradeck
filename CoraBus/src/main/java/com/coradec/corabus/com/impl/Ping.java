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

import com.coradec.coracom.model.Recipient;
import com.coradec.coracore.model.Origin;
import com.coradec.corasession.model.Session;

import java.util.Map;

/**
 * ​​A ping request to an (external) bus system.
 * <p>
 * The request will succeed if the bus is ready for service, fail if the bus is alive but denying
 * access, or stall if the bus is not alive.
 * <p>
 * Typically, the PING request to host {@code example.com} is triggered like this:
 * <p>
 * <pre>
 * int n = the number of seconds you want to spend waiting for the PING;
 * Session session = your session context;
 * try {
 *     inject(new Ping(session, this, "//example.com/").standby(n, SECONDS);
 *     debug("Bus at example.com is up and ready for service");
 * } catch (OperationTimedoutException e) {
 *     error(e, "Bus at example.com is down!");
 * } catch (AccessDenied e) {
 *     error(e, "Bus at example.com is not ready to service session %s!", session);
 * }
 * </pre>
 */
public class Ping extends BasicNetworkRequest {

    public Ping(final Session session, final Origin sender, final Recipient recipient) {
        super(session, sender, recipient, "PING");
    }

    /**
     * Initializes a new instance of BasicNetworkRequest from the specified property map.
     *
     * @param properties the property map.
     */
    public Ping(final Map<String, Object> properties) {
        super(properties);
    }

}
