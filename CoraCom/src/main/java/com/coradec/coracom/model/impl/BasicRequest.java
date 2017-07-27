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

import com.coradec.coracom.com.RequestCompleteEvent;
import com.coradec.coracom.ctrl.MessageQueue;
import com.coradec.coracom.ctrl.Observer;
import com.coradec.coracom.model.Asynchronous;
import com.coradec.coracom.model.Command;
import com.coradec.coracom.model.Deferred;
import com.coradec.coracom.model.Information;
import com.coradec.coracom.model.Message;
import com.coradec.coracom.model.ParallelMultiRequest;
import com.coradec.coracom.model.Recipient;
import com.coradec.coracom.model.Request;
import com.coradec.coracom.model.Sender;
import com.coradec.coracom.model.SerialMultiRequest;
import com.coradec.coracom.state.RequestState;
import com.coradec.coracom.trouble.RequestCancelledException;
import com.coradec.coracom.trouble.RequestFailedException;
import com.coradec.coracore.annotation.Implementation;
import com.coradec.coracore.annotation.Inject;
import com.coradec.coracore.annotation.Nullable;
import com.coradec.coracore.annotation.ToString;
import com.coradec.coracore.model.Factory;
import com.coradec.coracore.trouble.OperationInterruptedException;
import com.coradec.coracore.trouble.OperationTimedoutException;
import com.coradec.coratext.model.LocalizedText;
import com.coradec.coratext.model.Text;

import java.net.URI;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * ​​Basic implementation of a request.
 */
@SuppressWarnings({"ClassHasNoToStringMethod", "PackageVisibleField", "WeakerAccess"})
@Implementation
public class BasicRequest extends BasicMessage implements Request, Asynchronous {

    private static final Text TEXT_MESSAGE_BOUNCED = LocalizedText.define("MessageBounced");
    private static final Text TEXT_NOT_EXECUTING = LocalizedText.define("NotExecuting");
    @Inject private static Factory<MessageQueue> MQ;
    @Inject private static Factory<ParallelMultiRequest> PARALLEL_MULTI_REQUEST;
    @Inject private static Factory<SerialMultiRequest> Serial_MULTI_REQUEST;
    private static final Text TEXT_CANNOT_HANDLE_MESSAGE =
            LocalizedText.define("CannotHandleMessage");

    RequestState requestState;
    final Semaphore completion = new Semaphore(0);
    @Nullable Throwable problem;
    final Set<Observer> completionObservers = new HashSet<>();
    Set<Runnable> successCallbacks = new CopyOnWriteArraySet<>();
    Set<Consumer<Throwable>> failureCallbacks = new CopyOnWriteArraySet<>();
    final Set<RequestState> states = new HashSet<>();

    /**
     * Initializes a new instance of BasicRequest with the specified sender and list of recipients.
     *
     * @param sender     the sender.
     * @param recipients the list of recipients
     */
    public BasicRequest(final Sender sender, final Recipient... recipients) {
        super(sender, recipients);
        requestState = NEW;
    }

    protected void setRequestState(final RequestState state) {
        this.requestState = state;
        this.states.add(state);
        if (state == SUCCESSFUL) {
            if (!this.successCallbacks.isEmpty()) {
                for (final Runnable successCallback : this.successCallbacks) {
                    try {
//                            debug("success >> %s", successCallback);
                        successCallback.run();
                    } catch (Exception e) {
                        error(e);
                    }
                }
                this.successCallbacks.clear();
            }
            sendCompletionEvents();
            this.completion.release();
        } else if (state == FAILED || state == CANCELLED) {
            if (!this.failureCallbacks.isEmpty()) {
                final Throwable problem = getProblem();
                for (final Consumer<Throwable> failureCallback : this.failureCallbacks) {
                    try {
//                            debug("failure >> %s", failureCallback);
                        failureCallback.accept(problem);
                    } catch (Exception e) {
                        error(e);
                    }
                }
                this.failureCallbacks.clear();
            }
            sendCompletionEvents();
            this.completion.release();
        }
    }

    private void setRequestState(final RequestState state, @Nullable final Throwable problem) {
        this.problem = problem;
        setRequestState(state);
    }

    private void sendCompletionEvents() {
        final Observer[] co = completionObservers.toArray(new Observer[completionObservers.size()]);
        if (co.length > 0) {
            RequestCompleteEvent event = new RequestCompleteEventImpl(this);
            for (final Observer observer : co) {
//                debug("Completion event to %s", observer);
                if (observer.notify(event)) completionObservers.remove(observer);
            }
        }
    }

    @Override @ToString public RequestState getRequestState() {
        return requestState;
    }

    @Override
    public Request hold(final long amount, final TimeUnit unit, final Supplier<Exception> reason) {
        return MQ.get().inject(new FailMeCommand(amount, unit, reason));
    }

    @Override @ToString public Set<RequestState> getStates() {
        return states;
    }

    @Override public @Nullable Throwable getProblem() {
        return problem;
    }

