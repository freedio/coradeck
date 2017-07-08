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

import static com.coradec.corabus.model.ProcessState.*;
import static java.util.concurrent.TimeUnit.*;
import static org.hamcrest.CoreMatchers.*;

import com.coradec.corabus.com.Resumption;
import com.coradec.corabus.com.Suspension;
import com.coradec.coracom.model.Recipient;
import com.coradec.coracom.model.Sender;
import com.coradec.coracore.annotation.Inject;
import com.coradec.coracore.ctrl.Factory;
import com.coradec.corajet.test.CoradeckJUnit4TestRunner;
import com.coradec.corasession.model.Session;
import org.hamcrest.MatcherAssert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * ​​Test suite for the BasicBusProcess.
 */
@SuppressWarnings("WeakerAccess")
@RunWith(CoradeckJUnit4TestRunner.class)
public class BasicBusApplicationTest extends BasicBusTest {

    @Inject Factory<Suspension> suspensionFactory;
    @Inject Factory<Resumption> resumptionFactory;

    final BasicBusApplication testee = new BasicBusApplication();

    @Test public void normalSetupAndShutdownShouldWork() throws InterruptedException {
        testNormalSetupAndShutdown(testee, STARTED);
    }

    @Test public void normalSuspendAndResumeShouldWork() throws InterruptedException {
        testNormalSetupAndShutdown(testee, STARTED, new SuspendAndResumeTest());
    }

    private class SuspendAndResumeTest extends Inbetween {

        @Override protected void execute(final Session session, final Sender sender,
                                         final Recipient... recipients)
                throws InterruptedException {
            Suspension suspension = inject(suspensionFactory.create(session, sender, recipients));
            suspension.standby(1, SECONDS)
                      .andThen(() -> MatcherAssert.assertThat(testee.getState(), is(SUSPENDED)));
            Resumption resumption = inject(resumptionFactory.create(session, sender, recipients));
            resumption.standby(1, SECONDS)
                      .andThen(() -> MatcherAssert.assertThat(testee.getState(), is(STARTED)));
        }
    }

}
