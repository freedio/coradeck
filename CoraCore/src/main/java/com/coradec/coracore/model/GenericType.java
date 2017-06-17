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

import com.coradec.coracore.annotation.Nullable;
import com.coradec.coracore.util.ClassUtil;

import java.lang.reflect.Array;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * ​​Representation of a parametrized type.
 */
public final class GenericType<T> implements Representable, ParameterizedType {

    private final Class<T> type;
    private final Type[] parameters;

    private GenericType(final Class<T> type, final Type... parameters) {
        this.type = type;
        this.parameters = parameters;
    }

    /**
     * Creates a new parametrized type based on the specified class with the specified parameter
     * types.
     *
     * @param <X>        the base type.
     * @param type       the base type selector.
     * @param parameters the parameter types.
     * @return a new parametrized type.
     */
    @SuppressWarnings("unchecked") public static <X> GenericType<X> of(final Class<? super X> type,
                                                                       Class<?>... parameters) {
        return new GenericType(type, parameters);
    }

    @Override public String represent() {
        return String.format("%s%s", //
                ClassUtil.nameOf(type), //
                Stream.of(parameters)
                      .map(ClassUtil::nameOf)
                      .collect(Collectors.joining(",", "<", ">")));
    }

    @Override public String toString() {
        return ClassUtil.toString(this);
    }

    @SuppressWarnings("unchecked") public T[] arrayOf(final int size) {
        return (T[])Array.newInstance(type, size);
    }

    @Override public Type[] getActualTypeArguments() {
        return parameters;
    }

    @Override public Type getRawType() {
        return type;
    }

    @Override public @Nullable Type getOwnerType() {
        return null;
    }

    @Override public String getTypeName() {
        final StringBuilder result = new StringBuilder(getRawType().getTypeName());
        final String typeArgs = Stream.of(getActualTypeArguments())
                                      .map(String::valueOf)
                                      .collect(Collectors.joining(", "));
        if (!typeArgs.isEmpty()) result.append('<').append(typeArgs).append('>');
        return result.toString();
    }

    public boolean isInstance(final @Nullable Object value) {
        return type.isInstance(value);
    }
}
