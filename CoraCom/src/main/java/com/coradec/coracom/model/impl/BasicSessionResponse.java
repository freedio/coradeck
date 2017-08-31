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

package com.coradec.coracom.model.impl;

import static com.coradec.coracom.state.Answer.*;

import com.coradec.coracom.model.Recipient;
import com.coradec.coracom.model.Response;
import com.coradec.coracom.model.SessionResponse;
import com.coradec.coracom.state.Answer;
import com.coradec.coracore.annotation.Nullable;
import com.coradec.coracore.model.Origin;
import com.coradec.corasession.model.Session;

import java.util.Map;
import java.util.UUID;

/**
 * ​​Basic implementation of a session response.
 */
@SuppressWarnings("ClassHasNoToStringMethod")
public class BasicSessionResponse extends BasicResponse implements SessionResponse {

    private final Session session;

    /**
     * Initializes a new instance of BasicSessionResponse with the specified sender and recipient,
     * request reference, answer and either a body (answer = OK), a problem (answer = KO) or nothing
     * at all (possible in all cases) in the context of the specified session.
     *
     * @param session   the session context.
     * @param sender    the sender.
     * @param recipient the recipient.
     * @param reference a reference to the request this is a response to.
     * @param answer    the answer.
     * @param arg       an argument (either an exception or a response body, or nothing at all).
     */
    public BasicSessionResponse(final Session session, final Origin sender,
            final Recipient recipient, final UUID reference, final Answer answer,
            final @Nullable Object arg) {
        super(session, sender, recipient, reference, answer, arg);
        this.session = session;
    }

    /**
     * Initializes a new instance of BasicSessionResponse from the specified property map.
     *
     * @param properties the property map.
     */
    public BasicSessionResponse(final Map<String, Object> properties) {
        super(properties);
        this.session = Session.get(get(UUID.class, PROP_SESSION));
    }

    public BasicSessionResponse(final Session session, final Response response) {
        this(session, response.getOrigin(), response.getRecipient(), response.getReference(),
                response.getAnswer(), //
                response.getAnswer() == OK ? response.getBody() : response.getAnswer() == KO
                                                                  ? response.getFailureReason()
                                                                  : null);
    }

    @Override protected void collect() {
        set(PROP_SESSION, session.getId());
        super.collect();
    }

    /**
     * Returns the session context.
     *
     * @return the session context.
     */
    @Override public Session getSession() {
        return session;
    }

}
