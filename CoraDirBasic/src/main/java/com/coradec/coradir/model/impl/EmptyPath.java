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
import com.coradec.coradir.model.Path;
import com.coradec.coradir.model.TranscendentPath;
import com.coradec.coradir.trouble.CannotTranscendEmptyPathException;
import com.coradec.coradir.trouble.PathEmptyException;

import java.net.URI;
import java.util.Collections;
import java.util.List;

/**
 * ​​Implementation of the empty path.
 */
@Implementation
public class EmptyPath implements Path {

    public EmptyPath() {
    }

    @Override public URI toURI(final String schema) {
        return URI.create(schema + ":.");
    }

    @Override public boolean isTranscendent() {
        return false;
    }

    @Override public boolean isAbsolute() {
        return false;
    }

    @Override public boolean isLocalAbsolute() {
        return false;
    }

    @Override public Path add(final String name) {
        return new BasicPath(name);
    }

    @Override public boolean isEmpty() {
        return true;
    }

    @Override public boolean isName() {
        return false;
    }

    @Override public String head() throws PathEmptyException {
        throw new PathEmptyException();
    }

    @Override public Path tail() {
        throw new PathEmptyException();
    }

    @Override public Path localize() {
        return this;
    }

    @Override public TranscendentPath transcend() {
        throw new CannotTranscendEmptyPathException();
    }

    @Override public String represent() {
        return ".";
    }

    @Override public int hashCode() {
        return 0;
    }

    @Override public boolean equals(final Object obj) {
        return obj instanceof EmptyPath;
    }

    protected List<String> getPath() {
        return Collections.emptyList();
    }

    /**
     * Returns the URI representation of the origin.
     *
     * @return the URI representation of the origin.
     */
    @Override public URI toURI() {
        return URI.create("empty");
    }
}
