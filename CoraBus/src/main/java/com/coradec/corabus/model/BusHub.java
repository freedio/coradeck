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

import com.coradec.corabus.trouble.MemberNotFoundException;
import com.coradec.coracom.model.Request;
import com.coradec.coradir.model.Path;
import com.coradec.corasession.model.Session;

import java.util.Optional;

/**
 * The focal point of a couple of nodes working together; as such, a hub is the bus; as node, it is
 * an element of yet another hierarchically higher bus layer.
 */
public interface BusHub extends BusNode {

    /**
     * Adds the specified node to the hub under the specified name in the context of the specified
     * session.
     *
     * @param session the session context.
     * @param name    the name.
     * @param node    the node to add.
     * @return a request to track progress of the operation.
     */
    Request add(Session session, String name, BusNode node);

    /**
     * Adds the specified node to the hub under the specified relative path in the context of the
     * specified session.
     * <p>
     * Note that this method does not support absolute paths.  To add a node under an absolute path,
     * use {@link Bus#add(Session, Path, BusNode)}.
     *
     * @param session the session context.
     * @param path    the relative path of the member-to-be.
     * @param node    the node to add.
     * @return a request to track progress of the operation.
     */
    Request add(Session session, Path path, BusNode node);

    /**
     * Looks up the direct member node with the specified name in the context of the specified
     * session.
     *
     * @param session the session context.
     * @param name    the member node.
     * @return the node associated with the specified name, or {@link Optional#empty()}.
     */
    Optional<BusNode> lookup(Session session, String name);

    /**
     * Looks up the node with the specified relative path in the context of the specified session.
     * <p>
     * Note that this method does not support absolute paths.  To lookup a node under an absolute
     * path, use {@link Bus#lookup(Session, Path)}.
     *
     * @param session the session context.
     * @param path    the relative path of the member.
     * @return the node associated with the specified path, or {@link Optional#empty()}.
     */
    Optional<BusNode> lookup(Session session, Path path);

    /**
     * Returns the node with the specified relative path in the context of the specified session.
     * <p>
     * Note that this method does not support absolute paths.  To lookup a node under an absolute
     * path, use {@link Bus#lookup(Session, Path)}.
     *
     * @param session the session context.
     * @param path    the relative path of the member.
     * @return the node associated with the specified path.
     * @throws MemberNotFoundException if no member is associated with the specified path.
     */
    BusNode get(Session session, Path path) throws MemberNotFoundException;

    /**
     * Checks in the context of the specified session if the specified relative path is associated
     * with a member node, in other words: if {@link #get(Session, Path)} would return a node
     * instead of throwing a exception.
     *
     * @param session the session context.
     * @param path    the relative path of the member.
     * @return {@code true} if the specified path is associated with a member node, {@code false} if
     * not.
     */
    boolean has(Session session, Path path);

}
