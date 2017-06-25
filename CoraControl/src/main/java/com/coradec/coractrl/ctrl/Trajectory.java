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

import com.coradec.coractrl.model.StateTransition;

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
     * Removes the specified transitions from the trajectory.
     * <p>
     * The method silently ignores transitions which are not members of the trajectory.
     *
     * @param transitions the transitions to remove.
     * @return this trajectory. for method chaining.
     */
    @SuppressWarnings("unchecked") Trajectory remove(
            Class<? extends StateTransition>... transitions);

}
