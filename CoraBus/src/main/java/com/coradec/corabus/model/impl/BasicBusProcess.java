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

package com.coradec.corabus.model.impl;

import static com.coradec.corabus.state.ProcessState.*;

import com.coradec.corabus.com.Invitation;
import com.coradec.corabus.com.Resumption;
import com.coradec.corabus.com.Suspension;
import com.coradec.corabus.model.BusProcess;
import com.coradec.corabus.view.BusContext;
import com.coradec.coracom.model.Request;
import com.coradec.coracore.annotation.Nullable;
import com.coradec.coracore.model.State;
import com.coradec.coracore.trouble.OperationInterruptedException;
import com.coradec.coractrl.model.StateTransition;
import com.coradec.corasession.model.Session;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.Semaphore;

/**
 * ​​Basic implementation of a bus process.
 */
public abstract class BasicBusProcess extends BasicNode implements BusProcess {

    private Thread worker;
    private final Semaphore suspension = new Semaphore(1);

    public BasicBusProcess() {
        addRoute(Suspension.class, this::suspend);
        addRoute(Resumption.class, this::resume);
    }

    @Override protected Collection<StateTransition> getSetupTransitions(final Session session,
            final BusContext context, final Invitation invitation) {
        final Collection<StateTransition> result =
                super.getSetupTransitions(session, context, invitation);
        Collections.addAll(result, new Starting(session), new Started(session),
                new Suspending(session), new Suspended(session), new Resuming(session),
                new Resumed(session), new Restarted(session));
        return result;
    }

    @Override protected Collection<StateTransition> getShutdownTransitions(final Session session) {
        final Collection<StateTransition> result = super.getShutdownTransitions(session);
        Collections.addAll(result, new Stopping(session), new Stopped(session),
                new Terminating(session));
        return result;
    }

    @Override protected State getReadyState() {
        return STARTED;
    }

    private void suspend(final Suspension suspension) {
        final Session session = suspension.getSession();
        stateMachine.setTargetState(SUSPENDED);
        stateMachine.start().reportCompletionTo(suspension);
    }

    private void resume(final Resumption resumption) {
        final Session session = resumption.getSession();
        stateMachine.setTargetState(STARTED);
        stateMachine.start().reportCompletionTo(resumption);
    }

    /**
     * Checks if the application is suspended.  If so, the method will wait until it is resumed.
     *
     * @throws InterruptedException if the method was interrupted while waiting for the suspension
     *                              to be terminate.  The application should exit immediately on
     *                              this exception.
     */
    protected void checkSuspend() throws InterruptedException {
        suspension.acquire();
        suspension.release();
    }

    /**
     * Interceptor invoked during the state transition from INITIALIZED to STARTING.
     * <p>
     * Can be used to do preliminary start checks.
     * <p>
     * The base method invokes START on all runnable children.
     * <p>
     * Subclasses can wrap this method early (i.e. override it and invoke the superclass method as
     * soon as possible).
     * <p>
     * This method usually returns {@code null}; if however some asynchronous work needs to be done
     * in order for it to succeed, the corresponding request can be returned here.  The state
     * machine will wait for the request to complete before moving on.
     *
     * @param session the session context.
     */
    protected @Nullable Request onStarting(final Session session) {
        setState(STARTING);
        return null;
    }

    /**
     * Interceptor invoked during the state transition from STARTING to STARTED.
     * <p>
     * Can be used to do after-start setup.
     * <p>
     * The base method waits until all runnable children have started.
     * <p>
     * Subclasses can wrap this method early (i.e. override it and invoke the superclass method as
     * soon as possible).
     * <p>
     * This method usually returns {@code null}; if however some asynchronous work needs to be done
     * in order for it to succeed, the corresponding request can be returned here.  The state
     * machine will wait for the request to complete before moving on.
     *
     * @param session the session context.
     */
    protected @Nullable Request onStart(final Session session) {
        worker = new Thread(this);
        worker.start();
        setState(STARTED);
        return null;
    }

    /**
     * Interceptor invoked during the state transition from STARTED to RUNNING.
     * <p>
     * Used to wait for the actual process to terminate.
     * <p>
     * The base method waits until the run method returns.
     * <p>
     * Subclasses can wrap this method early (i.e. override it and invoke the superclass method as
     * soon as possible).
     *
     * @param session the session context.
     */
    protected void onExecute(final Session session) {
        setState(RUNNING);
        try {
            worker.join();
        } catch (InterruptedException e) {
            throw new OperationInterruptedException();
        }
    }

    /**
     * Interceptor invoked during the state transition from STARTED to STOPPING.
     * <p>
     * Can be used to do pre-stop checks.
     * <p>
     * The base method invokes stop on all children.
     * <p>
     * Subclasses can wrap this method late (i.e. override it and invoke the superclass method as
     * late as possible).
     * <p>
     * This method usually returns {@code null}; if however some asynchronous work needs to be done
     * in order for it to succeed, the corresponding request can be returned here.  The state
     * machine will wait for the request to complete before moving on.
     *
     * @param session the session context.
     */
    @Nullable Request onStopping(final Session session) {
        setState(STOPPING);
        return null;
    }

