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
import com.coradec.coradoc.cssenum.CssUnit;
import com.coradec.coradoc.model.Dimension;
import com.coradec.coradoc.model.IntegerMeasure;

/**
 * ​​An integer dimension.
 */
@SuppressWarnings("ClassHasNoToStringMethod")
public class IntegerDimension extends IntegerToken implements Dimension, IntegerMeasure {

    private final CssUnit unit;

    public IntegerDimension(final String repr, final int value, final CssUnit unit) {
        super(repr, value);
        this.unit = unit;
    }

    @Override @ToString public CssUnit getUnit() {
        return unit;
    }

    @Override public boolean equals(final Object o) {
        return o instanceof IntegerDimension &&
               super.equals(o) &&
               unit.equals(((IntegerDimension)o).getUnit());
    }

    @Override public int hashCode() {
        return super.hashCode() * 3 + unit.hashCode();
    }

}
