package com.coradec.corajet.ctrl;

import com.coradec.coracore.annotation.Nullable;
import com.coradec.corajet.test.Implementation;
import com.coradec.corajet.test.Interface;

import java.util.Optional;

/**
 * ​​The basic injector.
 */
public class Injector {

    private static final Injector INSTANCE = new Injector();

    public static void finish(final Object obj) {
        INSTANCE.doInjectOn(obj);
    }

    /**
     * Tries to find an implementation for the specified class.
     * @param type the interface or abstract class to implement.
     * @return an implementation, if any available.
     */
    public static <T> Optional<Class<? extends T>> findImplementationFor(final Class<T> type) {
        return Optional.ofNullable(INSTANCE.implementationOf(type));
    }

    public static <T> T implement(final Class<? super T> type) {
        return null;
    }

    private Injector() {
    }

    private <T> @Nullable Class<? extends T> implementationOf(final Class<T> type) {
        if (Interface.class.isAssignableFrom(type)) return (Class<? extends T>)Implementation.class;
        return null;
    }

    private void doInjectOn(final Object obj) {

    }

}
