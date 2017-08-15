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

import com.coradec.corabus.model.ClientConnection;
import com.coradec.corabus.model.ServerConnection;
import com.coradec.corabus.view.NetworkProtocol;
import com.coradec.coracore.annotation.Inject;
import com.coradec.coracore.collections.HashCache;
import com.coradec.corasession.model.Session;
import com.coradec.coratext.model.LocalizedText;
import com.coradec.coratext.model.Text;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URI;
import java.nio.channels.SocketChannel;

/**
 * The network client.
 */
@SuppressWarnings("ClassHasNoToStringMethod")
public class NetworkClient extends AbstractNetworkComponent {

    private static final Text TEXT_CLIENT_DISCONNECTED = LocalizedText.define("ClientDisconnected");
    private static final Text TEXT_NO_PROTOCOL = LocalizedText.define("NoProtocol");
    private static final Text TEXT_NO_ADDRESS = LocalizedText.define("NoAddress");
    private static final Text TEXT_NO_PORT = LocalizedText.define("NoPort");
    static final Text TEXT_INVALID_RESOURCE_IDENTIFIER =
            LocalizedText.define("InvalidResourceIdentifier");

    @Inject private HashCache<SocketAddress, ClientConnection> connections;

    public NetworkClient() {
        super(TEXT_CLIENT_DISCONNECTED);
    }

    /**
     * Returns the client connection associated with the specified resource identifier in the
     * context of the specified session.
     *
     * @param session the session context.
     * @param target  the resource identifier.
     * @return the associated client connection.
     */
    ServerConnection getServerConnection(final Session session, final URI target)
            throws IOException {
        final String name = target.toString();
        ServerConnection result = (ServerConnection)lookup(session, name).orElse(null);
        if (result == null) {
            final SocketChannel server = SocketChannel.open();
            server.configureBlocking(false);
            String protocol$ = target.getScheme();
            final NetworkProtocol protocol = getService(NetworkProtocol.class, protocol$);
            int port = target.getPort();
            if (port < 1) port = protocol.getStandardPort();
            server.connect(new InetSocketAddress(target.getHost(), port));
            server.register(getSelector(), OP_CONNECT);
            add(session, name, result = new BasicServerConnection(getSelector(), server, target));
        }
        return result;
    }

}
