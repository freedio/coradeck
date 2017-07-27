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

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * ​An object that will hold a value once it gets set by another thread.  Similar to {@link
 * Future}.
 *
 * @param <V> the value type.
 */
public interface Voucher<V> {

    /**
     * Waits until the value has been set.
     *
     * @throws InterruptedException if the thread was interrupted while waiting for the value to
     *                              appear.
     */
    void standBy() throws InterruptedException;

    /**
     * Waits for at most the specified amount of time for the value before timing out.
     *
     * @param amount the amount of time to wait.
     * @param unit   the time unit.
     * @throws InterruptedException if the thread has been interrupted while waiting for the value
     *                              to appear.
     * @throws TimeoutException     if the request timed out.
     */
    void standBy(long amount, TimeUnit unit) throws InterruptedException, TimeoutException;

}
