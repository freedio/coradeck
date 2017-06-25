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

import com.coradec.coracom.ctrl.Observer;
import com.coradec.coracore.annotation.Nullable;
import com.coradec.coracore.trouble.OperationTimedoutException;

import java.util.concurrent.TimeUnit;

/**
 * ​An event that needs permission to happen.
 */
public interface Request extends Event, Observer {

    /**
     * Waits the specified amount of time for the request to become complete.
     *
     * @param amount the amount of time.
     * @param unit   the time unit.
     * @return this request, for method chaining.
     */
    Request standby(long amount, TimeUnit unit)
            throws OperationTimedoutException, InterruptedException;

    /**
     * Returns the problem in case of a failure.
     *
     * @return the problem, or {@code null} if the request did not fail, or failed without an
     * indication of a problem.
     */
    @Nullable Throwable getProblem();

    /**
     * Marks the request as successful.
     */
    void succeed();

    /**
     * Marks the request as failed with the specified optional problem.
     *
     * @param problem the problem, if known.
     */
    void fail(@Nullable Throwable problem);

    /**
     * Marks the request as cancelled.
     */
    void cancel();

    /**
     * Checks if the request was successful.
     *
     * @return {@code true} if the request was successful, {@code false} if not.
     */
    boolean isSuccessful();

    /**
     * Checks if the request failed.
     *
     * @return {@code true} if the request failed, {@code false} if not.
     */
    boolean isFailed();

    /**
     * Checks if the request was cancelled.
     *
     * @return {@code true} if the request was cancelled, {@code false} if not.
     */
    boolean isCancelled();

    /**
     * Forwards state changes to the specified request.
     *
     * @param request the request to forward to.
     */
    void reportCompletionTo(Request request);

    /**
     * Performs the specified action when the request was successful.
     * <p>
     * <span style="color: yellow; background:red;"><strong>IMPORTANT NOTE:</strong></span>
     * <p>
     * The specified action is executed asynchronously and may re-introduce concurrency in an
     * otherwise single-threaded context.  Do not perform any state changes or other side effects
     * inside the action without proper caution.
     *
     * @param action the action to take.
     * @return this request, for method chaining.
     */
    Request andThen(Runnable action);

    /**
     * Performs the specified action when the request failed.
     * <p>
     * <span style="color: yellow; background:red;"><strong>IMPORTANT NOTE:</strong></span>
     * <p>
     * The specified action is executed asynchronously and may re-introduce concurrency in an
     * otherwise single-threaded context.  Do not perform any state changes or other side effects
     * inside the action without proper caution.
     *
     * @param action the action to take.
     * @return this request, for method chaining.
     */
    Request orElse(Runnable action);

}
