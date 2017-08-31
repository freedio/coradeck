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
import com.coradec.corabus.com.Invitation;
import com.coradec.corabus.com.impl.BasicFocusChangedEvent;
import com.coradec.corabus.com.impl.BasicKeyProcessedEvent;
import com.coradec.corabus.view.BusContext;
import com.coradec.corabus.view.NetworkProtocol;
import com.coradec.coracom.model.Request;
import com.coradec.coracore.annotation.Nullable;
import com.coradec.coracore.trouble.InitializationError;
import com.coradec.corasession.model.Session;
import com.coradec.coratext.model.LocalizedText;
import com.coradec.coratext.model.Text;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URI;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

/**
 * ​​The network server component.
 */
@SuppressWarnings("ClassHasNoToStringMethod")
public class NetworkServer extends AbstractNetworkComponent {

    private static final Text TEXT_SERVER_DISCONNECTED = LocalizedText.define("ServerDisconnected");
    private static final Text TEXT_CONNECTION_ACCEPTED = LocalizedText.define("ConnectionAccepted");
    private static final Text TEXT_UNKNOWN_SOCKET_TYPE = LocalizedText.define("UnknownSocketType");

    private final String protocol$;
    private NetworkProtocol protocol;
    private int port;
    private ServerSocketChannel server;
    private Session session;

    public NetworkServer(String protocol, int port) {
        super(TEXT_SERVER_DISCONNECTED);
        this.protocol$ = protocol;
        this.port = port;
    }

    public NetworkServer(String protocol) {
        this(protocol, 0);
    }

    @Override protected @Nullable Request onAttach(final Session session, final BusContext context,
            final Invitation invitation) {
        final Request request = super.onAttach(session, context, invitation);
        protocol = getService(NetworkProtocol.class, protocol$);
        if (port < 1) port = protocol.getStandardPort();
        addRoute(ConnectionAcceptableEvent.class, this::onAcceptable);
        return request;
    }

    @Override protected @Nullable Request onInitialize(final Session session) {
        final @Nullable Request request = super.onInitialize(session);
        try {
            final InetSocketAddress endpoint = new InetSocketAddress(port);
            debug("Opening server socket for accept on %s", endpoint);
            server = ServerSocketChannel.open();
            server.configureBlocking(false);
            server.socket().bind(endpoint);
            inject(new BasicFocusChangedEvent(this, server, OP_ACCEPT));
            this.session = session;
            return request;
        } catch (IOException e) {
            throw new InitializationError(e);
        }
    }

    private void onAcceptable(final ConnectionAcceptableEvent event) {
        try {
            SocketChannel client = server.accept();
            if (client != null) {
                client.configureBlocking(false);
                final URI uri = uriOf(client);
                add(session, uri.toString(), new BasicClientConnection(client, protocol, uri));
            }
        } catch (IOException e) {
            error(e);
        } finally {
            inject(new BasicKeyProcessedEvent(this, event.getSelectionKey()));
        }
    }

    @SuppressWarnings("StringBufferReplaceableByString")
    private URI uriOf(final SocketChannel client) throws IOException {
        final StringBuilder collector = new StringBuilder(512);
        collector.append(protocol.getScheme()).append("://");
        collector.append(resolve(client.getRemoteAddress()));
        collector.append('/');
        return URI.create(collector.toString());
    }

    private String resolve(SocketAddress address) {
        if (address instanceof InetSocketAddress) {
            InetSocketAddress internetAddress = (InetSocketAddress)address;
            String hostName = internetAddress.getHostName();
            if (hostName == null) hostName = internetAddress.getHostString();
            final int port = internetAddress.getPort();
            return hostName + ":" + port;
        } else throw new IllegalArgumentException(
                TEXT_UNKNOWN_SOCKET_TYPE.resolve(address.getClass().getName()));
    }

}
