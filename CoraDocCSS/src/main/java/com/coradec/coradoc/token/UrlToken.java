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

package com.coradec.coradoc.token;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * ​​A URL token.
 */
@SuppressWarnings("ClassHasNoToStringMethod")
public class UrlToken extends BasicParserToken {

    private String value;

    public void setValue(final String value) {
        this.value = value;
    }

    /**
     * Returns the raw string value of the URL.
     *
     * @return the raw value.
     */
    public String getValue() {
        return value;
    }

    /**
     * Returns the value as a URL.
     *
     * @return the URL.
     * @throws MalformedURLException if the raw string value does not represent an URL.
     */
    public URL toURL() throws MalformedURLException {
        Path currentDir = Paths.get(System.getProperty("user.dir"));
        final URL context = currentDir.toUri().toURL();
        return new URL(context, value);
    }

}
