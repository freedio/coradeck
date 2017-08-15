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

import com.coradec.corabus.com.ConnectableEvent;
import com.coradec.corabus.com.ConnectionAcceptableEvent;
import com.coradec.corabus.com.ReadyToReadEvent;
import com.coradec.corabus.com.ReadyToSendEvent;
import com.coradec.corabus.model.NetworkComponent;
import com.coradec.coracom.ctrl.NetworkConnection;
import com.coradec.coracom.model.Request;
import com.coradec.coracom.model.impl.BasicMessage;
import com.coradec.coracore.annotation.Nullable;
import com.coradec.coracore.trouble.InitializationError;
import com.coradec.corasession.model.Session;
import com.coradec.coratext.model.Text;

import java.io.IOException;
import java.nio.channels.ClosedSelectorException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;

/**
 * B​ase class of all network components.
 */
public abstract class AbstractNetworkComponent extends BasicBusApplication
        implements NetworkComponent {

    private Selector selector;
    private final Text disconnectionText;

    public AbstractNetworkComponent(final Text disconnectionText) {
        this.disconnectionText = disconnectionText;
    }

    @Override protected @Nullable Request onInitialize(final Session session) {
        final @Nullable Request request = super.onInitialize(session);
        try {
            selector = Selector.open();
        } catch (IOException e) {
            throw new InitializationError(e);
        }
        return request;
    }

    protected Selector getSelector() {
        return selector;
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
                        if (key.isAcceptable()) onConnectionAcceptable(key);
                        else if (key.isConnectable()) onConnectable(key);
                        else if (key.isReadable()) onReadable(key);
                        else if (key.isWritable()) onWritable(key);
                        it.remove();
                    }
                } else Thread.sleep(10);
            } catch (ClosedSelectorException e) {
                info(disconnectionText);
                // we're done
                break;
            } catch (IOException e) {
                error(e);
            } catch (InterruptedException e) {
                break;
            }
        }
    }

    private void onConnectionAcceptable(final SelectionKey key) {
        final Object attachment = key.attachment();
        if (attachment instanceof NetworkConnection)
            inject(new InternalConnectionAcceptableEvent((NetworkConnection)attachment));
    }

    private void onConnectable(final SelectionKey key) {
        final Object attachment = key.attachment();
        if (attachment instanceof NetworkConnection)
            inject(new InternalConnectableEvent((NetworkConnection)attachment));
    }

    private void onReadable(final SelectionKey key) {
        final Object attachment = key.attachment();
        if (attachment instanceof NetworkConnection)
            inject(new InternalReadyToReadEvent((NetworkConnection)attachment));
    }

    private void onWritable(final SelectionKey key) {
        final Object attachment = key.attachment();
        if (attachment instanceof NetworkConnection)
            inject(new InternalReadyToSendEvent((NetworkConnection)attachment));
    }

    private class InternalConnectableEvent extends BasicMessage implements ConnectableEvent {

        InternalConnectableEvent(final NetworkConnection recipient) {
            super(AbstractNetworkComponent.this, recipient);
        }

    }

    private class InternalReadyToReadEvent extends BasicMessage implements ReadyToReadEvent {

        InternalReadyToReadEvent(final NetworkConnection recipient) {
            super(AbstractNetworkComponent.this, recipient);
        }

    }

    private class InternalReadyToSendEvent extends BasicMessage implements ReadyToSendEvent {

        InternalReadyToSendEvent(final NetworkConnection recipient) {
            super(AbstractNetworkComponent.this, recipient);
        }

    }

    private class InternalConnectionAcceptableEvent extends BasicMessage
            implements ConnectionAcceptableEvent {

        InternalConnectionAcceptableEvent(final NetworkConnection recipient) {
            super(AbstractNetworkComponent.this, recipient);
        }

    }
}
