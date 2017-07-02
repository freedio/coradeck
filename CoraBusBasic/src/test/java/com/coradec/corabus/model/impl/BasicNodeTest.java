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

import static com.coradec.corabus.model.NodeState.*;
import static java.util.concurrent.TimeUnit.*;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;

import com.coradec.corabus.com.Invitation;
import com.coradec.corabus.model.BusNode;
import com.coradec.corabus.view.BusContext;
import com.coradec.coracom.ctrl.MessageQueue;
import com.coradec.coracom.model.Message;
import com.coradec.coracom.model.Recipient;
import com.coradec.coracom.model.Request;
import com.coradec.coracom.model.Sender;
import com.coradec.coracore.annotation.Inject;
import com.coradec.coracore.ctrl.Factory;
import com.coradec.corajet.cldr.Syslog;
import com.coradec.corajet.test.CoradeckJUnit4TestRunner;
import com.coradec.corasession.model.Session;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.net.URI;
import java.util.Optional;

@RunWith(CoradeckJUnit4TestRunner.class)
public class BasicNodeTest {

    @SuppressWarnings("WeakerAccess") public static final String SYSLOG_LEVEL = "DEBUG";

    static {
        if (!SYSLOG_LEVEL.equals("INFORMATION")) Syslog.setLevel(SYSLOG_LEVEL);
    }

//    private static SimpleMessageQueue MQ = new SimpleMessageQueue();

    @Inject private static MessageQueue MQ;
    @Inject private Factory<Invitation> invitationFactory;
    @Inject private Factory<Session> sessionFactory;

    private final BasicNode testee = new BasicNode();

    @Test public void normalSetupAndShutdownShouldSucceed() throws InterruptedException {
        final Session session = sessionFactory.create();
        final BusContext dummyContext = new TestBusContext();
        final Sender testEnv = new TestEnvironment();
        final Invitation invitation =
                invitationFactory.create(session, dummyContext, testEnv, new Recipient[] {testee});
        assertThat(testee.getState(), is(UNATTACHED));
        MQ.inject(invitation).standby(1, SECONDS).andThen(() -> {
            assertThat(dummyContext.getNode().orElse(null), is(equalTo(testee)));
            assertThat(testee.getState(), is(ATTACHED));
        }).orElse(problem -> Assert.fail("Invitation failed with " + problem));
        final Request dismissal = invitation.getMember().dismiss();
        MQ.inject(dismissal).standby(1, SECONDS).andThen(() -> {
            assertThat(dummyContext.getNode().orElse(null), is(nullValue()));
            assertThat(testee.getState(), is(DETACHED));
        }).orElse(problem -> Assert.fail("Dismissal failed with " + problem));
    }

    @SuppressWarnings("ClassHasNoToStringMethod")
    private class TestBusContext implements BusContext {

        private BusNode node;

        @Override public void joined(final BusNode node) {
            if (this.node != null) throw new IllegalStateException("Node already attached!");
            this.node = node;
        }

        @Override public Optional<BusNode> getNode() {
            return Optional.ofNullable(node);
        }

        @Override public void left(final BusNode node) {
            if (this.node != node) throw new IllegalStateException("Node not attached!");
            this.node = null;
        }
    }

    private class TestEnvironment implements Sender {

        @Override public String represent() {
            return "TestEnvironment";
        }

        @Override public URI toURI() {
            return URI.create(represent());
        }

        @Override public void bounce(final Message message) {
            Syslog.error("Message %s bounced", message);
        }
    }
}
