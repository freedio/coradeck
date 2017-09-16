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

package com.coradec.coradoc.model;

import java.util.Map;

/**
 * A list of typed attributes.
 */
public interface XmlAttributes {

    /**
     * Adds a typeless attribute with the specified name and value.
     *
     * @param name  the name.
     * @param value the value.
     * @throws IllegalStateException if an attribute with the specified name already existed.
     */
    void add(String name, String value) throws IllegalStateException;

    /**
     * Returns the value of the attribute with the specified name.
     *
     * @param name the attribute name.
     * @return the attribute value.
     */
    String getValue(String name);

    /**
     * Returns the name to value mapping.
     *
     * @return the name to value mapping.
     */
    Map<String, String> getValueMap();

}
