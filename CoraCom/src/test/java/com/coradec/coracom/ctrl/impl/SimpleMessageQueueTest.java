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

package com.coradec.coracom.ctrl.impl;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;

import com.coradec.coracom.model.Message;
import com.coradec.coracom.model.Recipient;
import com.coradec.coracom.model.Sender;
import com.coradec.coracom.model.impl.AbstractCommand;
import com.coradec.coracom.model.impl.BasicMessage;
import com.coradec.coracore.ctrl.AutoOrigin;
import com.coradec.corajet.cldr.Syslog;
import com.coradec.corajet.test.CoradeckJUnit4TestRunner;
import com.coradec.coralog.ctrl.impl.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.net.URI;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@SuppressWarnings({"WeakerAccess", "PackageVisibleField"})
@RunWith(CoradeckJUnit4TestRunner.class)
public class SimpleMessageQueueTest {

    public static final String SYSLOG_LEVEL = "INFORMATION";
    private static final int AGENT_WORKTIME = 5;

    static {
        if (!SYSLOG_LEVEL.equals("INFORMATION")) Syslog.setLevel(SYSLOG_LEVEL);
    }

    static final AtomicInteger ID = new AtomicInteger(0);
    static final Semaphore termLock = new Semaphore(0);
    static AtomicInteger APPROVED = new AtomicInteger(0);
    static AtomicInteger GENERATED = new AtomicInteger(0);
    static AtomicInteger DISPATCHED = new AtomicInteger(0);
    static Queue<TestAgent> agents = new ConcurrentLinkedQueue<>();
    static int nAgents;

    @SuppressWarnings("PackageVisibleField")
    SimpleMessageQueue MQ = new SimpleMessageQueue();

    /**
     * Runs 10 test agents in parallel, each sending 10..500 messages to itself.
     */
    @Test public void testSimpleMessageQueue() throws InterruptedException {
        nAgents = 10;
        long elapsed = System.currentTimeMillis();
        new TestExecutor().launch();
        termLock.tryAcquire(nAgents, 50, TimeUnit.SECONDS);
        elapsed = System.currentTimeMillis() - elapsed;
        final int approved = APPROVED.get();
        final int generated = GENERATED.get();
        final int dispatched = DISPATCHED.get();
        Syslog.info("Approved: %d, generated: %d, dispatched: %d.", approved, generated,
                dispatched);
        Syslog.info("Total time elapsed: %d ms with 1 message processor.", elapsed);
        Syslog.info("Throughput: %.3f ms/msg @ %d ms of work/msg.", (double)elapsed / dispatched,
                AGENT_WORKTIME);
        Syslog.info("Throughput: %.3f ms/msg.", (double)(elapsed - 5 * dispatched) / dispatched);
        assertThat(elapsed < 10 * dispatched, is(true));
        assertThat(generated, is(approved));
        assertThat(dispatched, is(approved));
        assertThat(termLock.availablePermits(), is(0)); // All tests reported finished.
    }

    @SuppressWarnings("ClassHasNoToStringMethod")
    private final class TestAgent extends Logger implements Sender, Recipient {

        private final int id;
        private final int genSize;
        private final Random random = new Random();
        private final ConcurrentLinkedQueue<Message> messages;
        private final AtomicBoolean sent;

        TestAgent() {
            id = ID.incrementAndGet();
            genSize = random.nextInt(490) + 10;
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
            if (message instanceof StopMessage) {
                termLock.release();
                return;
            }
            messages.remove(message);
            try {
                Thread.sleep(AGENT_WORKTIME); // simulate some work
            } catch (InterruptedException e) {
                throw new InternalError();
            }
            DISPATCHED.incrementAndGet();
        }

        ConcurrentLinkedQueue<Message> getMessages() {
            return messages;
        }

        public void run() {
            for (int i = 0; i < genSize; ++i) {
                sendMessage();
            }
            sent.set(true);
            MQ.inject(new StopMessage(this));
        }

        private void sendMessage() {
            GENERATED.incrementAndGet();
            final BasicMessage message = new BasicMessage(this, this);
            messages.add(message);
            MQ.inject(message);
        }

        @Override public void bounce(final Message message) {
            Syslog.error(message.toString());
        }

    }

    @SuppressWarnings("ClassHasNoToStringMethod")
    private class ExecuteAgentCommand extends AbstractCommand {

        private final TestAgent testAgent;

        public ExecuteAgentCommand(final Sender sender, final TestAgent testAgent) {
            super(sender);
            this.testAgent = testAgent;
            agents.add(testAgent);
        }

        @Override public void execute() {
            testAgent.run();
        }
    }

    private class TestExecutor extends AutoOrigin implements Sender, Recipient {

        @Override public String represent() {
            return "TestExecutor";
        }

        @Override public void onMessage(final Message message) {
            if (message instanceof ExecuteAgentCommand) {
                final ExecuteAgentCommand command = (ExecuteAgentCommand)message;
                try {
                    command.execute();
                    command.succeed();
                } catch (Exception e) {
                    command.fail(e);
                }
            }
            else Syslog.error("Invalid message: " + message);
        }

        @Override public URI toURI() {
            return URI.create(represent());
        }

        @Override public void bounce(final Message message) {
            Syslog.error(message.toString());
        }

        void launch() throws InterruptedException {
            for (int i = 0; i < nAgents; ++i) {
                MQ.inject(new ExecuteAgentCommand(this, new TestAgent()));
            }
        }
    }

    private class StopMessage extends BasicMessage {

        /**
         * Initializes a new instance of StopMessage with the specified sender.
         *
         * @param sender the sender.
         */
        public StopMessage(final Sender sender) {
            super(sender);
        }

    }

}
