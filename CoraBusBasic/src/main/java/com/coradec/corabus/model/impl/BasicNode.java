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

import static com.coradec.corabus.model.NodeState.*;

import com.coradec.corabus.com.Invitation;
import com.coradec.corabus.model.BusNode;
import com.coradec.corabus.model.NodeState;
import com.coradec.corabus.trouble.NodeAlreadyAttachedException;
import com.coradec.corabus.view.BusContext;
import com.coradec.corabus.view.Member;
import com.coradec.coracom.model.Request;
import com.coradec.coracore.annotation.Inject;
import com.coradec.coracore.annotation.ToString;
import com.coradec.coracore.ctrl.Factory;
import com.coradec.coracore.model.State;
import com.coradec.coracore.util.ClassUtil;
import com.coradec.coractrl.com.ExecuteStateTransitionRequest;
import com.coradec.coractrl.ctrl.StateMachine;
import com.coradec.coractrl.ctrl.impl.BasicAgent;
import com.coradec.coractrl.model.StateTransition;
import com.coradec.coractrl.model.impl.AbstractStateTransition;
import com.coradec.corasession.model.Session;
import com.coradec.corasession.view.impl.AbstractView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

/**
 * ​​Basic implementation of a node.
 */
public class BasicNode extends BasicAgent implements BusNode {

    @Inject private static Factory<Request> REQUEST_FACTORY;

    @SuppressWarnings("WeakerAccess") @Inject StateMachine stateMachine;
    private NodeState state;
    private BusContext contact, context;
    private final BusContext defaultContext;

    @SuppressWarnings("WeakerAccess") public BasicNode() {
        state = UNATTACHED;
        context = defaultContext = new DefaultBusContext();
        stateMachine.initialize(state);
        addRoute(Invitation.class, this::invite);
        addRoute(ExecuteStateTransitionRequest.class, this::transit);
    }

    @Override public NodeState getState() {
        return state;
    }

    @SuppressWarnings("WeakerAccess") protected void setState(NodeState state) {
        this.state = state;
    }

    private BusContext getContext() {
        return context;
    }

    private void setContext(final BusContext context) {
        this.context = context;
    }

    /**
     * Returns the setup trajectory for the specified bus context in the context of the specified
     * session according to the specified invitation.
     *
     * @param session    the session context.
     * @param context    the bus context.
     * @param invitation the invitation
     * @return the setup trajectory.
     */
    protected Collection<StateTransition> getSetupTransitions(final Session session,
                                                              final BusContext context,
                                                              final Invitation invitation) {
        return new ArrayList<>(Arrays.asList(new Attaching(session, context),
                new Attached(session, context, invitation), new Initializing(session),
                new Initialized(session)));
    }

    /**
     * Returns the shutdown trajectory in the context of the specified session.
     *
     * @param session the session context.
     * @return the shutdown trajectory.
     */
    protected Collection<StateTransition> getShutdownTransitions(final Session session) {
        return new ArrayList<>(Arrays.asList(new Terminating(session), new Terminated(session),
                new Detaching(session), new Detached(session)));
    }

    /**
     * Returns the state that needs to be attained before the node can be considered ready for
     * operation.
     *
     * @return the ready or UP state.
     */
    protected State getReadyState() {
        return INITIALIZED;
    }

    private void transit(final ExecuteStateTransitionRequest request) {
        final StateTransition transition = request.getTransition();
//        debug("Executing state transition %s", transition);
        try {
            final Optional<Request> result = transition.execute();
            if (result.isPresent()) result.get().reportCompletionTo(request);
            else request.succeed();
        } catch (Exception e) {
            request.fail(e);
        }
    }

    @SuppressWarnings("WeakerAccess") private void invite(final Invitation invitation) {
        Session session = invitation.getSession();
        BusContext context = invitation.getContext();
        stateMachine.addTransitions(getSetupTransitions(session, context, invitation));
        stateMachine.addTransitions(getShutdownTransitions(session));
        stateMachine.setTargetState(getReadyState());
        stateMachine.start().reportCompletionTo(invitation);
    }

