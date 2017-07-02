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

package com.coradec.coractrl.model;

import com.coradec.coracom.model.Request;
import com.coradec.coracore.model.State;

import java.util.Optional;

/**
 * ​Definition of an action to execute between two states of a state machine.
 */
public interface StateTransition extends Comparable<StateTransition> {

    /**
     * Returns the order of execution.
     * <p>
     * The order of execution is relevant in cases only where more than one transition is viable at
     * a particular state.  The default order is 1000.  Lower orders are considered first, higher
     * later.  If a particular transition needs to be preferred over another one, invoke setOrder
     * with a value lower than 1000.
     *
     * @return the order of execution.
     */
    int getOrder();

    /**
     * Indicates whether this state transition is viable at the moment of invocation.  The default
     * is {@code true}.  If a state transition has certain preconditions to be met before it can be
     * executed, these can be checked in the implementation code of this method.
     *
     * @return {@code true} if the state transition can be executed now, otherwise {@code false}.
     */
    boolean isViable();

    /**
     * Returns the initial state of the transition..
     *
     * @return the initial state.
     */
    State getInitialState();

    /**
     * Returns the terminal state of the transition.
     *
     * @return the terminal state.
     */
    State getTerminalState();

    /**
     * Performs the state transition.
     *
     * @return a request to track progress of the operation, or none, if the state transition
     * executed synchronously.
     */
    Optional<Request> execute();

    /**
     * Checks if the transition ends with the specified state.
     *
     * @param terminal the expected terminal state.
     * @return {@code true} if the transition end with the specified terminal state.
     */
    boolean endsWith(State terminal);

}
