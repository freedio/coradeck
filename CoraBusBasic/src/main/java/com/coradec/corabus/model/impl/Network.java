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

import com.coradec.corabus.com.ConnectionAcceptableEvent;
import com.coradec.corabus.com.FocusChangedEvent;
import com.coradec.corabus.com.KeyProcessedEvent;
import com.coradec.corabus.com.ReadyToConnectEvent;
import com.coradec.corabus.com.ReadyToReadEvent;
import com.coradec.corabus.com.ReadyToSendEvent;
import com.coradec.corabus.protocol.ProtocolHandler;
import com.coradec.coracom.ctrl.MessageQueue;
import com.coradec.coracom.ctrl.NetworkConnection;
import com.coradec.coracom.ctrl.Observer;
import com.coradec.coracom.model.Information;
import com.coradec.coracom.model.Recipient;
import com.coradec.coracom.model.Request;
import com.coradec.coracom.model.impl.BasicMessage;
import com.coradec.coraconf.model.Property;
import com.coradec.coracore.annotation.Inject;
import com.coradec.coracore.annotation.Nullable;
import com.coradec.coracore.annotation.ToString;
import com.coradec.coracore.model.GenericType;
import com.coradec.coracore.model.Origin;
import com.coradec.coracore.trouble.InitializationError;
import com.coradec.corasession.model.Session;
import com.coradec.coratext.model.LocalizedText;
import com.coradec.coratext.model.Text;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.ClosedSelectorException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * ​​The network component of the bus.
 */
@SuppressWarnings("ClassHasNoToStringMethod")
public class Network extends BasicBusApplication implements Observer {

    private static final Property<List<String>> PROP_ENABLED_PROTOCOLS =
            Property.define("EnabledProtocols", GenericType.of(List.class, String.class),
                    Collections.singletonList("CMP"));

    private static final Text TEXT_DISCONNECTION_TEXT = LocalizedText.define("DisconnectionText");
    private static final Text TEXT_CONNECTION_ACCEPTABLE =
            LocalizedText.define("ConnectionAcceptable");
    private static final Text TEXT_CONNECTIBLE = LocalizedText.define("Connectible");
    private static final Text TEXT_READBLE = LocalizedText.define("Readable");
    private static final Text TEXT_WRITABLE = LocalizedText.define("Writable");

    private final boolean serverAlso;
    private Selector selector;
    private final BlockingQueue<FocusChangedEvent> focusChanges = new ArrayBlockingQueue<>(128);
    @Inject MessageQueue MQ;
    private final BlockingQueue<SelectionKey> processedKeys = new ArrayBlockingQueue<>(1024);

    public Network(final boolean serverAlso) {
        this.serverAlso = serverAlso;
    }

    @Override protected @Nullable Request onInitialize(final Session session) {
        MQ.subscribe(this);
        final Request request = super.onInitialize(session);
        try {
            selector = Selector.open();
        } catch (IOException e) {
            throw new InitializationError(e);
        }
        PROP_ENABLED_PROTOCOLS.value().forEach(proto -> {
            add(session, proto + "-handler", createHandler(proto));
            if (serverAlso) add(session, proto + "-server", new NetworkServer(proto));
        });
        add(session, "client", new NetworkClient());
        return request;
    }

    @Override protected @Nullable Request onTerminate(final Session session) {
        MQ.unsubscribe(this);
        return super.onTerminate(session);
    }

    private ProtocolHandler createHandler(final String protocol) {
        return ProtocolHandler.fore(protocol);
    }

