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

package com.coradec.corabus.com.impl;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;

import com.coradec.coracom.ctrl.RecipientResolver;
import com.coradec.coracom.model.Message;
import com.coradec.coracom.model.Recipient;
import com.coradec.coracore.annotation.Inject;
import com.coradec.coracore.annotation.Nullable;
import com.coradec.coracore.model.Origin;
import com.coradec.coracore.model.impl.URIgin;
import com.coradec.corajet.test.CoradeckJUnit4TestRunner;
import com.coradec.corasession.model.Session;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.net.URI;
import java.util.Map;

@RunWith(CoradeckJUnit4TestRunner.class)
public class BasicNetworkRequestTest implements RecipientResolver {

    @Inject Session session;
    private final Origin origin = new URIgin(URI.create("/test"));
    Recipient recipient = new TestRecipient();

    @Test public void deSerializiationTest() {
        RecipientResolver.register(this);
        try {
            final BasicNetworkRequest request1 =
                    new BasicNetworkRequest(session, origin, recipient, "DO IT!");
            final Map<String, Object> properties = request1.getProperties();
            final BasicNetworkRequest request2 = new BasicNetworkRequest(properties);
            assertThat(request2.getCommand(), is(equalTo(request1.getCommand())));
            assertThat(request2.getRecipient(), is(equalTo(request1.getRecipient())));
        } finally {
            RecipientResolver.unregister(this);
        }
    }

    @Override public Recipient recipientOf(@Nullable final Session session, final String id) {
        if (!"Recipient#1".equals(id)) throw new IllegalArgumentException(id);
        return recipient;
    }

    private class TestRecipient implements Recipient {

        @Override public void onMessage(final Message message) {

        }

        @Override public String getRecipientId() {
            return "Recipient#1";
        }
    }

    private class TestResolver implements RecipientResolver {

        @Override public Recipient recipientOf(final Session session, final String id) {
            return recipient;
        }

    }

}
