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

package com.coradec.coradoc.model.impl;

import com.coradec.coracore.annotation.ToString;
import com.coradec.coracore.util.ClassUtil;
import com.coradec.coradoc.model.CssPosition;
import com.coradec.coradoc.model.IntegerMeasure;

import java.util.Objects;

/**
 * ​​Basic implementation of a CSS position.
 */
public class BasicCssPosition implements CssPosition {

    private final IntegerMeasure horizontal;
    private final IntegerMeasure vertical;

    public BasicCssPosition(final IntegerMeasure horizontal, final IntegerMeasure vertical) {
        this.horizontal = horizontal;
        this.vertical = vertical;
    }

    @Override @ToString public IntegerMeasure getX() {
        return horizontal;
    }

    @Override @ToString public IntegerMeasure getY() {
        return vertical;
    }

    @Override public boolean equals(final Object o) {
        return o instanceof CssPosition &&
               Objects.equals(getX(), ((CssPosition)o).getX()) &&
               Objects.equals(getY(), ((CssPosition)o).getY());
    }

    @Override public int hashCode() {
        return 3 * Objects.hashCode(horizontal) + 7 * Objects.hashCode(vertical);
    }

    @Override public String toString() {
        return ClassUtil.toString(this);
    }
}
