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

import static com.coradec.coracom.state.RequestState.*;

import com.coradec.coracom.ctrl.Observer;
import com.coradec.coracom.ctrl.impl.SimpleMessageQueue;
import com.coradec.coracom.model.Asynchronous;
import com.coradec.coracom.model.Message;
import com.coradec.coracom.model.Recipient;
import com.coradec.coracom.model.Request;
import com.coradec.coracom.model.Sender;
import com.coradec.coracom.state.RequestState;
import com.coradec.coracore.annotation.Implementation;
import com.coradec.coracore.annotation.Nullable;
import com.coradec.coracore.trouble.OperationInterruptedException;
import com.coradec.coracore.trouble.OperationTimedoutException;
import com.coradec.coratext.model.LocalizedText;
import com.coradec.coratext.model.Text;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * ​​Basic implementation of a request.
 */
@SuppressWarnings({"ClassHasNoToStringMethod", "PackageVisibleField", "WeakerAccess"})
@Implementation
public class BasicRequest extends BasicEvent implements Request, Asynchronous {

    private static final Text TEXT_MESSAGE_BOUNCED = LocalizedText.define("MessageBounced");
    private static SimpleMessageQueue MQ;

    private final Semaphore completion = new Semaphore(0);
    final ReentrantLock stateLock = new ReentrantLock();
    private @Nullable Exception problem;
    final Set<Observer> completionObservers = new HashSet<>();
    private final Set<RequestState> states = new HashSet<>();

    /**
     * Initializes a new instance of BasicRequest with the specified sender and list of recipients.
     *
     * @param sender     the sender.
     * @param recipients the list of recipients
     */
    public BasicRequest(final Sender sender, final Recipient... recipients) {
        super(sender, recipients);
    }

    protected Request setState(final RequestState state) {
        stateLock.lock();
        super.setState(state);
        stateLock.unlock();
        return this;
    }

    private Set<RequestState> getStates() {
        return states;
    }

    @Override public Request standby(final long amount, final TimeUnit unit)
            throws OperationTimedoutException, OperationInterruptedException {
        try {
            if (!completion.tryAcquire(amount, unit)) throw new OperationTimedoutException();
        } catch (InterruptedException e) {
            throw new OperationInterruptedException();
        }
        return this;
    }

    @Override public @Nullable Throwable getProblem() {
        return problem;
    }

    @Override public void succeed() {
        setState(SUCCESSFUL).andThen(completion::release);
    }

    @Override public void fail(final Throwable problem) {
        setState(FAILED).andThen(completion::release);
    }

    @Override public void cancel() {
        setState(CANCELLED).andThen(completion::release);
    }

    @Override public boolean isSuccessful() {
        return getStates().contains(SUCCESSFUL);
    }

    @Override public boolean isFailed() {
        return getStates().contains(FAILED);
    }

    @Override public boolean isCancelled() {
        return getStates().contains(CANCELLED);
    }

    @Override public void reportCompletionTo(final Request request) {
        addCompletionObserver(request);
    }

    private void addCompletionObserver(final Request request) {
        MQ.inject(new AddCompletionObserverCommand(request));
    }

    @Override public Request andThen(final Runnable action) {
        return null;
    }

    @Override public Request orElse(final Runnable action) {
        return null;
    }

    @Override public String represent() {
        return String.format("%s#%s", getClass().getName(), getId());
    }

    @Override public void onMessage(final Message message) {

    }

    @Override public URI toURI() {
        return URI.create(represent());
    }

    @Override public void bounce(final Message message) {
        error(TEXT_MESSAGE_BOUNCED, message);
    }

    private abstract class InternalCommand extends AbstractCommand {

        /**
         * Initializes a new instance of InternalCommand with the specified sender and list of
         * recipients.
         */
        InternalCommand() {
            super(BasicRequest.this);
        }

        @Override public boolean isUrgent() {
            return true;
        }
    }

    private class AddCompletionObserverCommand extends InternalCommand {

        private final Request completionObserver;

        /**
         * Initializes a new instance of InternalCommand with the specified completion observer.
         */
        AddCompletionObserverCommand(final Request completionObserver) {
            this.completionObserver = completionObserver;
        }

        @Override public void execute() {
            stateLock.lock();
            if (isSuccessful()) completionObserver.succeed();
            else if (isFailed()) completionObserver.fail(getProblem());
            else if (isCancelled()) completionObserver.cancel();
            else completionObservers.add(completionObserver);
        }

    }

}
