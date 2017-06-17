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

package com.coradec.coracore.collections.impl;

import com.coradec.coracore.annotation.Implementation;
import com.coradec.coracore.collections.HashCache;

import java.util.Objects;

/**
 * ​​Implementation of a cache based on a hash map.
 */
@Implementation
public class BasicHashCache<K, V> extends BasicCache<K, V> implements HashCache<K, V> {

    /**
     * Initializes a new instance of BasicHashCache with the specified initial capacity and load
     * factor.
     *
     * @param initialCapacity the initial capacity.
     * @param loadFactor      the load factor.
     */
    public BasicHashCache(final int initialCapacity, final float loadFactor) {
        super(initialCapacity, loadFactor);
    }

    /**
     * Initializes a new instance of BasicHashCache with the specified initial capacity and default
     * load factor.
     *
     * @param initialCapacity the initial capacity.
     */
    public BasicHashCache(final int initialCapacity) {
        super(initialCapacity);
    }

    /**
     * Initializes a new instance of BasicHashCache with default initial capacity and default load
     * factor.
     */
    public BasicHashCache() {
    }

    /**
     * Compares two keys with each other for equality.
     *
     * @param key1 one key.
     * @param key2 the other key.
     * @return {@code true} if both keys are equal, {@code false} if the are different.
     */
    @Override protected boolean isEqual(final Object key1, final K key2) {
        return Objects.equals(key1, key2);
    }
}
