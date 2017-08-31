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

package com.coradec.corabus.com;

import com.coradec.corabus.trouble.NodeNotAttachedException;
import com.coradec.corabus.view.BusContext;
import com.coradec.corabus.view.Member;
import com.coradec.coracom.model.SessionRequest;

/**
 * ​Request to join a bus.
 */
public interface Invitation extends SessionRequest {

    String PROP_NAME = "Name";

    /**
     * Returns the name under which the node is invited.
     *
     * @return the name.
     */
    String getName();

    /**
     * Returns the bus context to attach to.
     *
     * @return the bus context.
     */
    BusContext getContext();

    /**
     * Returns the member, if one is attached.
     *
     * @return the member.
     * @throws NodeNotAttachedException if no member is attached.
     */
    Member getMember() throws NodeNotAttachedException;

    /**
     * Sets the member.
     *
     * @param member the member view.
     */
    void setMember(Member member);

}
