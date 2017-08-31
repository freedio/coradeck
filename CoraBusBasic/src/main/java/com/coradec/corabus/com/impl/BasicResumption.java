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

import com.coradec.corabus.com.Resumption;
import com.coradec.coracom.model.Recipient;
import com.coradec.coracom.model.Request;
import com.coradec.coracom.model.impl.BasicSessionRequest;
import com.coradec.coracore.annotation.Implementation;
import com.coradec.coracore.model.Origin;
import com.coradec.corasession.model.Session;

/**
 * ​​Basic implementation of a resumption request.
 */
@Implementation
public class BasicResumption extends BasicSessionRequest implements Resumption {

    /**
     * Initializes a new instance of BasicResumption with the specified sender and recipient in the
     * context of the specified session.
     *
     * @param session   the session context.
     * @param sender    the sender.
     * @param recipient the recipient.
     */
    public BasicResumption(final Session session, final Origin sender, final Recipient recipient) {
        super(session, sender, recipient);
    }

    /**
     * Initializes a new instance of BasicResumption from the specified request in the context of
     * the specified session.
     *
     * @param session the session context.
     * @param request the request.
     */
    private BasicResumption(final Session session, final Request request) {
        super(session, request);
    }

}
