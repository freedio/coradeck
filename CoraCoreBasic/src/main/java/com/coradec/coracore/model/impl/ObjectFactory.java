package com.coradec.coracore.model.impl;

import com.coradec.coracore.annotation.Implementation;
import com.coradec.coracore.ctrl.Factory;

/**
 * ​​Basic implementation of an object factory.
 */
@Implementation
public class ObjectFactory<G> implements Factory<G> {

    @Override public G get(final Object... args) {
        return null;
    }

    @Override public G create(final Object... args) {
        return null;
    }
}
