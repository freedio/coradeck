/*
 * Copyright ⓒ 2017 by Coradec GmbH.
 *
 * This file is part of the Coradeck.
 *
 * Coradeck is free software: you can redistribute it under the the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or any later version.
 *
 * Coradeck is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR ANY PARTICULAR PURPOSE.  See the GNU General Public License for further details.
 *
 * The GNU General Public License is available from <http://www.gnu.org/licenses/>.
 *
 * @license GPL-3.0+ <http://spdx.org/licenses/GPL-3.0+>
 *
 * @author Dominik Wezel <dom@coradec.com>
 */

package com.coradec.coratext.model.impl;

import com.coradec.coracore.annotation.Implementation;
import com.coradec.coracore.annotation.Inject;
import com.coradec.coracore.annotation.ToString;
import com.coradec.coracore.util.ClassUtil;
import com.coradec.coratext.ctrl.TextResolver;
import com.coradec.coratext.model.Text;

/**
 * ​​Basic implementation of a text literal.
 */
@Implementation
public class BasicText implements Text {

    @SuppressWarnings("PackageVisibleField")
    @Inject
    static TextResolver RESOLVER;

    private final String name;

    /**
     * Initializes a new instance of BasicText with the specified literal name.
     *
     * @param name the literal name.
     */
    @SuppressWarnings("WeakerAccess") public BasicText(final String name) {
        this.name = name;
    }

    @Override @ToString public String getName() {
        return this.name;
    }

    @Override public String resolve(final Object... args) {
        return RESOLVER.resolve(null, getName(), args);
    }

    @Override public String toString() {
        return ClassUtil.toString(this);
    }
}
