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

import com.coradec.coracom.model.Message;
import com.coradec.coracom.model.Recipient;
import com.coradec.coracom.model.Sender;
import com.coradec.coracom.model.impl.BasicMessage;
import com.coradec.coracore.annotation.Inject;
import com.coradec.coracore.ctrl.AutoOrigin;
import com.coradec.coractrl.model.MessageQueue;
import com.coradec.corajet.cldr.Syslog;
import com.coradec.corajet.test.CoradeckJUnit4TestRunner;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.net.URI;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@SuppressWarnings("WeakerAccess")
@RunWith(CoradeckJUnit4TestRunner.class)
public class CentralMessageQueueTest {

    static final AtomicInteger ID = new AtomicInteger(0);
    static final Semaphore termLock = new Semaphore(0);

    @SuppressWarnings("PackageVisibleField")
    @Inject
    MessageQueue CMQ;

    @BeforeClass public static void setupSuite() {
        Syslog.setLevel("INFORMATION");

    }

    /**
     * Runs 100 test agents in parallel, each sending 10..1000 messages to itself.
     */
    @Test public void testCMQ() throws InterruptedException {
        final int nAgents = 1;
        for (int i = 0; i < nAgents; ++i) {
            CMQ.schedule(new TestAgent());
        }
        termLock.acquire(nAgents);
        Syslog.info("Terminated");
    }

    @SuppressWarnings("ClassHasNoToStringMethod")
    private final class TestAgent extends AutoOrigin implements Sender, Recipient, Runnable {

        private final int id;
        private final int genSize;
        private final Random random = new Random();
        private final Map<Message, Message> messages;
        private final AtomicBoolean sent;

        TestAgent() {
            id = ID.incrementAndGet();
            genSize = random.nextInt(990) + 10;
            messages = new ConcurrentHashMap<>();
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
//            Syslog.info("%d messages to go...", messages.size());
            if (sent.get() && messages.isEmpty()) termLock.release();
        }

        @Override public void run() {
            for (int i = 0; i < genSize; ++i) {
//                Syslog.info("Sending message %d", i);
                sendMessage();
            }
            sent.set(true);
        }

        private void sendMessage() {
            final BasicMessage message = new BasicMessage(this, this);
            messages.put(message, message);
            CMQ.inject(message);
        }
    }

}
