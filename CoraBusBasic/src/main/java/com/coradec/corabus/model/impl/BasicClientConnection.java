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
import static java.util.concurrent.TimeUnit.*;

import com.coradec.corabus.com.ConnectionEstablishedEvent;
import com.coradec.corabus.model.ClientConnection;
import com.coradec.coracom.ctrl.ChannelReader;
import com.coradec.coracom.ctrl.ChannelWriter;
import com.coradec.coracom.model.Request;
import com.coradec.coracom.model.impl.BasicRequest;
import com.coradec.coracore.annotation.Nullable;
import com.coradec.coracore.trouble.InitializationError;
import com.coradec.coracore.trouble.OperationTimedoutException;
import com.coradec.corasession.model.Session;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.channels.WritableByteChannel;

/**
 * ​​Basic implementation of a client connection.
 */
@SuppressWarnings("ClassHasNoToStringMethod")
public class BasicClientConnection extends AbstractConnection implements ClientConnection {

    private final ChannelReader reader;
    private final ChannelWriter writer;
    private @Nullable SocketChannel client;
    private @Nullable Request initialization;
    private SelectionKey selection;
    private int selectionOps;

    BasicClientConnection(final Selector selector, final SocketAddress socket,
            final ChannelReader reader, final ChannelWriter writer) {
        super(selector, socket);
        this.reader = reader;
        this.writer = writer;
        initialization = new BasicRequest(this);
        addRoute(ConnectionEstablishedEvent.class, this::onConnectionEstablished);
    }

    private void onConnectionEstablished(final ConnectionEstablishedEvent event) {
        if (initialization == null) throw new IllegalStateException("Initialization absent!");
        initialization.succeed();
        initialization = null;
        select(selectionOps & ~OP_CONNECT);
    }

    private void select(final int ops) {
        if (ops == 0) shutdown();
        else try {
            if (client != null)
                selection = client.register(getSelector(), selectionOps = ops, this);
        } catch (ClosedChannelException e) {
            error(e);
        }
    }

    @Override protected @Nullable Request onInitialize(final Session session) {
        try {
            client = SocketChannel.open();
            client.configureBlocking(false);
            client.connect(getSocket());
            select(OP_CONNECT | OP_READ | OP_WRITE);
            Request request = super.onInitialize(session, selection);
            if (initialization != null) request =
                    initialization.and(request).hold(5, SECONDS, OperationTimedoutException::new);
            return request;
        } catch (IOException e) {
            throw new InitializationError(e);
        }
    }

    @Override protected @Nullable Request onTerminate(final Session session) {
        final Request request = super.onTerminate(session);
        if (client != null) {
            try {
                client.close();
            } catch (IOException e) {
                error(e);
            } finally {
                client = null;
            }
        }
        return request;
    }

    @Override protected boolean read(final ReadableByteChannel peer) throws IOException {
        final boolean more = reader.read(peer);
        if (!more) select(selectionOps & ~OP_READ);
        return more;
    }

    @Override protected boolean write(final WritableByteChannel peer) throws IOException {
        final boolean more = writer.write(peer);
        if (!more) select(selectionOps & ~OP_WRITE);
        return more;
    }

}
