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

import static com.coradec.corabus.state.HubState.*;
import static com.coradec.corabus.state.MetaState.*;
import static java.util.concurrent.TimeUnit.*;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;

import com.coradec.corabus.com.Invitation;
import com.coradec.corabus.state.NodeState;
import com.coradec.corabus.view.BusContext;
import com.coradec.corabus.view.BusService;
import com.coradec.corabus.view.impl.BasicBusContext;
import com.coradec.coracom.ctrl.MessageQueue;
import com.coradec.coracom.model.Information;
import com.coradec.coracom.model.Message;
import com.coradec.coracom.model.Recipient;
import com.coradec.coracom.model.Request;
import com.coradec.coracom.model.Sender;
import com.coradec.coracore.annotation.Inject;
import com.coradec.coracore.model.Factory;
import com.coradec.corajet.cldr.Syslog;
import com.coradec.coralog.ctrl.impl.Logger;
import com.coradec.corasession.model.Session;
import com.coradec.coratext.model.LocalizedText;
import com.coradec.coratext.model.Text;
import org.junit.Assert;

import java.net.URI;
import java.util.Optional;

/**
 * ​​Base class of all bus tests.
 */
@SuppressWarnings({"ClassHasNoToStringMethod", "ProtectedField"})
class BasicBusTestInfrastructure extends Logger {

    public static final String SYSLOG_LEVEL = "INFORMATION";

    static {
        //noinspection ConstantConditions
        if (!SYSLOG_LEVEL.equals("INFORMATION")) Syslog.setLevel(SYSLOG_LEVEL);
    }

    @Inject private Factory<Invitation> invitationFactory;
    @Inject private Factory<Session> sessionFactory;
    @Inject private MessageQueue MQ;
    static final Text TEXT_MESSAGE_BOUNCED =
            LocalizedText.define("MessageBounced");

    protected void testNormalSetupAndShutdown(final String name, final BasicNode testee,
            final NodeState terminalState,
            final int timeoutSeconds, final Inbetween... inBetweens) throws InterruptedException {
        final Session session = sessionFactory.create();
        final BusContext dummyContext = new TestBusContext(session);
        final Sender testEnv = new TestEnvironment();
        assertThat(testee.getState(), is(UNATTACHED));
        final Invitation invitation = MQ.inject(
                invitationFactory.create(session, name, dummyContext, testEnv,
                        new Recipient[] {testee}));
        invitation.standby(timeoutSeconds, SECONDS).andThen(() -> {
            assertThat(dummyContext.contains(invitation.getMember()), is(true));
            assertThat(testee.getState(), is(terminalState));
            assertThat(testee.getMetaState(), is(UP));
        }).orElse(problem -> Assert.fail("Invitation failed with " + problem));
        for (final Inbetween inBetween : inBetweens) {
            inBetween.execute(session, testEnv, testee);
        }
        final Request dismissal = invitation.getMember().dismiss();
        dismissal.standby(timeoutSeconds, SECONDS).andThen(() -> {
            assertThat(dummyContext.contains(invitation.getMember()), is(false));
            assertThat(testee.getState(), is(DETACHED));
            assertThat(testee.getMetaState(), is(DOWN));
        }).orElse(problem -> Assert.fail("Dismissal failed with " + problem));
    }

    protected <I extends Information> I inject(I info) {
        return MQ.inject(info);
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

    private static class TestBusContext extends BasicBusContext {

        public TestBusContext(final Session session) {
            super(session);
        }

        @Override public <S extends BusService> boolean provides(final Class<? super S> type,
                final Object... args) {
            return false;
        }

        @Override public <S extends BusService> Optional<S> findService(final Class<? super S> type,
                final Object... args) {
            return Optional.empty();
        }

    }
}
