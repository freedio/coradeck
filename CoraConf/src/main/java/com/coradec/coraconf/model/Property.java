package com.coradec.coraconf.model;

import com.coradec.coracore.ctrl.Factory;
import com.coradec.coracore.model.GenericFactory;
import com.coradec.coracore.model.Type;
import com.coradec.coracore.util.ExecUtil;

/**
 * ​A property definition.
 */
public interface Property<T> {

    Factory<Property<?>> property = new GenericFactory<>(Property.class);

    @SuppressWarnings("unchecked")
    static <X, D extends X> Property<X> define(String name, Class<? super X> type, D dflt) {
        return (Property<X>)property.get(Property.class,
                ExecUtil.getCallerStackFrame().getClassFileName(), name, type, dflt);
    }

    /**
     * Returns the property name.
     *
     * @return the property name.
     */
    String getName();

    /**
     * Returns the property type.
     *
     * @return the property type.
     */
    Type<T> getType();

    /**
     * Returns the value of the property, converted from the raw value after fitting the latter one
     * with the specified arguments..
     *
     * @param args arguments to fit into the value template.
     */
    T value(Object... args);

}
