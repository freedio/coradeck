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

package com.coradec.corasession.model;

import com.coradec.coracore.annotation.Inject;
import com.coradec.coracore.collections.HashCache;
import com.coradec.corasession.trouble.SessionNotFoundException;

import java.util.Optional;
import java.util.UUID;

/**
 * Implementation of a session manager.
 */
@SuppressWarnings("ClassHasNoToStringMethod")
public final class Sessions {

    private static final Sessions MANAGER = new Sessions();

    public static void register(final UUID sessionId, final Session session) {
        MANAGER.registerSession(sessionId, session);
    }

    /**
     * Returns the session with the specified session ID.
     *
     * @param sessionId the session ID.
     * @return the session with the specified ID.
     * @throws SessionNotFoundException if the session was not found.
     */
    static Session get(final UUID sessionId) throws SessionNotFoundException {
        return lookup(sessionId).orElseThrow(() -> new SessionNotFoundException(sessionId));
    }

    /**
     * Looks up the session with the specified session ID, if there is any such session.
     *
     * @param sessionId the session ID.
     * @return the session with the specified ID, or {@link Optional#empty()}.
     */
    static Optional<Session> lookup(final UUID sessionId) throws SessionNotFoundException {
        return MANAGER.lookupSession(sessionId);
    }

    @Inject private HashCache<UUID, Session> sessionCache;

    private Sessions() {
    }

    /**
     * Returns the session with the specified session ID.
     *
     * @param sessionId the session ID.
     * @return the session with the specified ID.
     * @throws SessionNotFoundException if a session with the specified ID was not found.
     */
    private Optional<Session> lookupSession(final UUID sessionId) throws SessionNotFoundException {
        return Optional.ofNullable(sessionCache.get(sessionId));
    }

    private void registerSession(final UUID sessionId, final Session session) {
        sessionCache.put(sessionId, session);
    }

}
