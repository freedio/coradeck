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

package com.coradec.coractrl.ctrl;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;

import com.coradec.coracom.model.Message;
import com.coradec.coracom.model.Recipient;
import com.coradec.coracom.model.Sender;
import com.coradec.coracom.model.impl.BasicMessage;
import com.coradec.coracore.annotation.Inject;
import com.coradec.coracore.ctrl.AutoOrigin;
import com.coradec.coractrl.model.MessageQueue;
import com.coradec.corajet.cldr.Syslog;
import com.coradec.corajet.test.CoradeckJUnit4TestRunner;
import org.junit.AfterClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.net.URI;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@SuppressWarnings({"WeakerAccess", "PackageVisibleField"})
@RunWith(CoradeckJUnit4TestRunner.class)
public class CentralMessageQueueTest {

    public static final String SYSLOG_LEVEL = "INFORMATION";

    static {
        Syslog.setLevel(SYSLOG_LEVEL);
    }

    static final AtomicInteger ID = new AtomicInteger(0);
    static final Semaphore termLock = new Semaphore(0);
    static AtomicInteger APPROVED = new AtomicInteger(0);
    static AtomicInteger GENERATED = new AtomicInteger(0);
    static AtomicInteger DISPATCHED = new AtomicInteger(0);

    @SuppressWarnings("PackageVisibleField")
    @Inject
    MessageQueue CMQ;

    @AfterClass public static void teardownSuite() {
        SysControl.terminate();
    }

    /**
     * Runs 100 test agents in parallel, each sending 10..1000 messages to itself.
     */
    @Test public void testCMQ() throws InterruptedException {
        long elapsed = System.currentTimeMillis();
        final int nAgents = 100;
        for (int i = 0; i < nAgents; ++i) {
            CMQ.schedule(new TestAgent());
        }
        termLock.tryAcquire(nAgents, 20, TimeUnit.SECONDS);
        elapsed = System.currentTimeMillis() - elapsed;
        final int approved = APPROVED.get();
        final int generated = GENERATED.get();
        final int dispatched = DISPATCHED.get();
        Syslog.info("Terminated, %d messages approved, %d generated, %d dispatched in %d ms",
                approved, generated, dispatched, elapsed);
        assertThat(generated, is(approved));
        assertThat(dispatched, is(approved));
    }

    @SuppressWarnings("ClassHasNoToStringMethod")
    private final class TestAgent extends AutoOrigin implements Sender, Recipient, Runnable {

        private final int id;
        private final int genSize;
        private final Random random = new Random();
        private final ConcurrentLinkedQueue<Message> messages;
        private final AtomicBoolean sent;

        TestAgent() {
            id = ID.incrementAndGet();
            genSize = random.nextInt(990) + 10;
            APPROVED.addAndGet(genSize);
            messages = new ConcurrentLinkedQueue<>();
            sent = new AtomicBoolean();
        }

        @Override public String represent() {
            return String.format("TestAgent#%d", id);
        }

        @Override public URI toURI() {
            return URI.create(represent());
        }

        @Override public void onMessage(final Message message) {
            messages.remove(message);
            DISPATCHED.incrementAndGet();
            if (sent.get() && messages.isEmpty()) termLock.release();
        }

        @Override public void run() {
            for (int i = 0; i < genSize; ++i) {
                sendMessage();
            }
            sent.set(true);
        }

        private void sendMessage() {
            GENERATED.incrementAndGet();
            final BasicMessage message = new BasicMessage(this, this);
            messages.add(message);
            CMQ.inject(message);
        }
    }

}
