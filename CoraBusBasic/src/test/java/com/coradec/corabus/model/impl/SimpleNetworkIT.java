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

import static com.coradec.corabus.model.impl.NetworkIntegrationTestPlatform.TestResult.*;
import static com.coradec.coracore.util.NetworkUtil.*;
import static java.util.concurrent.TimeUnit.*;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;

import com.coradec.corabus.view.NetworkService;
import com.coradec.coracom.model.Information;
import com.coradec.coracom.model.Sender;
import com.coradec.coracom.model.TextMessage;
import com.coradec.coracom.model.Voucher;
import com.coradec.coracom.model.impl.BasicInformation;
import com.coradec.corajet.test.CoradeckJUnit4TestRunner;
import com.coradec.coratext.model.LocalizedText;
import com.coradec.coratext.model.Text;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.net.SocketAddress;
import java.util.concurrent.Semaphore;

/**
 * ​​Simple network integration test.
 */
@RunWith(CoradeckJUnit4TestRunner.class)
public class SimpleNetworkIT extends NetworkIntegrationTestPlatform {

    static final Text TEXT_NO_NETWORK_SERVICE = LocalizedText.define("NoNetworkService");

    @Test public void externalServerSetupAndShutdown() throws IOException {
        startExternalBus();
        shutdownExternalBus();
    }

    @Ignore @Test public void clientServerCommunicationWithOneClientAndServer() throws Exception {
        final SocketAddress clientSocket = getLocalAddress(10);
        TestClient client = new TestClient(clientSocket);
        TestServer server = new TestServer();
        launchServerSetup(server);
        launchClientSetup(client);
        client.standby();
        assertThat(communication, is(SUCCESSFUL));
    }

    private class TestClient extends BasicBusProcess implements Client {

        private final Semaphore resultLock = new Semaphore(0);
        private final SocketAddress socket;
        NetworkService network;

        TestClient(SocketAddress socket) {
            this.socket = socket;
        }

        /**
         * Waits for the client to finish whatever it has to do.
         */
        void standby() throws InterruptedException {
            resultLock.acquire();
        }

        @Override public void run() {
            network.send(new TestMessage(this, "Hello!"));
            final Voucher<Information> response = network.receive();
            try {
                response.standBy(5, SECONDS);
                if (response instanceof TextMessage) {
                    assertThat(((TextMessage)response).getContent(), is(equalTo("Welcome!")));
                } else Assert.fail("Answer was not a text message!");
            } catch (Exception e) {
                error(e);
                Assert.fail("Failed with " + e + "!");
            }
        }

    }

    private class TestServer extends BasicNode implements Server {

    }

    private class TestMessage extends BasicInformation implements TextMessage {

        private final String text;

        TestMessage(final Sender sender, final String text) {
            super(sender);
            this.text = text;
        }

        @Override public String getContent() {
            return text;
        }

    }

}
