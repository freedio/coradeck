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

import com.coradec.coradir.model.Path;

/**
 * ​Context for an attachment between a member node and its bus hub.
 */
public interface BusContext {

    /**
     * Callback invoked when the specified node is has left the context.
     *
     * @param name the name of the leaving node.
     */
    void left(String name);

    /**
     * Callback invoked when the specified node has joined the context with the specified name.
     *
     * @param name   the name.
     * @param member the joining node.
     */
    void joined(String name, Member member);

    /**
     * Checks if the context contains the specified node.
     *
     * @param member the node.
     * @return {@code true} if the context contains the node, otherwise {@code false}.
     */
    boolean contains(Member member);

    /**
     * Returns the path of the member with the specified name.
     *
     * @return the member path.
     */
    Path getPath(String name);

}
