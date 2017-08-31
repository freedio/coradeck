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

package com.coradec.corabus.view;

import com.coradec.coracom.model.SessionInformation;
import com.coradec.coracore.annotation.Nullable;
import com.coradec.coracore.model.GenericType;
import com.coradec.coradir.model.Path;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.util.UUID;

/**
 * Interprets incoming and outgoing messages and the relation between them.
 */
public interface NetworkProtocol extends BusService {

    /**
     * Returns the “well-known” or standard port of the protocol.
     *
     * @return the “well-known” or standard port of the protocol.
     */
    int getStandardPort();

    /**
     * Returns the scheme name of the protocol (e.g. "http", "cmp", etc.)
     *
     * @return the scheme name.
     */
    String getScheme();

    /**
     * Serializes the specified message with the specified id into a byte buffer on behalf of the
     * specified recipient.
     *
     * @param id        the message ID.
     * @param info      the message to serialize.
     * @param recipient the recipient path.
     * @return the serialized information.
     */
    ByteBuffer serialize(final UUID id, SessionInformation info, final Path recipient);

    /**
     * Reads data from the channel and tries to create a kind of information from it.
     * <p>
     * An information will only be returned when (a) enough data have been read from the socket, and
     * (b) the data from the socket is a valid type of information.  In case (b), invalid objects
     * will be dropped after logging an error.
     *
     * @param channel the channel.
     * @return a kind of information, if one could be created, otherwise {@code null}.
     * @throws IOException if the read operation failed.
     */
    @Nullable SessionInformation read(ReadableByteChannel channel) throws IOException;

    /**
     * Decodes the specified response body into a value of the specified type.
     *
     * @param <V>  the type.
     * @param type the type selector.
     * @param data the response body, if any.
     * @return the decoded body.
     */
    @Nullable <V> V decode(GenericType<V> type, @Nullable byte[] data);

    /**
     * Encodes the specified value into a response body of the specified type.
     *
     * @param <V>   the type.
     * @param type  the type selector.
     * @param value the value, if any.  @return the response body.
     */
    <V> @Nullable byte[] encode(final GenericType<V> type, @Nullable V value);

}
