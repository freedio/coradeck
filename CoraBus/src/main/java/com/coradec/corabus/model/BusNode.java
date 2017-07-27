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

package com.coradec.corabus.model;

import com.coradec.corabus.state.MetaState;
import com.coradec.corabus.state.NodeState;
import com.coradec.coracom.model.Recipient;
import com.coradec.coradir.model.Path;

import java.net.URI;

/**
 * ​A node in the bus system.  A node is like a cell: self-contained, but dependant on its hub to
 * exchange information and fulfill its purpose.
 * <p>
 * A node can be attached to a hub in two ways:  either the node sends a request to a hub to add it,
 * or the hub invites the node to join it.  The first way actually only triggers the second way.
 */
public interface BusNode extends Recipient {

    /**
     * Returns the node's current state (last achieved state).
     *
     * @return the node's current state.
     */
    NodeState getState();

    /**
     * Returns the node's meta-state.
     *
     * @return the meta-state.
     */
    MetaState getMetaState();

    /**
     * Returns the identifier of the node.
     *
     * @return the node identifier.
     */
    URI getIdentifier();

    /**
     * Returns the current path of the node.
     *
     * @return the path.
     */
    Path getPath();

}
