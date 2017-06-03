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

package com.coradec.coracore.model;

import com.coradec.coracore.annotation.ToString;
import com.coradec.coracore.util.ClassUtil;

/**
 * ​​A tuple of objects
 */
public class Tuple {

    private final Object[] values;

    public Tuple(Object... values) {
        this.values = values.clone();
    }

    public <T> T get(int index) {
        return (T)values[index];
    }

    @ToString public Object[] getValues() {
        return values.clone();
    }

    @Override public String toString() {
        return ClassUtil.toString(this);
    }
}
