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

import static com.coradec.coracom.state.Answer.*;
import static com.coradec.coracom.state.RequestState.*;
import static java.util.concurrent.TimeUnit.*;

import com.coradec.corabus.com.ReadyToReadEvent;
import com.coradec.corabus.com.ReadyToSendEvent;
import com.coradec.corabus.trouble.StaleConnectionException;
import com.coradec.corabus.view.NetworkProtocol;
import com.coradec.coracom.com.RequestCompleteEvent;
import com.coradec.coracom.ctrl.Observer;
import com.coradec.coracom.model.Information;
import com.coradec.coracom.model.Recipient;
import com.coradec.coracom.model.Request;
import com.coradec.coracom.model.Sender;
import com.coradec.coracom.model.SessionEvent;
import com.coradec.coracom.model.SessionRequest;
import com.coradec.coracom.model.SessionResponse;
import com.coradec.coracom.model.Voucher;
import com.coradec.coracom.model.impl.BasicSessionResponse;
import com.coradec.coracom.state.Answer;
import com.coradec.coracom.state.RequestState;
import com.coradec.coraconf.model.Property;
import com.coradec.coracore.annotation.Nullable;
import com.coradec.coracore.time.Duration;
import com.coradec.coracore.trouble.OperationInterruptedException;
import com.coradec.corasession.model.Session;
import com.coradec.coratext.model.LocalizedText;
import com.coradec.coratext.model.Text;

import java.io.IOException;
import java.net.SocketAddress;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * ​​Base class of a client or server connection.
 */
public abstract class AbstractNetworkConnection extends BasicNode implements Observer {

    private static final Property<Duration> PROP_MAX_Q_WAIT =
            Property.define("MaxQueueWait", Duration.class, Duration.of(2, SECONDS));
    private static final Text TEXT_RESPONSE_WITHOUT_SENDER =
            LocalizedText.define("ResponseWithoutSender");
    private static final Text TEXT_RESPONSE_WITHOUT_RECIPIENT =
            LocalizedText.define("ResponseWithoutRecipient");
    private static final Text TEXT_NOT_ESTABLISHED = LocalizedText.define("NotEstablished");
    private static final Text TEXT_CLOSING_STALE_CONNECTION =
            LocalizedText.define("ClosingStaleConnection");

    private final SocketChannel channel;
    private final Selector selector;
    private final URI resource;
    private final String proto;
    private int selectionOps;
    private NetworkProtocol protocol;
    private SocketAddress socket;
    private Queue<Information> outQueue;
    private Information currentOut;
    private ByteBuffer currentOutData;
    private @Nullable SelectionKey selection;
    private Session initialSession;
    private final Map<UUID, SessionRequest> outboundRequests = new HashMap<>();
    private final Map<UUID, SessionRequest> inboundRequests = new HashMap<>();
    private final BlockingQueue<SessionResponse> outboundResponses = new ArrayBlockingQueue<>(128);
    private Throwable problem;

    public AbstractNetworkConnection(final Selector selector, final SocketChannel channel,
            final int selectionOps, final URI resource) {
        this.selector = selector;
        this.channel = channel;
        this.selectionOps = selectionOps;
        this.resource = resource;
        proto = resource.getScheme();
    }

    /**
     * Returns the socket channel.
     *
     * @return the channel.
     */
    protected SocketChannel getChannel() {
        return channel;
    }

    /**
     * Returns the applicable network protocol.
     *
     * @return the protocol.
     */
    protected NetworkProtocol getProtocol() {
        return protocol;
    }

    /**
     * Returns the session in whose context the network connection was initialized.
     *
     * @return the initial session.
     */
    protected Session getInitialSession() {
        return initialSession;
    }

    @Override protected @Nullable Request onInitialize(final Session session) {
        final Request request = super.onInitialize(session);
        initialSession = session;
        addRoute(ReadyToReadEvent.class, this::onReadyToRead);
        addRoute(ReadyToSendEvent.class, this::onReadyToWrite);
        return request;
    }

    @Override protected @Nullable Request onTerminate(final Session session) {
        final Request request = super.onTerminate(session);
        removeRoute(ReadyToReadEvent.class);
        removeRoute(ReadyToSendEvent.class);
        if (selection != null) {
            selection.cancel();
            selection = null;
        }
        if (problem != null) outboundRequests.values().forEach(req -> req.fail(problem));
        else outboundRequests.values().forEach(Request::cancel);
        outboundRequests.clear();
        inboundRequests.clear();
        outboundResponses.clear();
        return request;
    }

    protected void select(int mask) {
        if (selection != null) selection.cancel();
        selectionOps |= mask;
        if (channel != null) try {
            //noinspection MagicConstant
            selection = channel.register(selector, selectionOps, this);
        } catch (ClosedChannelException e) {
            throw new IllegalStateException(TEXT_NOT_ESTABLISHED.resolve(socket), e);
        }
    }

    protected void deselect(int mask) {
        if (selection != null) selection.cancel();
        if ((selectionOps &= ~mask) == 0) shutdown();
        else {
            if (channel != null) try {
                channel.register(selector, selectionOps, this);
            } catch (ClosedChannelException e) {
                throw new IllegalStateException(TEXT_NOT_ESTABLISHED.resolve(socket), e);
            }
        }
    }

