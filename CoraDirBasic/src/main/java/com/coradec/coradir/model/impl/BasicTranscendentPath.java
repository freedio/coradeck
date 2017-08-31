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
import com.coradec.coracore.annotation.NonNull;
import com.coradec.coracore.util.NetworkUtil;
import com.coradec.coradir.model.Path;
import com.coradec.coradir.model.TranscendentPath;

import java.net.URI;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * ​​Implementation of a transcendent path.
 */
@SuppressWarnings("ClassHasNoToStringMethod")
@Implementation
public class BasicTranscendentPath extends BasicPath implements TranscendentPath {

    private final String hostname;

    /**
     * Initializes a new instance of BasicTranscendentPath consisting of the specified names in the
     * order of their appearance relative to the specified host name.  The resulting path will be
     * transcendent. If the last element is the empty string, the resulting path will refer to a
     * directory.
     *
     * @param names a list of names.
     */
    public BasicTranscendentPath(final @NonNull String hostname, final String... names) {
        this(hostname, Arrays.asList(names));
    }

    /**
     * Initializes a new instance of BasicTranscendentPath consisting of the specified names in
     * their order in the list relative to the specified host and machine name.  The resulting path
     * will be transcendent. If the last element is the empty string, the resulting path will refer
     * to a directory.
     *
     * @param names a list of names.
     */
    public BasicTranscendentPath(final @NonNull String hostname, final List<String> names) {
        super(names);
        this.hostname = canonical(hostname);
    }

    /**
     * Initializes a new instance of BasicTranscendentPath from the specified URI.
     *
     * @param uri the URI.
     */
    public BasicTranscendentPath(final @NonNull URI uri) {
        super(uri.getPath().replaceFirst("^/+", ""));
        hostname = uri.getAuthority();
        debug("Constructing path from URI %s → %s:%s", uri, getHostname(), super.toString());
    }

    public String getHostname() {
        return hostname;
    }

    @Override public String represent() {
        return "//" + hostname + Path.separator() + super.represent();
    }

    @Override public URI toURI(final String schema) {
        final String collector = schema + ':' + represent();
        return URI.create(collector);
    }

    @Override public boolean isTranscendent() {
        return true;
    }

    @Override public boolean isAbsolute() {
        return true;
    }

    @Override public boolean isLocalAbsolute() {
        return false;
    }

    @Override public boolean isName() {
        return false;
    }

    @Override public Path tail() {
        return localize();
    }

    @Override public Path add(final String name) {
        final List<String> names = new ArrayList<>(getPath());
        names.add(name);
        return new BasicTranscendentPath(getHostname(), names);
    }

    @Override public TranscendentPath transcend() {
        return this;
    }

    private String canonical(String hostname) {
        try {
            if (hostname == null || hostname.isEmpty()) return NetworkUtil.getCanonicalHostName();
            return NetworkUtil.getCanonicalHostName(hostname);
        } catch (UnknownHostException e) {
            return hostname;
        }
    }

    @Override public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof TranscendentPath)) return false;
        final TranscendentPath that = (TranscendentPath)o;
        return hostname.equals(that.getHostname()) && super.equals(o);
    }

    @Override public int hashCode() {
        return 31 * super.hashCode() + (hostname == null ? 0 : hostname.hashCode());
    }

}
