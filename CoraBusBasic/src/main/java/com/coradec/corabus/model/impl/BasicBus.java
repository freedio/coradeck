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

import static com.coradec.coracore.model.Scope.*;

import com.coradec.corabus.com.Invitation;
import com.coradec.corabus.com.impl.BasicOutboundMessage;
import com.coradec.corabus.com.impl.BusSystemTerminatedEvent;
import com.coradec.corabus.com.impl.ShutdownRequest;
import com.coradec.corabus.model.ApplicationBus;
import com.coradec.corabus.model.Bus;
import com.coradec.corabus.model.BusApplication;
import com.coradec.corabus.model.BusHub;
import com.coradec.corabus.model.BusNode;
import com.coradec.corabus.model.SystemBus;
import com.coradec.corabus.protocol.handler.CMP_Handler;
import com.coradec.corabus.trouble.MemberNotFoundException;
import com.coradec.corabus.trouble.MountPointUndefinedException;
import com.coradec.corabus.trouble.NodeNotFoundException;
import com.coradec.corabus.view.BusContext;
import com.coradec.corabus.view.BusService;
import com.coradec.corabus.view.Member;
import com.coradec.corabus.view.impl.BasicBusContext;
import com.coradec.coracom.ctrl.MessageQueue;
import com.coradec.coracom.model.Message;
import com.coradec.coracom.model.Recipient;
import com.coradec.coracom.model.Request;
import com.coradec.coraconf.model.Property;
import com.coradec.coracore.annotation.Implementation;
import com.coradec.coracore.annotation.Inject;
import com.coradec.coracore.annotation.Nullable;
import com.coradec.coracore.model.Factory;
import com.coradec.coracore.model.Origin;
import com.coradec.coracore.model.Representable;
import com.coradec.coracore.time.Duration;
import com.coradec.coractrl.ctrl.SysControl;
import com.coradec.coradir.model.Path;
import com.coradec.coralog.ctrl.impl.Logger;
import com.coradec.corasession.model.Session;
import com.coradec.coratext.model.LocalizedText;
import com.coradec.coratext.model.Text;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * ​​Basic implementation of the bus infrastructure (façade).
 */
@SuppressWarnings("ClassHasNoToStringMethod")
@Implementation(SINGLETON)
public class BasicBus extends Logger implements Bus, Origin {

    private static final Text TEXT_MESSAGE_BOUNCED = LocalizedText.define("MessageBounced");
    private static final Property<Duration> PROP_SYSTEM_BUS_PROBE_TIMEOUT =
            Property.define("SystemBusProbeTimeout", Duration.class,
                    Duration.of(1, TimeUnit.SECONDS));

    @Inject private static Factory<Invitation> INVITATION;

    @Inject MessageQueue MQ;
    @Inject private Session setupSession;
    private SystemBus systemBus;
    private final BusContext rootContext;
    private Member sysBusMember;
    boolean initialized;
    Invitation invitation;
    private boolean startedExternal;
    private BusHub applicationBus;

    public BasicBus() {
        rootContext = new RootBusContext(setupSession);
        initialized = false;
    }

    void init() {
        if (!initialized) {
            final InetSocketAddress socketAddr =
                    new InetSocketAddress("localhost", CMP_Handler.PROP_STANDARD_PORT.value());
            Socket socket = new Socket();
            final int timeout = (int)PROP_SYSTEM_BUS_PROBE_TIMEOUT.value().toMillis();
            debug("Connecting to socket %s (waiting %d ms)", socketAddr, timeout);
            try {
                socket.connect(socketAddr, timeout);
                debug("Successfully connected → setting up system bus proxy.");
                systemBus = new SystemBusProxy(socket);
            } catch (IOException e) {
                debug("Failed to connect → setting up local system bus.");
                systemBus = new CentralSystemBus();
            }
            invitation = INVITATION.create(setupSession, this, systemBus, "", rootContext);
            final String mbusid;
            try {
                MQ.inject(invitation)
                  .andThen(() -> sysBusMember = invitation.getMember())
                  .standby();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            SysControl.onShutdown(() -> {
                try {
                    systemBus.shutdown(setupSession).standby();
                } catch (InterruptedException e) {
                    error(e);
                }
            });
            initialized = true;
        }
    }

    @Override public Request add(final Session session, final Path path, final BusNode node) {
        init();
        if (path.isTranscendent()) return getSystemBus().add(session, path, node);
        else if (path.isAbsolute()) return getSystemBus().add(session, path.localize(), node);
        else if (node instanceof BusApplication)
            return getApplicationBus(session).add(session, path, node);
        throw new MountPointUndefinedException(path, node);
//        return getApplicableServiceLevel().add(session, path, node);
    }

    @Override public Request setup(final Session session) {
        init();
        return invitation;
    }

    @Override public void shutdown(final Session session) {
        if (initialized) try {
            sysBusMember.dismiss().standby();
            MQ.inject(new BusSystemTerminatedEvent(this));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        debug("Bus system shut down.");
    }

    @Override public void setupExternal(final Session session, final File serverJar, long amount,
            TimeUnit unit) throws IOException {
        long timeout = unit.toMillis(amount);
        if ((startedExternal = !runs())) {
            debug("Server package pkg: \"%s\"", serverJar);
            final Process process =
                    Runtime.getRuntime().exec(String.format("java -jar %s", serverJar.getPath()));
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
                    if (stdout.toString().contains("Bus ready.")) ready = true;
                    if (stderr.toString().contains("Server failed.")) failed = true;
                } while (!ready && !failed && System.currentTimeMillis() - then < timeout);
                debug("Stdout: \"%s\"", stdout.toString().replace("\n", "\n\t"));
                debug("Stderr: \"%s\"", stderr.toString().replace("\n", "\n\t"));
                if (failed) throw new IOException("Server setup failed!");
                if (!ready) throw new IOException("Server setup timed out!");
            }
        }
    }

