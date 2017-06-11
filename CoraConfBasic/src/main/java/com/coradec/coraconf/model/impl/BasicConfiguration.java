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

package com.coradec.coraconf.model.impl;

import com.coradec.coraconf.model.Configuration;
import com.coradec.coraconf.model.Property;
import com.coradec.coracore.annotation.Implementation;
import com.coradec.coracore.annotation.Nullable;
import com.coradec.coracore.model.GenericType;
import com.coradec.coracore.util.ClassUtil;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * ​​Basic implementation of a configuration.
 *
 * @param <V> the configuration value type.
 */
@Implementation
public class BasicConfiguration<V> implements Configuration<V> {

    private final GenericType<V> type;
    private @Nullable Map<String, V> properties;

    public BasicConfiguration(final GenericType<V> type) {
        this.type = type;
    }

    private Map<String, V> getProperties() {
        if (properties == null) properties = createPropertyMap();
        return this.properties;
    }

    @SuppressWarnings("WeakerAccess") protected Map<String, V> createPropertyMap() {
        return new HashMap<>();
    }

    @Override public Optional<V> lookup(final String name) {
        return Optional.ofNullable(getProperties().get(name));
    }

    @Override
    public Configuration<V> add(final Collection<? extends Property<? extends V>> properties) {
        getProperties().putAll(
                properties.stream().collect(Collectors.toMap(Property::getName, Property::value)));
        return this;
    }

    @Override public String toString() {
        return ClassUtil.toString(this);
    }
}
