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

import static java.util.concurrent.TimeUnit.*;
import static java.util.stream.Collectors.*;

import com.coradec.coracom.model.Recipient;
import com.coradec.coracom.model.Request;
import com.coradec.coracom.model.impl.BasicCommand;
import com.coradec.coracom.model.impl.BasicEvent;
import com.coradec.coracom.model.impl.BasicRequest;
import com.coradec.coracore.annotation.Implementation;
import com.coradec.coracore.annotation.Inject;
import com.coradec.coracore.annotation.ToString;
import com.coradec.coracore.model.Factory;
import com.coradec.coracore.model.State;
import com.coradec.coracore.trouble.OperationInterruptedException;
import com.coradec.coracore.util.ClassUtil;
import com.coradec.coractrl.com.ExecuteStateTransitionRequest;
import com.coradec.coractrl.com.StartStateMachineRequest;
import com.coradec.coractrl.com.StateMachineReachedStateEvent;
import com.coradec.coractrl.ctrl.Executable;
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
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Stream;

/**
 * ​​Basic implementation of a state machine.
 */
@Implementation
public class BasicStateMachine extends BasicAgent implements StateMachine {

    private static final Text TEXT_NO_TRANSITIONS = LocalizedText.define("NoTransitions");
    private static final Text TEXT_INVALID_STATE = LocalizedText.define("InvalidState");
    @Inject private static Factory<Trajectory> TRAJECTORY_FACTORY;

    final Recipient agent;
    State currentState, targetState;
    private final Set<StateTransition> transitions = new HashSet<>();
    volatile Set<Trajectory> trajectories;

    /**
     * Initializes a new instance of BasicStateMachine on behalf of the specified recipient.
     *
     * @param owner the recipient.
     */
    public BasicStateMachine(Recipient owner) {
        this.agent = owner;
        addRoute(StartStateMachineRequest.class, this::doStart);
        approve(PerformStateTransitionCommand.class);
    }

    @Override @ToString public State getCurrentState() {
        return currentState;
    }

    protected void setCurrentState(final State currentState) {
        this.currentState = currentState;
        inject(new InternalStateMachineReachedStateEvent(currentState));
    }

    @Override @ToString public State getTargetState() {
        return targetState;
    }

    @Override public void initialize(final State state) {
        execute(() -> {
            setCurrentState(targetState = state);
//            debug("%s: CurrentState and TargetState initialized to %s", this, state);
        });
    }

    private Set<StateTransition> getTransitions() {
        return transitions;
    }

    @Override public void addTransitions(final Collection<StateTransition> transitions) {
        execute(() -> {
            this.transitions.addAll(transitions);
            trajectories = null;
        });
    }

    @Override public void setTargetState(final State state) {
        execute(() -> {
            this.targetState = state;
            trajectories = null;
        });
    }

    private void doStart(InternalStartMachineRequest request) {
//        debug("%s: request to start %s → %s", this, getCurrentState(), getTargetState());
        if (getCurrentState() == getTargetState()) request.succeed();
        else try {
            checkPrerequisites();
            computeTrajectories();
            proceed(request);
        } catch (Exception e) {
            request.fail(e);
        }
    }

    private void checkPrerequisites() {
        if (transitions.isEmpty()) throw new IllegalStateException(TEXT_NO_TRANSITIONS.resolve());
        if (currentState == null || targetState == null) throw new IllegalStateException(
                TEXT_INVALID_STATE.resolve(getCurrentState(), getTargetState()));
    }

    @Override public StartStateMachineRequest start() {
        return inject(new InternalStartMachineRequest());
    }

    @Override public void onState(final State state, final Executable task) {
        on(false, StateMachineReachedStateEvent.class, event -> {
            if (event.getReachedState() == state) {
                try {
                    task.execute();
                } catch (Exception e) {
                    error(e);
                }
            }
        });
    }

    Set<Trajectory> getTrajectories() {
        if (trajectories == null) {
            try {
                execute(this::getTs).standby();
            } catch (InterruptedException e) {
                throw new OperationInterruptedException();
            }
        }
        return trajectories;
    }

