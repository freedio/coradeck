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

package com.coradec.coraconf.model.impl;

import com.coradec.coraconf.model.AnnotatedProperty;
import com.coradec.coracore.annotation.Implementation;
import com.coradec.coracore.annotation.Nullable;
import com.coradec.coracore.annotation.ToString;
import com.coradec.coracore.model.GenericType;

/**
 * ​​Basic implementation of an annotated property.
 */
@Implementation
@SuppressWarnings("ClassHasNoToStringMethod")
public class BasicAnnotatedProperty extends BasicProperty<String> implements AnnotatedProperty {

    private final String type;
    private final String annotation;

    public BasicAnnotatedProperty(final String name, final @Nullable String type,
                                  final String value, final @Nullable String annotation) {
        super(GenericType.of(String.class), name, value);
        this.type = type;
        this.annotation = annotation;
    }

    @Override @ToString public @Nullable String getRawType() {
        return this.type;
    }

    @Override @ToString public @Nullable String getAnnotation() {
        return this.annotation;
    }

    @Override @ToString public String getRawValue() {
        return getDefaultValue();
    }

}
