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

package com.coradec.coracom.ctrl;

import java.nio.channels.WritableByteChannel;

/**
 * ​A writer capable of handling a writable channel.
 */
public interface ChannelWriter {

    /**
     * Writes the maximum or available amount of collected data to the specified channel, ready to
     * accept that not all data may fit into the provided underlying buffer.
     *
     * @param channel the channel to write to.
     * @return {@code true} if there will be potentially more data to come, {@code false} if that
     * was it.
     */
    boolean write(WritableByteChannel channel);

}
