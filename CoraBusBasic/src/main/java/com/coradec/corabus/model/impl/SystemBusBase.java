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

import static com.coradec.corabus.state.NodeState.*;

import com.coradec.corabus.model.SystemBus;
import com.coradec.coracom.model.Request;
import com.coradec.coracore.annotation.Nullable;
import com.coradec.coractrl.com.StartStateMachineRequest;
import com.coradec.corasession.model.Session;

/**
 * Implementation base of a system bus.
 */
public abstract class SystemBusBase extends BasicHub implements SystemBus {

    @Override protected @Nullable Request onInitialize(final Session session) {
        final Request request = super.onInitialize(session);
        add(session, "net", new Network(startServer()));
        add(session, "app", new BasicApplicationBus());
        add(session, "buscon", new BusConsole());
        return request;
    }

    @Override public StartStateMachineRequest shutdown(final Session session) {
        stateMachine.setTargetState(DETACHED);
        return stateMachine.start();
    }

    /**
     * Determines whether to also start the network server.
     *
     * @return {@code true} if the server has to be started, {@code false} if not.
     */
    protected abstract boolean startServer();

}
