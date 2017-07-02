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

/**
 * ​​Type-safe extensible enumeration of hub states.
 */
@SuppressWarnings("WeakerAccess")
public class HubState extends NodeState {

    public static final NodeState LOADING = new HubState("LOADING", 1000);
    public static final NodeState LOADED = new HubState("LOADED", 1010);
    public static final NodeState UNLOADING = new HubState("UNLOADING", 8000);
    public static final NodeState UNLOADED = new HubState("UNLOADED", 8010);

    protected HubState(final String name, final int rank) {
        super(name, rank);
    }
}
