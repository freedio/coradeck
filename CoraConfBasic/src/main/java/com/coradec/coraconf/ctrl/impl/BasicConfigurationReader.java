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

package com.coradec.coraconf.ctrl.impl;

import com.coradec.coraconf.ctrl.ConfigurationReader;
import com.coradec.coraconf.model.AnnotatedProperty;
import com.coradec.coraconf.model.impl.BasicAnnotatedProperty;
import com.coradec.coracore.annotation.Nullable;
import com.coradec.coracore.annotation.ToString;
import com.coradec.coracore.util.ClassUtil;
import com.coradec.coralog.ctrl.impl.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * ​​Basic implementation of a configuration reader.
 */
public abstract class BasicConfigurationReader extends Logger implements ConfigurationReader {

    private final String context;
    private final URL resource;

    protected BasicConfigurationReader(final String context, final URL resource) {
        this.context = context;
        this.resource = resource;
    }

    /**
     * Returns the resource as a stream.
     *
     * @return the resource as a stream.
     * @throws IOException if the resource could not be opened.
     */
    protected InputStream getResourceAsStream() throws IOException {
        return getResource().openStream();
    }

    /**
     * Returns the next property from the resource.
     * <p>
     * When this method returns {@link Optional#empty()}, the property file has been read to the
     * end.
     *
     * @return a property, if another one was available.
     */
    protected abstract Optional<AnnotatedProperty> getNextProperty();

    /**
     * Returns the context name.
     *
     * @return the context.
     */
    @ToString public String getContext() {
        return this.context;
    }

    /**
     * Returns the resource location.
     *
     * @return the resource location.
     */
    @ToString public URL getResource() {
        return this.resource;
    }

    /**
     * Returns the complete set of properties from the reader.
     *
     * @return the complete property set.
     * @throws IOException if the properties could not be read.
     */
    @Override public Set<AnnotatedProperty> getProperties() throws IOException {
        open();
        final Set<AnnotatedProperty> result = new HashSet<>();
        for (Optional<AnnotatedProperty> property = getNextProperty();
             property.isPresent();
             property = getNextProperty()) {
            //noinspection OptionalGetWithoutIsPresent
            result.add(property.get());
        }
        close();
        return result;
    }

    /**
     * Prepares the resource stream.
     *
     * @throws IOException if the stream could not be prepared.
     */
    protected abstract void open() throws IOException;

    /**
     * Closes the resource stream.
     *
     * @throws IOException if the stream could not be closed.
     */
    protected abstract void close() throws IOException;

    /**
     * Creates an instance of AnnotatedProperty with the specified name, type (if specified), value
     * and optional annotation.
     *
     * @param name       the property name.
     * @param type       the property type (if known).
     * @param value      the property value.
     * @param annotation the property annotation (if present).
     * @return a new annotated property.
     */
    protected AnnotatedProperty createPropertyFrom(final String name, @Nullable final String type,
                                                   final String value,
                                                   @Nullable final String annotation) {
        return new BasicAnnotatedProperty(name, type, value, annotation);
    }

    @Override public String toString() {
        return ClassUtil.toString(this);
    }

}
