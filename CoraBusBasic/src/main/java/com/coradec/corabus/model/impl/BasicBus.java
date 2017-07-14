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
import com.coradec.corabus.model.BusNode;
import com.coradec.corabus.model.MachineBus;
import com.coradec.corabus.model.SystemBus;
import com.coradec.corabus.trouble.MountPointUndefinedException;
import com.coradec.corabus.view.BusContext;
import com.coradec.corabus.view.impl.BasicBusContext;
import com.coradec.coracom.ctrl.MessageQueue;
import com.coradec.coracom.model.Message;
import com.coradec.coracom.model.Recipient;
import com.coradec.coracom.model.Request;
import com.coradec.coracom.model.Sender;
import com.coradec.coracore.annotation.Implementation;
import com.coradec.coracore.annotation.Inject;
import com.coradec.coracore.model.Factory;
import com.coradec.coracore.util.SystemUtil;
import com.coradec.coractrl.ctrl.SysControl;
import com.coradec.coradir.model.Path;
import com.coradec.coralog.ctrl.impl.Logger;
import com.coradec.corasession.model.Session;
import com.coradec.coratext.model.LocalizedText;
import com.coradec.coratext.model.Text;

import java.net.URI;

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

    public BasicBus() {
        rootContext = new RootBusContext();
        sysBus = SystemBus.create();
        MQ.inject(INVITATION.create(setupSession, "", rootContext, this, new Recipient[] {sysBus}))
          .andThen(() -> sysBus.add(setupSession, SystemUtil.getLocalMachineId(), machBus)
                               .andThen(() -> machBus.add(setupSession, "apps", appBus)));
        SysControl.onShutdown(() -> {
            try {
                sysBus.shutdown(setupSession).standby();
            } catch (InterruptedException e) {
                error(e);
            }
        });
    }

    @Override public Request add(final Session session, final String name, final BusNode node) {
        if (node instanceof BusApplication) {
            return getApplicationBus().add(session, name, node);
        }
        throw new MountPointUndefinedException(name, node);
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

        @Override public Path getPath(final String name) {
            return Path.of(name);
        }
    }

}
