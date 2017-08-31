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

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;

import com.coradec.corabus.com.impl.Ping;
import com.coradec.corabus.model.Bus;
import com.coradec.corabus.view.NetworkProtocol;
import com.coradec.coracom.ctrl.OriginResolver;
import com.coradec.coracom.ctrl.RecipientResolver;
import com.coradec.coracom.model.Message;
import com.coradec.coracom.model.Recipient;
import com.coradec.coracom.model.SessionInformation;
import com.coradec.coracore.annotation.Inject;
import com.coradec.coracore.annotation.NonNull;
import com.coradec.coracore.annotation.Nullable;
import com.coradec.coracore.model.Origin;
import com.coradec.coradir.model.Path;
import com.coradec.corajet.cldr.Syslog;
import com.coradec.corajet.test.CoradeckJUnit4TestRunner;
import com.coradec.corasession.model.Session;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.UUID;

@RunWith(CoradeckJUnit4TestRunner.class)
public class CMP_HandlerTest implements RecipientResolver, OriginResolver {

    private final CMP_Handler testee = new CMP_Handler();
    @Inject Bus bus;
    @Inject Session session;
    private TestRecipient recipient;
    private TestSender sender;

    @Test public void testDeserialization() throws IOException {
        RecipientResolver.register(this);
        OriginResolver.register(this);
        try {
            final NetworkProtocol protocol =
                    testee.getService(session, NetworkProtocol.class, CMP_Handler.NAME_PROTOCOL);
            final UUID uuid = UUID.randomUUID();
            sender = new TestSender();
            recipient = new TestRecipient();
            final Ping target = new Ping(session, sender, recipient);

            final ByteBuffer serialized = protocol.serialize(uuid, target, Path.of("/test/path"));
            serialized.flip();
            final InputStream instr = new ByteBufferInputStream(serialized);
            ReadableByteChannel channel = Channels.newChannel(instr);
            final SessionInformation pingAgain = protocol.read(channel);
            assert pingAgain != null;
            assertThat(pingAgain, is(instanceOf(Ping.class)));
            Ping reping = (Ping)pingAgain;
            assertThat(reping.getSession(), is(session));
            assertThat(reping.getOrigin(), is(sender));
            assertThat(reping.getRecipient(), is(recipient));
            assertThat(reping.getState(), is(equalTo(target.getState())));
            assertThat(reping.isUrgent(), is(equalTo(target.isUrgent())));
        } finally {
            RecipientResolver.unregister(this);
            OriginResolver.unregister(this);
        }
    }

    @Override public Recipient recipientOf(@Nullable final Session session, final String id) {
        if (!"TestRecipient".equals(id)) throw new IllegalArgumentException(id);
        return recipient;
    }

    @Override public Origin originOf(@Nullable final Session session, final String id) {
        if (!"TestSender".equals(id)) throw new IllegalArgumentException(id);
        return sender;
    }

    private class TestSender implements Origin {

        @Override public URI toURI() {
            return URI.create(represent());
        }

        @Override public String represent() {
            return "TestSender";
        }
    }

    private class TestRecipient implements Recipient {

        @Override public void onMessage(final Message message) {
            Syslog.error("Received message «%s»", message);
        }

        @Override public String getRecipientId() {
            return "TestRecipient";
        }
    }

    private class ByteBufferInputStream extends InputStream {

        private final ByteBuffer buffer;

        public ByteBufferInputStream(final ByteBuffer buffer) {
            this.buffer = buffer;
        }

        @Override public int read() throws IOException {
            return buffer.get();
        }

        @Override public int read(final @NonNull byte[] b, final int off, final int len)
                throws IOException {
            int previous = buffer.remaining();
            buffer.get(b, off, len);
            return previous - buffer.remaining();
        }

        @Override public long skip(final long n) throws IOException {
            int previous = buffer.position();
            buffer.position((int)(buffer.position() + n));
            return buffer.position() - previous;
        }

        @Override public synchronized void mark(final int readlimit) {
            buffer.mark();
        }

        @Override public synchronized void reset() throws IOException {
            buffer.reset();
        }

        @Override public int available() throws IOException {
            return buffer.remaining();
        }

        @Override public boolean markSupported() {
            return true;
        }

    }
}
