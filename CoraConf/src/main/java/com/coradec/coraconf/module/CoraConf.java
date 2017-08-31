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

package com.coradec.coraconf.module;

import com.coradec.coraconf.model.Property;
import com.coradec.coracore.annotation.Nullable;
import com.coradec.coracore.model.DynamicFactory;
import com.coradec.coracore.model.GenericType;

/**
 * ​​The configuration service façade.
 */
@SuppressWarnings("ClassHasNoToStringMethod")
public final class CoraConf {

    private static final CoraConf INSTANCE = new CoraConf();

    public static <X, D extends X> Property<X> define(final @Nullable String context,
            final String name,
            final GenericType<X> type, @Nullable final D dflt) {
        return INSTANCE.createProperty(context, name, type, dflt);
    }

    public static <X> Property<X> define(final String context, final String name,
            final GenericType<X> type) {
        return INSTANCE.createProperty(context, name, type);
    }

    private final DynamicFactory<Property<?>> property = new DynamicFactory<>();

    private CoraConf() {
    }

    @SuppressWarnings("unchecked")
    private <X, D extends X> Property<X> createProperty(final @Nullable Object context,
            final String name,
            final GenericType<X> type, @Nullable final D dflt) {
        return (Property<X>)property.of(Property.class, type).get(type, context, name, dflt);
    }

    @SuppressWarnings("unchecked")
    private <X> Property<X> createProperty(final String context, final String name,
            final GenericType<X> type) {
        return (Property<X>)property.of(Property.class, type).get(type, context, name, null);
    }
}
