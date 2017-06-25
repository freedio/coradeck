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

/**
 * ​Definition of an action to perform between two states of a state machine.
 */
public interface StateTransition {

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
     * @return a request to track progress of the operation.
     */
    Request perform();

}