    /**
     * Interceptor invoked during the state transition from UNATTACHED to ATTACHING.
     * <p>
     * Can be used to check for preconditions to attaching to the specified context in the context
     * of the specified session.  If the conditions are not met, the method is suggested to throw
     * RequestRefused, AccessDenied or any other suitable exception — an exception being thrown here
     * must be anticipated by the requester.
     * <p>
     * In particular, the base method checks if the node is already attached; if that is the case,
     * the method throws NodeAlreadyAttachedException.  Furthermore, the base method checks if the
     * context is privileged enough to take the node aboard.
     * <p>
     * Subclasses can wrap this method early (i.e. override it and invoke the superclass method as
     * soon as possible.
     *
     * @param session the session context.
     * @param context the bus context to attach to.
     */
    @SuppressWarnings("WeakerAccess") protected void onAttaching(final Session session,
                                                                 final BusContext context) {
        if (this.context != defaultContext) throw new NodeAlreadyAttachedException();
        contact = context;
        setState(ATTACHING);
    }

    /**
     * Interceptor invoked during the state transition from ATTACHING to ATTACHED.
     * <p>
     * The base method creates a member context and registers it in the invitation.
     * <p>
     * Can be used to do post-attachment work using the specified bus context in the context of the
     * specified session.  Exceptions should not be thrown by this method except if something really
     * bad happens or a fraud attempt is detected.
     * <p>
     * In particular, the base method sets the context field to the specified context and makes sure
     * that the context specified here is the same as the one in
     * {@link #onAttaching(Session, BusContext)}.
     * <p>
     * Subclasses can wrap this method early (i.e. override it and invoke the superclass method as
     * soon as possible.
     *
     * @param session    the session context.
     * @param context    the bus context to attach to.
     * @param invitation the invitation to accept.
     */
    @SuppressWarnings("WeakerAccess") protected void onAttach(final Session session,
                                                              final BusContext context,
                                                              final Invitation invitation) {
        if (contact != context) throw new IllegalArgumentException("context");
        this.context.left(this);
        setContext(context);
        context.joined(this);
        setState(ATTACHED);
        invitation.setMember(new InternalMember(session));
    }

    /**
     * Interceptor invoked during the state transition from ATTACHED to INITIALIZING.
     * <p>
     * Can be used to do preliminary material checks, like requesting services and resources
     * the node used, as well as announcing services and resources it provides.
     * <p>
     * The base method currently does nothing, but might do so in an advanced version.
     * <p>
     * Subclasses can wrap this method early (i.e. override it and invoke the superclass method as
     * soon as possible.
     *
     * @param session the session context.
     */
    @SuppressWarnings("WeakerAccess") protected void onInitializing(final Session session) {
        setState(INITIALIZING);
    }

    /**
     * Interceptor invoked during the state transition from INITIALIZING to INITIALIZED.
     * <p>
     * Can be used to do initialization work.
     * <p>
     * The base method currently does nothing, but this might change in future versions.
     * <p>
     * Subclasses can wrap this method early (i.e. override it and invoke the superclass method as
     * soon as possible.
     *
     * @param session the session context.
     */
    @SuppressWarnings("WeakerAccess") protected void onInitialize(final Session session) {
        setState(INITIALIZED);
    }

    /**
     * Interceptor invoked during the state transition from INITIALIZED to TERMINATING.
     * <p>
     * Can be used to initiate shutdown, e.g. closing resources and services.
     * <p>
     * The base method currently does nothing, but this might change in future versions.
     * <p>
     * Subclasses can wrap this method late (i.e. override it and invoke the superclass method as
     * late as possible, possibly from within a finally-block..
     *
     * @param session the session context.
     */
    @SuppressWarnings("WeakerAccess") protected void onTerminating(final Session session) {
        setState(TERMINATING);
    }

    /**
     * Interceptor invoked during the state transition from TERMINATING to TERMINATED.
     * <p>
     * Can be used to prepare the node for solitaire state.
     * <p>
     * The base method currently does nothing, but this might change in future versions.
     * <p>
     * Subclasses can wrap this method late (i.e. override it and invoke the superclass method as
     * late as possible, possibly from within a finally-block..
     *
     * @param session the session context.
     */
    @SuppressWarnings("WeakerAccess") protected void onTerminate(final Session session) {
        setState(TERMINATED);
    }

    /**
     * Interceptor invoked during the state transition from TERMINATED to DETACHING.
     * <p>
     * Can be used to initiate cleanup before the node gets abandoned.
     * <p>
     * The base method currently does nothing, but this might change in future versions.
     * <p>
     * Subclasses can wrap this method late (i.e. override it and invoke the superclass method as
     * late as possible, possibly from within a finally-block..
     *
     * @param session the session context.
     */
    @SuppressWarnings("WeakerAccess") protected void onDetaching(final Session session) {
        setState(DETACHING);
    }

