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

package com.coradec.coratext.ctrl.impl;

import com.coradec.coraconf.model.Configuration;
import com.coradec.coraconf.model.Property;
import com.coradec.coracore.annotation.Implementation;
import com.coradec.coracore.model.Scope;
import com.coradec.coratext.ctrl.ApplicationTextBase;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ​​Basic implementation of an application text base.
 */
@SuppressWarnings("ClassHasNoToStringMethod")
@Implementation(Scope.SINGLETON)
public class BasicApplicationTextBase implements ApplicationTextBase {

    private final Map<String, String> applicationTextBase = new ConcurrentHashMap<>();

    @Override public Optional<String> lookup(final String name) {
        return Optional.ofNullable(applicationTextBase.get(name));
    }

    @Override public Configuration<String> add(
            final Collection<? extends Property<? extends String>> properties) {
        properties.forEach(prop -> applicationTextBase.put(prop.getName(), prop.value()));
        return this;
    }

}
