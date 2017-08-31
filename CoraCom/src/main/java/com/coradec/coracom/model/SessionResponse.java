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

package com.coradec.coracom.model;

import com.coradec.coracore.model.Factory;
import com.coradec.coracore.model.GenericFactory;
import com.coradec.corasession.model.Session;

/**
 * ​Response with a session context.
 */
public interface SessionResponse extends Response, SessionMessage {

    Factory<SessionResponse> SESSION_RESPONSE = new GenericFactory<>(SessionResponse.class);

    /**
     * Wraps a response with a session context into a session response.
     *
     * @param session the session context.
     * @param response the response.
     * @return a session response.
     */
    static SessionResponse wrap(Session session, Response response) {
        return SESSION_RESPONSE.create(session, response);
    }

}
