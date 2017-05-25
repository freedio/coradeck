package com.coradec.coracore.model;

import com.coradec.coracore.util.ClassUtil;

import java.lang.reflect.Array;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * ​​Representation of a parametrized type.
 */
public final class Type<T> implements Representable {

    private final Class<T> type;
    private final Class<?>[] parameters;

    private Type(final Class<T> type, final Class<?>... parameters) {
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
    @SuppressWarnings("unchecked") public static <X> Type<X> of(final Class<X> type,
                                                                Class<?>... parameters) {
        return new Type(type, parameters);
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

}
