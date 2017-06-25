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

import com.coradec.coracom.model.Request;
import com.coradec.coracom.model.impl.BasicRequest;
import com.coradec.coracore.annotation.Implementation;
import com.coradec.coracore.annotation.ToString;
import com.coradec.coracore.model.State;
import com.coradec.coracore.util.ClassUtil;
import com.coradec.coractrl.ctrl.StateMachine;
import com.coradec.coractrl.ctrl.Trajectory;

import java.util.ArrayList;
import java.util.List;

/**
 * ​​Basic implementation of a state machine.
 */
@Implementation
public class BasicStateMachine extends BasicAgent implements StateMachine {

    private State currentState, targetState;
    private final List<Trajectory> trajectories = new ArrayList<>();

    public BasicStateMachine() {
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

    @Override public void addTrajectory(final Trajectory trajectory) {
        trajectories.add(trajectory);
    }

    @Override public Request setTargetState(final State state) {
        final SetTargetStateRequest request = new SetTargetStateRequest(state);
        inject(request);
        return request;
    }

    private class SetTargetStateRequest extends BasicRequest {

        SetTargetStateRequest(final State state) {
            super(BasicStateMachine.this);
        }

    }

    @Override public String toString() {
        return ClassUtil.toString(this);
    }
}
