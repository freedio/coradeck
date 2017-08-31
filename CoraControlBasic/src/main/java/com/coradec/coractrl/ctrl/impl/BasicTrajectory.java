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
import com.coradec.coracore.annotation.Implementation;
import com.coradec.coracore.annotation.Nullable;
import com.coradec.coracore.annotation.ToString;
import com.coradec.coracore.model.State;
import com.coradec.coracore.util.ClassUtil;
import com.coradec.coractrl.ctrl.Trajectory;
import com.coradec.coractrl.model.StateTransition;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * ​​Basic implementation of a trajectory.
 */
@Implementation
public class BasicTrajectory implements Trajectory {

    private final List<StateTransition> transitions;
    private @Nullable Set<Request> blockedRequests;

    public BasicTrajectory(final StateTransition... transitions) {
        this.transitions = new ArrayList<>(Arrays.asList(transitions));
    }

    public BasicTrajectory(final StateTransition first, final StateTransition... rest) {
        transitions = new ArrayList<>();
        transitions.add(first);
        Collections.addAll(transitions, rest);
    }

    private BasicTrajectory(final List<StateTransition> transitions) {
        this.transitions = transitions;
    }

    @Override public Trajectory add(final StateTransition... ts) {
        final List<StateTransition> transitions = this.transitions;
        transitions.addAll(Arrays.asList(ts));
        return new BasicTrajectory(transitions);
    }

    @Override @ToString public List<StateTransition> getTransitions() {
        return transitions;
    }

    @Override public Trajectory prepend(final StateTransition transition) {
        return new BasicTrajectory(transition,
                transitions.toArray(new StateTransition[transitions.size()]));
    }

    @Override public boolean startsWith(final State state) {
        return getInitialState() == state;
    }

    @Override public State getInitialState() {
        return transitions.get(0).getInitialState();
    }

    @Override public boolean isCyclic() {
        return transitions.stream().distinct().count() != transitions.size();
    }

    @Override public boolean connects(final State s1, final State s2) {
        final StateTransition first = transitions.get(0);
        final StateTransition last = transitions.get(transitions.size() - 1);
        return (first.getInitialState() == s1 && last.getTerminalState() == s2 ||
                first.getInitialState() == s2 && last.getTerminalState() == s1);
    }

    @Override public boolean isViable(final Request request, final State state) {
        return !blockedFor(request) &&
               transitionFor(state).map(StateTransition::isViable).orElse(false);
    }

    private boolean blockedFor(final Request request) {
        return blockedRequests != null && blockedRequests.contains(request);
    }

    @Override public Optional<StateTransition> transitionFor(final State state) {
        return getTransitions().stream()
                               .filter(transition -> transition.getInitialState() == state)
                               .findFirst();
    }

    @Override public boolean contains(final StateTransition transition) {
        return getTransitions().contains(transition);
    }

    @Override public void block(final Request request) {
        if (blockedRequests == null) blockedRequests = new HashSet<>();
        blockedRequests.add(request);
    }

    @Override public String toString() {
        return ClassUtil.toString(this);
    }

    @Override public boolean equals(final Object obj) {
        return obj instanceof Trajectory &&
               getTransitions().equals(((Trajectory)obj).getTransitions());
    }

    @Override public int hashCode() {
        return getTransitions().hashCode();
    }

}
