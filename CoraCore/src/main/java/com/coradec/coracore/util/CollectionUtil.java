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

package com.coradec.coracore.util;

import java.util.HashMap;
import java.util.Map;
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
    @SafeVarargs public static <T> Set<T> setOf(final T... array) {
        return Stream.of(array).collect(Collectors.toSet());
    }

    public static Map<String, String> mapOf(final String repr, final Object... args) {
        final Map<String, String> result = new HashMap<>();
        for (String entry : repr.split("\0")) {
            final String[] fields = entry.split(":");
            if (fields.length == 2) result.put(fields[0], fields[1]);
            else //noinspection UseOfSystemOutOrSystemErr
                System.err.printf("Invalid mapping «%s» skipped!", entry);
        }
        return result;
    }

}
