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

import com.coradec.coracore.annotation.Inject;

import java.lang.reflect.Type;

/**
 * ​​Dynamic implementation of a generic factory.
 */
@SuppressWarnings("ClassHasNoToStringMethod")
public class DynamicFactory<T> {

    @Inject private MetaFactory<T> META_FACTORY;

    public DynamicFactory() {
    }

    /**
     * Returns a suitable factory for parametrized instances with the specified raw type and type
     * parameters.
     *
     * @param baseType the base type.
     * @param paras    the type parameters.
     * @return a generic factory.
     */
    public Factory<T> of(final Class<? super T> baseType, final Type... paras) {
        return META_FACTORY.get(baseType, paras);
    }

    /**
     * Returns a suitable factory for instances of the specified generic type.
     *
     * @param genericType the generic type.
     * @return a generic factory.
     */
    public Factory<T> of(final GenericType<? super T> genericType) {
        return META_FACTORY.get(genericType);
    }

}
