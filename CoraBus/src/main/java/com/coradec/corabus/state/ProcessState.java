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

package com.coradec.corabus.state;

/**
 * ​​​​Type-safe extensible enumeration of process states.
 */
public class ProcessState extends HubState {

    public static final ProcessState STARTING = new ProcessState("STARTING", 2000);
    public static final ProcessState STARTED = new ProcessState("STARTED", 2100);
    public static final ProcessState RUNNING = new ProcessState("STARTED", 3000);
    public static final ProcessState SUSPENDING = new ProcessState("SUSPENDING", 3100);
    public static final ProcessState SUSPENDED = new ProcessState("SUSPENDED", 3200);
    public static final ProcessState RESUMING = new ProcessState("RESUMING", 3300);
    public static final ProcessState RESUMED = new ProcessState("RESUMED", 3400);
    public static final ProcessState STOPPING = new ProcessState("STOPPING", 7000);
    public static final ProcessState STOPPED = new ProcessState("STOPPED", 7100);

    @SuppressWarnings("WeakerAccess") protected ProcessState(final String name, final int rank) {
        super(name, rank);
    }
}
