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
import com.coradec.coracom.state.RequestState;
import com.coradec.coracom.trouble.RequestFailedException;
import com.coradec.coracore.annotation.Nullable;
import com.coradec.coracore.trouble.OperationTimedoutException;

import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * ​An event that needs permission to happen.
 */
public interface Request extends Message, Observer {

    /**
     * Waits for the request to become complete.
     *
     * @return this request, for method chaining.
     * @throws InterruptedException   if the thread was interruoted while waiting for the request to
     *                                complete.
     * @throws RequestFailedException if the request failed.
     */
    Request standby() throws InterruptedException, RequestFailedException;

    /**
     * Waits the specified amount of time for the request to become complete.
     *
     * @param amount the amount of time.
     * @param unit   the time unit.
     * @return this request, for method chaining.
     * @throws OperationTimedoutException if the set time limit expired without the request becoming
     *                                    complete.
     * @throws InterruptedException       if the thread was interrupted while waiting for the
     *                                    request to complete.
     * @throws RequestFailedException     if the request failed.
     */
    Request standby(long amount, TimeUnit unit)
            throws OperationTimedoutException, InterruptedException, RequestFailedException;

    /**
     * Returns the complete set of states the request has ever had.
     *
     * @return the request state history.
     */
    Set<RequestState> getStates();

    /**
     * Returns the problem in case of a failure.
     *
     * @return the problem, or {@code null} if the request did not fail, or failed without an
     * indication of a problem.
     */
    @Nullable Throwable getProblem();

    /**
     * Asynchronously marks the request as successful.
     */
    void succeed();

    /**
     * Asynchronously marks the request as failed with the specified optional problem.
     *
     * @param problem the problem, if known.
     */
    void fail(@Nullable Throwable problem);

    /**
     * Asynchronously marks the request as cancelled.
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
     * Notifies the specified observer when the request is complete.
     *
     * @param observer the observer to notify.
     */
    void reportCompletionTo(Observer observer);

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
     * Returns a serial multi-request containing this and the specified request, which both must
     * succeed for the request to succeed.
     * <p>
     * For reasons of convenience, the specified request may be {@code null}, in which case this
     * request is returned.
     *
     * @param request the request to add.
     * @return a serial multi-request, or this request in the absence of an argument.
     */
    Request andThen(@Nullable Request request);

    /**
     * Returns a parallel multi-request containing this and the specified request, which both must
     * succeed for the result to succeed.
     * <p>
     * For reasons of convenience, the specified request may be {@code null}, in which case this
     * request is returned.
     *
     * @param request the request to add.
     * @return a parallel multi-request, or this request in the absence of an argument.
     */
    Request and(@Nullable Request request);

    /**
     * Performs the specified action when the request failed.  The action takes a failure reason
     * which me be absent.
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
    Request orElse(Consumer<Throwable> action);

    @Override Request renew();

    /**
     * Checks if the request is complete.
     *
     * @return {@code true} if the request is complete, otherwise {@code false}.
     */
    boolean isComplete();

    /**
     * Returns the current request state.
     * <p>
     * <span style="color:red">RequestState is not to be confused with State.  RequestState is used
     * to track progress of the request, while the simple State is used to track message processing
     * <em>before</em> request processing starts taking place.</span>
     * <p>
     * After being created, the request is in state NEW.  While being processing its state changes
     * to SUBMITTED.  If the request is cancelled, its state changes to CANCELLED (a terminal
     * state).  If it fails during processing, it becomes FAILED (a terminal state) and the Problem
     * attribute may receive a meaningful value.  If processing succeeds, the request becomes
     * SUCCESSFUL (a terminal state).
     * <p>
     * Terminal states of a request are also broadcast as events.
     *
     * @return the request state.
     * @see #getState()
     */
    RequestState getRequestState();

    /**
     * Fail the request with a timeout if it has not become successful after the specified amount of
     * time.
     *
     * @param amount the amount of time.
     * @param unit   the unit of time.
     * @param reason the reason for failing.
     * @return this request, for method chaining.
     */
    Request hold(long amount, TimeUnit unit, final Supplier<Exception> reason);

}
