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

import com.coradec.corabus.com.OutboundMessage;
import com.coradec.corabus.com.ReadyToReadEvent;
import com.coradec.corabus.com.ReadyToSendEvent;
import com.coradec.corabus.com.impl.BasicFocusChangedEvent;
import com.coradec.corabus.com.impl.BasicKeyProcessedEvent;
import com.coradec.corabus.com.impl.ExternalRecipient;
import com.coradec.corabus.view.NetworkProtocol;
import com.coradec.coracom.com.RequestCompleteEvent;
import com.coradec.coracom.ctrl.Observer;
import com.coradec.coracom.model.Information;
import com.coradec.coracom.model.Recipient;
import com.coradec.coracom.model.Request;
import com.coradec.coracom.model.Response;
import com.coradec.coracom.model.SessionEvent;
import com.coradec.coracom.model.SessionInformation;
import com.coradec.coracom.model.SessionMessage;
import com.coradec.coracom.model.SessionRequest;
import com.coradec.coracom.model.SessionResponse;
import com.coradec.coracom.model.Voucher;
import com.coradec.coracom.model.impl.BasicSessionResponse;
import com.coradec.coracom.state.Answer;
import com.coradec.coracom.state.RequestState;
import com.coradec.coracom.trouble.InvalidOriginException;
import com.coradec.coracom.trouble.InvalidTargetException;
import com.coradec.coraconf.model.Property;
import com.coradec.coracore.annotation.Nullable;
import com.coradec.coracore.model.Origin;
import com.coradec.coracore.model.impl.URIgin;
import com.coradec.coracore.time.Duration;
import com.coradec.coracore.trouble.OperationInterruptedException;
import com.coradec.coradir.model.Path;
import com.coradec.corasession.model.Session;
import com.coradec.coratext.model.LocalizedText;
import com.coradec.coratext.model.Text;

