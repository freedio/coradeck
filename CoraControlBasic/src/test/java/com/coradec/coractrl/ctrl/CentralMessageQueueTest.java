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

import static java.util.concurrent.TimeUnit.*;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;

import com.coradec.coracom.ctrl.Observer;
import com.coradec.coracom.model.Information;
import com.coradec.coracom.model.Message;
import com.coradec.coracom.model.Recipient;
import com.coradec.coracom.model.Sender;
import com.coradec.coracom.model.impl.AbstractCommand;
import com.coradec.coracom.model.impl.BasicEvent;
import com.coradec.coracom.model.impl.BasicInformation;
import com.coradec.coracom.model.impl.BasicMessage;
import com.coradec.coracore.annotation.Inject;
import com.coradec.coracore.annotation.ToString;
import com.coradec.coracore.ctrl.AutoOrigin;
import com.coradec.coracore.model.Origin;
import com.coradec.coracore.trouble.OperationTimedoutException;
import com.coradec.coracore.util.ClassUtil;
import com.coradec.coractrl.ctrl.impl.BasicAgent;
import com.coradec.corajet.cldr.Syslog;
import com.coradec.corajet.test.CoradeckJUnit4TestRunner;
import com.coradec.coralog.ctrl.impl.Logger;
import org.junit.AfterClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@SuppressWarnings({"WeakerAccess", "PackageVisibleField"})
@RunWith(CoradeckJUnit4TestRunner.class)
public class CentralMessageQueueTest {

    public static final String SYSLOG_LEVEL = "INFORMATION";
    private static final int AGENT_WORKTIME = 5;
    static final Random RANDOM = new Random();

    static {
        if (!SYSLOG_LEVEL.equals("INFORMATION")) Syslog.setLevel(SYSLOG_LEVEL);
    }

    static final Class<?>[] INFO_TYPES = {
            Info0.class, Info1.class, Info2.class
    };

    static final AtomicInteger ID = new AtomicInteger(0);
    static final Semaphore termLock = new Semaphore(0);
    static AtomicInteger APPROVED = new AtomicInteger(0);
    static AtomicInteger GENERATED = new AtomicInteger(0);
    static AtomicInteger DISPATCHED = new AtomicInteger(0);
    static Queue<LoadTestAgent> LOAD_TEST_AGENTS = new ConcurrentLinkedQueue<>();
    static Queue<SequenceTestAgent> SEQUENCE_TEST_AGENTS = new ConcurrentLinkedQueue<>();
    static StringBuilder COLLECTOR = new StringBuilder();
    static StringBuilder SEQUENCE = new StringBuilder();

    @SuppressWarnings("PackageVisibleField") @Inject MultiThreadedMessageQueue CMQ;

    @AfterClass public static void teardownSuite() {
        SysControl.terminate();
    }

    /**
     * Runs 100 test LOAD_TEST_AGENTS in parallel, each sending 10..500 messages to itself.
     */
    @Test public void testLoad() throws InterruptedException {
        APPROVED.set(0);
        GENERATED.set(0);
        APPROVED.set(0);
        Syslog.info("Performing the load test ... (takes less than 50 seconds)");
        int nAgents = 100;
        long elapsed = System.currentTimeMillis();
        new LoadTestExecutor().launch(nAgents);
        termLock.tryAcquire(nAgents, 50, SECONDS);
        elapsed = System.currentTimeMillis() - elapsed;
        final int approved = APPROVED.get();
        final int generated = GENERATED.get();
        final int dispatched = DISPATCHED.get();
        Syslog.info("Approved: %d, generated: %d, dispatched: %d.", approved, generated,
                dispatched);
        final int maxUsed = CMQ.getMaxUsed();
        Syslog.info("Total time elapsed: %d ms with %d active message processor%s.", elapsed,
                maxUsed, maxUsed == 1 ? "" : "s");
        Syslog.info("Throughput: %.3f ms/msg @ %d ms of work/msg.", (double)elapsed / dispatched,
                AGENT_WORKTIME);
        assertThat(elapsed < 50000, is(true));
        assertThat(generated, is(approved));
        assertThat(dispatched, is(approved));
        assertThat(termLock.availablePermits(), is(0)); // All tests reported finished.
    }

