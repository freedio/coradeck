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

package com.coradec.corabus.protocol.handler;

import static com.coradec.coracom.model.Information.PROP_CLASS;
import static com.coradec.coracom.model.SessionInformation.*;

import com.coradec.corabus.com.NetworkRequest;
import com.coradec.corabus.model.Bus;
import com.coradec.corabus.model.impl.BasicServiceProvider;
import com.coradec.corabus.protocol.ProtocolHandler;
import com.coradec.corabus.view.BusService;
import com.coradec.corabus.view.NetworkProtocol;
import com.coradec.corabus.view.impl.BasicServiceView;
import com.coradec.coracom.ctrl.RecipientResolver;
import com.coradec.coracom.model.Event;
import com.coradec.coracom.model.PayloadMessage;
import com.coradec.coracom.model.Request;
import com.coradec.coracom.model.Response;
import com.coradec.coracom.model.SessionEvent;
import com.coradec.coracom.model.SessionInformation;
import com.coradec.coracom.model.SessionRequest;
import com.coradec.coracom.model.SessionResponse;
import com.coradec.coraconf.model.Property;
import com.coradec.coracore.annotation.Inject;
import com.coradec.coracore.annotation.Nullable;
import com.coradec.coracore.model.Factory;
import com.coradec.coracore.model.GenericType;
import com.coradec.coracore.trouble.RequestRefused;
import com.coradec.coracore.trouble.ServiceNotAvailableException;
import com.coradec.coracore.util.StringUtil;
import com.coradec.coradir.model.Path;
import com.coradec.corasession.model.ProxySession;
import com.coradec.corasession.model.Session;
import com.coradec.coratext.model.LocalizedText;
import com.coradec.coratext.model.Text;
import com.coradec.coratype.ctrl.TypeConverter;

import java.io.EOFException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.util.Base64;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

/**
 * ​​Implementation of the Coradec Messaging Protocol handler.
 */
@SuppressWarnings("ClassHasNoToStringMethod")
public class CMP_Handler extends BasicServiceProvider implements ProtocolHandler {

    public static final String NAME_PROTOCOL = "CMP";
    public static final String SCHEME = NAME_PROTOCOL.toLowerCase();
    public static final Property<Integer> PROP_STANDARD_PORT =
            Property.define("StandardPort", Integer.class, 1024);
    static final Property<String> PROP_SEPARATOR = Property.define("Separator", String.class, ":");
    static final Property<String> PROP_DELIMITER = Property.define("Delimiter", String.class, "\n");
    static final Property<String> PROP_RECIPIENT_SEPARATOR =
            Property.define("RecipientSeparator", String.class, ",");

    private static final Text TEXT_FAILED_TO_DECODE_REFERENCE =
            LocalizedText.define("FailedToDecodeReference");
    private static final Text TEXT_FAILED_TO_DECODE_ANSWER =
            LocalizedText.define("FailedToDecodeAnswer");
    private static final Text TEXT_FAILED_TO_DECODE_EVENT_TYPE =
            LocalizedText.define("FailedToDecodeEventType");
    private static final Text TEXT_FAILED_TO_DECODE_RECIPIENT =
            LocalizedText.define("FailedToDecodeRecipient");
    private static final Text TEXT_NO_RECIPIENT = LocalizedText.define("NoRecipients");
    private static final Text TEXT_MULTIPLE_RECIPIENTS_FOR_REQUEST =
            LocalizedText.define("MultipleRecipientsForRequest");
    private static final Text TEXT_MESSAGE_WITHOUT_SENDER =
            LocalizedText.define("MessageWithoutSender");
    private static final Text TEXT_FAILED_TO_INSTANTIATE =
            LocalizedText.define("FailedToInstantiate");

    static final Base64.Decoder DECODER = Base64.getMimeDecoder();
    static final Base64.Encoder ENCODER = Base64.getMimeEncoder();

