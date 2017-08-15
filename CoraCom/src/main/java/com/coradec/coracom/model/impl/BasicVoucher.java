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
import com.coradec.coracom.model.Sender;
import com.coradec.coracom.model.Voucher;
import com.coradec.coracom.trouble.RequestFailedException;
import com.coradec.coracom.trouble.ValueNotAvailableException;
import com.coradec.coracore.annotation.Nullable;
import com.coradec.coracore.annotation.ToString;
import com.coradec.coracore.model.GenericType;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * ​​Basic implementation of a voucher.
 */
@SuppressWarnings("ClassHasNoToStringMethod")
public class BasicVoucher<V> extends BasicRequest implements Voucher<V> {

    private final GenericType<V> type;
    private @Nullable V value;

    /**
     * Initializes a new instance of BasicVoucher with the specified type, sender and list of
     * recipients.
     *
     * @param sender     the sender.
     * @param recipients the list of recipients
     */
    public BasicVoucher(final GenericType<V> type, final Sender sender,
            final Recipient... recipients) {
        super(sender, recipients);
        this.type = type;
    }

    /**
     * Initializes a new instance of BasicVoucher with the specified type, sender and list of
     * recipients.
     *
     * @param sender     the sender.
     * @param recipients the list of recipients
     */
    public BasicVoucher(final Class<V> type, final Sender sender, final Recipient... recipients) {
        this(GenericType.of(type), sender, recipients);
    }

    @Override public GenericType<V> getType() {
        return type;
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

    @Override @ToString public @Nullable V getValue() {
        return value;
    }

    @Override public Voucher<V> setValue(final V value) {
        this.value = value;
        return this;
    }

}