    /**
     * Interceptor invoked during the state transition from DETACHING to DETACHED.
     * <p>
     * Can be used to terminate cleanup before the node gets abandoned.
     * <p>
     * The base method clears the context to its initial state.
     * <p>
     * Subclasses can wrap this method late (i.e. override it and invoke the superclass method as
     * late as possible, possibly from within a finally-block..
     *
     * @param session the session context.
     */
    @SuppressWarnings("WeakerAccess") void onDetach(final Session session) {
        this.context.left(this);
        this.context = defaultContext;
        this.context.joined(this);
        setState(DETACHED);
    }

    @SuppressWarnings("ClassHasNoToStringMethod")
    protected abstract class NodeStateTransition extends AbstractStateTransition {

        private final Session session;

        NodeStateTransition(final Session session, final State initialState,
                            final State terminalState) {
            super(initialState, terminalState);
            this.session = session;
        }

        @ToString Session getSession() {
            return this.session;
        }

        @Override public Optional<Request> execute() {
            debug("%s: %s → %s", BasicNode.this.getClass().getSimpleName(), getInitialState(),
                    getTerminalState());
            onExecute();
            return Optional.empty();
        }

        protected abstract void onExecute();

    }

    @SuppressWarnings({"WeakerAccess", "ClassHasNoToStringMethod"})
    private class Attaching extends NodeStateTransition {

        private final BusContext context;

        Attaching(final Session session, final BusContext context) {
            super(session, UNATTACHED, ATTACHING);
            this.context = context;
        }

        @ToString public BusContext getContext() {
            return context;
        }

        @Override protected void onExecute() {
            onAttaching(getSession(), getContext());
        }
    }

    @SuppressWarnings({"ClassHasNoToStringMethod", "WeakerAccess"})
    private class Attached extends NodeStateTransition {

        private final BusContext context;
        private final Invitation invitation;

        Attached(final Session session, final BusContext context, final Invitation invitation) {
            super(session, ATTACHING, ATTACHED);
            this.context = context;
            this.invitation = invitation;
        }

        @ToString public BusContext getContext() {
            return context;
        }

        @Override protected void onExecute() {
            onAttach(getSession(), getContext(), invitation);
        }
    }

    private class Initializing extends NodeStateTransition {

        Initializing(final Session session) {
            super(session, ATTACHED, INITIALIZING);
        }

        @Override protected void onExecute() {
            onInitializing(getSession());
        }
    }

    private class Initialized extends NodeStateTransition {

        Initialized(final Session session) {
            super(session, INITIALIZING, INITIALIZED);
        }

        @Override protected void onExecute() {
            onInitialize(getSession());
        }
    }

    protected class Terminating extends NodeStateTransition {

        Terminating(final Session session) {
            super(session, INITIALIZED, TERMINATING);
        }

        @Override protected void onExecute() {
            onTerminating(getSession());
        }
    }

    private class Terminated extends NodeStateTransition {

        Terminated(final Session session) {
            super(session, TERMINATING, TERMINATED);
        }

        @Override protected void onExecute() {
            onTerminate(getSession());
        }
    }

    private class Detaching extends NodeStateTransition {

        Detaching(final Session session) {
            super(session, TERMINATED, DETACHING);
        }

        @Override protected void onExecute() {
            onDetaching(getSession());
        }
    }

    private class Detached extends NodeStateTransition {

        Detached(final Session session) {
            super(session, DETACHING, DETACHED);
        }

        @Override protected void onExecute() {
            onDetach(getSession());
        }
    }

    @Override public String toString() {
        return ClassUtil.toString(this);
    }

    @SuppressWarnings("ClassHasNoToStringMethod")
    private class DefaultBusContext implements BusContext {

        private BusNode node;

        @Override public void left(final BusNode node) {
            this.node = null;
        }

        @Override public void joined(final BusNode node) {
            this.node = node;
        }

        @Override public Optional<BusNode> getNode() {
            return Optional.ofNullable(node);
        }
    }

    private class InternalMember extends AbstractView implements Member {

        /**
         * Initializes a new instance of InternalMember with the specified session context.
         *
         * @param session the session context.
         */
        InternalMember(final Session session) {
            super(session);
        }

        @Override public Request dismiss() {
            stateMachine.setTargetState(DETACHED);
            return stateMachine.start();
        }
    }
}
