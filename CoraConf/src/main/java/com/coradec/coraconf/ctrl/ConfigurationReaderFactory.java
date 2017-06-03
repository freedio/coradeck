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

package com.coradec.coraconf.ctrl;

import com.coradec.coraconf.trouble.ResourceInstantiationFailure;
import com.coradec.coracore.trouble.InitializationError;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Factory for configuration readers for various configuration file formats.
 */
@SuppressWarnings({
                          "MethodReturnOfConcreteClass", "OverlyCoupledClass", "unchecked",
                          "UseOfSystemOutOrSystemErr"
                  })
public abstract class ConfigurationReaderFactory {

    private static final Map<String, Class<? extends ConfigurationReader>> READERS_BY_EXTENSION =
            new HashMap<>(10);

    static {
        // initialize the reader-by-Extension map
        final Class<ConfigurationReaderFactory> me = ConfigurationReaderFactory.class;
        //noinspection MagicCharacter,HardcodedFileSeparator
        final String resourceName = me.getName().replace('.', '/') + ".properties";
        String className = null;
        try (final InputStream in = me.getClassLoader().getResourceAsStream(resourceName)) {
            if (in == null) throw new ResourceInstantiationFailure(resourceName);
            final Properties props = new Properties();
            props.load(in); // FIXME use an UTF8 reader for this
            for (final Map.Entry<Object, Object> entry : props.entrySet()) {
                className = String.valueOf(entry.getValue());
                final Class<?> klass =
                        ConfigurationReaderFactory.class.getClassLoader().loadClass(className);
//                final Class<?> klass = Class.forName(className);
                if (ConfigurationReader.class.isAssignableFrom(klass))
                    READERS_BY_EXTENSION.put(String.valueOf(entry.getKey()),
                            (Class<? extends ConfigurationReader>)klass);
            }
        }
        catch (final IOException e) {
            System.out.printf("Failed to read factory configuration ‹%s›", resourceName);
            throw new InitializationError(String.format("Failed to load resource %s", resourceName),
                    e);
        }
        catch (final ClassNotFoundException e) {
            System.out.printf("Configuration reader ‹%s› not found!", className);
            throw new InitializationError(String.format("Class not found: %s", className), e);
        }
    }

    /**
     * Returns a reader for the configuration with the specified configuration context located at
     * the specified resource based on the specified file extension.
     *
     * @param context   the configuration context.
     * @param resource  the configuration resource to read.
     * @param extension the file extension.
     * @return a configuration file reader.
     */
    public static ConfigurationReader createParser(final String context, final URL resource,
                                                   final String extension)
            throws ResourceInstantiationFailure {
        final Class<? extends ConfigurationReader> reader = READERS_BY_EXTENSION.get(extension);
        try {
            return reader != null ? reader.getConstructor(String.class, URL.class)
                                          .newInstance(context, resource)
                                  : createParser(context, resource);
        }
        catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                NoSuchMethodException e) {
            throw new ResourceInstantiationFailure(resource,
                    String.format("Failed to instantiate configuration file reader %s",
                            reader != null ? reader.getName() : "<unknown>"), e);
        }
    }

    private static ConfigurationReader createParser(final String context, final URL resource) {
        // TODO: parse as many lines of the resource file as necessary to determine its encoding,
        // then create an appropriate configuration file reader for the resource
        throw new InitializationError(
                String.format("Cannot determine content type of resource %s", resource));
    }

}
