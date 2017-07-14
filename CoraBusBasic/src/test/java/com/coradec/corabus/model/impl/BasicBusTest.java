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

import static com.coradec.corabus.state.ProcessState.*;
import static com.coradec.coracore.tools.hamcrest.RegexMatcher.*;
import static java.util.concurrent.TimeUnit.*;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;

import com.coradec.corabus.model.Bus;
import com.coradec.corabus.model.BusApplication;
import com.coradec.coracore.annotation.Inject;
import com.coradec.coracore.model.Factory;
import com.coradec.corajet.test.CoradeckJUnit4TestRunner;
import com.coradec.corasession.model.Session;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * ​​Test suite for the bus infrastructure
 */
@RunWith(CoradeckJUnit4TestRunner.class)
public class BasicBusTest {

    private final BusApplication application = new BasicBusApplication();
    @Inject private Bus bus;
    @Inject private Factory<Session> sessionFactory;

    @Test public void simpleSetupAndShutdownShouldSucceed() throws InterruptedException {
        final Session session = sessionFactory.create();
        bus.add(session, "SampleApplication", application).standby(2, SECONDS);
        assertThat(application.getState(), is(STARTED));
        assertThat(application.getIdentifier().toString(),
                matches("corabus://[^/]+/[^/]+/apps/SampleApplication"));
    }

}
