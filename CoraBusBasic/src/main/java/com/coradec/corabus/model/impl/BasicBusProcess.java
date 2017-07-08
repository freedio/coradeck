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

import static com.coradec.corabus.model.ProcessState.*;

import com.coradec.corabus.com.Invitation;
import com.coradec.corabus.com.Resumption;
import com.coradec.corabus.com.Suspension;
import com.coradec.corabus.view.BusContext;
import com.coradec.coracore.model.State;
import com.coradec.coractrl.model.StateTransition;
import com.coradec.corasession.model.Session;

import java.util.Collection;
import java.util.Collections;

/**
 * ​​Basic implementation of a bus process.
 */
public class BasicBusProcess extends BasicNode implements BusProcess {

    @SuppressWarnings("WeakerAccess") public BasicBusProcess() {
        addRoute(Suspension.class, this::suspend);
        addRoute(Resumption.class, this::resume);
    }

    @Override protected Collection<StateTransition> getSetupTransitions(final Session session,
                                                                        final BusContext context,
                                                                        final Invitation
                                                                                invitation) {
        final Collection<StateTransition> result =
                super.getSetupTransitions(session, context, invitation);
        Collections.addAll(result, new Starting(session), new Started(session),
                new Suspending(session), new Suspended(session), new Resuming(session),
                new Resumed(session), new Running(session));
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
     * Interceptor invoked during the state transition from INITIALIZED to STARTING.
     * <p>
     * Can be used to do preliminary start checks.
     * <p>
     * The base method invokes START on all runnable children.
     * <p>
     * Subclasses can wrap this method early (i.e. override it and invoke the superclass method as
     * soon as possible.
     *
     * @param session the session context.
     */
    @SuppressWarnings("WeakerAccess") protected void onStarting(final Session session) {
        setState(STARTING);
    }

    /**
     * Interceptor invoked during the state transition from STARTING to STARTED.
     * <p>
     * Can be used to do after-start setup.
     * <p>
     * The base method waits until all runnable children have started.
     * <p>
     * Subclasses can wrap this method early (i.e. override it and invoke the superclass method as
     * soon as possible.
     *
     * @param session the session context.
     */
    @SuppressWarnings("WeakerAccess") protected void onStart(final Session session) {
        setState(STARTED);
    }

    /**
     * Interceptor invoked during the state transition from STARTED to STOPPING.
     * <p>
     * Can be used to do pre-stop checks.
     * <p>
     * The base method invokes stop on all children.
     * <p>
     * Subclasses can wrap this method late (i.e. override it and invoke the superclass method as
     * late as possible.
     *
     * @param session the session context.
     */
    @SuppressWarnings("WeakerAccess") void onStopping(final Session session) {
        setState(STOPPING);
    }

    /**
     * Interceptor invoked during the state transition from STOPPING to STOPPED.
     * <p>
     * Can be used to do post-stop work.
     * <p>
     * The base method waits until all children have stopped.
     * <p>
     * Subclasses can wrap this method late (i.e. override it and invoke the superclass method as
     * late as possible.
     *
     * @param session the session context.
     */
    @SuppressWarnings("WeakerAccess") void onStop(final Session session) {
        setState(STOPPED);
    }

    /**
     * Interceptor invoked during the state transition from SUSPENDED to RESUMING.
     * <p>
     * Can be used to do preliminary resume checks.
     * <p>
     * The base method invokes RESUME on all runnable children.
     * <p>
     * Subclasses can wrap this method early (i.e. override it and invoke the superclass method as
     * soon as possible.
     *
     * @param session the session context.
     */
    @SuppressWarnings("WeakerAccess") protected void onResuming(final Session session) {
        setState(RESUMING);
    }

    /**
     * Interceptor invoked during the state transition from RESUMING to RESUMED.
     * <p>
     * Can be used to do after-resume setup.
     * <p>
     * The base method waits until all runnable children have started.
     * <p>
     * Subclasses can wrap this method early (i.e. override it and invoke the superclass method as
     * soon as possible.
     *
     * @param session the session context.
     */
    @SuppressWarnings("WeakerAccess") protected void onResume(final Session session) {
        setState(RESUMED);
    }

    /**
     * Interceptor invoked during the state transition from STARTED to SUSPENDING.
     * <p>
     * Can be used to do pre-suspend checks.
     * <p>
     * The base method invokes SUSPEND on all children.
     * <p>
     * Subclasses can wrap this method late (i.e. override it and invoke the superclass method as
     * late as possible.
     *
     * @param session the session context.
     */
    @SuppressWarnings("WeakerAccess") void onSuspending(final Session session) {
        setState(SUSPENDING);
    }

    /**
     * Interceptor invoked during the state transition from SUSPENDING to SUSPENDED.
     * <p>
     * Can be used to do post-suspend work.
     * <p>
     * The base method waits until all children are suspended.
     * <p>
     * Subclasses can wrap this method late (i.e. override it and invoke the superclass method as
     * late as possible.
     *
     * @param session the session context.
     */
    @SuppressWarnings("WeakerAccess") void onSuspend(final Session session) {
        setState(SUSPENDED);
    }

    private class Starting extends NodeStateTransition {

        Starting(final Session session) {
            super(session, INITIALIZED, STARTING);
        }

        @Override protected void onExecute() {
            onStarting(getSession());
        }

    }

    private class Started extends NodeStateTransition {

        Started(final Session session) {
            super(session, STARTING, STARTED);
        }

        @Override protected void onExecute() {
            onStart(getSession());
        }

    }

    private class Stopping extends NodeStateTransition {

        Stopping(final Session session) {
            super(session, STARTED, STOPPING);
        }

        @Override protected void onExecute() {
            onStopping(getSession());
        }

    }

    private class Stopped extends NodeStateTransition {

        Stopped(final Session session) {
            super(session, STOPPING, STOPPED);
        }

        @Override protected void onExecute() {
            onStop(getSession());
        }

    }

    private class Suspending extends NodeStateTransition {

        Suspending(final Session session) {
            super(session, STARTED, SUSPENDING);
        }

        @Override protected void onExecute() {
            onSuspending(getSession());
        }

    }

    private class Suspended extends NodeStateTransition {

        Suspended(final Session session) {
            super(session, SUSPENDING, SUSPENDED);
        }

        @Override protected void onExecute() {
            onSuspend(getSession());
        }

    }

    private class Resuming extends NodeStateTransition {

        Resuming(final Session session) {
            super(session, SUSPENDED, RESUMING);
        }

        @Override protected void onExecute() {
            onResuming(getSession());
        }

    }

    private class Resumed extends NodeStateTransition {

        Resumed(final Session session) {
            super(session, RESUMING, RESUMED);
        }

        @Override protected void onExecute() {
            onResume(getSession());
        }

    }

    private class Running extends NodeStateTransition {

        Running(final Session session) {
            super(session, RESUMED, STARTED);
        }

        @Override protected void onExecute() {
            setState(STARTED);
        }

    }

    private class Terminating extends NodeStateTransition {

        Terminating(final Session session) {
            super(session, STOPPED, TERMINATING);
        }

        @Override protected void onExecute() {
            onTerminating(getSession());
        }
    }

}
