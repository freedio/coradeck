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

package com.coradec.coracom.ctrl;

import com.coradec.coracore.annotation.Nullable;
import com.coradec.coracore.model.Origin;
import com.coradec.corasession.model.Session;
import com.coradec.coratype.ctrl.TypeConverter;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * ​An object capable of resolving recipient IDs.
 */
public interface OriginResolver {

    Set<OriginResolver> RESOLVERS = new CopyOnWriteArraySet<>();

    static void register(OriginResolver resolver) {
        RESOLVERS.add(resolver);
    }

    static void unregister(OriginResolver resolver) {
        RESOLVERS.remove(resolver);
    }

    static Origin resolveOrigin(@Nullable Session session, String id) {
        for (final OriginResolver resolver : RESOLVERS) {
            try {
                final Origin origin = resolver.originOf(session, id);
                if (origin != null) return origin;
            } catch (Exception e) {
                // not good ⇒ next!
            }
        }
        return TypeConverter.to(Origin.class).decode(id);
    }

    /**
     * Decodes an origin ID to an origin in the context of the specified session, if any.
     * <p>
     * Note that a session context is necessary in some cases, in some cases not.
     *
     * @param session the session context.
     * @param id      the origin ID.
     * @return a recipient.
     */
    Origin originOf(@Nullable Session session, String id);

}