    @Inject private static Factory<SessionResponse> RESPONSE;
    @Inject private static Factory<NetworkRequest> NETREQUEST;
    @Inject private static Factory<SessionRequest> REQUEST;
    @Inject private static Factory<SessionEvent> EVENT;
    @Inject private static Factory<SessionInformation> INFORMATION;
    @Inject private static Factory<ProxySession> PROXY_SESSION;

    @Inject Bus bus;

    @Override public <S extends BusService> boolean provides(final Session session,
            final Class<? super S> type, final Object... args) {
        return NetworkProtocol.class.isAssignableFrom(type) &&
               args.length > 0 &&
               args[0] instanceof String &&
               NAME_PROTOCOL.equalsIgnoreCase((String)args[0]);
    }

    @SuppressWarnings("unchecked") @Override
    public <S extends BusService> S getService(final Session session, final Class<? super S> type,
            final Object... args) throws ServiceNotAvailableException {
        if (!provides(session, type, args)) throw new RequestRefused("Invalid arguments!");
        return (S)new ProtocolImpl(session);
    }

    private class ProtocolImpl extends BasicServiceView implements NetworkProtocol, BusService {

        private ByteBuffer currentInputLength;
        private ByteBuffer currentInput;

        public ProtocolImpl(final Session session) {
            super(session);
        }

        @Override public int getStandardPort() {
            return PROP_STANDARD_PORT.value();
        }

        @Override public String getScheme() {
            return NAME_PROTOCOL.toLowerCase();
        }

        @Override
        public ByteBuffer serialize(final UUID id, SessionInformation info, final Path recipient) {
            final String del = PROP_DELIMITER.value();
            final String sep = PROP_SEPARATOR.value();
            StringBuilder collector = new StringBuilder(4096);
            final Map<String, Object> attributes = info.getProperties();
            attributes.put(PROP_CLASS, info.getClass().getName());
            attributes.entrySet()
                      .stream()
                      .sorted(Comparator.comparing(Entry::getKey))
                      .forEach(e -> collector.append(formatKey(e.getKey()))
                                             .append(sep)
                                             .append(formatValue(e.getValue()))
                                             .append(del));
            if (info instanceof PayloadMessage) {
                final @Nullable byte[] body = ((PayloadMessage)info).getBody();
                if (body != null && body.length > 0) //
                    collector.append(del)
                             .append(new String(ENCODER.encode(body), StringUtil.CHARSET));
            }
            final String serialized = collector.toString();
            debug("Message serialized as \"%s\"", serialized);
            final byte[] serializedBytes = serialized.getBytes(StringUtil.CHARSET);
            final ByteBuffer buffer = ByteBuffer.allocate(serializedBytes.length + Integer.BYTES);
            buffer.putInt(serializedBytes.length);
            buffer.put(serializedBytes);
            return buffer;
        }

        private SessionInformation deserialize(final Session session, final ByteBuffer buffer) {
            final String delimiter = PROP_DELIMITER.value();
            final String separator = PROP_SEPARATOR.value();
            Map<String, Object> attributes = new HashMap<>();
            byte[] all = new byte[buffer.limit()], body = null;
            buffer.get(all);
            final String message = new String(all, StringUtil.CHARSET);
            debug("Deserializing message \"%s\".", message);
            boolean doBody = false;
            for (final String attribute : message.split(delimiter)) {
                if (attribute.isEmpty()) {
                    doBody = true;
                    break;
                } else {
                    String[] fields = attribute.split(separator, 2);
                    if (fields.length == 2) attributes.put(fields[0], fields[1]);
                }
            }
            if (doBody) {
                int bodySep = message.indexOf(delimiter + delimiter);
                String bodyPart = message.substring(bodySep + 2);
                body = DECODER.decode(bodyPart.getBytes(StringUtil.CHARSET));
            }
            final SessionInformation info = construct(session, attributes);
            if (body != null && info instanceof PayloadMessage)
                ((PayloadMessage)info).setBody(body);
            return info;
        }