    @Test public void testSequence() throws InterruptedException {
        Syslog.info("Performing the sequence test ... (takes less than 50 seconds)");
        CMQ.resetUsage();
        long elapsed = System.currentTimeMillis();
        final int rounds = 50000;
        final SequenceTestAgent agent = new SequenceTestAgent();
        new SequenceTestExecutor().launch(rounds, agent);
        termLock.tryAcquire(rounds, 50, SECONDS);
        elapsed = System.currentTimeMillis() - elapsed;
        final String result = COLLECTOR.toString();
        final int maxUsed = CMQ.getMaxUsed();
        Syslog.info("Total time elapsed: %d ms with %d active message processor%s.", elapsed,
                maxUsed, maxUsed == 1 ? "" : "s");
        Syslog.info("Throughput: %.3f ms/msg.", (double)elapsed / rounds);
        Syslog.info("Result: %s",
                result.length() > 100 ? result.substring(0, 100) + "..." : result);
        final String seq = SEQUENCE.toString();
        Syslog.info("Expect: %s", seq.length() > 100 ? seq.substring(0, 100) + "..." : seq);
        assertThat(elapsed < 50000, is(true));
        assertThat(result.length(), is(seq.length()));
//        int differences = 0;
//        for (int i = 0; i < seq.length(); ++i) {
//            char c1 = seq.charAt(i);
//            char c2 = result.charAt(i);
//            if (c1 != c2) {
//                Syslog.info("Difference at index %d", i);
//                ++differences;
//            }
//        }
//        assertThat(differences, is(0));
        assertThat(result, is(equalTo(seq)));
        assertThat(termLock.availablePermits(), is(0)); // All tests reported finished.
    }

    @Test public void testInformationDispatch() throws InterruptedException {
        Syslog.info("Performing the information delivery test.");
        APPROVED.set(0);
        GENERATED.set(0);
        APPROVED.set(0);
        CMQ.resetUsage();
        final int nAgents = 1000;
        final int nMessages = 10000;
        long elapsed = System.currentTimeMillis();
        new InfoTestExecutor().launch(nAgents, nMessages);
//        termLock.tryAcquire(nAgents, 50, SECONDS);
        elapsed = System.currentTimeMillis() - elapsed;
        Syslog.info("Total time elapsed for %d notifications to %d agents: %d ms.", nMessages,
                nAgents, elapsed);
        Syslog.info("Throughput: %.3f μs/msg.", elapsed * 1000.0 / (nAgents * nMessages));
        assertThat(elapsed < 50000, is(true));
    }

    @SuppressWarnings("ClassHasNoToStringMethod")
    private final class LoadTestAgent extends Logger implements Sender, Recipient {

        private final int id;
        private final int genSize;
        private final Random random = new Random();
        private final ConcurrentLinkedQueue<Message> messages;
        private final AtomicBoolean sent;

        LoadTestAgent() {
            id = ID.incrementAndGet();
            genSize = random.nextInt(490) + 10;
            APPROVED.addAndGet(genSize);
            messages = new ConcurrentLinkedQueue<>();
            sent = new AtomicBoolean();
        }

        @Override public String represent() {
            return String.format("LoadTestAgent#%d", id);
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
            CMQ.inject(new StopMessage(this));
        }

        private void sendMessage() {
            GENERATED.incrementAndGet();
            final BasicMessage message = new BasicMessage(this, this);
            messages.add(message);
            CMQ.inject(message);
        }

        @Override public void bounce(final Message message) {
            Syslog.error(message.toString());
        }

    }

    @SuppressWarnings("ClassHasNoToStringMethod")
    private class ExecuteLoadTestAgentCommand extends AbstractCommand {

        private final LoadTestAgent agent;

        public ExecuteLoadTestAgentCommand(final Sender sender, final LoadTestAgent agent) {
            super(sender);
            this.agent = agent;
            LOAD_TEST_AGENTS.add(agent);
        }

        @Override public void execute() {
            agent.run();
        }
    }

    private class LoadTestExecutor implements Sender, Recipient {

        @Override public String represent() {
            return getClass().getSimpleName();
        }

        @Override public void onMessage(final Message message) {
            if (message instanceof ExecuteLoadTestAgentCommand) {
                final ExecuteLoadTestAgentCommand command = (ExecuteLoadTestAgentCommand)message;
                try {
                    command.execute();
                    command.succeed();
                } catch (Exception e) {
                    command.fail(e);
                }
            } else Syslog.error("Invalid message: " + message);
        }

        @Override public URI toURI() {
            return URI.create(represent());
        }

        @Override public void bounce(final Message message) {
            Syslog.error("Message bounced: %s", message.toString());
        }