import java.io.EOFException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

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
    private static final Text TEXT_UNPAIRED_RESPONSE = LocalizedText.define("UnpairedResponse");
    private static final Text TEXT_CANNOT_SEND_BACK_TO_ORIGIN =
            LocalizedText.define("CannotSendBackToOrigin");

    private final SocketChannel channel;
    private final URI resource;
    private final NetworkProtocol protocol;
    private final Queue<OutboundMessage> outQueue = new ConcurrentLinkedQueue<>();
    private SessionMessage currentOut;
    private ByteBuffer currentOutData;
    private Session initialSession;
    private final Queue<SessionResponse> responses = new ConcurrentLinkedQueue<>();
    private final Map<UUID, SessionInformation> outboundMessages = new HashMap<>();
    private final Map<UUID, SessionInformation> inboundMessages = new HashMap<>();
    private Throwable problem;
    private boolean freeWrite = false;
    private int interestSet;

    protected AbstractNetworkConnection(final SocketChannel channel, final NetworkProtocol protocol,
            final URI resource, final int initialInterestSet) {
        this.channel = channel;
        this.resource = resource;
        this.protocol = protocol;
        this.interestSet = initialInterestSet;
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
        select(interestSet);
        addRoute(ReadyToReadEvent.class, this::onReadyToRead);
        addRoute(ReadyToSendEvent.class, this::onReadyToWrite);
        return request;
    }

    @Override protected @Nullable Request onTerminate(final Session session) {
        removeRoute(ReadyToReadEvent.class);
        removeRoute(ReadyToSendEvent.class);
        deselect(interestSet);
        // fail or cancel each remaining request:
        outboundMessages.values().forEach(msg -> {
            if (msg instanceof Request) {
                Request req = (Request)msg;
                if (problem != null) req.fail(problem);
                else req.cancel();
            }
        });
        outboundMessages.clear();
        inboundMessages.clear();
        return super.onTerminate(session);
    }

    protected void select(int mask) {
        final int interestSet = this.interestSet |= mask;
        debug("Changing focus to %02x", interestSet);
        inject(new BasicFocusChangedEvent(this, channel, interestSet));
    }

    protected void deselect(int mask) {
        final int interestSet = this.interestSet &= ~mask;
        debug("Changing focus to %02x", interestSet);
        inject(new BasicFocusChangedEvent(this, channel, interestSet));
    }

    private void onReadyToRead(final ReadyToReadEvent event) {
        try {
            read();
        } catch (EOFException e) {
            debug("EOF on channel.");
            closeConnection();
        } catch (IOException e) {
            error(e);
        } finally {
            inject(new BasicKeyProcessedEvent(this, event.getSelectionKey()));
        }
    }

    private void onReadyToWrite(final ReadyToSendEvent event) {
        boolean release = false;
        try {
            release = write();
        } catch (IOException e) {
            error(e);
        } finally {
            if (release) inject(new BasicKeyProcessedEvent(this, event.getSelectionKey()));
        }
    }

    private void closeConnection() {
        debug("Closing connection.");
        try {
            channel.close();
        } catch (IOException e) {
            error(e);
        }
        shutdown();
        debug("Connection closed.");
    }

    /**
     * Reads as many bytes from the socket as possible.
     */
    protected void read() throws IOException {
        debug("Reading...");
        SessionInformation info = protocol.read(channel);
        if (info != null) {
            info.renew();
            if (info instanceof SessionRequest) {
                debug("Received request %s", info);
                requestReceived((SessionRequest)info);
            } else if (info instanceof SessionResponse) {
                debug("Received response %s", info);
                responseReceived((SessionResponse)info);
            } else if (info instanceof SessionEvent) {
                debug("Received event %s", info);
                eventReceived((SessionEvent)info);
            } else {
                debug("Received information %s", info);
                infoReceived(info);
            }
        }
    }

    /**
     * Writes as many bytes to the socket as possible.
     *
     * @return {@code true} if a write operation is in progress, {@code false} if there was nothing
     * to write.
     * @throws IOException if a write error occurred.
     */
    protected boolean write() throws IOException {
        debug("a) Ready to write");
        boolean result = false;
        UUID id = null;
        Path path = null;
        if (currentOut == null && currentOutData == null) {
            currentOut = responses.poll();
            if (currentOut != null) {
                id = currentOut.getId();
                path = ((Response)currentOut).getTarget();
                debug("b) Got a response to write");
            } else {
                final OutboundMessage outMsg = outQueue.poll();
                if (outMsg != null) {
                    currentOut = outMsg.getContent();
                    id = currentOut.getId();
                    path = outMsg.getPath();
                    outboundMessages.put(id, currentOut);
                    debug("b) Got an outbound message to write");
                }
            }
            if (currentOut == null) freeWrite = true;
            else {
                debug("c) Preparing %s for sending", currentOut);
                assert id != null;
                assert path != null;
                currentOutData = protocol.serialize(id, currentOut, path);
                currentOutData.flip();
                currentOut = null;
            }
        }
        if (currentOutData != null) {
            freeWrite = false;
            result = true;
            final int written = channel.write(currentOutData);
            debug("d) Wrote %d bytes to channel %s", written, channel);
            if (!currentOutData.hasRemaining()) {
                currentOutData = null;
            }
        }
        return result;
    }

    /**
     * Callback triggered when a request was received.
     *
     * @param request the received requests.
     */
    protected void requestReceived(final SessionRequest request) {
        inboundMessages.put(request.getId(), request);
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
        final SessionInformation info = outboundMessages.remove(reference);
        if (info instanceof Request) {
            Request request = (Request)info;
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
        } else error(TEXT_UNPAIRED_RESPONSE, response);
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
    protected abstract void infoReceived(final SessionInformation info);

    @Override public boolean notify(final Information info) {
        boolean wanted = wants(info);
        if (wanted) {
            RequestCompleteEvent event = (RequestCompleteEvent)info;
            final SessionRequest request = (SessionRequest)event.getRequest();
            final RequestState requestState = request.getRequestState();
            debug("Request %s completed with %s", request, requestState);
            if (requestState == SUCCESSFUL) {
                byte[] data = new byte[0];
                if (request instanceof Voucher) {
                    Voucher<?> voucher = (Voucher<?>)request;
                    data = getData(voucher);
                }
                debug("Sending OK-response");
                sendResponse(request, OK, data);
            } else if (requestState == FAILED) {
                debug("Sending KO-response");
                sendResponse(request, KO, request.getProblem());
            } else if (requestState == CANCELLED) {
                debug("Sending CN-response");
                sendResponse(request, CN, null);
            } else wanted = false;
        }
        return wanted;
    }

    @Override public boolean wants(final Information info) {
        return info instanceof RequestCompleteEvent;
    }

    private <V> byte[] getData(final Voucher<V> voucher) {
        return getProtocol().encode(voucher.getType(), voucher.getValue());
    }

    private void sendResponse(final SessionRequest request, final Answer answer,
            final @Nullable Object arg) throws OperationInterruptedException {
        final Recipient r = request.getRecipient();
        final Origin s = request.getOrigin();
        Origin sender = asSender(r);
        if (sender == null) {
            error(TEXT_RESPONSE_WITHOUT_SENDER);
            return;
        }
        Recipient recipient = asRecipient(s);
        if (recipient == null) {
            error(TEXT_RESPONSE_WITHOUT_RECIPIENT);
            return;
        }
        final Session session = request.getSession();
        SessionResponse response =
                new BasicSessionResponse(session, sender, recipient, request.getId(), answer, arg);
        Duration maxQwait = PROP_MAX_Q_WAIT.value();
        responses.add(response);
        debug("Response %s added to queue.", response);
        if (freeWrite) try {
            write();
        } catch (IOException e) {
            error(e);
        }
    }

    private Origin asSender(final Recipient r) {
        if (r instanceof Origin) return (Origin)r;
        else throw new InvalidTargetException(r);
    }

    private Recipient asRecipient(final Origin s) throws InvalidOriginException {
        if (s instanceof Recipient) return (Recipient)s;
        try {
            final SocketAddress remoteAddress = channel.getRemoteAddress();
            if (remoteAddress instanceof InetSocketAddress) {
                InetSocketAddress inetAddr = (InetSocketAddress)remoteAddress;
                if (s instanceof URIgin) {
                    return new ExternalRecipient(inetAddr.getHostName(), s.toURI());
                }
                if (s instanceof Path)
                    return new ExternalRecipient(inetAddr.getHostName(), s.toURI());
            }
        } catch (IOException e) {
            // throw InvalidOriginException below.
        }
        throw new InvalidOriginException(s, TEXT_CANNOT_SEND_BACK_TO_ORIGIN.resolve(s));
    }

    /**
     * Sends the specified outbound message to the specified recipient.
     *
     * @param message the message.
     */
    protected void output(final OutboundMessage message) throws IOException {
        debug("Adding message %s to outQ.", message);
        outQueue.add(message);
        if (freeWrite) write();
    }

}
