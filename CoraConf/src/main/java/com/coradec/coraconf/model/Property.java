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

import com.coradec.coracore.ctrl.Factory;
import com.coradec.coracore.model.GenericFactory;
import com.coradec.coracore.model.Type;
import com.coradec.coracore.util.ExecUtil;

/**
 * ​A property definition.
 */
public interface Property<T> {

    Factory<Property<?>> property = new GenericFactory<>(Property.class);

    @SuppressWarnings("unchecked")
    static <X, D extends X> Property<X> define(String name, Class<? super X> type, D dflt) {
        return (Property<X>)property.get(Property.class,
                ExecUtil.getCallerStackFrame().getClassFileName(), name, type, dflt);
    }

    /**
     * Returns the property name.
     *
     * @return the property name.
     */
    String getName();

    /**
     * Returns the property type.
     *
     * @return the property type.
     */
    Type<T> getType();

    /**
     * Returns the value of the property, converted from the raw value after fitting the latter one
     * with the specified arguments..
     *
     * @param args arguments to fit into the value template.
     */
    T value(Object... args);

}
