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

package com.coradec.coracore.model.impl;

import com.coradec.coracore.model.Origin;
import com.coradec.coracore.util.ClassUtil;

import java.net.URI;

/**
 * ​​An URI-based origin
 */
public class URIgin implements Origin {

    private final URI location;

    public URIgin(final URI location) {
        this.location = location;
    }

    @Override public URI toURI() {
        return location;
    }

    @Override public String represent() {
        return location.toString();
    }

    @Override public String toString() {
        return ClassUtil.toString(this);
    }

    @Override public boolean equals(final Object o) {
        return o instanceof URIgin && ((URIgin)o).toURI().equals(toURI());
    }

    @Override public int hashCode() {
        return location != null ? location.hashCode() : 0;
    }
}