    @Override public Request standby() throws InterruptedException, RequestFailedException {
        try {
            completion.acquire();
        } finally {
            completion.release();
        }
        if (isFailed()) throw Optional.ofNullable(getProblem())
                                      .map(RequestFailedException::new)
                                      .orElseGet(RequestFailedException::new);
        return this;
    }

    @Override public Request standby(final long amount, final TimeUnit unit)
            throws OperationTimedoutException, OperationInterruptedException,
                   RequestFailedException {
        try {
            if (!completion.tryAcquire(amount, unit)) throw new OperationTimedoutException();
        } catch (InterruptedException e) {
            throw new OperationInterruptedException();
        } finally {
            completion.release();
        }
        if (isFailed()) throw Optional.ofNullable(getProblem())
                                      .map(RequestFailedException::new)
                                      .orElseGet(RequestFailedException::new);
        if (isCancelled()) throw new RequestCancelledException();
        return this;
    }

    @Override public void succeed() {
        setRequestState(SUCCESSFUL);
    }

    @Override public void fail(@Nullable final Throwable problem) {
        setRequestState(FAILED, problem);
    }

    @Override public void cancel() {
        setRequestState(CANCELLED);
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

    public boolean isComplete() {
        return getStates().stream()
                          .anyMatch(rs -> rs == SUCCESSFUL || rs == FAILED || rs == CANCELLED);
    }

    @Override public void reportCompletionTo(final Observer observer) {
        inject(new AddCompletionObserverCommand(observer));
    }

    protected <I extends Information> I inject(final I info) {
        return MQ.get().inject(info);
    }

    @Override public Request andThen(final Runnable action) {
        if (isSuccessful()) {
//            debug("Exec direct of success action %s", action);
            action.run();
        } else successCallbacks.add(action);
        return this;
    }

    @Override public Request andThen(final Request request) {
        return request == null ? this : Serial_MULTI_REQUEST.create(Arrays.asList(this, request),
                getSender(), getRecipients());
    }

    @Override public Request and(@Nullable final Request request) {
        return request == null ? this : PARALLEL_MULTI_REQUEST.create(Arrays.asList(this, request),
                getSender(), getRecipients());
    }

    @Override public Request orElse(final Consumer<Throwable> action) {
        if (isFailed() || isCancelled()) {
            debug("Exec direct of failure action %s", action);
            action.accept(getProblem());
        } else failureCallbacks.add(action);
        return this;
    }

    @Override public String represent() {
        return String.format("%s#%s", getClass().getName(), getId());
    }

    @Override public void onMessage(final Message message) {
        if (message.getSender() == this) {
            if (message instanceof Command) {
//                debug("Executing command %s", message);
                final Command command = (Command)message;
                try {
                    command.execute();
                    command.succeed();
                } catch (Exception e) {
                    command.fail(e);
                }
            } else error(TEXT_CANNOT_HANDLE_MESSAGE, message);
        } else error(TEXT_NOT_EXECUTING, message, message.getSender());
    }

    @Override public URI toURI() {
        return URI.create(represent());
    }

    @Override public void bounce(final Message message) {
        error(TEXT_MESSAGE_BOUNCED, message);
    }

    @Override public boolean notify(final Information info) {
        final Request request = ((RequestCompleteEvent)info).getRequest();
        if (request.isSuccessful()) succeed();
        else if (request.isCancelled()) cancel();
        else if (request.isFailed()) fail(request.getProblem());
        return true;
    }

    @Override public boolean wants(final Information info) {
        return info instanceof RequestCompleteEvent;
    }

    private abstract class InternalCommand extends BasicCommand {

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

        private final Observer observer;

        /**
         * Initializes a new instance of InternalCommand with the specified completion observer.
         */
        AddCompletionObserverCommand(final Observer observer) {
            this.observer = observer;
        }

        @Override public void execute() {
            if (BasicRequest.this.isComplete()) {
//                debug("Sending completion event directly to %s", observer);
                observer.notify(new RequestCompleteEventImpl(BasicRequest.this));
            } else {
                BasicRequest.this.completionObservers.add(observer);
                debug("Added completion observer.");
            }
        }

    }

    private class RequestCompleteEventImpl extends BasicEvent implements RequestCompleteEvent {

        private final BasicRequest request;

        /**
         * Initializes a new instance of RequestCompleteEvent.
         *
         * @param request the request to report complete.
         */
        public RequestCompleteEventImpl(final BasicRequest request) {
            super(BasicRequest.this);
            this.request = request;
        }

        @Override @ToString public Request getRequest() {
            return request;
        }

    }

    @Override public Request renew() {
        states.clear();
        setRequestState(NEW);
        return (Request)super.renew();
    }

    private class FailMeCommand extends BasicDeferredCommand implements Command, Deferred {

        private final Supplier<Exception> reason;

        public FailMeCommand(final long amount, final TimeUnit unit,
                final Supplier<Exception> reason) {
            super(amount, unit, BasicRequest.this);
            this.reason = reason;
        }

        @Override public void execute() {
            BasicRequest.this.fail(reason.get());
        }

    }

}