    /**
     * Interceptor invoked during the state transition from STOPPING to STOPPED.
     * <p>
     * Can be used to do post-stop work.
     * <p>
     * The base method waits until all children have stopped.
     * <p>
     * Subclasses can wrap this method late (i.e. override it and invoke the superclass method as
     * late as possible).
     * <p>
     * This method usually returns {@code null}; if however some asynchronous work needs to be done
     * in order for it to succeed, the corresponding request can be returned here.  The state
     * machine will wait for the request to complete before moving on.
     *
     * @param session the session context.
     */
    @Nullable Request onStop(final Session session) {
        worker.interrupt();
        setState(STOPPED);
        return null;
    }

    /**
     * Interceptor invoked during the state transition from STARTED to SUSPENDING.
     * <p>
     * Can be used to do pre-suspend checks.
     * <p>
     * The base method invokes SUSPEND on all children.
     * <p>
     * Subclasses can wrap this method late (i.e. override it and invoke the superclass method as
     * late as possible).
     * <p>
     * This method usually returns {@code null}; if however some asynchronous work needs to be done
     * in order for it to succeed, the corresponding request can be returned here.  The state
     * machine will wait for the request to complete before moving on.
     *
     * @param session the session context.
     */
    @Nullable Request onSuspending(final Session session) {
        setState(SUSPENDING);
        return null;
    }

    /**
     * Interceptor invoked during the state transition from SUSPENDING to SUSPENDED.
     * <p>
     * Can be used to do post-suspend work.
     * <p>
     * The base method waits until all children are suspended.
     * <p>
     * Subclasses can wrap this method late (i.e. override it and invoke the superclass method as
     * late as possible).
     * <p>
     * This method usually returns {@code null}; if however some asynchronous work needs to be done
     * in order for it to succeed, the corresponding request can be returned here.  The state
     * machine will wait for the request to complete before moving on.
     *
     * @param session the session context.
     */
    @Nullable Request onSuspend(final Session session) {
        try {
            suspension.acquire();
        } catch (InterruptedException e) {
            throw new OperationInterruptedException();
        }
        setState(SUSPENDED);
        return null;
    }

    /**
     * Interceptor invoked during the state transition from SUSPENDED to RESUMING.
     * <p>
     * Can be used to do preliminary resume checks.
     * <p>
     * The base method invokes RESUME on all runnable children.
     * <p>
     * Subclasses can wrap this method early (i.e. override it and invoke the superclass method as
     * soon as possible).
     * <p>
     * This method usually returns {@code null}; if however some asynchronous work needs to be done
     * in order for it to succeed, the corresponding request can be returned here.  The state
     * machine will wait for the request to complete before moving on.
     *
     * @param session the session context.
     */
    protected @Nullable Request onResuming(final Session session) {
        setState(RESUMING);
        return null;
    }

    /**
     * Interceptor invoked during the state transition from RESUMING to RESUMED.
     * <p>
     * Can be used to do after-resume setup.
     * <p>
     * The base method waits until all runnable children have started.
     * <p>
     * Subclasses can wrap this method early (i.e. override it and invoke the superclass method as
     * soon as possible).
     * <p>
     * This method usually returns {@code null}; if however some asynchronous work needs to be done
     * in order for it to succeed, the corresponding request can be returned here.  The state
     * machine will wait for the request to complete before moving on.
     *
     * @param session the session context.
     */
    protected @Nullable Request onResume(final Session session) {
        suspension.release();
        setState(RESUMED);
        return null;
    }

    private class Starting extends NodeStateTransition {

        Starting(final Session session) {
            super(session, INITIALIZED, STARTING);
        }

        @Override protected @Nullable Request onExecute() {
            return onStarting(getSession());
        }

    }

    private class Started extends NodeStateTransition {

        Started(final Session session) {
            super(session, STARTING, STARTED);
        }

        @Override protected @Nullable Request onExecute() {
            return onStart(getSession());
        }

    }

    private class Stopping extends NodeStateTransition {

        Stopping(final Session session) {
            super(session, STARTED, STOPPING);
        }

        @Override protected @Nullable Request onExecute() {
            return onStopping(getSession());
        }

    }

    private class Stopped extends NodeStateTransition {

        Stopped(final Session session) {
            super(session, STOPPING, STOPPED);
        }

        @Override protected @Nullable Request onExecute() {
            return onStop(getSession());
        }

    }

    private class Suspending extends NodeStateTransition {

        Suspending(final Session session) {
            super(session, STARTED, SUSPENDING);
        }

        @Override protected @Nullable Request onExecute() {
            return onSuspending(getSession());
        }

    }

    private class Suspended extends NodeStateTransition {

        Suspended(final Session session) {
            super(session, SUSPENDING, SUSPENDED);
        }

        @Override protected @Nullable Request onExecute() {
            return onSuspend(getSession());
        }

    }

    private class Resuming extends NodeStateTransition {

        Resuming(final Session session) {
            super(session, SUSPENDED, RESUMING);
        }

        @Override protected @Nullable Request onExecute() {
            return onResuming(getSession());
        }

    }

    private class Resumed extends NodeStateTransition {

        Resumed(final Session session) {
            super(session, RESUMING, RESUMED);
        }

        @Override protected @Nullable Request onExecute() {
            return onResume(getSession());
        }

    }

    private class Restarted extends NodeStateTransition {

        Restarted(final Session session) {
            super(session, RESUMED, STARTED);
        }

        @Override protected @Nullable Request onExecute() {
            setState(STARTED);
            return null;
        }

    }

    private class Terminating extends NodeStateTransition {

        Terminating(final Session session) {
            super(session, STOPPED, TERMINATING);
        }

        @Override protected @Nullable Request onExecute() {
            return onTerminating(getSession());
        }
    }

}
