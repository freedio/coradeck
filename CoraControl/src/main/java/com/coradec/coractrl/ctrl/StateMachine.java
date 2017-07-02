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

import java.util.Collection;

/**
 * ​​API of a state machine.
 */
public interface StateMachine {

    /**
     * Returns the current state of the state machine.
     *
     * @return the current state.
     */
    State getCurrentState();

    /**
     * Returns the target state of the state machine.
     *
     * @return the target state.
     */
    State getTargetState();

    /**
     * Sets the initial state of the state machine.
     *
     * @param state the initial state.
     */
    void initialize(State state);

    /**
     * Adds the specified transitions to the set of state transitions.
     *
     * @param transitions the transitions to add.
     */
    void addTransitions(Collection<StateTransition> transitions);

    /**
     * Sets the target state and starts the state machine, unless this state is already reached.
     *
     * @param state the state.
     */
    void setTargetState(State state);

    /**
     * Starts the state machine.
     *
     * @return a ticket to track progress of the request.
     */
    Request start();

}
