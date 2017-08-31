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

import com.coradec.corabus.com.KeyProcessedEvent;
import com.coradec.coracom.model.impl.BasicEvent;
import com.coradec.coracore.annotation.Internal;
import com.coradec.coracore.annotation.ToString;
import com.coradec.coracore.model.Origin;

import java.nio.channels.SelectionKey;

/**
 * ​​Basic implementation of a key processed event.
 */
@SuppressWarnings("ClassHasNoToStringMethod")
@Internal
public class BasicKeyProcessedEvent extends BasicEvent implements KeyProcessedEvent {

    private final SelectionKey selectionKey;

    /**
     * Initializes a new instance of BasicKeyProcessedEvent from the specified origin with the
     * specified selection key.
     *
     * @param origin       the origin.
     * @param selectionKey the selection key.
     */
    public BasicKeyProcessedEvent(final Origin origin, final SelectionKey selectionKey) {
        super(origin);
        this.selectionKey = selectionKey;
    }

    @Override @ToString public SelectionKey getSelectionKey() {
        return selectionKey;
    }

}
