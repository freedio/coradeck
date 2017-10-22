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

import com.coradec.coraconf.model.ValueMap;

import java.util.List;

/**
 * ​A kind of CSS selector.
 */
public interface Selector extends ParserToken {

    /**
     * Returns the selector's name.
     *
     * @return the name.
     */
    String getName();

    /**
     * Creates a descendant selector with the specified descendant name for this selector.
     *
     * @param name the descendant name.
     * @return the descendant selector.
     */
    Selector createDescendant(String name);

    /**
     * Creates a child selector with the specified child name for this selector.
     *
     * @param name the child name.
     * @return the child selector.
     */
    Selector createChild(String name);

    /**
     * Creates a neighbor selector with the specified neighbor name for this selector.
     *
     * @param name the neighbor name.
     * @return the neighbor selector.
     */
    Selector createNeighbor(String name);

    /**
     * Creates a sibling selector with the specified sibling name for this selector.
     *
     * @param name the sibling name.
     * @return the sibling selector.
     */
    Selector createSibling(String name);

    /**
     * Checks if a component with specified path and attributes matches this selector.
     *
     * @param path       the component path.
     * @param attributes the component attributes.
     * @return {@code true} if the selector and component match, {@code false} if not.
     */
    boolean matches(List<String> path, ValueMap attributes);

}
