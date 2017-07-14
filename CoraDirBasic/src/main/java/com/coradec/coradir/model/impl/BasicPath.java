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

package com.coradec.coradir.model.impl;

import com.coradec.coraconf.model.Property;
import com.coradec.coracore.annotation.Implementation;
import com.coradec.coracore.util.ClassUtil;
import com.coradec.coracore.util.SystemUtil;
import com.coradec.coradir.model.Path;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ​​Basic implementation of a directory path.
 */
@Implementation
public class BasicPath implements Path {

    private static final Property<String> PROP_SEPARATOR =
            Property.define("PathSeparator", String.class, "/");
    private final ArrayList<String> path;

    /**
     * Initializes a new instance of BasicPath consisting of the specified names in the order of
     * their appearance.  If the first element is the empty string, the resulting path will be
     * absolute. If the last element is the empty string, the resulting path will refer to a
     * directory.
     *
     * @param names a list of names.
     */
    public BasicPath(String... names) {
        path = new ArrayList<>(Arrays.asList(names));
    }

    /**
     * Initializes a new instance of BasicPath consisting of the specified names in their order in
     * the list.  If the first element is the empty string, the resulting path will be
     * absolute. If the last element is the empty string, the resulting path will refer to a
     * directory.
     *
     * @param names a list of names.
     */
    public BasicPath(List<String> names) {
        path = new ArrayList<>(names);
    }

    @Override public String represent() {
        return path.stream().collect(Collectors.joining(PROP_SEPARATOR.value()));
    }

    @Override public String toString() {
        return ClassUtil.toString(this);
    }

    @Override public URI toURI(final String schema) {
        StringBuilder collector = new StringBuilder(256);
        collector.append(schema).append(':');
        if (isAbsolute()) {
            collector.append("//");
            collector.append(SystemUtil.getCanonicalHostName());
        }
        collector.append(represent());
        return URI.create(collector.toString());
    }

    @Override public boolean isAbsolute() {
        return !path.isEmpty() && path.get(0).isEmpty();
    }

    @Override public Path add(final String name) {
        final List<String> names = new ArrayList<>(path);
        names.add(name);
        return new BasicPath(names);
    }

}
