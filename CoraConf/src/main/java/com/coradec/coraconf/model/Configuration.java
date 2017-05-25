package com.coradec.coraconf.model;

import com.coradec.coracore.ctrl.Factory;
import com.coradec.coracore.model.GenericFactory;
import com.coradec.coracore.model.Type;

import java.util.Collection;
import java.util.Optional;

/**
 * ​Representation of a key-value-pairing.
 *
 * @param <V> the value type.
 */
public interface Configuration<V> {

    Factory<Configuration<?>> CONFIGURATION = new GenericFactory(Configuration.class);

    static <X> Configuration<X> of(Class<X> type, Class<?>... parameters) {
        return (Configuration<X>)CONFIGURATION.get(Configuration.class, Type.of(type, parameters));
    }

    /**
     * Looks up the property with the specified name.
     *
     * @param name the property name.
     * @return the property value, if available.
     */
    Optional<V> lookup(String name);

    /**
     * Adds the specified properties to the configuration.
     *
     * @param properties the properties to add.
     * @return this configuration, for method chaining.
     */
    Configuration<V> add(Collection<? extends Property<? extends V>> properties);

}
