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

import com.coradec.coracore.annotation.ToString;

import java.util.Objects;

/**
 * ​​A numeric token.
 */
@SuppressWarnings("ClassHasNoToStringMethod")
public class NumericToken extends BasicParserToken {

    private final String repr;

    public NumericToken(final String repr) {
        this.repr = repr;
    }

    @ToString public String getRepr() {
        return repr;
    }

    @Override public boolean equals(final Object o) {
        return o instanceof NumericToken && Objects.equals(repr, ((NumericToken)o).getRepr());
    }

    @Override public int hashCode() {
        return repr.hashCode();
    }

}
