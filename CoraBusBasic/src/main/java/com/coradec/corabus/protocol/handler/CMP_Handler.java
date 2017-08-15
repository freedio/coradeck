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

import com.coradec.corabus.com.impl.NetworkEvent;
import com.coradec.corabus.com.impl.NetworkMessage;
import com.coradec.corabus.com.impl.NetworkRequest;
import com.coradec.corabus.model.Bus;
import com.coradec.corabus.model.impl.BasicServiceProvider;
import com.coradec.corabus.protocol.ProtocolHandler;
import com.coradec.corabus.trouble.InvalidDataException;
import com.coradec.corabus.view.BusService;
import com.coradec.corabus.view.NetworkProtocol;
import com.coradec.corabus.view.impl.BasicServiceView;
import com.coradec.coracom.model.Information;
import com.coradec.coracom.model.PayloadMessage;
import com.coradec.coracom.model.Recipient;
import com.coradec.coracom.model.Sender;
import com.coradec.coracom.model.impl.BasicResponse;
import com.coradec.coracom.state.Answer;
import com.coradec.coracom.state.EventType;
import com.coradec.coracom.trouble.RequestFailedException;
import com.coradec.coraconf.model.Property;
import com.coradec.coracore.annotation.Inject;
import com.coradec.coracore.annotation.Nullable;
import com.coradec.coracore.model.GenericType;
import com.coradec.coracore.trouble.RequestRefused;
import com.coradec.coracore.trouble.ServiceNotAvailableException;
import com.coradec.coracore.util.ClassUtil;
import com.coradec.coracore.util.StringUtil;
import com.coradec.coradir.model.Path;
import com.coradec.corasession.model.Session;
import com.coradec.coratext.model.LocalizedText;
import com.coradec.coratext.model.Text;
import com.coradec.coratype.ctrl.TypeConverter;

import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * ​​Implementation of the Coradec Messaging Protocol handler.
 */
@SuppressWarnings("ClassHasNoToStringMethod")
public class CMP_Handler extends BasicServiceProvider implements ProtocolHandler {

    private static final String NAME_PROTOCOL = "CMP";
    public static final String SCHEME = NAME_PROTOCOL.toLowerCase();
    public static final Property<Integer> PROP_STANDARD_PORT =
            Property.define("StandardPort", Integer.class, 1024);
    static final Property<String> PROP_SEPARATOR = Property.define("Separator", String.class, ":");
    static final Property<String> PROP_DELIMITER = Property.define("Delimiter", String.class, "\n");
    static final Property<String> PROP_RECIPIENT_SEPARATOR =
            Property.define("RecipientSeparator", String.class, ",");

    static final Base64.Decoder DECODER = Base64.getMimeDecoder();
    static final Base64.Encoder ENCODER = Base64.getMimeEncoder();
    private static final String ATTR_FROM = "#FROM";
    private static final String ATTR_TO = "#TO";
    private static final String ATTR_REFERENCE = "#REFERENCE";
    private static final String ATTR_ANSWER = "#ANSWER";
    private static final String ATTR_PROBLEM = "#PROBLEM";
    private static final String ATTR_TYPE = "#TYPE";
    private static final String ATTR_COMMAND = "COMMAND";

    private static final Text TEXT_FAILED_TO_DECODE_REFERENCE =
            LocalizedText.define("FailedToDecodeReference");
    private static final Text TEXT_FAILED_TO_DECODE_ANSWER =
            LocalizedText.define("FailedToDecodeAnswer");
    private static final Text TEXT_FAILED_TO_DECODE_EVENT_TYPE =
            LocalizedText.define("FailedToDecodeEventType");

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

        @Override public ByteBuffer serialize(final Information info) {
            final String delimiter = PROP_DELIMITER.value();
            final String separator = PROP_SEPARATOR.value();
            StringBuilder collector = new StringBuilder(4096);
            ClassUtil.getAttributes(info)
                     .forEach((key, value) -> collector.append(formatKey(key))
                                                       .append(separator)
                                                       .append(formatValue(value))
                                                       .append(delimiter));
            if (info instanceof PayloadMessage) {
                final @Nullable byte[] body = ((PayloadMessage)info).getBody();
                if (body != null) collector.append(delimiter)
                                           .append(new String(ENCODER.encode(body),
                                                   StringUtil.CHARSET));
            }
            return ByteBuffer.wrap(collector.toString().getBytes(StringUtil.CHARSET));
        }

