package com.coradec.coracore.util;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * ​​Static library of collection related utilities.
 */
public final class CollectionUtil {

    private CollectionUtil() {
    }

    /**
     * Creates a set from the specified array.  There are no guarantees on the type, mutability,
     * serializability, or thread-safety of the Set returned.
     *
     * @param <T>   the type of data.
     * @param array the array containing the data for the set.
     * @return a set.
     */
    public static <T> Set<T> setOf(final T... array) {
        return Stream.of(array).collect(Collectors.toSet());
    }

}
