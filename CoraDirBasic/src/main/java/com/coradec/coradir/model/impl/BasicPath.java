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

import com.coradec.coracore.annotation.Implementation;
import com.coradec.coracore.util.ClassUtil;
import com.coradec.coradir.model.Path;
import com.coradec.coradir.model.TranscendentPath;
import com.coradec.coradir.trouble.CannotTranscendRelativePathException;
import com.coradec.coradir.trouble.PathEmptyException;
import com.coradec.coralog.ctrl.impl.Logger;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ​​Basic implementation of a directory path.
 */
@Implementation
public class BasicPath extends Logger implements Path {

    private final List<String> path;

    /**
     * Initializes a new instance of BasicPath consisting of the specified atomic name.
     * <p>
     * The resulting path will be relative.
     *
     * @param name a single name.
     */
    public BasicPath(final String name) {
        path = new ArrayList<>(Collections.singletonList(name));
    }

    /**
     * Initializes a new instance of BasicPath consisting of the specified names in the order of
     * their appearance.  If the first element is the empty string, the resulting path will be
     * absolute. If the last element is the empty string, the resulting path will refer to a
     * directory.
     * <p>
     * Note that an empty argument list will not create an empty path, but a root.  In order to
     * create an empty path, use {@code new EmptyPath()};
     *
     * @param names a list of names.
     */
    public BasicPath(final String... names) {
        if (names.length == 0) path = new ArrayList<>(Collections.singleton(""));
        else path = new ArrayList<>(Arrays.asList(names));
    }

    /**
     * Initializes a new instance of BasicPath consisting of the specified names in their order in
     * the list.  If the first element is the empty string, the resulting path will be absolute. If
     * the last element is the empty string, the resulting path will refer to a directory.
     *
     * @param names a list of names.
     */
    public BasicPath(final List<String> names) {
        path = new ArrayList<>(names);
    }

    protected List<String> getPath() {
        return path;
    }

    @Override public String represent() {
        return path.stream().collect(Collectors.joining(PROP_SEPARATOR.value()));
    }

    @Override public String toString() {
        return ClassUtil.toString(this);
    }

    @Override public URI toURI(final String schema) {
        String collector = schema + ':' + represent();
        if (collector.length() == schema.length() + 1) collector += Path.separator();
        return URI.create(collector);
    }

    @Override public boolean isTranscendent() {
        return path.size() > 2 && path.get(0).isEmpty() && path.get(1).isEmpty();
    }

    @Override public boolean isAbsolute() {
        return !path.isEmpty() && path.get(0).isEmpty();
    }

    @Override public boolean isLocalAbsolute() {
        return isAbsolute() || path.size() > 1 && path.get(0).isEmpty() && !path.get(1).isEmpty();
    }

    @Override public Path add(final String name) {
        final List<String> names = new ArrayList<>(path);
        names.add(name);
        return new BasicPath(names);
    }

    @Override public boolean isEmpty() {
        return path.isEmpty();
    }

    @Override public boolean isName() {
        return path.size() == 1;
    }

    @Override public String head() {
        if (path.isEmpty()) throw new PathEmptyException();
        return path.get(0);
    }

    @Override public Path tail() {
        return new BasicPath(path.stream().skip(1).collect(Collectors.toList()));
    }

    @Override public Path localize() {
        final List<String> localized = localized();
        return localized.isEmpty() ? new EmptyPath() : new BasicPath(localized);
    }

    protected List<String> localized() {
        List<String> path = new ArrayList<>(this.path);
        while (!path.isEmpty() && path.get(0).isEmpty()) path.remove(0);
        return path;
    }

    @Override public TranscendentPath transcend() {
        if (!isAbsolute()) throw new CannotTranscendRelativePathException();
        return new BasicTranscendentPath("", localized());
    }

    @Override public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof BasicPath)) return false;
        final BasicPath basicPath = (BasicPath)o;
        return getPath().equals(basicPath.getPath());
    }

    @Override public int hashCode() {
        return getPath().hashCode();
    }

    /**
     * Returns the URI representation of the origin.
     *
     * @return the URI representation of the origin.
     */
    @Override public URI toURI() {
        return URI.create(represent());
    }

}