    @Override
    public void shutdownExternal(final Session session, final long amount, final TimeUnit unit)
            throws InterruptedException {
        if (startedExternal) {
            MQ.inject(new ShutdownRequest(session, this, recipient(session, "///")))
              .standby(amount, unit);
        }
    }

    @Override public BusHub getRoot() {
        init();
        return systemBus;
    }

    @Override public Optional<BusNode> lookup(final Session session, final Path path) {
        init();
        return getRoot().lookup(session, path.isAbsolute() ? path.tail() : path);
    }

    @Override public BusNode get(final Session session, final Path path)
            throws NodeNotFoundException {
        return lookup(session, path).orElseThrow(() -> new NodeNotFoundException(path));
    }

    @Override public boolean has(final Session session, final Path path) {
        init();
        return getRoot().has(session, path.isAbsolute() ? path.tail() : path);
    }

    @Override public Origin sender(final Path path) {
        return new NetworkSender(path);
    }

    @Override public Origin sender(final String path) {
        return sender(Path.of(path));
    }

    @Override public Recipient recipient(final Session session, final Path path) {
        return new NetworkRecipient(session, path);
    }

    @Override public Recipient recipient(final Session session, final String path) {
        return new NetworkRecipient(session, Path.of(path));
    }

    @Override public boolean runs() {
        final InetSocketAddress socketAddr =
                new InetSocketAddress("localhost", CMP_Handler.PROP_STANDARD_PORT.value());
        Socket socket = new Socket();
        final int timeout = (int)PROP_SYSTEM_BUS_PROBE_TIMEOUT.value().toMillis();
        debug("Connecting to socket %s (waiting %d ms)", socketAddr, timeout);
        try {
            socket.connect(socketAddr, timeout);
            debug("Successfully connected → bus system is already up.");
            return true;
        } catch (IOException e) {
            debug("Failed to connect → bus system is down.");
            return false;
        }
    }

    @Override public String getProtocolScheme() {
        return CMP_Handler.SCHEME;
    }

    private SystemBus getSystemBus() {
        return systemBus;
    }

    @Override public String represent() {
        return "CoraBus";
    }

    @Override public URI toURI() {
        return URI.create(represent());
    }

    @Override public Recipient recipientOf(final Session session, final String id) {
        return get(session, Path.of(id));
    }

    @Override public Origin originOf(@Nullable final Session session, final String id) {
        return Path.of(id);
    }

    private BusHub getApplicationBus(final Session session) {
        if (applicationBus == null) applicationBus = (BusHub)get(session, ApplicationBus.PATH);
        return applicationBus;
    }

    private class RootBusContext extends BasicBusContext {

        RootBusContext(final Session session) {
            super(session);
        }

        @Override public Path getPath(final String name) {
            return Path.of(name);
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

    private class NetworkSender implements Origin, Representable {

        private final Path path;

        public NetworkSender(final Path path) {
            this.path = path;
        }

        @Override public URI toURI() {
            return path.transcend().toURI(CMP_Handler.SCHEME);
        }

        @Override public String represent() {
            return path.represent();
        }
    }

    private class NetworkRecipient implements Recipient, Representable {

        private final Session session;
        private final Path path;

        public NetworkRecipient(final Session session, final Path path) {
            this.session = session;
            this.path = path;
        }

        @Override public void onMessage(final Message message) {
            init();
            final Path clientPath = Path.of("/net/client");
            final BusNode client = lookup(session, clientPath).orElseThrow(
                    () -> new MemberNotFoundException(clientPath));
            debug("Sending message %s to %s (%s)", message, client, clientPath.represent());
            MQ.inject(new BasicOutboundMessage(session, message, path, client));
        }

        /**
         * Returns the recipient ID.
         *
         * @return the recipient ID.
         */
        @Override public String getRecipientId() {
            return path.represent();
        }

        @Override public String represent() {
            return path.transcend().toURI(CMP_Handler.SCHEME).toString();
        }
    }

}
