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

import com.coradec.coracore.model.Factory;
import com.coradec.coracore.model.GenericFactory;
import com.coradec.coracore.model.GenericType;

import java.util.Collection;
import java.util.Optional;

/**
 * ​Representation of a key-value-map.
 * <p>
 * Property values may have parameters in the form of formatting arguments as used in printf.  All
 * lookup methods allow to pass in the arguments to fit into the parameters, before they are cast to
 * their final type.
 */
public interface Configuration {

    @SuppressWarnings("unchecked")
    Factory<Configuration> CONFIGURATION = new GenericFactory(Configuration.class);

    /**
     * Returns the context, if present.
     * <p>
     * If no context is defined, the text base is called the default text base.
     *
     * @return the context, if defined.
     */
    Optional<String> getContext();

    /**
     * Looks up the property with the specified name and arguments.
     *
     * @param name the property name.
     * @param args the arguments.
     * @return the property value, if available.
     */
    Optional<?> lookup(String name, Object... args);

    /**
     * Looks up the property with the specified name and arguments, cast to the specified type.
     *
     * @param <T>  the property type.
     * @param type the property type selector.
     * @param name the property name.
     * @param args the arguments.
     * @return the property value, if available.
     */
    <T> Optional<T> lookup(Class<T> type, String name, Object... args);

    /**
     * Looks up the property with the specified name and arguments, cast to the specified generic
     * type.
     *
     * @param <T>  the property type.
     * @param type the property type selector.
     * @param name the property name.
     * @param args the arguments.
     * @return the property value, if available.
     */
    <T> Optional<T> lookup(GenericType<T> type, String name, Object... args);

    /**
     * Adds the specified properties to the configuration.
     *
     * @param properties the properties to add.
     * @return this configuration, for method chaining.
     */
    Configuration add(Collection<? extends Property<?>> properties);

}
