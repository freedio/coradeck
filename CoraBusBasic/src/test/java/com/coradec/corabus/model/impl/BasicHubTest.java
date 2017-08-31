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
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;

import com.coradec.coracom.model.Recipient;
import com.coradec.coracore.annotation.Inject;
import com.coradec.coracore.model.Origin;
import com.coradec.corajet.test.CoradeckJUnit4TestRunner;
import com.coradec.corasession.model.Session;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * ​​Test suite for the BasicHub.
 */
@RunWith(CoradeckJUnit4TestRunner.class)
public class BasicHubTest extends BasicBusTestInfrastructure {

    final BasicHub testee = new BasicHub();
    final BasicNode member = new BasicNode();
    @Inject Session initialSession;

    @Test public void normalSetupAndShutdownShouldWork() throws InterruptedException {
        testNormalSetupAndShutdown("proc1", testee, LOADED, 5);
    }

    @Test public void normalSetupAndShutdownWithMemberPreloadingShouldWork()
            throws InterruptedException {
        testee.add(initialSession, "member", member);
        testNormalSetupAndShutdown("proc2", testee, LOADED, 8);
    }

    @Test public void normalSetupAndShutdownWithMemberPostloadingShouldWork()
            throws InterruptedException {
        assertThat(member.getState(), is(UNATTACHED));
        assertThat(member.getMetaState(), is(DOWN));
        testNormalSetupAndShutdown("proc3", testee, LOADED, 10, new LoadMember(), new WaitAbit());
        assertThat(member.getState(), is(DETACHED));
        assertThat(member.getMetaState(), is(DOWN));
    }

    private class LoadMember extends Inbetween {

        @Override protected void execute(final Session session, final Origin sender,
                final Recipient recipient) throws InterruptedException {
            testee.add(initialSession, "member", member);
        }
    }

    private class WaitAbit extends Inbetween {

        @Override protected void execute(final Session session, final Origin sender,
                final Recipient recipient) throws InterruptedException {
            Thread.sleep(1000);
        }
    }
}
