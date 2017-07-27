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

package com.coradec.corabus.model.impl;

import com.coradec.coracom.model.Request;
import com.coradec.coracore.annotation.Inject;
import com.coradec.coracore.annotation.Nullable;
import com.coradec.coradir.model.Path;
import com.coradec.corasession.model.Session;

import java.net.SocketAddress;

/**
 * Implementation of the system bus as a local system bus.​​
 */
@SuppressWarnings("ClassHasNoToStringMethod")
class LocalSystemBus extends BasicSystemBus {

    private final SocketAddress server;
    @Inject private Session initSession;

    LocalSystemBus(final SocketAddress socket) {
        this.server = socket;
    }

    @Override protected @Nullable Request onInitialize(final Session session) {
        final Request request = super.onInitialize(session);
        add(initSession, Path.of("net"), new Network());
        add(initSession, Path.of("net/server"), new NetworkServer());
        add(initSession, Path.of("console"), new ServerConsole());
        return request;
    }
}