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

import com.coradec.corabus.com.ConnectionEstablishedEvent;
import com.coradec.corabus.com.DataAvailableEvent;
import com.coradec.corabus.com.ReadyToSendEvent;
import com.coradec.corabus.model.ClientConnection;
import com.coradec.corabus.model.ServiceProvider;
import com.coradec.corabus.view.BusService;
import com.coradec.corabus.view.NetworkService;
import com.coradec.corabus.view.impl.BasicServiceView;
import com.coradec.coracom.model.Information;
import com.coradec.coracom.model.Recipient;
import com.coradec.coracom.model.Request;
import com.coradec.coracom.model.Voucher;
import com.coradec.coracom.model.impl.BasicCommand;
import com.coradec.coracom.model.impl.BasicMessage;
import com.coradec.coraconf.model.Property;
import com.coradec.coracore.annotation.Nullable;
import com.coradec.coracore.model.GenericType;
import com.coradec.coracore.trouble.InitializationError;
import com.coradec.coracore.trouble.ServiceNotAvailableException;
import com.coradec.corasession.model.Session;
import com.coradec.coratext.model.LocalizedText;
import com.coradec.coratext.model.Text;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketAddress;
import java.net.URL;
import java.nio.channels.ClosedSelectorException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * The network client service.
 */
@SuppressWarnings("ClassHasNoToStringMethod")
public class NetworkClient extends BasicBusApplication implements ServiceProvider {

    private static final Property<Set<String>> PROP_SUPPORTED_PROTOCOLS =
            Property.define("SupportedProtocols", GenericType.of(Set.class, String.class));

    private static final Text TEXT_CLIENT_DISCONNECTED = LocalizedText.define("ClientDisconnected");
    private static final Text TEXT_NO_PROTOCOL = LocalizedText.define("NoProtocol");
    private static final Text TEXT_NO_ADDRESS = LocalizedText.define("NoAddress");
    private static final Text TEXT_NO_PORT = LocalizedText.define("NoPort");

    private Selector selector;

    public NetworkClient() {
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

    @Override protected @Nullable Request onTerminating(final Session session) {
        final @Nullable Request request = super.onTerminate(session);
        return inject(new ReclaimServicesCommand()).and(request);
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
                info(TEXT_CLIENT_DISCONNECTED);
                // we're done
                break;
            } catch (IOException e) {
                error(e);
            }
        }
    }

    @Override public <S extends BusService> boolean provides(final Session session,
            final Class<? super S> type, final Object... args) {
        if (!NetworkService.class.isAssignableFrom(type)) return false;
        if (args.length > 0) {
            if (args[0] instanceof URL && isProtocol(((URL)args[0]).getProtocol())) return true;
            if (args.length > 1) {
                if (args[0] instanceof SocketAddress && isProtocol(args[1])) return true;
                if (args.length > 2) {
                    if (args[0] instanceof InetAddress &&
                        args[1] instanceof Integer &&
                        isProtocol(args[2])) return true;
                    if (isHostname(args[0]) && args[1] instanceof Integer && isProtocol(args[2]))
                        return true;
                }
            }
        }
        return false;
    }

    @Override
    public <S extends BusService> S getService(final Session session, final Class<? super S> type,
            final Object[] args) throws ServiceNotAvailableException {
        if (!provides(session, type, args)) throw new ServiceNotAvailableException(type, args);
        try {
            //noinspection unchecked
            return (S)new InternalNetworkService(session, args);
        } catch (IllegalArgumentException e) {
            throw new ServiceNotAvailableException(e, type, args);
        }
    }

    private void onConnectionEstablished(final SelectionKey key) {
        final Object attachment = key.attachment();
        if (attachment instanceof ClientConnection) {
            inject(new NetworkClient.InternalConnectionEstablishedEvent(
                    (ClientConnection)attachment));
        }
    }

    private void onReadable(final SelectionKey key) {
        final Object attachment = key.attachment();
        if (attachment instanceof ClientConnection) {
            inject(new NetworkClient.InternalDataAvailableEvent((ClientConnection)attachment));
        }
    }

    private void onWritable(final SelectionKey key) {
        final Object attachment = key.attachment();
        if (attachment instanceof ClientConnection) {
            inject(new NetworkClient.InternalReadyToSendEvent((ClientConnection)attachment));
        }
    }

    boolean isProtocol(final Object arg) {
        return arg instanceof String && PROP_SUPPORTED_PROTOCOLS.value().contains(arg);
    }

    private boolean isHostname(final Object arg) {
        return arg instanceof String &&
               Pattern.matches("[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)*", String.valueOf(arg));
    }

    private class InternalConnectionEstablishedEvent extends BasicMessage
            implements ConnectionEstablishedEvent {

        InternalConnectionEstablishedEvent(final Recipient recipient) {
            super(NetworkClient.this, recipient);
        }

    }

    private class InternalDataAvailableEvent extends BasicMessage implements DataAvailableEvent {

        InternalDataAvailableEvent(final Recipient recipient) {
            super(NetworkClient.this, recipient);
        }

        @Override public SocketChannel getPeer() {
            try {
                return SocketChannel.open();
            } catch (IOException e) {
                throw new RuntimeException();
            }
        }
    }

    private class InternalReadyToSendEvent extends BasicMessage implements ReadyToSendEvent {

        InternalReadyToSendEvent(final Recipient recipient) {
            super(NetworkClient.this, recipient);
        }

        @Override public SocketChannel getPeer() {
            try {
                return SocketChannel.open();
            } catch (IOException e) {
                throw new RuntimeException();
            }
        }
    }

    private class ReclaimServicesCommand extends BasicCommand {

        /**
         * Initializes a new instance of ReclaimServicesCommand.
         */
        ReclaimServicesCommand() {
            super(NetworkClient.this);
        }

        @Override public void execute() {

        }
    }

    private class InternalNetworkService extends BasicServiceView implements NetworkService {

        InternalNetworkService(final Session session, final Object... args)
                throws IllegalArgumentException {
            super(session);
//            @Nullable String protocol = null;
//            @Nullable InetAddress address = null;
//            int port = 0;
//            if (args.length > 0) {
//                if (args[0] instanceof URL) {
//                    URL url = (URL)args[0];
//                    protocol = url.getProtocol();
//                    if (!isProtocol(protocol)) protocol = null;
//                    address = NetworkUtil.resolve(url.getAuthority());
//                    port = getDefaultPort(protocol);
//                } else if (args.length > 1) {
//                    if (args[0] instanceof SocketAddress && isProtocol(args[1])) {
//                        SocketAddress sockaddr = (SocketAddress)args[0];
//
//                    }
//                }
//            }
//            if (protocol == null) throw new IllegalArgumentException(TEXT_NO_PROTOCOL.resolve());
//            if (address == null) throw new IllegalArgumentException(TEXT_NO_ADDRESS.resolve());
//            if (port == 0) throw new IllegalArgumentException(TEXT_NO_PORT.resolve());
//            registerService(this);
        }

        @Override public void send(final Information info) {

        }

        @Nullable @Override public Voucher<Information> receive() {
            return null;
        }
    }
}
