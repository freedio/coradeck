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

import com.coradec.corabus.com.OutboundMessage;
import com.coradec.corabus.com.ReadyToConnectEvent;
import com.coradec.corabus.com.impl.BasicKeyProcessedEvent;
import com.coradec.corabus.model.Bus;
import com.coradec.corabus.model.ServerConnection;
import com.coradec.corabus.view.NetworkProtocol;
import com.coradec.coracom.model.SessionEvent;
import com.coradec.coracom.model.SessionInformation;
import com.coradec.coracom.model.SessionRequest;
import com.coradec.coracore.annotation.Inject;
import com.coradec.coracore.trouble.UnimplementedOperationException;

import java.io.IOException;
import java.net.URI;
import java.nio.channels.SocketChannel;

/**
 * ​​Basic implementation of a server connection.
 */
@SuppressWarnings("ClassHasNoToStringMethod")
public class BasicServerConnection extends AbstractNetworkConnection implements ServerConnection {

    private IOException connectionFailure;

    @Inject private Bus bus;

    public BasicServerConnection(final SocketChannel server, final NetworkProtocol protocol,
            final URI target) {
        super(server, protocol, target, OP_CONNECT);
        addRoute(ReadyToConnectEvent.class, this::establishConnection);
    }

    private void establishConnection(final ReadyToConnectEvent event) {
        final SocketChannel channel = getChannel();
        try {
            while (!channel.finishConnect()) Thread.yield();
            debug("Server connection %s established,", getChannel());
            deselect(OP_CONNECT);
            select(OP_READ | OP_WRITE);
        } catch (IOException e) {
            connectionFailure = e;
        } finally {
            inject(new BasicKeyProcessedEvent(this, event.getSelectionKey()));
        }
    }

    @Override protected void requestReceived(final SessionRequest request) {
        throw new UnimplementedOperationException();
    }

    @Override protected void eventReceived(final SessionEvent event) {
        throw new UnimplementedOperationException();
    }

    @Override protected void infoReceived(final SessionInformation info) {
        throw new UnimplementedOperationException();
    }

    @Override public void output(final OutboundMessage message) throws IOException {
        super.output(message);
    }

}
