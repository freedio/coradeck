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

package com.coradec.coradoc.model.impl;

import com.coradec.coracore.trouble.PropertyNotFoundException;
import com.coradec.coracore.util.ClassUtil;
import com.coradec.coradoc.model.XmlAttributes;
import com.coradec.coratype.ctrl.TypeConverter;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * ​​Basic implementation of XML attributes.
 */
public class BasicXmlAttributes implements XmlAttributes {

    private final Map<String, String> values;
    private final Map<String, String> types;

    public BasicXmlAttributes() {
        values = new HashMap<>();
        types = new HashMap<>();
    }

    @Override public void add(final String name, final String value) throws IllegalStateException {
        if (values.putIfAbsent(name, value) != null) throw new IllegalStateException();
    }

    @Override public String toString() {
        return ClassUtil.toString(this);
    }

    @Override public Optional<String> lookup(final String name) {
        return Optional.ofNullable(values.get(name));
    }

    @Override public String get(final String name) throws PropertyNotFoundException {
        return lookup(name).orElseThrow(() -> new PropertyNotFoundException(String.class, name));
    }

    @Override public <V> Optional<V> lookup(final Class<V> type, final String name) {
        return lookup(name).map(property -> TypeConverter.to(type).decode(property));
    }

    @Override public <V> V get(final Class<V> type, final String name)
            throws PropertyNotFoundException {
        return lookup(type, name).orElseThrow(
                () -> new PropertyNotFoundException(String.class, name));
    }

    @Override public Map<String, String> asMap() {
        return Collections.unmodifiableMap(values);
    }
}
