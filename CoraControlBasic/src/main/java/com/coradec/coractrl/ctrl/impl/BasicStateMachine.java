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

package com.coradec.coractrl.ctrl.impl;

import static java.util.stream.Collectors.*;

import com.coradec.coracom.model.Recipient;
import com.coradec.coracom.model.Request;
import com.coradec.coracom.model.impl.BasicRequest;
import com.coradec.coracore.annotation.Implementation;
import com.coradec.coracore.annotation.Inject;
import com.coradec.coracore.annotation.ToString;
import com.coradec.coracore.ctrl.Factory;
import com.coradec.coracore.model.State;
import com.coradec.coracore.util.ClassUtil;
import com.coradec.coractrl.com.ExecuteStateTransitionRequest;
import com.coradec.coractrl.com.StartStateMachineRequest;
import com.coradec.coractrl.ctrl.StateMachine;
import com.coradec.coractrl.ctrl.Trajectory;
import com.coradec.coractrl.model.StateTransition;
import com.coradec.coractrl.trouble.StateMachineStalledException;
import com.coradec.coratext.model.LocalizedText;
import com.coradec.coratext.model.Text;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

/**
 * ​​Basic implementation of a state machine.
 */
@Implementation
public class BasicStateMachine extends BasicAgent implements StateMachine {

    private static final Text TEXT_NO_TRANSITIONS = LocalizedText.define("NoTransitions");
    private static final Text TEXT_INVALID_STATE = LocalizedText.define("InvalidState");
    @Inject private static Factory<Trajectory> TRAJECTORY_FACTORY;

    private final Recipient agent;
    @SuppressWarnings("WeakerAccess") State currentState, targetState;
    private final Set<StateTransition> transitions = new HashSet<>();
    private final Set<Trajectory> trajectories = new HashSet<>();

    /**
     * Initializes a new instance of BasicStateMachine on behalf of the specified recipient.
     *
     * @param owner the recipient.
     */
    @SuppressWarnings("WeakerAccess") public BasicStateMachine(Recipient owner) {
        this.agent = owner;
        addRoute(StartStateMachineRequest.class, this::doStart);
    }

    @Override @ToString public State getCurrentState() {
        return currentState;
    }

    @Override @ToString public State getTargetState() {
        return targetState;
    }

    @Override public void initialize(final State state) {
        currentState = targetState = state;
    }

    private Set<StateTransition> getTransitions() {
        return transitions;
    }

    @Override public void addTransitions(final Collection<StateTransition> transitions) {
        stop();
        this.transitions.addAll(transitions);
    }

    @Override public void setTargetState(final State state) {
        stop();
        this.targetState = state;
    }

    private void doStart(InternalStartMachineRequest request) {
        checkPrerequisites();
        computeTrajectories();
        proceed(request);
    }

    private void checkPrerequisites() {
        if (transitions.isEmpty()) throw new IllegalStateException(TEXT_NO_TRANSITIONS.resolve());
        if (currentState == null || targetState == null) throw new IllegalStateException(
                TEXT_INVALID_STATE.resolve(getCurrentState(), getTargetState()));
    }

    @Override public StartStateMachineRequest start() {
        return inject(new InternalStartMachineRequest());
    }

    Set<Trajectory> getTrajectories() {
        if (trajectories.isEmpty()) {
            checkPrerequisites();
            computeTrajectories();
        }
        return trajectories;
    }

    /**
     * Stops the state machine.
     */
    private void stop() {

    }

