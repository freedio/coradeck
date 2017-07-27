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

package com.coradec.coradir.model;

import com.coradec.coraconf.model.Property;
import com.coradec.coracore.model.Factory;
import com.coradec.coracore.model.GenericFactory;
import com.coradec.coracore.model.Representable;

import java.net.URI;

/**
 * ​Representation of a directory path.
 */
public interface Path extends Representable {

    Factory<Path> PATH = new GenericFactory<>(Path.class);
    Property<String> PROP_SEPARATOR = Property.define("Separator", String.class, "/");

    /**
     * Returns a path consisting of the specified path representation.
     *
     * @param path the path.
     * @return a path.
     */
    static Path of(final String path) {
        return PATH.create((Object[])(path.split(PROP_SEPARATOR.value())));
    }

    /**
     * Returns a path from the specified list of names.
     *
     * @param names the list of names.
     * @return a path.
     */
    static Path of(String... names) {
        return PATH.create((Object)names);
    }

    /**
     * Returns the path as an URI with the specified schema.
     *
     * @param schema the schema (the part in front of the first colon).
     * @return the path as an URI.
     */
    URI toURI(String schema);

    /**
     * Checks if this path is absolute.
     *
     * @return {@code true} if the path is absolute, {@code false} if it is relative.
     */
    boolean isAbsolute();

    /**
     * Adds the specified name to the path.
     *
     * @param name the name to append.
     * @return a new path consisting of this path with the specified name appended.
     */
    Path add(String name);

    /**
     * Checks if the path is empty (has no elements).
     * <p>
     * Note: A path containing an empty string as its sole element is a singleton, not empty.
     *
     * @return {@code true} if the path is empty, {@code false} if not.
     */
    boolean isEmpty();

    /**
     * Checks if the path is a name (has only one element).
     *
     * @return {@code true} if the path is a name, {@code false} if not.
     */
    boolean isName();

    /**
     * Returns the first element of the path.
     *
     * @return the head.
     */
    String head();

    /**
     * Returns this path without its head.
     *
     * @return the tail of this path.
     */
    Path tail();

}