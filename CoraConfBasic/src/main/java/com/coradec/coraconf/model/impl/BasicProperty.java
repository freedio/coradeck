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

import com.coradec.coraconf.ctrl.PropertyResolver;
import com.coradec.coraconf.model.Property;
import com.coradec.coracore.annotation.Implementation;
import com.coradec.coracore.annotation.Inject;
import com.coradec.coracore.annotation.Nullable;
import com.coradec.coracore.annotation.ToString;
import com.coradec.coracore.model.Factory;
import com.coradec.coracore.model.GenericType;
import com.coradec.coracore.util.ClassUtil;

import java.util.Optional;

/**
 * ​​Basic implementation of a property.
 */
@SuppressWarnings("PackageVisibleField")
@Implementation
public class BasicProperty<R> implements Property<R> {

    @Inject static Factory<PropertyResolver> RESOLVER;

    private final @Nullable String context;
    private final String name;
    private final GenericType<R> type;
    private final R dflt;

    public <D extends R> BasicProperty(final GenericType<R> type, final @Nullable String context,
                                       final String name, final D dflt) {
        this.context = context;
        this.name = name;
        this.type = type;
        this.dflt = dflt;
    }

    public <D extends R> BasicProperty(final GenericType<R> type, final String name, final D dflt) {
        this(type, null, name, dflt);
    }

    @ToString @Nullable String getContext() {
        return this.context;
    }

    @Override @ToString public String getName() {
        return this.name;
    }

    @Override @ToString public GenericType<R> getType() {
        return this.type;
    }

    @ToString public R getDefaultValue() {
        return dflt;
    }

    @Override public R value(final Object... args) {
        return resolve(args).orElse(dflt);
    }

    private Optional<R> resolve(final Object... args) {
        return RESOLVER.get().resolve(getType(), getContext(), getName(), args);
    }

    @Override public String toString() {
        return ClassUtil.toString(this);
    }
}
