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

import static java.nio.channels.SelectionKey.*;

import com.coradec.corabus.model.ClientConnection;
import com.coradec.corabus.view.NetworkProtocol;
import com.coradec.coracom.model.SessionEvent;
import com.coradec.coracom.model.SessionInformation;
import com.coradec.coracore.trouble.UnimplementedOperationException;

import java.net.URI;
import java.nio.channels.SocketChannel;

/**
 * ​​Basic implementation of a client connection.
 */
@SuppressWarnings("ClassHasNoToStringMethod")
public class BasicClientConnection extends AbstractNetworkConnection implements ClientConnection {

    public BasicClientConnection(final SocketChannel client, final NetworkProtocol protocol,
            final URI target) {
        super(client, protocol, target, OP_READ | OP_WRITE);
    }

    @Override protected void eventReceived(final SessionEvent event) {
        throw new UnimplementedOperationException();
    }

    @Override protected void infoReceived(final SessionInformation info) {
        throw new UnimplementedOperationException();
    }

}
