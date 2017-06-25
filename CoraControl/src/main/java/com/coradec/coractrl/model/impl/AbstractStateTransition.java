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

package com.coradec.coractrl.model.impl;

import com.coradec.coracore.annotation.Implementation;
import com.coradec.coracore.annotation.ToString;
import com.coradec.coracore.model.State;
import com.coradec.coracore.util.ClassUtil;
import com.coradec.coractrl.model.StateTransition;

/**
 * ​​Basic implementation of a state transition.
 */
@Implementation
public abstract class AbstractStateTransition implements StateTransition {

    private final State initialState;
    private final State terminalState;

    protected AbstractStateTransition(final State initialState, final State terminalState) {
        this.initialState = initialState;
        this.terminalState = terminalState;
    }

    @Override @ToString public State getInitialState() {
        return initialState;
    }

    @Override @ToString public State getTerminalState() {
        return terminalState;
    }

    @Override public String toString() {
        return ClassUtil.toString(this);
    }

}
