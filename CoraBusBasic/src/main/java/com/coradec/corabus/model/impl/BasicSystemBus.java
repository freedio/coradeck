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
import com.coradec.coracom.model.Voucher;
import com.coradec.coraconf.model.Property;
import com.coradec.coracore.annotation.Nullable;
import com.coradec.coractrl.com.StartStateMachineRequest;
import com.coradec.corasession.model.Session;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Basic implementation of a system bus.
 */
@SuppressWarnings("ClassHasNoToStringMethod")
public class BasicSystemBus extends BasicHub implements SystemBus {

    private static final Property<String> PROP_MACHINE_BUS_NAMING_PATTERN =
            Property.define("MachineBusNamingPattern", String.class, "M%08x");

    private final AtomicLong ID = new AtomicLong();

    @Override protected @Nullable Request onInitialize(final Session session) {
        final Request request = super.onInitialize(session);
//        add(session, "console", new ServerConsole());
        add(session, "net", new Network());
        return request;
    }

    @Override public StartStateMachineRequest shutdown(final Session session) {
        stateMachine.setTargetState(DETACHED);
        return stateMachine.start();
    }

    @Override public Voucher<String> getMachineBusId(final Session session) {
        final Voucher<String> voucher = Voucher.of(String.class,
                String.format(PROP_MACHINE_BUS_NAMING_PATTERN.value(),
                        ID.getAndAccumulate(1, (x, inc) -> (x + inc) % Long.MAX_VALUE)), this);
        voucher.succeed();
        return voucher;
    }

    @Override protected void onReady() {
        super.onReady();
        discloseStringExtensions().info("Bus ready.");
    }

}
