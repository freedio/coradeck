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
import com.coradec.coracom.ctrl.Observer;
import com.coradec.coracom.ctrl.impl.SimpleMessageQueue;
import com.coradec.coracom.model.Asynchronous;
import com.coradec.coracom.model.Command;
import com.coradec.coracom.model.Event;
import com.coradec.coracom.model.Message;
import com.coradec.coracom.model.Recipient;
import com.coradec.coracom.model.Request;
import com.coradec.coracom.model.Sender;
import com.coradec.coracom.state.RequestState;
import com.coradec.coracom.trouble.RequestFailedException;
import com.coradec.coracore.annotation.Implementation;
import com.coradec.coracore.annotation.Nullable;
import com.coradec.coracore.annotation.ToString;
import com.coradec.coracore.trouble.OperationInterruptedException;
import com.coradec.coracore.trouble.OperationTimedoutException;
import com.coradec.coratext.model.LocalizedText;
import com.coradec.coratext.model.Text;

import java.net.URI;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * ​​Basic implementation of a request.
 */
@SuppressWarnings({"ClassHasNoToStringMethod", "PackageVisibleField", "WeakerAccess"})
@Implementation
public class BasicRequest extends BasicEvent implements Request, Asynchronous {

    private static final Text TEXT_MESSAGE_BOUNCED = LocalizedText.define("MessageBounced");
    private static final Text TEXT_NOT_EXECUTING = LocalizedText.define("NotExecuting");
    private static final SimpleMessageQueue MQ = new SimpleMessageQueue();
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
        final Set<Observer> completionObservers = this.completionObservers;
        if (!completionObservers.isEmpty()) {
            RequestCompleteEvent event = new RequestCompleteEventImpl();
            for (final Observer observer : completionObservers) {
                debug("Completion event to %s", observer);
                observer.notify(event);
            }
        }
    }

    private RequestState getRequestState() {
        return requestState;
    }

    private Set<RequestState> getStates() {
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
        MQ.inject(new AddCompletionObserverCommand(observer));
    }

    @Override public Request andThen(final Runnable action) {
        if (isSuccessful()) {
            debug("Exec direct of success action %s", action);
            action.run();
        } else successCallbacks.add(action);
        return this;
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

    @Override public boolean notify(final Event event) {
        if (event instanceof RequestCompleteEvent) {
            final Request request = ((RequestCompleteEvent)event).getRequest();
            if (request.isSuccessful()) succeed();
            if (request.isCancelled()) cancel();
            if (request.isFailed()) fail(getProblem());
        }
        return true;
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

        private final Observer observer;

        /**
         * Initializes a new instance of InternalCommand with the specified completion observer.
         */
        AddCompletionObserverCommand(final Observer observer) {
            this.observer = observer;
        }

        @Override public void execute() {
            if (BasicRequest.this.isComplete()) {
                debug("Sending completion event directly to %s", observer);
                observer.notify(new RequestCompleteEventImpl());
            } else BasicRequest.this.completionObservers.add(observer);
        }

    }

    private class RequestCompleteEventImpl extends BasicEvent implements RequestCompleteEvent {

        /**
         * Initializes a new instance of RequestCompleteEvent.
         */
        public RequestCompleteEventImpl() {
            super(BasicRequest.this);
        }

        @Override @ToString public Request getRequest() {
            return (Request)getSender();
        }

    }

}
