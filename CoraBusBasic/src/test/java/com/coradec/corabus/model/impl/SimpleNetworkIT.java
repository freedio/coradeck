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

import static java.util.concurrent.TimeUnit.*;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;

import com.coradec.corabus.com.Invitation;
import com.coradec.corabus.com.impl.Ping;
import com.coradec.corabus.com.impl.ShutdownRequest;
import com.coradec.corabus.model.Bus;
import com.coradec.corabus.view.BusContext;
import com.coradec.corabus.view.EchoService;
import com.coradec.coracom.model.Request;
import com.coradec.coracom.model.TextMessage;
import com.coradec.coracom.model.impl.BasicInformation;
import com.coradec.coracore.annotation.Inject;
import com.coradec.coracore.annotation.Nullable;
import com.coradec.coracore.model.Origin;
import com.coradec.coradir.model.Path;
import com.coradec.corajet.test.CoradeckJUnit4TestRunner;
import com.coradec.corasession.model.Session;
import com.coradec.coratext.model.LocalizedText;
import com.coradec.coratext.model.Text;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;

/**
 * ​​Simple network integration test.
 */
@RunWith(CoradeckJUnit4TestRunner.class)
public class SimpleNetworkIT /*extends NetworkIntegrationTestPlatform*/ {

    static final Text TEXT_NO_NETWORK_SERVICE = LocalizedText.define("NoNetworkService");

    private static boolean SETUP = false;
    private static TestClient CLIENT;

    @Inject static Bus BUS;
    @Inject static Session SESSION;

    @Test public void aa_testFindServerJar() throws IOException {
        assertThat(getServerJar().getPath().endsWith(".jar"), is(true));
    }

    @Test public void bb_testPingToServer() throws Exception {
        setupIf();
        CLIENT.ping();
    }

    @Ignore @Test public void cc_testEchoServer() throws Exception {
        setupIf();
        assertThat(CLIENT.echo("Hello"), is(equalTo("World")));
        assertThat(CLIENT.echo("What's Up?"), is(equalTo("WHAT'S UP?")));
    }

    @Ignore @Test public void dd_testSendMessageToHelloServer() {

    }

    @Ignore @Test public void ee_testInvokeExternalService() {

    }

    @Ignore @Test public void ff_testListenToServerHeartBeat() {

    }

    @Ignore @Test public void gg_testListenToExternalEvents() {

    }

    @Test public void zz_shutdown() throws InterruptedException, IOException {
        setupIf();
        CLIENT.shutdownX();
    }

    private void setupIf() throws InterruptedException, IOException {
        if (!SETUP) {
            BUS.setupExternal(SESSION, getServerJar(), 10, SECONDS);
            BUS.setup(SESSION).standby(5, SECONDS);
            Thread.sleep(2000);
            BUS.add(SESSION, Path.of("/client"), CLIENT = new TestClient());
            SETUP = true;
        }
    }

    private void shutdownIf() throws InterruptedException {
        if (SETUP) {
            BUS.shutdownExternal(SESSION, 5, SECONDS);
            BUS.shutdown(SESSION);
            SETUP = false;
        }
    }

    private File getServerJar() throws IOException {
        File file = new File(System.getProperty("user.home"));
        file = new File(file, ".m2/repository/com/coradec/coradeck/corabus-server/");
        if (!file.isDirectory()) throw new FileNotFoundException(file.getPath());
        Double maxVersion = null;
        String maxVersion$ = "";
        final File[] versions = file.listFiles((File f) -> {
            try {
                //noinspection ResultOfMethodCallIgnored
                Double.valueOf(f.getName());
                return true;
            } catch (NumberFormatException e) {
                return false;
            }
        });
        if (versions == null) throw new IOException("No versions");
        for (final File version : versions) {
            final Double v = Double.valueOf(version.getName());
            if (maxVersion == null || v > maxVersion) {
                maxVersion = v;
                maxVersion$ = version.getName();
            }
        }
        file = new File(file, maxVersion$);
        file = new File(file, "corabus-server-0.3.jar");
        if (!file.canRead()) throw new FileNotFoundException(file.getPath());
        return file;
    }

    private class TestClient extends BasicNode {

        private EchoService echoService;

        @Override
        protected @Nullable Request onAttach(final Session session, final BusContext context,
                final Invitation invitation) {
            final Request request = super.onAttach(session, context, invitation);
//            echoService = getService(EchoService.class);
            return request;
        }

        void ping() {
            inject(new Ping(SESSION, this, BUS.recipient(SESSION, "///"))).standby(5, SECONDS);
        }

        String echo(final String input) {
            return echoService.echo(input);
        }

        void shutdownX() {
            inject(new ShutdownRequest(SESSION, this, BUS.recipient(SESSION, "///"))).standby(5,
                    SECONDS);
        }
    }

    private static class TestMessage extends BasicInformation implements TextMessage {

        private static final String PROP_TEXT = "Text";

        private final String text;

        TestMessage(final Origin sender, final String text) {
            super(sender);
            this.text = text;
        }

        /**
         * Initializes a new instance of BasicInformation from the specified property map.
         *
         * @param properties the property map.
         */
        private TestMessage(final Map<String, Object> properties, final String text) {
            super(properties);
            this.text = get(String.class, PROP_TEXT);
        }

        @Override public String getContent() {
            return text;
        }

        /**
         * Collects the properties into the built-in property map in preparation for {@link
         * #getProperties()}.
         * <p>
         * Every subclass defining its own attributes should override this method and add its own
         * properties by calling {@link BasicInformation#set(String, Object)} for each of them.
         */
        @Override protected void collect() {
            super.collect();
            set(PROP_TEXT, getContent());
        }

    }

}
