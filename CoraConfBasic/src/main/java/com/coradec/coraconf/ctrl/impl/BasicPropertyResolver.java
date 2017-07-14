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

package com.coradec.coraconf.ctrl.impl;

import static com.coradec.coracore.model.Scope.*;

import com.coradec.coraconf.ctrl.PropertyResolver;
import com.coradec.coraconf.model.Configuration;
import com.coradec.coracore.annotation.Implementation;
import com.coradec.coracore.annotation.Inject;
import com.coradec.coracore.annotation.Nullable;
import com.coradec.coracore.collections.HashCache;
import com.coradec.coracore.model.Factory;
import com.coradec.coracore.model.GenericType;

import java.util.Optional;

/**
 * ​​Basic implementation of a property resolver.
 */
@SuppressWarnings("ClassHasNoToStringMethod")
@Implementation(SINGLETON)
public class BasicPropertyResolver implements PropertyResolver {

    @Inject
    private Factory<Configuration> configurationFactory;
    @Inject
    private HashCache<String, Configuration> configurations;

    @Override
    public <R> Optional<R> resolve(final GenericType<R> type, @Nullable final String context,
                                   final String name, final Object[] args) {
        Configuration configuration = getConfiguration(context);
        return configuration.lookup(type, name, args);
    }

    private Configuration getConfiguration(final @Nullable String context) {
        return context != null //
               ? configurations.computeIfAbsent(context, ctx -> configurationFactory.get(ctx))
               : configurations.computeIfAbsent("", ctx -> configurationFactory.get());
    }

}
