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
import com.coradec.corabus.model.ApplicationBus;
import com.coradec.corabus.model.Bus;
import com.coradec.corabus.model.BusApplication;
import com.coradec.corabus.model.BusHub;
import com.coradec.corabus.model.BusNode;
import com.coradec.corabus.model.MachineBus;
import com.coradec.corabus.model.MachineService;
import com.coradec.corabus.model.SystemBus;
import com.coradec.corabus.trouble.MountPointUndefinedException;
import com.coradec.corabus.trouble.NodeNotFoundException;
import com.coradec.corabus.view.BusContext;
import com.coradec.corabus.view.BusService;
import com.coradec.corabus.view.Member;
import com.coradec.corabus.view.impl.BasicBusContext;
import com.coradec.coracom.ctrl.MessageQueue;
import com.coradec.coracom.model.Information;
import com.coradec.coracom.model.Message;
import com.coradec.coracom.model.Recipient;
import com.coradec.coracom.model.Request;
import com.coradec.coracom.model.Sender;
import com.coradec.coracore.annotation.Implementation;
import com.coradec.coracore.annotation.Inject;
import com.coradec.coracore.model.Factory;
import com.coradec.coracore.util.NetworkUtil;
import com.coradec.coractrl.ctrl.SysControl;
import com.coradec.coradir.model.Path;
import com.coradec.coralog.ctrl.impl.Logger;
import com.coradec.corasession.model.Session;
import com.coradec.coratext.model.LocalizedText;
import com.coradec.coratext.model.Text;

import java.net.URI;
import java.util.Optional;

/**
 * ​​Basic implementation of the bus infrastructure (façade).
 */
@SuppressWarnings("ClassHasNoToStringMethod")
@Implementation(SINGLETON)
public class BasicBus extends Logger implements Bus, Sender {

    private static final Text TEXT_MESSAGE_BOUNCED = LocalizedText.define("MessageBounced");

    @Inject private static Factory<Invitation> INVITATION;

    @Inject private MessageQueue MQ;
    @Inject private ApplicationBus appBus;
    @Inject private MachineBus machBus;
    @Inject private Session setupSession;
    private final SystemBus sysBus;
    private final BusContext rootContext;
    private Member sysBusMember;
    private boolean initialized;

    public BasicBus() {
        rootContext = new RootBusContext(setupSession);
        sysBus = SystemBus.create();
        initialized = false;
    }

    private void init() {
        if (!initialized) {
            final Invitation invitation = INVITATION.create(setupSession, "", rootContext, this,
                    new Recipient[] {sysBus});
            MQ.inject(invitation)
              .andThen(() -> sysBus.add(setupSession, NetworkUtil.getLocalMachineId(), machBus)
                                   .andThen(() -> machBus.add(setupSession, "apps", appBus)))
              .andThen(() -> sysBusMember = invitation.getMember());
            SysControl.onShutdown(() -> {
                try {
                    sysBus.shutdown(setupSession).standby();
                } catch (InterruptedException e) {
                    error(e);
                }
            });
            initialized = true;
        }
    }

    @Override public Request add(final Session session, final Path path, final BusNode node) {
        init();
        if (path.isAbsolute()) return getSystemBus().add(session, path.tail(), node);
        else if (node instanceof BusApplication)
            return getApplicationBus().add(session, path, node);
        else if (node instanceof MachineService) return getMachineBus().add(session, path, node);
        throw new MountPointUndefinedException(path, node);
    }

    @Override public void setup() {
        init();
    }

    @Override public void shutdown() {
        if (initialized) try {
            sysBusMember.dismiss().standby();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        debug("Exiting system.");
        System.exit(0);
    }

    @Override public BusHub getRoot() {
        return sysBus;
    }

    @Override public Optional<BusNode> lookup(final Session session, final Path path) {
        return getRoot().lookup(session, path.isAbsolute() ? path.tail() : path);
    }

    @Override public BusNode get(final Session session, final Path path)
            throws NodeNotFoundException {
        init();
        return lookup(session, path).orElseThrow(() -> new NodeNotFoundException(path));
    }

    @Override public boolean has(final Session session, final Path path) {
        init();
        return getRoot().has(session, path.isAbsolute() ? path.tail() : path);
    }

    @Override public <I extends Information> I send(final I information) {
        init();
        return MQ.inject(information);
    }

    private SystemBus getSystemBus() {
        return sysBus;
    }

    private MachineBus getMachineBus() {
        return machBus;
    }

    private ApplicationBus getApplicationBus() {
        return appBus;
    }

    @Override public String represent() {
        return "CoraBus";
    }

    @Override public URI toURI() {
        return URI.create(represent());
    }

    @Override public void bounce(final Message message) {
        error(TEXT_MESSAGE_BOUNCED, message);
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

}
