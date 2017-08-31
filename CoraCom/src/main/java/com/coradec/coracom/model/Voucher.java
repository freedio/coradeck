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

import com.coradec.coracom.model.impl.BasicVoucher;
import com.coradec.coracom.trouble.RequestFailedException;
import com.coradec.coracom.trouble.ValueNotAvailableException;
import com.coradec.coracore.annotation.Nullable;
import com.coradec.coracore.model.GenericType;
import com.coradec.coracore.model.Origin;

import java.io.Serializable;
import java.util.Optional;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * ​An object that will hold a value once it gets set by another thread.  Similar to {@link
 * Future}.
 *
 * @param <V> the value type.
 */
public interface Voucher<V> extends Request {

    String PROP_VALUE = "Value";
    String PROP_RESULT_TYPE = "ResultType";

    static <X extends Serializable> Voucher<X> of(Origin sender, Recipient recipient, Class<X> type,
            X value) {
        return new BasicVoucher<>(sender, recipient, type).setValue(value);
    }

    /**
     * Looks up the value, if it is already available.
     *
     * @return the value, or {@link Optional#empty()} if the value is not yet available.
     */
    Optional<V> lookup();

    /**
     * Returns the value, waiting for it to become available, if necessary.
     *
     * @return the value.
     * @throws InterruptedException       if the thread was interrupted while waiting for the value
     *                                    to become available.
     * @throws RequestFailedException     if the request to get the value failed.
     * @throws ValueNotAvailableException if the value was not available when expected.
     */
    V value() throws InterruptedException, RequestFailedException, ValueNotAvailableException;

    /**
     * Returns the value, waiting not longer than the specified amount of time for it to become
     * available.
     *
     * @param amount the amount of time.
     * @param unit   the time unit.
     * @return the value.
     * @throws InterruptedException       if the thread was interrupted while waiting for the value
     *                                    to become available.
     * @throws TimeoutException           if the value was not available in the specified amount of
     *                                    time.
     * @throws RequestFailedException     if the request to get the value failed.
     * @throws ValueNotAvailableException if the value was not available when expected.
     */
    V value(long amount, TimeUnit unit)
            throws InterruptedException, TimeoutException, RequestFailedException,
                   ValueNotAvailableException;

    /**
     * Returns the value, whatever it currently is.
     *
     * @return the value, or {@code null} if the value has not yet been set.
     */
    @Nullable V getValue();

    /**
     * Returns the expected result type.
     *
     * @return the expected result type.
     */
    GenericType<V> getType();

    /**
     * Sets the value of the voucher.
     *
     * @param value the value.
     * @return the voucher itself, for method chaining.
     */
    Voucher<V> setValue(V value);

}
