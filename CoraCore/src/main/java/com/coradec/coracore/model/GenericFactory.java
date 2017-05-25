package com.coradec.coracore.model;

import com.coradec.coracore.annotation.Inject;
import com.coradec.coracore.ctrl.Factory;

/**
 * ​​Generic implementation of a factory.
 */
@SuppressWarnings("ClassHasNoToStringMethod")
public class GenericFactory<G> implements Factory<G> {

    @Inject private Factory<G> delegate;

    public GenericFactory(final Class<? super G> textClass) {
    }

    @Override public G get(final Object... args) {
        return delegate.get(args);
    }

    @Override public G create(final Object... args) {
        return delegate.create(args);
    }
}
