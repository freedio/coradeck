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

package com.coradec.coracore.ctrl;

import com.coradec.coracore.collections.WeakIdentityHashMap;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * ​​An object registry for recursive resolution.
 */
@SuppressWarnings("ClassHasNoToStringMethod")
public final class RecursiveObjects {

    private static final RecursiveObjects INSTANCE = new RecursiveObjects();

    public static RecursiveObjects getInstance() {
        return INSTANCE;
    }

    private final Map<Object, AtomicInteger> registry = new WeakIdentityHashMap<>();

    private RecursiveObjects() {
    }

    /**
     * Adds the specified object to the registry.
     *
     * @param object the object to add.
     */
    public void add(final Object object) {
        registry.computeIfAbsent(object, o -> new AtomicInteger(0)).incrementAndGet();
    }

    /**
     * Dereferences the specified object from the registry.
     * <p>
     * If no more references are left, the object is removed from the registry.
     *
     * @param object the object to dereference.
     */
    public void remove(final Object object) {
        if (registry.containsKey(object) && registry.get(object).decrementAndGet() == 0)
            registry.remove(object);
    }

    /**
     * Checks if the object is registered.
     *
     * @param object the object.
     * @return {@code true} if the object is registered, {@code false} if not.
     */
    public boolean contains(final Object object) {
        return registry.containsKey(object);
    }

}
