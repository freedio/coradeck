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

import com.coradec.coracom.model.Request;
import com.coradec.coracore.annotation.Nullable;
import com.coradec.coracore.trouble.InitializationError;
import com.coradec.coracore.util.NetworkUtil;
import com.coradec.corasession.model.Session;
import com.coradec.coratext.model.LocalizedText;
import com.coradec.coratext.model.Text;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.channels.ClosedSelectorException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

/**
 * ​​The network server component.
 * <p>
 * Clients like system bus proxies connect to this component via TCP/IP.
 */
@SuppressWarnings("ClassHasNoToStringMethod")
public class NetworkServer extends BasicBusApplication {

    private static final Text TEXT_SERVER_DISCONNECTED = LocalizedText.define("ServerDisconnected");
    private static final Text TEXT_CONNECTION_ACCEPTED = LocalizedText.define("ConnectionAccepted");

    private final SocketAddress socket;
    private ServerSocketChannel server;
    private Selector selector;
    private SelectionKey selection;

    NetworkServer() {
        this.socket = NetworkUtil.getLocalAddress(BasicSystemBus.getServerSocketPort());
    }

    @Override protected @Nullable Request onInitialize(final Session session) {
        final @Nullable Request request = super.onInitialize(session);
        try {
            selector = Selector.open();
            server = ServerSocketChannel.open();
            server.socket().bind(socket);
            server.configureBlocking(false);
            selection = server.register(selector, OP_ACCEPT);
        } catch (IOException e) {
            throw new InitializationError(e);
        }
        return request;
    }

    @Override protected @Nullable Request onTerminate(final Session session) {
        final @Nullable Request request = super.onTerminate(session);
        try {
            server.close();
            selector.close();
        } catch (IOException e) {
            error(e);
        }
        return request;
    }

    @Override public void run() {
        while (!Thread.interrupted()) {
            final int selected;
            try {
                selected = selector.select();
                if (selected != 0) {
                    for (Iterator<SelectionKey> it = selector.selectedKeys().iterator();
                         it.hasNext(); ) {
                        final SelectionKey key = it.next();
                        if (key.isConnectable()) onConnectionEstablished(key);
                        else if (key.isReadable()) onReadable(key);
                        else if (key.isWritable()) onWritable(key);
                        it.remove();
                    }
                }
            } catch (ClosedSelectorException e) {
                info(TEXT_SERVER_DISCONNECTED);
                // we're done
                break;
            } catch (IOException e) {
                error(e);
            }
        }
    }

    private void onConnectionEstablished(final SelectionKey key) throws IOException {
        final SelectableChannel channel = key.channel();
        if (channel instanceof ServerSocketChannel) {
            ServerSocketChannel serverSocket = (ServerSocketChannel)channel;
            final SocketChannel client = serverSocket.accept();
            info(TEXT_CONNECTION_ACCEPTED, client);
            client.configureBlocking(false);
            client.register(selector, OP_READ | OP_WRITE, client);
        }
    }

    private void onReadable(final SelectionKey key) {

    }

    private void onWritable(final SelectionKey key) {

    }

}
