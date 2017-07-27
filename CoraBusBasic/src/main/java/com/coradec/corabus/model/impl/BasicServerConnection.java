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

import com.coradec.corabus.model.ServerConnection;
import com.coradec.coracom.ctrl.ChannelReader;
import com.coradec.coracom.ctrl.ChannelWriter;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.Selector;
import java.nio.channels.WritableByteChannel;

/**
 * ​​Basic implementation of a server connection.
 */
@SuppressWarnings("ClassHasNoToStringMethod")
public class BasicServerConnection extends AbstractConnection implements ServerConnection {

    private final ChannelReader reader;
    private final ChannelWriter writer;

    public BasicServerConnection(final Selector selector, final SocketAddress socket,
            final ChannelReader reader, final ChannelWriter writer) {
        super(selector, socket);
        this.reader = reader;
        this.writer = writer;
    }

    @Override protected boolean read(final ReadableByteChannel peer) throws IOException {
        return reader.read(peer);
    }

    @Override protected boolean write(final WritableByteChannel peer) throws IOException {
        return writer.write(peer);
    }

}
