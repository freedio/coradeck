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

import com.coradec.corabus.com.DataAvailableEvent;
import com.coradec.corabus.com.ReadyToSendEvent;
import com.coradec.coracom.model.Request;
import com.coradec.coraconf.model.Property;
import com.coradec.coracore.annotation.NonNull;
import com.coradec.coracore.annotation.Nullable;
import com.coradec.coracore.annotation.ToString;
import com.coradec.corasession.model.Session;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.channels.WritableByteChannel;

/**
 * ​​Basic implementation of a connection as a bus node.
 */
public abstract class AbstractConnection extends BasicNode {

    private static final Property<Integer> PROP_XFER_BUFFER_SIZE =
            Property.define("XferBufferSize", Integer.class, 4096);

    private final Selector selector;
    private final SocketAddress socket;
    private @Nullable SelectionKey selection;

    protected AbstractConnection(final Selector selector, final SocketAddress socket) {
        this.selector = selector;
        this.socket = socket;
        addRoute(DataAvailableEvent.class, this::onDataAvailable);
        addRoute(ReadyToSendEvent.class, this::onReadyToSend);
    }

    private SelectionKey getSelection() {
        if (selection == null) throw new IllegalStateException("Not initialized!");
        return selection;
    }

    private void setSelection(final @NonNull SelectionKey selection) {
        this.selection = selection;
    }

    protected Selector getSelector() {
        return selector;
    }

    @ToString public SocketAddress getSocket() {
        return socket;
    }

    protected @Nullable Request onInitialize(final Session session, SelectionKey selection) {
        this.selection = selection;
        return super.onInitialize(session);
    }

    private void onReadyToSend(final ReadyToSendEvent event) {
        SocketChannel peer = event.getPeer();
        try {
            if (!write(peer)) shutdown();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void onDataAvailable(final DataAvailableEvent event) {
        SocketChannel peer = event.getPeer();
        try {
            if (!read(peer)) shutdown();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override protected @Nullable Request onTerminate(final Session session) {
        final Request request = super.onTerminate(session);
        if (selection != null) {
            selection.cancel();
            selection = null;
        }
        return request;
    }

    /**
     * Triggers a read operation on the specified channel.  Subclasses are supposed to try to read
     * as much data as immediately available from the channel.  Due to the channel's asynchronous
     * (non-blocking) nature, only part of the subclass's buffer may be filled, so the subclass has
     * to wait for another read trigger once less data than expected arrive.
     *
     * @param peer the peer with data to read.
     * @return {@code true} if there are more data to read, {@code false} if the channel has been
     * closed for good.
     * @throws IOException if a read error occurred on the socket.
     */
    protected abstract boolean read(final ReadableByteChannel peer) throws IOException;

    /**
     * Triggers a write operation on the specified channel.  Subclasses are supposed to try to write
     * their accumulated data to the channel.  Due to the channel's asynchronous (non-blocking)
     * nature, only part of the data may actually get written, so the subclass has to adjust the
     * output buffer and wait for another write trigger to continue writing.
     *
     * @param peer the peer with capacity for writing.
     * @return {@code true} if the client has more data to write, {@code false} if the connection is
     * to be closed.
     * @throws IOException if a write error occurred on the socket.
     */
    protected abstract boolean write(final WritableByteChannel peer) throws IOException;

}
