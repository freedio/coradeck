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

package com.coradec.coraconf.model;

import com.coradec.coracore.trouble.PropertyNotFoundException;

import java.util.Map;
import java.util.Optional;

/**
 * Simple value mapping.
 */
public interface ValueMap {

    /**
     * Looks up the raw value with the specified name.
     *
     * @param name the property name.
     * @return the raw value, if a mapping for the specified name exists.
     */
    Optional<String> lookup(String name);

    /**
     * Returns the raw value with the specified name.
     *
     * @param name the property name.
     * @return the raw value.
     * @throws PropertyNotFoundException if no mapping for the specified name exists.
     */
    String get(String name) throws PropertyNotFoundException;

    /**
     * Looks up the value with the specified name, converted to the specified type.
     *
     * @param <V>  the property type.
     * @param type the type selector.
     * @param name the property name.
     * @return the value, if a mapping for the specified name exists.
     */
    <V> Optional<V> lookup(Class<V> type, String name);

    /**
     * Returns the value with the specified name, converted to the specified type.
     *
     * @param <V>  the property type.
     * @param type the type selector.
     * @param name the property name.
     * @return the value.
     * @throws PropertyNotFoundException if no mapping for the specified name exists.
     */
    <V> V get(Class<V> type, String name) throws PropertyNotFoundException;

    /**
     * Returns the value mapping as a map.
     *
     * @return the map.
     */
    Map<String, String> asMap();

}