    /**
     * Computes the trajectories from the current state to the target state.
     */
    private void computeTrajectories() {
        final Set<Trajectory> trajectories;
        try {
            trajectories =
                    getTransitionsTo(getTargetState()).map(this::trajectoryFrom).collect(toSet());
            // extract direct trajectory, if any:
            for (Iterator<Trajectory> it = trajectories.iterator(); it.hasNext(); ) {
                final Trajectory trajectory = it.next();
                if (trajectory.connects(getCurrentState(), getTargetState())) {
                    this.trajectories.add(trajectory);
                    it.remove();
                    break;
                }
            }
            findTrajectories(getCurrentState(), trajectories);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates a suitable trajectory from the specified state transition.
     *
     * @param transition the state transition.
     * @return a trajectory containing only the specified transition.
     */
    private Trajectory trajectoryFrom(final StateTransition transition) {
        return TRAJECTORY_FACTORY.create(transition);
    }

    /**
     * Develop the specified set of trajectories backwards towards the specified initial state.
     * <p>
     * Complete trajectories will be stored in the (@code trajectories} field, cyclic trajectories
     * will be dropped.
     *
     * @param from the initial state.
     * @param trs  the set of building trajectories.
     */
    private void findTrajectories(final State from, final Set<Trajectory> trs) {
        if (!trs.isEmpty()) findTrajectories(from, //
                trs.stream()
                   .flatMap(trajectory -> getTransitionsTo(trajectory.getInitialState()).map(
                           trajectory::prepend))
                   .map(trajectory -> {
                       if (trajectory.startsWith(from)) {
                           trajectories.add(trajectory);
                           return null;
                       }
                       return trajectory;
                   })
                   .filter(trajectory -> trajectory != null && !trajectory.isCyclic())
                   .distinct()
                   .collect(toSet()));
    }

    private Stream<StateTransition> getTransitionsTo(final State state) {
        return getTransitions().stream().filter(transition -> transition.endsWith(state));
    }

    /**
     * Performs the next state transition in the context of the specified request.
     *
     * @param request the request.
     */
    private void proceed(final InternalStartMachineRequest request)
            throws StateMachineStalledException {
        while (getCurrentState() != getTargetState()) {
            debug("Proceeding from %s to %s", getCurrentState(), getTargetState());
            final State state = getCurrentState();
            request.addState(state);
            try {
                final StateTransition transition = //
                        getTrajectories().stream()
                                         .filter(t -> t.isViable(request, state))
                                         .map(ty -> ty.transitionFor(state).orElse(null))
                                         .filter(Objects::nonNull)
                                         .sorted()
                                         .findFirst()
                                         .orElseThrow(
                                                 () -> new StateMachineStalledException(state));
//            debug("Proceeding from %s to %s", transition.getInitialState(),
//                    transition.getTerminalState());
                inject(new InternalExecuteStateTransitionRequest(agent, transition)).andThen(() -> {
//                    debug("Proceed → succees");
                    final State newState = transition.getTerminalState();
                    setCurrentState(newState);
                    if (newState == getTargetState()) {
                        debug("Trajectory successful, reached state %s", newState);
                        request.addState(newState);
                        request.succeed();
                    }
                }).orElse(problem -> {
//                    debug("Proceed → fail with %s", problem);
                    blockTransition(request, transition);
                }).standby();
            } catch (Exception e) {
                error(e);
                request.fail(e);
                break;
            }
//            debug("Proceed to next");
        }
    }

    private void blockTransition(final Request request, final StateTransition transition) {
        getTrajectories().stream()
                         .filter(trajectory -> trajectory.contains(transition))
                         .forEach(trajectory -> trajectory.block(request));
    }

    @Override public String toString() {
        return ClassUtil.toString(this);
    }

    private void setCurrentState(final State state) {
        currentState = state;
    }

    private class InternalStartMachineRequest extends BasicRequest
            implements StartStateMachineRequest {

        private final List<State> states = new ArrayList<>();

        /**
         * Initializes a new instance of InternalStartMachineRequest.
         */
        InternalStartMachineRequest() {
            super(BasicStateMachine.this);
            getTrajectories().clear();
        }

        void addState(final State state) {
            states.add(state);
        }

        @Override @ToString public List<State> getPassedStates() {
            return Collections.unmodifiableList(states);
        }

        @Override public String toString() {
            return ClassUtil.toString(this);
        }
    }

    @SuppressWarnings("ClassHasNoToStringMethod")
    private class InternalExecuteStateTransitionRequest extends BasicRequest
            implements ExecuteStateTransitionRequest {

        private final StateTransition transition;

        InternalExecuteStateTransitionRequest(final Recipient agent,
                                              final StateTransition transition) {
            super(BasicStateMachine.this, agent);
            this.transition = transition;
        }

        @ToString public StateTransition getTransition() {
            return transition;
        }

    }

}