    private void onReadyToRead(final ReadyToReadEvent event) {
        try {
            read();
        } catch (IOException e) {
            error(e);
        }
    }

    private void onReadyToWrite(final ReadyToSendEvent event) {
        try {
            write();
        } catch (IOException e) {
            error(e);
        }
    }

    /**
     * Reads as many bytes from the socket as possible.
     */
    protected void read() throws IOException {
        Information info = protocol.read(channel);
        if (info != null) {
            if (info instanceof SessionRequest) {
                requestReceived((SessionRequest)info);
            } else if (info instanceof SessionResponse) {
                responseReceived((SessionResponse)info);
            } else if (info instanceof SessionEvent) {
                eventReceived((SessionEvent)info);
            } else {
                infoReceived(info);
            }
        }
    }

    /**
     * Writes as many bytes to the socket as possible.
     */
    protected void write() throws IOException {
        if (currentOut == null) {
            currentOut = outQueue.poll();
        }
        if (currentOut != null && currentOutData == null) {
            if (currentOut instanceof Request) currentOutData = protocol.serialize(currentOut);
        }
        if (currentOutData != null) {
            channel.write(currentOutData);
            if (!currentOutData.hasRemaining()) {
                currentOut = null;
                currentOutData = null;
            }
        }
    }

    /**
     * Callback triggered when a request was received.
     *
     * @param request the received requests.
     */
    protected void requestReceived(final SessionRequest request) {
        inboundRequests.put(request.getId(), request);
        request.reportCompletionTo(this);
        inject(request);
    }

    /**
     * Callback triggered when a response was received.
     *
     * @param response the received response.
     */
    protected void responseReceived(final SessionResponse response) {
        final UUID reference = response.getReference();
        final SessionRequest request = outboundRequests.get(reference);
        switch (response.getAnswer()) {
            case OK:
                if (request instanceof Voucher) {
                    //noinspection unchecked
                    Voucher<Object> voucher = (Voucher<Object>)request;
                    final Object value =
                            getProtocol().decode(voucher.getType(), response.getBody());
                    if (value != null) voucher.setValue(value);
                }
                request.succeed();
                break;
            case KO:
                request.fail(response.getFailureReason());
                break;
            case CN:
                request.cancel();
                break;
        }
    }

    /**
     * Callback triggered when another kind of event was received.
     *
     * @param event the received event.
     */
    protected abstract void eventReceived(final SessionEvent event);

    /**
     * Callback triggered when another kind of information was received.
     *
     * @param info the received information.
     */
    protected abstract void infoReceived(final Information info);

    @Override public boolean notify(final Information info) {
        boolean wanted = wants(info);
        if (wanted) {
            RequestCompleteEvent event = (RequestCompleteEvent)info;
            final SessionRequest request = (SessionRequest)event.getRequest();
            final RequestState requestState = request.getRequestState();
            if (requestState == SUCCESSFUL) {
                byte[] data = new byte[0];
                if (request instanceof Voucher) {
                    Voucher<?> voucher = (Voucher<?>)request;
                    data = getData(voucher);
                }
                sendResponse(request, OK, data);
            } else if (requestState == FAILED) {
                sendResponse(request, KO, request.getProblem());
            } else if (requestState == CANCELLED) {
                sendResponse(request, CN, null);
            } else wanted = false;
        }
        return wanted;
    }

    private <V> byte[] getData(final Voucher<V> voucher) {
        return getProtocol().encode(voucher.getType(), voucher.getValue());
    }

    @Override public boolean wants(final Information info) {
        return info instanceof RequestCompleteEvent;
    }

    private void sendResponse(final SessionRequest request, final Answer answer,
            final @Nullable Object arg) throws OperationInterruptedException {
        if (outboundResponses == null) return;
        final Collection<Recipient> recipients = request.getRecipients();
        final Recipient r = recipients.iterator().hasNext() ? recipients.iterator().next() : null;
        final Sender s = request.getSender();
        Sender sender = asSender(r);
        Recipient recipient = asRecipient(s);
        if (sender == null) {
            error(TEXT_RESPONSE_WITHOUT_SENDER);
            return;
        }
        if (recipient == null) {
            error(TEXT_RESPONSE_WITHOUT_RECIPIENT);
            return;
        }
        SessionResponse response =
                new BasicSessionResponse(request.getSession(), request.getId(), answer, arg, sender,
                        recipient);
        Duration maxQwait = PROP_MAX_Q_WAIT.value();
        try {
            if (!outboundResponses.offer(response, maxQwait.getAmount(), maxQwait.getUnit())) {
                handleStaleConnection();
            }
        } catch (InterruptedException e) {
            throw new OperationInterruptedException();
        }
    }

    protected void handleStaleConnection() {
        info(TEXT_CLOSING_STALE_CONNECTION, socket);
        problem = new StaleConnectionException(socket);
        shutdown();
    }

    private @Nullable Sender asSender(final @Nullable Recipient r) {
        if (r instanceof Sender) return (Sender)r;
        else return null;
    }

    private @Nullable Recipient asRecipient(final Sender s) {
        if (s instanceof Recipient) return (Recipient)s;
        else return null;
    }
}
