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

package com.coradec.corabus.server.main;

import static com.coradec.corabus.state.NodeState.*;

import com.coradec.corabus.com.StateAchievedEvent;
import com.coradec.corabus.model.Bus;
import com.coradec.corabus.model.impl.ServerConsole;
import com.coradec.coracom.ctrl.MessageQueue;
import com.coradec.coracom.ctrl.Observer;
import com.coradec.coracom.model.Information;
import com.coradec.coracore.annotation.Inject;
import com.coradec.coralog.ctrl.impl.Logger;
import com.coradec.corasession.model.Session;

import java.util.concurrent.Semaphore;

/**
 * ​​Runs the standalone system server.
 */
@SuppressWarnings("ClassHasNoToStringMethod")
public final class Server extends Logger implements Observer {

    @Inject private static MessageQueue CMQ;

    private final String[] args;
    private final Semaphore lock = new Semaphore(0);
    private ServerConsole serverConsole;
    @Inject private Bus bus;
    @Inject private Session session;

    private Server(final String[] args) {
        this.args = args;
    }

    public static void main(String[] args) {
        new Server(args).launch();
    }

    private void launch() {
        CMQ.subscribe(this);
        bus.setup();
        try {
            lock.acquire();
        } catch (InterruptedException e) {
            // interrupted → time to go
        } finally {
            CMQ.unsubscribe(this);
        }
    }

    @Override public boolean notify(final Information info) {
        StateAchievedEvent event = (StateAchievedEvent)info;
        if (event.getAchievedState() == DETACHED) {
            lock.release();
            return true;
        }
        return false;
    }

    @Override public boolean wants(final Information info) {
        return info instanceof StateAchievedEvent && info.getOrigin() == serverConsole;
    }
}
