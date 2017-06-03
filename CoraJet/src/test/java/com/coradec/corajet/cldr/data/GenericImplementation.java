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

package com.coradec.corajet.cldr.data;

import com.coradec.coracore.util.ClassUtil;

/**
 * ​​Implementation of the simple generic interface.
 */
@com.coradec.coracore.annotation.Implementation
public class GenericImplementation<Q> implements GenericInterface<Q> {

    private final Class<Q> type;

    public GenericImplementation(final Class<Q> type) {
        this.type = type;
    }

    @Override public Q value() {
        if (type == Integer.TYPE || type == Integer.class) return type.cast(42);
        if (type == Long.TYPE || type == Long.class) return type.cast(4711L);
        if (type == Float.TYPE || type == Float.class) return type.cast(2.71828184f);
        if (type == Double.TYPE || type == Double.class) return type.cast(Math.PI);
        if (type == String.class) {
            return type.cast("Hello, World!");
        }
        throw new IllegalArgumentException("Don't know how to handle " + type);
    }

    @Override public String toString() {
        return ClassUtil.toString(this);
    }
}
