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

import static java.nio.channels.SelectionKey.*;

import com.coradec.corabus.com.ConnectionAcceptableEvent;
import com.coradec.corabus.model.Bus;
import com.coradec.corabus.model.ServerConnection;
import com.coradec.coracom.model.Information;
import com.coradec.coracom.model.Recipient;
import com.coradec.coracom.model.SessionEvent;
import com.coradec.coracom.model.SessionRequest;
import com.coradec.coracom.model.impl.BasicRequest;
import com.coradec.coracore.annotation.Inject;
import com.coradec.coracore.trouble.UnimplementedOperationException;
import com.coradec.coradir.model.Path;

import java.io.IOException;
import java.net.URI;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * ​​Basic implementation of a server connection.
 */
@SuppressWarnings("ClassHasNoToStringMethod")
public class BasicServerConnection extends AbstractNetworkConnection implements ServerConnection {

    private IOException connectionFailure;

    private final Map<UUID, SessionRequest> outboundRequests = new HashMap<>();
    @Inject private Bus bus;

    public BasicServerConnection(final Selector selector, final SocketChannel server,
            final URI target) {
        super(selector, server, OP_CONNECT, target);
        addRoute(ConnectionAcceptableEvent.class, this::establishConnection);
    }

    private void establishConnection(final ConnectionAcceptableEvent event) {
        final SocketChannel channel = getChannel();
        try {
            while (!channel.finishConnect()) Thread.yield();
            deselect(OP_CONNECT);
            select(OP_READ | OP_WRITE);
        } catch (IOException e) {
            connectionFailure = e;
        }
    }

    @Override protected void requestReceived(final SessionRequest request) {
        throw new UnimplementedOperationException();
    }

    @Override protected void eventReceived(final SessionEvent event) {
        inject(new ResolveEventRequest(bus.get(getInitialSession(), Path.of("/net/resolver"))));
    }

    @Override protected void infoReceived(final Information info) {
        throw new UnimplementedOperationException();
    }

    private class ResolveEventRequest extends BasicRequest {

        public ResolveEventRequest(final Recipient... recipients) {
            super(BasicServerConnection.this, recipients);
        }

    }

}
