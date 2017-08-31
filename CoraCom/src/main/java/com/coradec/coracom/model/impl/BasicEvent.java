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

package com.coradec.coracom.model.impl;

import com.coradec.coracom.model.Event;
import com.coradec.coracore.annotation.Implementation;
import com.coradec.coracore.annotation.ToString;
import com.coradec.coracore.model.Origin;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * ​​Basic implementation of an event notification.
 */
@SuppressWarnings("ClassHasNoToStringMethod")
@Implementation
public class BasicEvent extends BasicInformation implements Event {

    private final LocalDateTime occurred;

    /**
     * Initializes a new instance of BasicEvent from the specified origin.
     *
     * @param origin the origin of the event.
     */
    public BasicEvent(final Origin origin) {
        super(origin);
        this.occurred = getCreatedAt();
    }

    /**
     * Initializes a new instance of BasicEvent with the specified event timestamp from the
     * specified origin.
     *
     * @param origin         the origin.
     * @param eventTimestamp the event timestamp.
     */
    public BasicEvent(final Origin origin, LocalDateTime eventTimestamp) {
        super(origin);
        this.occurred = eventTimestamp;
    }

    /**
     * Initializes a new instance of BasicEvent from the specified property map.
     *
     * @param properties the property map.
     */
    public BasicEvent(final Map<String, Object> properties) {
        super(properties);
        this.occurred = get(LocalDateTime.class, PROP_OCCURRED);
    }

    @ToString public LocalDateTime getEventTimestamp() {
        return this.occurred;
    }

    /**
     * Collects the properties into the built-in property map in preparation for {@link
     * #getProperties()}.
     * <p>
     * Every subclass defining its own attributes should override this method and add its own
     * properties by calling {@link BasicInformation#set(String, Object)} for each of them.
     */
    @Override protected void collect() {
        super.collect();
        set(PROP_OCCURRED, getEventTimestamp());
    }

}
