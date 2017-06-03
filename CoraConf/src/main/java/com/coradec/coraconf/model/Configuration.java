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

import java.util.Collection;
import java.util.Optional;

/**
 * ​Representation of a key-value-pairing.
 *
 * @param <V> the value type.
 */
public interface Configuration<V> {

    Factory<Configuration<?>> CONFIGURATION = new GenericFactory<>(Configuration.class);

    @SuppressWarnings("unchecked") static <X> Configuration<X> of(Class<X> type, Class<?>... parameters) {
        return (Configuration<X>)CONFIGURATION.get(Configuration.class, Type.of(type, parameters));
    }

    /**
     * Looks up the property with the specified name.
     *
     * @param name the property name.
     * @return the property value, if available.
     */
    Optional<V> lookup(String name);

    /**
     * Adds the specified properties to the configuration.
     *
     * @param properties the properties to add.
     * @return this configuration, for method chaining.
     */
    Configuration<V> add(Collection<? extends Property<? extends V>> properties);

}