    Set<Trajectory> getTs() {
        if (trajectories == null) {
            final long millis = System.currentTimeMillis();
            checkPrerequisites();
            computeTrajectories();
            debug("ComputeTrajectories took %d millis", System.currentTimeMillis() - millis);
        }
        return trajectories;
    }

    /**
     * Computes the trajectories from the current state to the target state.
     */
    private void computeTrajectories() {
        this.trajectories = new HashSet<>();
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
    void proceed(final InternalStartMachineRequest request) {
        inject(new PerformStateTransitionCommand(request));
    }

    @Override public String toString() {
        return ClassUtil.toString(this);
    }

    private class InternalStartMachineRequest extends BasicRequest
            implements StartStateMachineRequest {

        private final List<State> states = new ArrayList<>();
        private final ReentrantLock lock = new ReentrantLock();

        /**
         * Initializes a new instance of InternalStartMachineRequest.
         */
        InternalStartMachineRequest() {
            super(BasicStateMachine.this);
        }

        void addState(final State state) {
            lock.lock();
            try {
                states.add(state);
            } finally {
                lock.unlock();
            }
        }

        @Override @ToString public List<State> getPassedStates() {
            return Collections.unmodifiableList(states);
        }

        void removeState(final State state) {
            lock.lock();
            try {
                states.remove(state);
            } finally {
                lock.unlock();
            }
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

        @ToString public Recipient getAgent() {
            return getRecipientList()[0];
        }

        @ToString public StateTransition getTransition() {
            return transition;
        }

    }

    @SuppressWarnings("ClassHasNoToStringMethod")
    private class PerformStateTransitionCommand extends BasicCommand {

        private final InternalStartMachineRequest trigger;

        PerformStateTransitionCommand(final InternalStartMachineRequest request) {
            super(BasicStateMachine.this);
            trigger = request;
            preventMessageQueueShutdown();
        }

        @Override public void execute() {
            if (getCurrentState() == getTargetState()) {
                trigger.succeed();
            }
//            debug("Proceeding from %s to %s", getCurrentState(), getTargetState());
            final State state = getCurrentState();
            trigger.addState(state);
            try {
                final StateTransition transition = //
                        getTs().stream()
                               .filter(t -> t.isViable(trigger, state))
                               .map(ty -> ty.transitionFor(state).orElse(null))
                               .filter(Objects::nonNull)
                               .sorted()
                               .findFirst()
                               .orElseThrow(() -> new StateMachineStalledException(state,
                                       getTargetState()));
//                debug("Proceeding from %s to %s", transition.getInitialState(),
//                        transition.getTerminalState());
                inject(new InternalExecuteStateTransitionRequest(agent, transition)).andThen(() -> {
//                    debug("Proceed → succees");
                    final State newState = transition.getTerminalState();
                    setCurrentState(newState);
                    if (newState == getTargetState()) {
//                        debug("Trajectory successful, reached state %s", newState);
                        trigger.addState(newState);
                        trigger.succeed();
                        allowMessageQueueShutdown();
                    }
                }).orElse(problem -> {
//                    debug("Proceed → fail with %s", problem);
                    blockTransition(trajectories, trigger, transition);
                    trigger.removeState(state);
                }).standby(5, SECONDS);
                if (getCurrentState() != getTargetState()) again(this);
            } catch (Exception e) {
                error(e);
                trigger.fail(e);
                allowMessageQueueShutdown();
            }
        }

        private void blockTransition(final Set<Trajectory> trajectories, final Request request,
                final StateTransition transition) {
            trajectories.stream()
                        .filter(trajectory -> trajectory.contains(transition))
                        .forEach(trajectory -> trajectory.block(request));
        }

    }

    @SuppressWarnings("ClassHasNoToStringMethod")
    private class InternalStateMachineReachedStateEvent extends BasicEvent
            implements StateMachineReachedStateEvent {

        private final State state;

        InternalStateMachineReachedStateEvent(final State state) {
            super(BasicStateMachine.this);
            this.state = state;
        }

        @Override public State getReachedState() {
            return state;
        }

    }

}