        void launch(final int nAgents) throws InterruptedException {
            for (int i = 0; i < nAgents; ++i) {
                CMQ.inject(new ExecuteLoadTestAgentCommand(this, new LoadTestAgent()));
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

    private class SequenceTestAgent extends AutoOrigin implements Recipient {

        @Override public void onMessage(final Message message) {
            if (message instanceof AddCharacterCommand) {
                ((AddCharacterCommand)message).execute();
            } else Syslog.error("Cannot process message %s", message);
        }
    }

    @SuppressWarnings("ClassHasNoToStringMethod")
    private class AddCharacterCommand extends AbstractCommand {

        private final char c;

        public AddCharacterCommand(final Sender sender, Recipient recipient, final char c) {
            super(sender, recipient);
            this.c = c;
        }

        @Override public void execute() {
            COLLECTOR.append(c);
            termLock.release();
        }

    }

    private class SequenceTestExecutor implements Sender {

        void launch(final int rounds, final Recipient agent) {
            for (int i = 0; i < rounds; ++i) {
                final int range = Character.MAX_VALUE + 1;
                final char offset = ' ';
                char c = (char)(offset + RANDOM.nextInt(range));
                SEQUENCE.append(c);
                CMQ.inject(new AddCharacterCommand(this, agent, c));
            }
        }

        @Override public String represent() {
            return getClass().getSimpleName();
        }

        @Override public URI toURI() {
            return URI.create(represent());
        }

        @Override public void bounce(final Message message) {
            Syslog.error("Message bounced: %s", message.toString());
        }
    }

    @SuppressWarnings("ClassHasNoToStringMethod")
    private class InfoTestExecutor implements Sender {

        private final List<InfoTestAgent> agents = new ArrayList<>();
        private final int[] distribution = new int[3];

        void launch(final int nAgents, final int nMessages) throws InterruptedException {
            for (int i = 0; i < nAgents; ++i) {
                final InfoTestAgent testAgent = new InfoTestAgent();
                agents.add(testAgent);
                CMQ.subscribe(testAgent);
            }
            Syslog.info("Sending %d notifications expected to be picked up by %d agents.",
                    nMessages, nAgents);
            for (int i = 0; i < nMessages; ++i) {
                final int mtype = RANDOM.nextInt(3);
                try {
                    CMQ.inject((Information)INFO_TYPES[mtype].getConstructor(Origin.class)
                                                             .newInstance(this));
                } catch (Exception e) {
                    Syslog.error(e);
                }
                ++distribution[mtype];
            }
            Syslog.info("Sending the stop event.");
            CMQ.inject(new StopEvent(this));
            if (!termLock.tryAcquire(nAgents, 50, SECONDS)) {
                Syslog.warn("Only %d/%d locks available!", termLock.availablePermits(), nAgents);
                throw new OperationTimedoutException();
            }
            int totalMisses = 0;
            for (final InfoTestAgent agent : agents) {
                final StatRecord statistics = agent.getStatistics();
                totalMisses += statistics.getMisses();
                final int hits = statistics.getHits();
                final Class<Information> interested = statistics.getInterestedIn();
                for (int i = 0; i < 3; ++i) {
                    if (interested == INFO_TYPES[i]) {
                        assertThat(hits, is(distribution[i]));
                        break;
                    }
                }
            }
            assertThat(totalMisses, is(0));
        }

        @Override public String represent() {
            return getClass().getSimpleName();
        }

        @Override public URI toURI() {
            return URI.create(represent());
        }

        @Override public void bounce(final Message message) {
            Syslog.error("Message bounced: %s", message.toString());
        }

    }

    @SuppressWarnings("ClassHasNoToStringMethod")
    private static class InfoTestAgent extends BasicAgent implements Observer {

        private final Class<Information> interestedIn;
        private int match;
        private int mismatch;

        InfoTestAgent() {
            //noinspection unchecked
            interestedIn = (Class<Information>)INFO_TYPES[RANDOM.nextInt(3)];
        }

        @Override public boolean notify(final Information info) {
            if (info instanceof StopEvent) {
                termLock.release();
                return false;
            }
            if (!interestedIn.isInstance(info)) ++mismatch;
            else ++match;
            return false;
        }

        @Override public boolean wants(final Information info) {
            return interestedIn.isInstance(info) || info instanceof StopEvent;
        }

        public StatRecord getStatistics() {
            return new StatRecord(interestedIn, match, mismatch);
        }

    }

    private static class Info0 extends BasicInformation {

        public Info0(final Origin origin) {
            super(origin);
        }
    }

    private static class Info1 extends BasicInformation {

        public Info1(final Origin origin) {
            super(origin);
        }
    }

    private static class Info2 extends BasicInformation {

        public Info2(final Origin origin) {
            super(origin);
        }
    }

    private static class StatRecord {

        private final Class<Information> interestedIn;
        private final int match;
        private final int mismatch;

        public StatRecord(final Class<Information> interestedIn, final int match,
                final int mismatch) {
            this.interestedIn = interestedIn;
            this.match = match;
            this.mismatch = mismatch;
        }

        @ToString public Class<Information> getInterestedIn() {
            return interestedIn;
        }

        @ToString public int getHits() {
            return match;
        }

        @ToString public int getMisses() {
            return mismatch;
        }

        @Override public String toString() {
            return ClassUtil.toString(this);
        }
    }

    private class StopEvent extends BasicEvent {

        public StopEvent(final Origin origin) {
            super(origin);
        }
    }
}