        private Information deserialize(final Session session, final ByteBuffer buffer)
                throws InvalidDataException {
            final String delimiter = PROP_DELIMITER.value();
            final String separator = PROP_SEPARATOR.value();
            Map<String, String> attributes = new HashMap<>();
            byte[] all = new byte[buffer.limit()], body = new byte[0];
            buffer.get(all);
            final String message = new String(all, StringUtil.CHARSET);
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
            return construct(session, attributes, body);
        }

        @Override public @Nullable Information read(final SocketChannel channel)
                throws IOException {
            if (currentInput == null) {
                if (currentInputLength == null) {
                    currentInputLength = ByteBuffer.allocate(Integer.BYTES);
                }
                if (currentInputLength.hasRemaining())
                    if (channel.read(currentInputLength) == -1) throw new EOFException();
                if (currentInputLength.hasRemaining()) return null;
                currentInputLength.flip();
                currentInput = ByteBuffer.allocate(currentInputLength.getInt());
            }
            if (channel.read(currentInput) == -1) throw new EOFException();
            Information result = null;
            if (!currentInput.hasRemaining()) {
                currentInput.flip();
                result = deserialize(getSession(), currentInput);
            }
            return result;
        }

        @SuppressWarnings("unchecked") @Nullable @Override
        public <V> V decode(final GenericType<? super V> type, @Nullable final byte[] data) {
            return data == null ? null : (V)TypeConverter.to(type).unmarshal(data);
        }

        @Nullable @Override
        public <V> byte[] encode(final GenericType<V> type, @Nullable final V value) {
            return value == null ? null : TypeConverter.to(type).marshal(value);
        }
    }

    Information construct(final Session session, final Map<String, String> attributes,
            final byte[] body) throws InvalidDataException {
        Information result = null;
        final String from$ = attributes.remove(ATTR_FROM);
        final Sender sender = bus.sender(Path.of(from$));
        List<Recipient> recipientList = new ArrayList<>();
        for (String to$ : attributes.remove(ATTR_TO).split(PROP_RECIPIENT_SEPARATOR.value())) {
            recipientList.add(bus.recipient(Path.of(to$)));
        }
        Recipient[] recipients = recipientList.toArray(new Recipient[recipientList.size()]);
        final String reference$ = attributes.remove(ATTR_REFERENCE);
        if (reference$ != null) { // response, request or voucher
            final UUID reference;
            try {
                reference = UUID.fromString(reference$);
            } catch (IllegalArgumentException e) {
                throw new InvalidDataException(TEXT_FAILED_TO_DECODE_REFERENCE.resolve(reference$),
                        e);
            }
            final String answer$ = attributes.remove(ATTR_ANSWER);
            if (answer$ != null) { // response
                final Answer answer;
                try {
                    answer = Answer.valueOf(answer$);
                } catch (IllegalArgumentException e) {
                    throw new InvalidDataException(TEXT_FAILED_TO_DECODE_ANSWER.resolve(answer$),
                            e);
                }
                Object arg = null;
                switch (answer) {
                    case OK:
                        arg = body;
                        break;
                    case KO:
                        final String problem$ = attributes.remove(ATTR_PROBLEM);
                        if (problem$ != null) {
                            arg = new RequestFailedException(problem$);
                        }
                        break;
                    case CN:
                        break;
                }
                result = new BasicResponse(session, reference, answer, arg, sender, recipients);
            } else { // request or voucher
                String command = attributes.remove(ATTR_COMMAND);
                result = new NetworkRequest(session, command, attributes, body, sender, recipients);
            }
        } else { // event or message
            final String type$ = attributes.remove(ATTR_TYPE);
            EventType type;
            try {
                type = EventType.valueOf(type$);
            } catch (IllegalArgumentException e) {
                throw new InvalidDataException(TEXT_FAILED_TO_DECODE_EVENT_TYPE.resolve(type$), e);
            }
            switch (type) {
                case MESSAGE:
                    result = new NetworkMessage(session, attributes, body, sender, recipients);
                    break;
                case NOTIFICATION:
                    result = new NetworkEvent(session, attributes, body, sender);
                    break;
            }
        }
        return result;
    }

    String formatKey(final String key) {
        StringBuilder collector = new StringBuilder(128);
        for (int i = 0, is = key.length(); i < is; ++i) {
            char c = key.charAt(i);
            if (Character.isUpperCase(c)) collector.append(' ').append(c);
            else collector.append(Character.toUpperCase(c));
        }
        return collector.toString().trim();
    }

    String formatValue(final Object value) {
        return StringUtil.represent(value);
    }
}
