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

import com.coradec.coraconf.ctrl.ConfigurationReaderFactory;
import com.coradec.coraconf.model.AnnotatedProperty;
import com.coradec.coraconf.model.Configuration;
import com.coradec.coraconf.model.Property;
import com.coradec.coraconf.trouble.ConfigurationException;
import com.coradec.coraconf.trouble.ConfigurationNotFoundException;
import com.coradec.coracore.annotation.Implementation;
import com.coradec.coracore.annotation.Nullable;
import com.coradec.coracore.annotation.ToString;
import com.coradec.coracore.model.DynamicFactory;
import com.coradec.coracore.model.GenericType;
import com.coradec.coracore.util.ClassUtil;
import com.coradec.coratype.ctrl.TypeConverter;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * ​​Basic implementation of a configuration.
 */
@SuppressWarnings("unchecked")
@Implementation
public class BasicConfiguration implements Configuration {

    private static final Map<String, String> APPLICATION_CONFIOURATION = new HashMap<>();

    private static final DynamicFactory<TypeConverter<?>> FACTORY = new DynamicFactory<>();

    private static final String CONF_FILE_PATTERN =
            System.getProperty("com.coradec.coraconf.model.Configuration.FileTemplate",
                    "%s.config");

    private final @Nullable String context;
    private final @Nullable URL baseFile;
    private @Nullable Map<String, String> rawProperties;
    private @Nullable Map<String, Object> properties;

    public BasicConfiguration(@SuppressWarnings("NullableProblems") final String context) {
        this.context = context;
        final String contxt = String.format(CONF_FILE_PATTERN, context.replace('.', '/'));
        baseFile = getClass().getClassLoader().getResource(contxt);
        if (baseFile == null) throw new ConfigurationNotFoundException(contxt);
    }

    public BasicConfiguration() {
        this.context = null;
        this.baseFile = null;
    }

    @ToString public Optional<String> getContext() {
        return Optional.ofNullable(this.context);
    }

    @ToString public Optional<URL> getBaseFile() {
        return Optional.ofNullable(this.baseFile);
    }

    private Map<String, Object> getProperties() {
        if (properties == null) properties = createPropertyMap();
        return this.properties;
    }

    private Map<String, String> getRawProperties() {
        if (rawProperties == null) rawProperties = createRawPropertyMap();
        return this.rawProperties;
    }

    @SuppressWarnings("WeakerAccess") protected Map<String, String> createRawPropertyMap() {
        final HashMap<String, String> result = new HashMap<>();
        if (baseFile == null || context == null) {
            return APPLICATION_CONFIOURATION;
        } else {
            try {
                final Set<AnnotatedProperty> properties =
                        ConfigurationReaderFactory.createParser(context, baseFile, ".text")
                                                  .getProperties();
                properties.forEach(prop -> result.put(prop.getName(), prop.getRawValue()));
            } catch (IOException e) {
                throw new ConfigurationException(e);
            }
        }
        return result;
    }

    @SuppressWarnings("WeakerAccess") protected Map<String, Object> createPropertyMap() {
        return new HashMap<>();
    }

    @Override public Optional<?> lookup(final String name, final Object... args) {
        return lookup(GenericType.of(Object.class), name, args);
    }

    private <T> Optional<T> transform(final GenericType<T> type, final String name,
                                      final Object... args) {
        return Optional.ofNullable(getRawProperties().get(name))
                       .map(s -> String.format(s, args))
                       .map(s -> ((TypeConverter<T>)FACTORY.of(TypeConverter.class, type).get(type))
                               .convert(s));
    }

    @Override public <T> Optional<T> lookup(final Class<? super T> type, final String name,
                                            final Object... args) {
        return lookup(GenericType.of(type), name, args);
    }

    @Override public <T> Optional<T> lookup(final GenericType<T> type, final String name,
                                            final Object... args) {
        if (args.length == 0) {
            return Optional.ofNullable(getProperties().compute(name, (key, value) -> //
                    value != null //
                    ? value //
                    : transform(type, key, args).orElse(null)))
                           .map(o -> ((TypeConverter<T>)FACTORY.of(TypeConverter.class, type)
                                                               .get(type)).convert(o));
        } else return transform(type, name, args);
    }

    @Override public Configuration add(final Collection<? extends Property<?>> properties) {
        getProperties().putAll(
                properties.stream().collect(Collectors.toMap(Property::getName, Property::value)));
        return this;
    }

    @Override public String toString() {
        return ClassUtil.toString(this);
    }

}
