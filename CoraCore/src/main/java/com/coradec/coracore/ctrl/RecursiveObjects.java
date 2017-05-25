package com.coradec.coracore.ctrl;

import com.coradec.coracore.collections.WeakIdentityHashMap;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * ​​An object registry for recursive resolution.
 */
public final class RecursiveObjects {

    private static final RecursiveObjects INSTANCE = new RecursiveObjects();

    public static RecursiveObjects getInstance() {
        return INSTANCE;
    }

    private Map<Object, AtomicInteger> registry = new WeakIdentityHashMap<>();

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
