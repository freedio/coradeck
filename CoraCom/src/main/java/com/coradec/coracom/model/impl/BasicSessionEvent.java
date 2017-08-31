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

import com.coradec.coracom.model.Event;
import com.coradec.coracom.model.SessionEvent;
import com.coradec.coracore.model.Origin;
import com.coradec.corasession.model.Session;

import java.util.Map;
import java.util.UUID;

/**
 * ​​Basic implementation of a session based event.
 */
@SuppressWarnings("ClassHasNoToStringMethod")
public class BasicSessionEvent extends BasicEvent implements SessionEvent {

    private final Session session;

    /**
     * Initializes anew instance of BasicSessionEvent from the specified origin in the context of
     * the specified session.
     *
     * @param session the session context.
     * @param origin  the origin of event.
     */
    public BasicSessionEvent(final Session session, final Origin origin) {
        super(origin);
        this.session = session;
    }

    /**
     * Initializes a new instance of BasicSessionEvent from the specified property map.
     *
     * @param properties the property map.
     */
    public BasicSessionEvent(final Map<String, Object> properties) {
        super(properties);
        this.session = Session.get(get(UUID.class, PROP_SESSION));
    }

    /**
     * Initializes a new instance of BasicSessionEvent from the specified event in the context of
     * the specified session.
     *
     * @param session the session context.
     * @param event   the event.
     */
    public BasicSessionEvent(final Session session, final Event event) {
        super(event.getProperties());
        this.session = session;
    }

    @Override protected void collect() {
        super.collect();
        set(PROP_SESSION, session.getId());
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
