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
 * <p>
 * Paths appear in 6 flavors: <dl> <dt>System transcendent path</dt><dd>
 * <code>//as.coradec.com/vm0/app/my-app/myComponent</code> <br/><em>This notation addresses every
 * single accessible component in the world through the explicit system bus (= host name, here:
 * as.coradec.com) and the explicit machine bus address part</em></dd> <dt>Machine transcendent
 * path</dt><dd><code>///vm0/app/my-app/myComponent</code><br/><em>Leaving the hostname away in a
 * global absolute path implicitly fills the gap with “localhost”<br/> System absolute addresses are
 * only valid within the host system</em></dd> <dt>Machine absolute
 * path</dt><dd><code>////my-app/myComponent</code><br/><em>Leaving
 * away the machine bus name in a system absolute path implicitly fills the gap with the current
 * machine bus name<br/>Local absolute addresses are valid within the local VM only.</em></dd>
 * <dt>Local absolute path</dt><dd><code>/my-app/myComponent</code><br/><em>This is an alternative
 * representation of the machine absolute path (which has too many slashes)<br/>Again, this type of
 * paths is valid only within the VM.</em></dd> <dt>Application relative
 * path</dt><dd><code>~/myComponent</code><br/><em>This path is relative and confined to the
 * application within which it is used<br/>This type of paths is valid only within an
 * application.</em></dd> <dt>Local relative path</dt><dd><code>myComponent</code><br/><em>A
 * relative path is relative to the innermost applicable service level from where it is used.
 * Typically, within an application, the local root is the application itself; from other points, it
 * is usually the machine bus.  “Applicable” means that if the path has no meaning within a lower
 * service level, it is applied to the next upper service level, a.s.o..  Thus, to access a
 * particular database table, e.g., it suffices to specify "db/DB-NAME/TABLE-NAME". If there is a
 * database on the application level that provides the specified table, it will be used; otherwise a
 * database on the machine bus may applicable; if not, the path will be applied to the system
 * bus.</em></dd> </dl>
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
     * Returns a path consisting of the specified atomic name.
     *
     * @param name the name.
     * @return a path.
     */
    static Path from(String name) {
        return PATH.create(name);
    }

    /**
     * Returns the path as an URI with the specified schema.
     *
     * @param schema the schema (the part in front of the first colon).
     * @return the path as an URI.
     */
    URI toURI(String schema);

    /**
     * Checks if the path is (system or machine) transcendent.
     *
     * @return {@code true} if the path is transcendent.
     */
    boolean isTranscendent();

    /**
     * Checks if the path is (machine or local) absolute.
     *
     * @return {@code true} if the path is absolute.
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

    /**
     * Returns the localized part of a transcendent or absolute path.
     *
     * @return a localized path.
     */
    Path localize();

}
