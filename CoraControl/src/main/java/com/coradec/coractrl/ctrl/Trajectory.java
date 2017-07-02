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

package com.coradec.coractrl.ctrl;

import com.coradec.coracom.model.Request;
import com.coradec.coracore.model.State;
import com.coradec.coractrl.model.StateTransition;

import java.util.List;
import java.util.Optional;

/**
 * ​A trajectory consisting of several state transitions.
 */
public interface Trajectory {

    /**
     * Adds the specified state transitions to the trajectory.
     *
     * @param transitions the state transitions to add.
     * @return this trajectory. for method chaining.
     */
    Trajectory add(StateTransition... transitions);

    /**
     * Returns the list of transitions.
     *
     * @return the list of transitions.
     */
    List<StateTransition> getTransitions();

    /**
     * Returns a trajectory with the specified transition prepended.
     *
     * @param transition the transition to prepend.
     * @return this trajectory, with the specified transition prepended.
     */
    Trajectory prepend(StateTransition transition);

    /**
     * Checks if this trajectory begins with the specified state.
     *
     * @param state the state to check for.
     * @return {@code true} if the trajectory begins with the specified state.
     */
    boolean startsWith(State state);

    /**
     * Returns the initial state of the trajector.
     *
     * @return the initial state of the trajector.
     */
    State getInitialState();

    /**
     * Checks if the trajectory is cyclic.
     * <p>
     * A trajectory is cyclic if it contains the same state transition more than once.
     *
     * @return {@code true} if the trajectory is cyclic.
     */
    boolean isCyclic();

    /**
     * Checks if this trajectory connects states s1 and s2.
     *
     * @param s1 the first state.
     * @param s2 the second state.
     * @return {@code true} if the trajectory connects the first and second state.
     */
    boolean connects(State s1, State s2);

    /**
     * Checks if this trajectory is viable for the specified request from the specified state.
     *
     * @param request the request.
     * @param state   the state to go from.
     * @return {@code true} if the trajectory is viable, {@code false} if not.
     */
    boolean isViable(final Request request, State state);

    /**
     * Returns the state transition that leads on from the specified state, if there is such a state
     * transition.
     *
     * @param state the state to move on from.
     * @return a state transition, or none, if the state is not present in the trajectory.
     */
    Optional<StateTransition> transitionFor(State state);

    /**
     * Checks if the trajectory contains the specified state transition.
     *
     * @param transition the transition.
     * @return {@code true} if the trajectory contains the state transition.
     */
    boolean contains(StateTransition transition);

    /**
     * Blocks this trajectory for the specified request.
     *
     * @param request the request.
     */
    void block(Request request);
}
