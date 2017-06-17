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

import com.coradec.coracore.ctrl.Factory;

import java.lang.reflect.Type;

/**
 * ​A factory creating factories.
 */
public interface MetaFactory<G> {

    /**
     * Returns a factory for objects of the specified base type with the specified type arguments..
     *
     * @param baseType the base type selector.
     * @param typeArgs additional type arguments as needed.
     * @return a factory for instances of G.
     */
    Factory<G> get(Class<? super G> baseType, Type... typeArgs);

    /**
     * Returns a factory for objects of the specified generic type.
     *
     * @param genericType the generic type.
     * @return a factory for instances of G.
     */
    Factory<G> get(GenericType<? super G> genericType);

}
