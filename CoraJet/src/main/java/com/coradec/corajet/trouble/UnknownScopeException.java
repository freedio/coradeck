package com.coradec.corajet.trouble;

import com.coradec.coracore.annotation.ToString;
import com.coradec.coracore.model.Scope;
import com.coradec.coracore.trouble.BasicException;
import com.coradec.corajet.cldr.CarInjector;

/**
 * ​​Indicates an attempt to instantiate a class with an unknown scope.  This typically means that
 * the {@link Scope} enum has been extended without reflecting this change in the {@link
 * CarInjector}.
 */
public class UnknownScopeException extends BasicException {

    private final Scope scope;

    public UnknownScopeException(final Scope scope) {
        this.scope = scope;
    }

    @ToString public Scope getScope() {
        return this.scope;
    }

}
