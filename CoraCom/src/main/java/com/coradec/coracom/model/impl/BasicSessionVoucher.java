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

import com.coradec.coracom.model.Recipient;
import com.coradec.coracom.model.SessionVoucher;
import com.coradec.coracom.model.Voucher;
import com.coradec.coracore.annotation.Implementation;
import com.coradec.coracore.model.GenericType;
import com.coradec.coracore.model.Origin;
import com.coradec.corasession.model.Session;

import java.util.Map;
import java.util.UUID;

/**
 * ​​Basic implementation of a session based request.
 *
 * @param <V> the value type.
 */
@SuppressWarnings("ClassHasNoToStringMethod")
@Implementation
public class BasicSessionVoucher<V> extends BasicVoucher<V> implements SessionVoucher<V> {

    private final Session session;

    /**
     * Initializes a new instance of BasicSessionVoucher of the specified type with the specified
     * sender and recipient in the context of the specified session.
     *
     * @param session   the session context.
     * @param sender    the sender.
     * @param recipient the recipient.
     * @param type      the type of voucher.
     */
    public BasicSessionVoucher(final Session session, final Origin sender,
            final Recipient recipient, GenericType<V> type) {
        super(sender, recipient, type);
        this.session = session;
    }

    /**
     * Initializes a new instance of BasicSessionVoucher of the specified type with the specified
     * sender and recipient in the context of the specified session.
     *
     * @param session   the session context.
     * @param sender    the sender.
     * @param recipient the recipient.
     * @param type      the type of voucher.
     */
    public BasicSessionVoucher(final Session session, final Origin sender,
            final Recipient recipient, Class<V> type) {
        super(sender, recipient, type);
        this.session = session;
    }

    /**
     * Initializes a new instance of BasicSessionVoucher from the specified property map.
     *
     * @param properties the property map.
     */
    public BasicSessionVoucher(final Map<String, Object> properties) {
        super(properties);
        this.session = Session.get(get(UUID.class, PROP_SESSION));
    }

    /**
     * Initializes a new instance of BasicSessionVoucher from the specified voucher in the context
     * of the specified session.
     *
     * @param session the session context.
     * @param voucher the voucher.
     */
    public BasicSessionVoucher(final Session session, final Voucher<V> voucher) {
        super(voucher.getProperties());
        this.session = session;
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