        @Override @Nullable public SessionInformation read(final ReadableByteChannel channel)
                throws IOException {
            if (currentInput == null) {
                if (currentInputLength == null) {
                    currentInputLength = ByteBuffer.allocate(Integer.BYTES);
                }
                if (currentInputLength.hasRemaining()) if (channel.read(currentInputLength) == -1)
                    throw new EOFException(
                            String.format("%d more bytes to read", currentInputLength.remaining()));
                if (currentInputLength.hasRemaining()) return null;
                currentInputLength.flip();
                final int bufferLength = currentInputLength.getInt();
                currentInput = ByteBuffer.allocate(bufferLength);
                debug("Length of receive buffer: %d bytes.", bufferLength);
                currentInputLength = null;
            }
            if (channel.read(currentInput) == -1) throw new EOFException(
                    String.format("%d more bytes to read", currentInput.remaining()));
            SessionInformation result = null;
            if (!currentInput.hasRemaining()) {
                currentInput.flip();
                result = deserialize(getSession(), currentInput);
                currentInput = null;
            }
            return result;
        }

        @Nullable @SuppressWarnings("unchecked") @Override
        public <V> V decode(final GenericType<V> type, @Nullable final byte[] data) {
            return data == null ? null : TypeConverter.to(type).unmarshal(data);
        }

        @Nullable @Override
        public <V> byte[] encode(final GenericType<V> type, @Nullable final V value) {
            return value == null ? null : TypeConverter.to(type).marshal(value);
        }
    }

    /**
     * Constructs a new instance of session information from the specified attribute map in the
     * context of the specified session.
     *
     * @param session    the session context.
     * @param attributes the attribute map.
     * @return a kind of information.
     */
    SessionInformation construct(final Session session, final Map<String, Object> attributes) {
        RecipientResolver.register(bus);
        try {
            final String session$ = (String)attributes.get(PROP_SESSION);
            if (session$ != null) {
                final UUID sessionId = TypeConverter.to(UUID.class).decode(session$);
                Session inSession =
                        Session.lookup(sessionId).orElseGet(() -> PROXY_SESSION.create(sessionId));
            }
            final String klass$ = (String)attributes.get(PROP_CLASS);
            if (klass$ != null) try {
                Class<?> klass = Class.forName(klass$);
                if (SessionInformation.class.isAssignableFrom(klass)) {
                    return (SessionInformation)klass.getConstructor(Map.class)
                                                    .newInstance(attributes);
                }
            } catch (ClassNotFoundException | IllegalAccessException | InstantiationException |
                    NoSuchMethodException | InvocationTargetException e) {
                // fall through, but log anyway:
                warn(e, TEXT_FAILED_TO_INSTANTIATE, klass$);
            }
            debug("Deserializing object with attributes %s", attributes);
            final String ref = (String)attributes.get(Response.PROP_REFERENCE);
            if (ref != null) {
                debug("Reference: %s", ref);
                return RESPONSE.create(attributes);
            }
            final String eventType = (String)attributes.get(Event.PROP_EVENT_TYPE);
            if (eventType != null) {
                debug("Event Type: %s", eventType);
                return EVENT.create(attributes);
            }
            final String command = (String)attributes.get(NetworkRequest.PROP_COMMAND);
            if (command != null) {
                debug("Command: %s", command);
                return NETREQUEST.create(attributes);
            }
            final String reqstate = (String)attributes.get(Request.PROP_REQUEST_STATE);
            if (reqstate != null) {
                debug("Request State: ", reqstate);
                return REQUEST.create(attributes);
            }
            return INFORMATION.create(attributes);
        } finally {
            RecipientResolver.unregister(bus);
        }
    }

    String formatKey(final String key) {
//        StringBuilder collector = new StringBuilder(128);
//        for (int i = 0, is = key.length(); i < is; ++i) {
//            char c = key.charAt(i);
//            if (Character.isUpperCase(c)) collector.append(' ');
//            collector.append(Character.toUpperCase(c));
//        }
//        return collector.toString().trim();
        return key;
    }

    String formatValue(final Object value) {
        return StringUtil.represent(value);
    }

}
