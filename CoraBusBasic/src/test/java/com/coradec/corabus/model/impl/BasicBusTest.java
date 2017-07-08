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

import static com.coradec.corabus.model.HubState.*;
import static java.util.concurrent.TimeUnit.*;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;

import com.coradec.corabus.com.Invitation;
import com.coradec.corabus.model.BusNode;
import com.coradec.corabus.model.NodeState;
import com.coradec.corabus.view.BusContext;
import com.coradec.coracom.ctrl.MessageQueue;
import com.coradec.coracom.model.Message;
import com.coradec.coracom.model.Recipient;
import com.coradec.coracom.model.Request;
import com.coradec.coracom.model.Sender;
import com.coradec.coracore.annotation.Inject;
import com.coradec.coracore.ctrl.Factory;
import com.coradec.corajet.cldr.Syslog;
import com.coradec.coralog.ctrl.impl.Logger;
import com.coradec.corasession.model.Session;
import com.coradec.coratext.model.LocalizedText;
import com.coradec.coratext.model.Text;
import org.hamcrest.MatcherAssert;
import org.junit.Assert;

import java.net.URI;
import java.util.Optional;

/**
 * ​​Base class of all bus tests.
 */
@SuppressWarnings({"ClassHasNoToStringMethod", "ProtectedField"})
class BasicBusTest extends Logger {

    @SuppressWarnings("WeakerAccess") public static final String SYSLOG_LEVEL = "INFORMATION";

    static {
        if (!SYSLOG_LEVEL.equals("INFORMATION")) Syslog.setLevel(SYSLOG_LEVEL);
    }

    @Inject private Factory<Invitation> invitationFactory;
    @Inject private Factory<Session> sessionFactory;
    @Inject private static MessageQueue MQ;
    @SuppressWarnings("WeakerAccess") static final Text TEXT_MESSAGE_BOUNCED =
            LocalizedText.define("MessageBounced");

    @SuppressWarnings("WeakerAccess")
    protected void testNormalSetupAndShutdown(final BasicNode testee, final NodeState terminalState,
                                              final Inbetween... inBetweens)
            throws InterruptedException {
        final Session session = sessionFactory.create();
        final BusContext dummyContext = new TestBusContext();
        final Sender testEnv = new TestEnvironment();
        MatcherAssert.assertThat(testee.getState(), is(UNATTACHED));
        final Invitation invitation = MQ.inject(
                invitationFactory.create(session, dummyContext, testEnv, new Recipient[] {testee}));
        invitation.standby(1, SECONDS).andThen(() -> {
            assertThat(dummyContext.getNode().orElse(null), is(equalTo(testee)));
            MatcherAssert.assertThat(testee.getState(), is(terminalState));
        }).orElse(problem -> Assert.fail("Invitation failed with " + problem));
        for (final Inbetween inBetween : inBetweens) {
            inBetween.execute(session, testEnv, testee);
        }
        final Request dismissal = invitation.getMember().dismiss();
        dismissal.standby(1, SECONDS).andThen(() -> {
            assertThat(dummyContext.getNode().orElse(null), is(nullValue()));
            MatcherAssert.assertThat(testee.getState(), is(DETACHED));
        }).orElse(problem -> Assert.fail("Dismissal failed with " + problem));
    }

    @SuppressWarnings("WeakerAccess") protected <M extends Message> M inject(M message) {
        return MQ.inject(message);
    }

    @SuppressWarnings("ClassHasNoToStringMethod")
    protected class TestBusContext implements BusContext {

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

    protected class TestEnvironment implements Sender {

        @Override public String represent() {
            return "TestEnvironment";
        }

        @Override public URI toURI() {
            return URI.create(represent());
        }

        @Override public void bounce(final Message message) {
            error(TEXT_MESSAGE_BOUNCED, message);
        }
    }

    protected abstract class Inbetween {

        protected abstract void execute(final Session session, final Sender sender,
                                        final Recipient... recipients) throws InterruptedException;

    }

}
