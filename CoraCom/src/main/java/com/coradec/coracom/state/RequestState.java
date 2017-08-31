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

package com.coradec.coracom.state;

import com.coradec.coracore.annotation.ToString;
import com.coradec.coracore.model.State;
import com.coradec.coracore.util.ClassUtil;

/**
 * Type-safe extensible enumeration of request states.
 */
public class RequestState implements State {

    public static final RequestState NEW = new RequestState("NEW", 0);
    public static final RequestState SUBMITTED = new RequestState("SUBMITTED", 100);
    public static final RequestState SUCCESSFUL = new RequestState("SUCCESSFUL", 1000);
    public static final RequestState FAILED = new RequestState("FAILED", 1001);
    public static final RequestState CANCELLED = new RequestState("CANCELLED", 1002);

    private final String name;
    private final int rank;

    public RequestState(final String name, final int rank) {
        this.name = name;
        this.rank = rank;
    }

    @Override @ToString public String name() {
        return name;
    }

    @Override @ToString public int ordinal() {
        return rank;
    }

    @Override public String toString() {
        return ClassUtil.toString(this, this);
    }

}
