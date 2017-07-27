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

import com.coradec.corabus.com.impl.ShutdownRequest;
import com.coradec.corabus.model.Bus;
import com.coradec.corabus.model.BusNode;
import com.coradec.coracom.model.Message;
import com.coradec.coracom.model.Sender;
import com.coradec.coraconf.model.Property;
import com.coradec.coracore.annotation.Inject;
import com.coradec.coradir.model.Path;
import com.coradec.coralog.ctrl.impl.Logger;
import com.coradec.corasession.model.Session;
import com.coradec.coratext.model.LocalizedText;
import com.coradec.coratext.model.Text;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;

/**
 * Implementation of the common platform for network integration tests.
 */
@SuppressWarnings({"ProtectedField", "ClassHasNoToStringMethod"})
public class NetworkIntegrationTestPlatform extends Logger implements Sender {

    private static final Property<File> PROP_JAR_LOCATION =
            Property.define("JarLocation", File.class);
    private static final Text TEXT_STARTING_SERVER = LocalizedText.define("StartingServer");
    private static final Text TEXT_STARTING_CLIENT = LocalizedText.define("StartingClient");
    private static final Text TEXT_CLIENT_STARTED = LocalizedText.define("ClientStarted");
    private static final Text TEXT_SERVER_STARTED = LocalizedText.define("ServerStarted");
    private static final Text TEXT_MESSAGE_BOUNCED = LocalizedText.define("MessageBounced");

    @Inject Bus bus;
    @Inject Session session;
    protected Communication communication = new Communication();

    protected void launchClientSetup(final Client client) throws Exception {
        info(TEXT_STARTING_CLIENT);
        if (communication.isOK()) try {
            bus.add(session, Path.of("/TestClient"), client).standby();
        } catch (InterruptedException e) {
            error(e);
            communication.fails(e);
        }
        if (communication.isOK()) info(TEXT_CLIENT_STARTED);
    }

    protected void launchServerSetup(final Server server) throws Exception {
        info(TEXT_STARTING_SERVER);
        startExternalBus();
        bus.add(session, Path.of("/TestServer"), server).standby();
        info(TEXT_SERVER_STARTED);
    }

    protected void startExternalBus() throws IOException {
        if (communication.isOK()) {
            final File pkg = PROP_JAR_LOCATION.value();
            debug("Server package pkg: \"%s\"", pkg);
            final Process process = Runtime.getRuntime().exec(String.format("java -jar %s", pkg));
            StringBuilder stdout = new StringBuilder();
            StringBuilder stderr = new StringBuilder();
            boolean ready = false, failed = false;
            long then = System.currentTimeMillis();
            try (BufferedReader in = new BufferedReader(
                    new InputStreamReader(process.getInputStream())); //
                 BufferedReader err = new BufferedReader(
                         new InputStreamReader(process.getErrorStream()))) {
                do {
                    while (in.ready()) stdout.append((char)in.read());
                    while (err.ready()) stderr.append((char)err.read());
                    if (stdout.toString().contains("Bus system ready.")) ready = true;
                    if (stderr.toString().contains("Server failed.")) failed = true;
                } while (!ready && !failed && System.currentTimeMillis() - then < 5000);
                debug("Stdout: \"%s\"", stdout);
                debug("Stderr: \"%s\"", stderr);
                if (failed) throw new IOException("Server setup failed!");
                if (!ready) throw new IOException("Server setup timed out!");
            }
        }
    }

    protected void shutdownExternalBus() {
        bus.send(new ShutdownRequest(session, this, bus.get(session, Path.of("/"))))
           .standby(5, SECONDS);
    }

    @Override public void bounce(final Message message) {
        error(TEXT_MESSAGE_BOUNCED, message);
    }

    @Override public URI toURI() {
        return URI.create(represent());
    }

    @Override public String represent() {
        return getClass().getSimpleName();
    }

    enum TestResult {
        ONGOING,
        FAILED,
        SUCCESSFUL
    }

    @SuppressWarnings("ClassHasNoToStringMethod")
    private class Communication {

        Communication() {
            this.state = TestResult.ONGOING;
        }

        private TestResult state;
        private Throwable problem;

        void fails(final Throwable problem) {
            error(problem);
            state = TestResult.FAILED;
            this.problem = problem;
        }

        boolean isOK() {
            return state == TestResult.ONGOING;
        }

    }

    protected interface Client extends BusNode {

    }

    protected interface Server extends BusNode {

    }
}
