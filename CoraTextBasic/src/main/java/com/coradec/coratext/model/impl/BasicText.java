package com.coradec.coratext.model.impl;

import com.coradec.coracore.annotation.Component;
import com.coradec.coracore.annotation.Implementation;
import com.coradec.coracore.annotation.Inject;
import com.coradec.coracore.annotation.ToString;
import com.coradec.coracore.util.ClassUtil;
import com.coradec.coratext.ctrl.TextResolver;
import com.coradec.coratext.model.Text;

/**
 * ​​Basic implementation of a text literal.
 */
@Implementation @Component
public class BasicText implements Text {

    @SuppressWarnings("ProtectedField") @Inject protected static TextResolver RESOLVER;

    private final String name;

    /**
     * Initializes a new instance of BasicText with the specified literal name.
     *
     * @param name the literal name.
     */
    public BasicText(final String name) {
        this.name = name;
    }

    @Override @ToString public String getName() {
        return this.name;
    }

    @Override public String resolve(final Object... args) {
        return RESOLVER.resolve(null, getName(), args);
    }

    @Override public String toString() {
        return ClassUtil.toString(this);
    }
}
