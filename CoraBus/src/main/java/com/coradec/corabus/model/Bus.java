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

import com.coradec.corabus.trouble.NodeNotFoundException;
import com.coradec.coracom.model.Recipient;
import com.coradec.coracom.model.Request;
import com.coradec.coracom.model.Sender;
import com.coradec.coradir.model.Path;
import com.coradec.corasession.model.Session;

import java.util.Optional;

/**
 * ​Representation of the bus system.
 */
public interface Bus {

    /**
     * Adds the specified node to the bus under the specified path in the context of the specified
     * session.
     *
     * @param session the session context.
     * @param path    the path.
     * @param node    the node.
     * @return a request to track progress.
     */
    Request add(final Session session, Path path, BusNode node);

    /**
     * Starts the bus system manually.
     */
    void setup();

    /**
     * Shuts the bus system down.
     */
    void shutdown();

    /**
     * Returns the root hub of the bus system.
     *
     * @return the root.
     */
    BusHub getRoot();

    /**
     * Looks up the bus node with the specified path in the context of the specified session.
     *
     * @param session the session context.
     * @param path    the path of the node to look up.
     * @return the bus node, or {@link Optional#empty()} if the node was not found.
     */
    Optional<BusNode> lookup(Session session, Path path);

    /**
     * Returns the bus node with the specified path in the context of the specified session.
     *
     * @param session the session context.
     * @param path    the path of the node to return.
     * @return the bus node.
     * @throws NodeNotFoundException if no node was associated with the specified path.
     */
    BusNode get(Session session, Path path) throws NodeNotFoundException;

    /**
     * Checks in the context of the specified session if the bus system has a node with the
     * specified path.
     *
     * @param session the session context.
     * @param path    the path.
     * @return {@code true} if the bus system has a node with the specified path, {@code false} if
     * not.
     */
    boolean has(Session session, Path path);

    /**
     * Returns a sender representation from the specified path.
     *
     * @param path the path.
     * @return a sender.
     */
    Sender sender(Path path);

    /**
     * Returns a sender representation from the specified path.
     *
     * @param path the path.
     * @return a sender.
     */
    Sender sender(String path);

    /**
     * Returns a recipient representation from the specified path.
     *
     * @param path the path.
     * @return a recipient.
     */
    Recipient recipient(Path path);

    /**
     * Returns a recipient representation from the specified path.
     *
     * @param path the path.
     * @return a recipient.
     */
    Recipient recipient(String path);

}
