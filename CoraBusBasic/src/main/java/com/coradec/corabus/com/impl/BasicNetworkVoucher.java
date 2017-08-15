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

import com.coradec.coracom.model.Recipient;
import com.coradec.coracom.model.Sender;
import com.coradec.coracom.model.Voucher;
import com.coradec.coracom.trouble.RequestFailedException;
import com.coradec.coracom.trouble.ValueNotAvailableException;
import com.coradec.coracore.annotation.Nullable;
import com.coradec.coracore.model.GenericType;
import com.coradec.corasession.model.Session;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * ​​Implementation of a voucher across the wire.
 *
 * @param <V> the value type.
 */
@SuppressWarnings("ClassHasNoToStringMethod")
public class BasicNetworkVoucher<V> extends NetworkRequest implements Voucher<V> {

    private final GenericType<V> type;
    private @Nullable V value;

    /**
     * Initializes a new instance of NetworkVoucher of the specified type from the specified sender
     * to the specified recipient(s) with the specified additional headers and the specified body
     * (if any) in the context of the specified session.
     *
     * @param session    the session context.
     * @param type       the type of the result value.
     * @param attributes the additional header attributes.
     * @param body       the body (optional).
     * @param sender     the sender.
     * @param recipients the recipient(s).
     */
    public BasicNetworkVoucher(final Session session, final String command,
            final GenericType<V> type, final Map<String, String> attributes,
            @Nullable final byte[] body, final Sender sender, final Recipient... recipients) {
        super(session, command, attributes, body, sender, recipients);
        this.type = type;
    }

    @Override public Optional<V> lookup() {
        return Optional.ofNullable(value);
    }

    @Override public V value()
            throws InterruptedException, RequestFailedException, ValueNotAvailableException {
        standby();
        if (value == null) throw new ValueNotAvailableException();
        return value;
    }

    @Override public V value(final long amount, final TimeUnit unit)
            throws InterruptedException, TimeoutException, RequestFailedException,
                   ValueNotAvailableException {
        standby(amount, unit);
        if (value == null) throw new ValueNotAvailableException();
        return value;
    }

    @Override public @Nullable V getValue() {
        return value;
    }

    @Override public GenericType<V> getType() {
        return type;
    }

    @Override public Voucher<V> setValue(final V value) {
        this.value = value;
        return this;
    }
}
