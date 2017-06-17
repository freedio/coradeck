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

package com.coradec.coraconf.ctrl;

import com.coradec.coracore.annotation.Nullable;
import com.coradec.coracore.model.GenericType;

import java.util.Optional;

/**
 * ​Resolves a property to its final value.
 */
public interface PropertyResolver {

    /**
     * Resolves the property with the specified name (in the specified context) and type, fitting
     * its raw representation with the specified arguments before decoding it to the target value.
     *
     * @param <R>     the property type.
     * @param type    the property type selector.
     * @param context the name space / context.
     * @param name    the property name.
     * @param args    optional args to fit into the raw representation.
     * @return the resolved property value.
     */
    <R> Optional<R> resolve(final GenericType<R> type, @Nullable String context, String name,
                            Object[] args);
}
