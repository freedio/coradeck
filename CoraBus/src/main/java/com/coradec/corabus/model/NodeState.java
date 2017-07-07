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

package com.coradec.corabus.model;

import com.coradec.coracore.model.State;
import com.coradec.coracore.util.ClassUtil;

/**
 * ​​Type-safe extensible enumeration of node states.
 */
@SuppressWarnings("WeakerAccess")
public class NodeState implements State {

    public static final NodeState UNATTACHED = new NodeState("UNATTACHED", 0);
    public static final NodeState ATTACHING = new NodeState("ATTACHING", 100);
    public static final NodeState ATTACHED = new NodeState("ATTACHED", 200);
    public static final NodeState INITIALIZING = new NodeState("INITIALIZING", 300);
    public static final NodeState INITIALIZED = new NodeState("INITIALIZED", 400);
    public static final NodeState TERMINATING = new NodeState("TERMINATING", 9700);
    public static final NodeState TERMINATED = new NodeState("TERMINATED", 9800);
    public static final NodeState DETACHING = new NodeState("DETACHING", 9900);
    public static final NodeState DETACHED = new NodeState("DETACHED", 10000);

    private final String name;
    private final int rank;

    protected NodeState(final String name, final int rank) {
        this.name = name;
        this.rank = rank;
    }

    @Override public String name() {
        return name;
    }

    @Override public int ordinal() {
        return rank;
    }

    @Override public boolean precedes(final State state) {
        return ordinal() < state.ordinal();
    }

    @Override public String toString() {
        return ClassUtil.toString(this, this);
    }

}
