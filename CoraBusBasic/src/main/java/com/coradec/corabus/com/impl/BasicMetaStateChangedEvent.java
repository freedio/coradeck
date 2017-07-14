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
import com.coradec.coracom.model.Sender;
import com.coradec.coracom.model.impl.BasicEvent;
import com.coradec.coracore.annotation.Implementation;

/**
 * ​​Basic implementation of a meta-state change event.
 */
@Implementation
public class BasicMetaStateChangedEvent extends BasicEvent implements MetaStateChangedEvent {

    /**
     * Initializes a new instance of BasicMetaStateChangedEvent regarding a meta-state change from
     * the specified old value to the specified new value in the specified node with the specified
     * sender.
     *
     * @param sender the sender.
     */
    public BasicMetaStateChangedEvent(final Sender sender) {
        super(sender);
    }
}
