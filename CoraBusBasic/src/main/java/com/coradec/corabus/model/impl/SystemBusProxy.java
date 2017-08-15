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

import com.coradec.corabus.com.impl.BasicNetworkVoucher;
import com.coradec.corabus.model.Bus;
import com.coradec.corabus.model.SystemBus;
import com.coradec.coracom.model.Request;
import com.coradec.coracom.model.Sender;
import com.coradec.coracom.model.Voucher;
import com.coradec.coracore.annotation.Inject;
import com.coradec.coracore.annotation.Nullable;
import com.coradec.coracore.model.GenericType;
import com.coradec.coracore.util.CollectionUtil;
import com.coradec.coractrl.com.StartStateMachineRequest;
import com.coradec.corasession.model.Session;

import java.net.Socket;

/**
 * ​​A proxy for the real system bus.
 */
@SuppressWarnings("ClassHasNoToStringMethod")
public class SystemBusProxy extends BasicHub implements SystemBus {

    private final Socket socket;
    @Inject Bus bus;

    public SystemBusProxy(final Socket socket) {
        this.socket = socket;
    }

    @Override protected @Nullable Request onInitialize(final Session session) {
        final Request request = super.onInitialize(session);
        return add(session, "net", new Network()).andThen(
                add(session, "net/client", new NetworkClient())).and(request);
    }

    @Override public StartStateMachineRequest shutdown(final Session session) {
        stateMachine.setTargetState(DETACHED);
        return stateMachine.start();
    }

    @Override public Voucher<String> getMachineBusId(final Session session) {
        return inject(new GetMachineBusIdVoucher(session, this));
    }

    private class GetMachineBusIdVoucher extends BasicNetworkVoucher<String> {

        public GetMachineBusIdVoucher(final Session session, final Sender sender) {
            super(session, "GET", GenericType.of(String.class),
                    CollectionUtil.mapOf("ITEM:MACHINEBUSID"), null, sender, bus.recipient("///"));
        }
    }
}
