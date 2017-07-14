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
import com.coradec.coraconf.model.Property;
import com.coradec.coracore.time.Duration;
import com.coradec.coracore.util.SystemUtil;
import com.coradec.coractrl.com.StartStateMachineRequest;
import com.coradec.corasession.model.Session;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.concurrent.TimeUnit;

/**
 * ​​Abstract implementation of a system bus.  Use {@link #create()} to get a suitable system bus
 * instance.
 */
public class BasicSystemBus extends BasicHub implements SystemBus {

    private static final Property<Integer> PROP_SYSTEM_BUS_PORT =
            Property.define("SystemBusPort", Integer.class, 10);
    private static final Property<Duration> PROP_SYSTEM_BUS_TIMEOUT =
            Property.define("SystemBusTimeout", Duration.class, Duration.of(1, TimeUnit.SECONDS));

    public static SystemBus create() {
        SocketAddress server = null;
        try {
            server = new InetSocketAddress(SystemUtil.getLocalAddress(),
                    PROP_SYSTEM_BUS_PORT.value());
            final Socket socket = new Socket();
            socket.connect(server, (int)PROP_SYSTEM_BUS_TIMEOUT.value().toMillis());
            return new SystemBusProxy(socket);
        } catch (final IOException e) {
            if (server == null)
                throw new IllegalStateException("Localhost cannot act as a server!");
            return new LocalSystemBus(server);
        }
    }

    @Override public StartStateMachineRequest shutdown(final Session session) {
        stateMachine.setTargetState(DETACHED);
        return stateMachine.start();
    }

}
