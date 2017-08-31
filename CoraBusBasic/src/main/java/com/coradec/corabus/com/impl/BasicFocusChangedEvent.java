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

import com.coradec.corabus.com.FocusChangedEvent;
import com.coradec.coracom.model.impl.BasicEvent;
import com.coradec.coracore.model.Origin;

import java.nio.channels.SelectableChannel;

/**
 * ​​Basic implementation of a focus changed event.
 */
@SuppressWarnings("ClassHasNoToStringMethod")
public class BasicFocusChangedEvent extends BasicEvent implements FocusChangedEvent {

    private final SelectableChannel channel;
    private final int focus;

    /**
     * Initializes a new instance of BasicFocusChangedEvent from the specified origin regard a
     * change of focus on the specified channel to the specified interest set.
     *
     * @param origin the origin of the event.
     */
    public BasicFocusChangedEvent(final Origin origin, final SelectableChannel channel,
            final int focus) {
        super(origin);
        this.channel = channel;
        this.focus = focus;
    }

    @Override public SelectableChannel getChannel() {
        return channel;
    }

    @Override public int getFocus() {
        return focus;
    }

}
