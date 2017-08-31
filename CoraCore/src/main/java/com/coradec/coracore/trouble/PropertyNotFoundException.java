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

package com.coradec.coracore.trouble;

import java.lang.reflect.Type;

/**
 * ​​Indicates an attempt to retrieve a property which does not exist.
 */
public class PropertyNotFoundException extends PropertyException {

    /**
     * Initializes a new instance of PropertyNotFoundException for a property with the specified
     * type and name.
     *
     * @param type the property type.
     * @param name the property name.
     */
    public PropertyNotFoundException(final Type type, final String name) {
        super(type, name);
    }

}
