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
 * ​Information with a session context.
 */
public interface SessionInformation extends Information {

    String PROP_SESSION = "Session";

    Factory<SessionInformation> SESSION_INFO = new GenericFactory<>(SessionInformation.class);

    /**
     * Wraps an information with a session context into a session information.
     *
     * @param session     the session context.
     * @param information the information.
     * @return a session information.
     */
    static SessionInformation wrap(Session session, Information information) {
        return SESSION_INFO.create(session, information);
    }

    /**
     * Returns the session context.
     *
     * @return the session context.
     */
    Session getSession();

}