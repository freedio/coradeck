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

import com.coradec.corabus.model.BusNode;

import java.util.Optional;

/**
 * ​Context for an attachment between a member node and its bus hub.
 */
public interface BusContext {

    /**
     * Callback invoked when the specified node is has left the context.
     *
     * @param node the leaving node.
     */
    void left(BusNode node);

    /**
     * Callback invoked when the specified node has joined the context.
     *
     * @param node the joining node.
     */
    void joined(BusNode node);

    /**
     * Returns the currently attached node, if any.
     *
     * @return the currently attached node.
     */
    Optional<BusNode> getNode();

}
