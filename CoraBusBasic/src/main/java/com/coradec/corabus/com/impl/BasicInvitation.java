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

package com.coradec.corabus.com.impl;

import com.coradec.corabus.com.Invitation;
import com.coradec.corabus.trouble.NodeNotAttachedException;
import com.coradec.corabus.view.BusContext;
import com.coradec.corabus.view.Member;
import com.coradec.coracom.model.Recipient;
import com.coradec.coracom.model.Sender;
import com.coradec.coracom.model.impl.BasicSessionRequest;
import com.coradec.coracore.annotation.Implementation;
import com.coradec.coracore.annotation.NonNull;
import com.coradec.coracore.annotation.Nullable;
import com.coradec.coracore.annotation.ToString;
import com.coradec.corasession.model.Session;

/**
 * ​​Basic implementation of an invitation.
 */
@SuppressWarnings("ClassHasNoToStringMethod")
@Implementation
public class BasicInvitation extends BasicSessionRequest implements Invitation {

    private final String name;
    private final BusContext context;
    private @Nullable Member member;

    /**
     * Initializes a new instance of BasicInvitation with the specified sender and list of
     * recipients in the context of the specified session.
     *
     * @param session    the session context.
     * @param sender     the sender.
     * @param recipients the list of recipients
     */
    public BasicInvitation(final Session session, final String name, final BusContext context,
            final Sender sender, final Recipient... recipients) {
        super(session, sender, recipients);
        this.name = name;
        this.context = context;
    }

    @Override @ToString public String getName() {
        return name;
    }

    @Override @ToString public BusContext getContext() {
        return context;
    }

    @Override public Member getMember() throws NodeNotAttachedException {
        if (member == null) throw new NodeNotAttachedException();
        return member;
    }

    @Override public void setMember(final @NonNull Member member) {
        this.member = member;
    }

}
