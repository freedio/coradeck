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

package com.coradec.corajet.cldr;

import com.coradec.coracore.annotation.Implementation;
import com.coradec.coracore.ctrl.Factory;
import com.coradec.coracore.model.GenericType;
import com.coradec.coracore.model.MetaFactory;
import com.coradec.coracore.trouble.ObjectInstantiationFailure;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;

/**
 * ​​Implementation of a factory factory.
 */
@SuppressWarnings({"unchecked", "ClassHasNoToStringMethod"})
@Implementation
public class BasicMetaFactory<G> implements MetaFactory<G> {

    private final ClassLoader loader;

    public BasicMetaFactory() {
        loader = getClass().getClassLoader();
        if (!loader.getClass()
                   .getName()
                   .equals(getClass().getPackage().getName() + ".CarClassLoader"))
            throw new IllegalStateException("Invalid ClassLoader: " + loader);
    }

    @Override public Factory<G> get(final Class<? super G> baseType, final Type... typeArgs) {
        try {
            return new ImplementationFactory<>(loader, baseType, typeArgs);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("UnsuitableClassLoader (requires CarClassLoader): " +
                                            getClass().getClassLoader());
        }
    }

    @Override public Factory<G> get(final GenericType<? super G> genericType) {
        try {
            return new ImplementationFactory<>(loader, (Class<? super G>)genericType.getRawType(),
                    genericType.getActualTypeArguments());
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("UnsuitableClassLoader (requires CarClassLoader): " +
                                            getClass().getClassLoader());
        }
    }

    @SuppressWarnings("unchecked")
    private class ImplementationFactory<I> implements Factory<I> {

        private final ClassLoader loader;
        private final Class<? super I> klass;
        private final List<Type> typeArgs;
        private final Method implement;

        ImplementationFactory(final ClassLoader loader, final Class<? super I> klass,
                              final Type... typeArgs) throws NoSuchMethodException {
            this.loader = loader;
            this.implement = loader.getClass()
                                   .getMethod("implement", Class.class, List.class, Object[].class);
            this.klass = klass;
            this.typeArgs = Arrays.asList(typeArgs);
        }

        @Override public I get(final Object... args) {
            try {
                return (I)implement.invoke(loader, klass, typeArgs, args);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new ObjectInstantiationFailure(klass, e);
            }
        }

        @Override public I create(final Object... args) {
            try {
                return (I)implement.invoke(loader, klass, typeArgs, args);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new ObjectInstantiationFailure(klass, e);
            }
        }

    }

}
