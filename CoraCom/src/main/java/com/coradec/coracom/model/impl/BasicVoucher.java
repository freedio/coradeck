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
import com.coradec.coracom.model.Voucher;
import com.coradec.coracom.trouble.RequestFailedException;
import com.coradec.coracom.trouble.ValueNotAvailableException;
import com.coradec.coracore.annotation.Nullable;
import com.coradec.coracore.annotation.ToString;
import com.coradec.coracore.model.GenericType;
import com.coradec.coracore.model.Origin;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

/**
 * ​​Basic implementation of a voucher.
 */
@SuppressWarnings("ClassHasNoToStringMethod")
public class BasicVoucher<V> extends BasicRequest implements Voucher<V> {

    private @Nullable V value;
    private final GenericType<V> type;
    Set<Consumer<V>> valueCallbacks = new CopyOnWriteArraySet<>();

    /**
     * Initializes a new instance of BasicVoucher with the specified type, sender and recipient.
     *
     * @param sender    the sender.
     * @param recipient the recipient.
     * @param type      the expected type of result.
     */
    public BasicVoucher(final Origin sender, final Recipient recipient, final GenericType<V> type) {
        super(sender, recipient);
        this.type = type;
    }

    /**
     * Initializes a new instance of BasicVoucher with the specified type, sender and recipient.
     *
     * @param sender    the sender.
     * @param recipient the recipient.
     * @param type      the expected type of result.
     */
    public BasicVoucher(final Origin sender, final Recipient recipient, final Class<V> type) {
        this(sender, recipient, GenericType.of(type));
    }

    /**
     * Initializes a new instance of BasicVoucher from the specified property map.
     *
     * @param properties the property map.
     */
    public BasicVoucher(final Map<String, Object> properties) {
        super(properties);
        //noinspection unchecked
        this.type = (GenericType<V>)get(GenericType.class, PROP_RESULT_TYPE);
        this.value = lookup(type, PROP_VALUE).orElse(null);
    }

    @SuppressWarnings("unchecked") @Override public GenericType<V> getType() {
        return type;
    }

    @Override public Optional<V> lookup() {
        return Optional.ofNullable(getValue());
    }

    @Override public V value()
            throws InterruptedException, RequestFailedException, ValueNotAvailableException {
        standby();
        return lookup().orElseThrow(ValueNotAvailableException::new);
    }

    @Override public V value(final long amount, final TimeUnit unit)
            throws InterruptedException, TimeoutException, RequestFailedException,
                   ValueNotAvailableException {
        standby(amount, unit);
        return lookup().orElseThrow(ValueNotAvailableException::new);
    }

    @SuppressWarnings("unchecked") @Override @ToString public @Nullable V getValue() {
        return value;
    }

    @Override public Voucher<V> setValue(final V value) {
        this.value = value;
        return this;
    }

    @Override public Voucher<V> andThen(final Consumer<V> action) {
        if (isSuccessful()) {
//            debug("Exec direct of success action %s", action);
            action.accept(getValue());
        } else valueCallbacks.add(action);
        return this;
    }

    @Override protected void collect() {
        super.collect();
        set(PROP_RESULT_TYPE, type);
        if (value != null) set(PROP_VALUE, value);
    }

    @Override protected void furtherSuccessActions() {
        super.furtherSuccessActions();
        if (!this.valueCallbacks.isEmpty()) {
            for (final Consumer<V> successCallback : this.valueCallbacks) {
                try {
//                            debug("success >> %s", successCallback);
                    successCallback.accept(value);
                } catch (Exception e) {
                    error(e);
                }
            }
            this.valueCallbacks.clear();
        }
    }

}
