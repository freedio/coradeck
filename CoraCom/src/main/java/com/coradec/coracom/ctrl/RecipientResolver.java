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

import com.coradec.coracom.model.Recipient;
import com.coradec.coracom.trouble.RecipientNotFoundException;
import com.coradec.coracore.annotation.Nullable;
import com.coradec.corasession.model.Session;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * ​An object capable of resolving recipient IDs.
 */
public interface RecipientResolver {

    Set<RecipientResolver> RESOLVERS = new CopyOnWriteArraySet<>();

    static void register(RecipientResolver resolver) {
        RESOLVERS.add(resolver);
    }

    static void unregister(RecipientResolver resolver) {
        RESOLVERS.remove(resolver);
    }

    static Recipient resolveRecipient(@Nullable Session session, String id) {
        for (final RecipientResolver resolver : RESOLVERS) {
            try {
                final Recipient recipient = resolver.recipientOf(session, id);
                if (recipient != null) return recipient;
            } catch (Exception e) {
                // not good ⇒ next!
            }
        }
        throw new RecipientNotFoundException(id);
    }

    /**
     * Decodes a recipient ID to a recipient in the context of the specified session, if any.
     * <p>
     * Note that a session context is necessary in some cases, in some cases not.
     *
     * @param session the session context.
     * @param id      the recipient ID.
     * @return a recipient.
     */
    Recipient recipientOf(@Nullable Session session, String id);

}