    @SuppressWarnings("MagicConstant") @Override public void run() {
        debug("Network server loop started.");
        while (!Thread.interrupted()) {
            long next = System.currentTimeMillis() + 20;
            // Process one focus change per loop turn:
            final FocusChangedEvent fchg = focusChanges.poll();
            if (fchg != null) {
                final SelectableChannel channel = fchg.getChannel();
                final int focus = fchg.getFocus();
                SelectionKey key = channel.keyFor(selector);
                int previous = 0;
                try {
                    if (key == null) {
                        if (focus != 0) channel.register(selector, focus, fchg.getOrigin());
                    } else {
                        previous = key.interestOps();
                        if (focus == 0) key.cancel();
                        else key.interestOps(focus);
                    }
                    debug("Focus of %s changed from %08x to %08x", channel, previous, focus);
                } catch (ClosedChannelException e) {
                    error(e);
                }
            }

            // Remove all processed keys from the selection set:
            final Set<SelectionKey> selectionKeys = selector.selectedKeys();
            for (SelectionKey key = processedKeys.poll(); key != null; key = processedKeys.poll()) {
                debug("Removing selection key %s on %s", key.channel(), key.attachment());
                selectionKeys.remove(key);
            }

            // Check selection:
            try {
                final long period = next - System.currentTimeMillis();
                if (period < 0) continue;
                final int selected = selector.select(period);
                if (selected != 0) {
                    for (final SelectionKey key : selector.selectedKeys()) {
                        debug("Got key %s with interest %d for %d", key.channel(),
                                key.interestOps(), selected);
                        if (key.isAcceptable()) onConnectionAcceptable(key);
                        else if (key.isConnectable()) onConnectible(key);
                        else if (key.isReadable()) onReadable(key);
                        else if (key.isWritable()) onWritable(key);
                        else debug("Key not precessed!");
                    }
                }
                next -= System.currentTimeMillis();
                if (next > 0L) Thread.sleep(next);
            } catch (ClosedSelectorException e) {
                info(TEXT_DISCONNECTION_TEXT);
                // we're done
                break;
            } catch (IOException e) {
                error(e);
            } catch (InterruptedException e) {
                break;
            }
        }
        debug("Network server loop terminated.");
    }

    private void onConnectionAcceptable(final SelectionKey key) {
        final Object attachment = key.attachment();
        if (attachment instanceof NetworkServer) {
            info(TEXT_CONNECTION_ACCEPTABLE, attachment);
            inject(new InternalConnectionAcceptableEvent(key, (NetworkServer)attachment));
        }
    }

    private void onConnectible(final SelectionKey key) {
        final Object attachment = key.attachment();
        if (attachment instanceof NetworkConnection) {
//            info(TEXT_CONNECTIBLE, attachment);
            inject(new InternalReadyToConnectEvent(key, (NetworkConnection)attachment));
        }
    }

    private void onReadable(final SelectionKey key) {
        final Object attachment = key.attachment();
        if (attachment instanceof NetworkConnection) {
            info(TEXT_READBLE, attachment);
            inject(new InternalReadyToReadEvent(key, (NetworkConnection)attachment));
        }
    }

    private void onWritable(final SelectionKey key) {
        final Object attachment = key.attachment();
        if (attachment instanceof NetworkConnection) {
//            info(TEXT_WRITABLE, attachment);
            inject(new InternalReadyToSendEvent(key, (NetworkConnection)attachment));
        }
    }

    @Override public boolean notify(final Information info) {
        debug("Notification for %s", info);
        if (info instanceof FocusChangedEvent) {
            focusChanges.add((FocusChangedEvent)info);
        } else if (info instanceof KeyProcessedEvent) {
            processedKeys.add(((KeyProcessedEvent)info).getSelectionKey());
        }
        return false;
    }

    @Override public boolean wants(final Information info) {
        return info instanceof FocusChangedEvent || info instanceof KeyProcessedEvent;
    }

    private static class SelectionMessage extends BasicMessage {

        private final SelectionKey key;

        /**
         * Initializes a new instance of SelectionMessage with the specified selection key and
         * recipient.
         *
         * @param key       the selection key.
         * @param recipient the recipient.
         */
        SelectionMessage(final Origin sender, final Recipient recipient, final SelectionKey key) {
            super(sender, recipient);
            this.key = key;
        }

        @ToString public SelectionKey getSelectionKey() {
            return key;
        }
    }

    @SuppressWarnings("ClassHasNoToStringMethod")
    private class InternalReadyToConnectEvent extends SelectionMessage
            implements ReadyToConnectEvent {

        InternalReadyToConnectEvent(final SelectionKey key, final NetworkConnection recipient) {
            super(Network.this, recipient, key);
        }

    }

    @SuppressWarnings("ClassHasNoToStringMethod")
    private class InternalReadyToReadEvent extends SelectionMessage implements ReadyToReadEvent {

        InternalReadyToReadEvent(final SelectionKey key, final NetworkConnection recipient) {
            super(Network.this, recipient, key);
        }

    }

    @SuppressWarnings("ClassHasNoToStringMethod")
    private class InternalReadyToSendEvent extends SelectionMessage implements ReadyToSendEvent {

        InternalReadyToSendEvent(final SelectionKey key, final NetworkConnection recipient) {
            super(Network.this, recipient, key);
        }

    }

    @SuppressWarnings("ClassHasNoToStringMethod")
    private class InternalConnectionAcceptableEvent extends SelectionMessage
            implements ConnectionAcceptableEvent {

        InternalConnectionAcceptableEvent(final SelectionKey key, final NetworkServer recipient) {
            super(Network.this, recipient, key);
        }

    }

}
