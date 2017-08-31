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

import com.coradec.corabus.com.impl.Ping;
import com.coradec.corabus.com.impl.ShutdownRequest;
import com.coradec.corabus.model.SystemBus;
import com.coradec.coracom.model.Message;
import com.coradec.coracom.model.Recipient;
import com.coradec.coracom.model.impl.BasicCommand;
import com.coradec.coraconf.model.Property;
import com.coradec.coracore.model.Origin;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Basic implementation of a system bus.
 */
@SuppressWarnings("ClassHasNoToStringMethod")
public class CentralSystemBus extends SystemBusBase implements SystemBus {

    private static final Property<String> PROP_MACHINE_BUS_NAMING_PATTERN =
            Property.define("MachineBusNamingPattern", String.class, "M%08x");

    private final AtomicLong ID = new AtomicLong();

    public <R extends Message> CentralSystemBus() {
        addRoute(Ping.class, this::pong);
        addRoute(ShutdownRequest.class, this::doShutdown);
        approve(TerminateSystemCommand.class);
    }

    private void doShutdown(final ShutdownRequest request) {
        request.succeed();
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        inject(shutdown(request.getSession()).andThen(new TerminateSystemCommand(this, this)));
    }

    private void pong(final Ping ping) {
        ping.succeed();
    }

    @Override protected void onReady() {
        super.onReady();
        discloseStringExtensions().info("Bus ready.");
    }

    @Override protected boolean startServer() {
        return true;
    }

    private class TerminateSystemCommand extends BasicCommand {

        /**
         * Initializes a new instance of TerminateSystemCommand with the specified sender and
         * recipient.
         *
         * @param sender    the sender.
         * @param recipient the recipient.
         */
        protected TerminateSystemCommand(final Origin sender, final Recipient recipient) {
            super(sender, recipient);
        }

        @Override public void execute() {
            discloseStringExtensions().info("Exit System.");
            succeed();
            System.exit(0);
        }
    }
}
