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

package com.coradec.coracore.model;

import com.coradec.coracore.util.StringUtil;

/**
 * ​Representation of a state.
 */
public interface State extends Representable {

    /**
     * Returns the name of the state.
     *
     * @return the name of the state.
     */
    String name();

    /**
     * Returns the ordinal number of the state.
     *
     * @return the ordinal number.
     */
    int ordinal();

    /**
     * Checks if this state precedes the specified state.
     *
     * @param state the state to check against.
     * @return {@code true} if this state precedes the specified state.
     */
    default boolean precedes(final State state) {
        return ordinal() < state.ordinal();
    }

    default String represent() {
        return StringUtil.toTitleCase(name().toLowerCase());
    }

}
