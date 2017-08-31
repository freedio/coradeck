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

package com.coradec.corabus.com.impl;

import com.coradec.corabus.com.MetaStateChangedEvent;
import com.coradec.corabus.state.MetaState;
import com.coradec.coracom.model.impl.BasicEvent;
import com.coradec.coracore.annotation.Implementation;
import com.coradec.coracore.annotation.ToString;
import com.coradec.coracore.model.Origin;

import java.util.Map;

/**
 * ​​Basic implementation of a meta-state changed event.
 */
@SuppressWarnings("ClassHasNoToStringMethod")
@Implementation
public class BasicMetaStateChangedEvent extends BasicEvent implements MetaStateChangedEvent {

    private final MetaState oldState;
    private final MetaState newState;

    public BasicMetaStateChangedEvent(final Origin origin, final MetaState oldState,
            final MetaState newState) {
        super(origin);
        this.oldState = oldState;
        this.newState = newState;
    }

    /**
     * Initializes a new instance of BasicMetaStateChangedEventEvent from the specified property
     * map.
     *
     * @param properties the property map.
     */
    private BasicMetaStateChangedEvent(final Map<String, Object> properties) {
        super(properties);
        this.oldState = get(MetaState.class, PROP_OLD_STATE);
        this.newState = get(MetaState.class, PROP_NEW_STATE);
    }

    @Override @ToString public MetaState getOldState() {
        return oldState;
    }

    @Override @ToString public MetaState getNewState() {
        return newState;
    }

    @Override protected void collect() {
        super.collect();
        set(PROP_OLD_STATE, getOldState());
        set(PROP_NEW_STATE, getNewState());
    }

}
