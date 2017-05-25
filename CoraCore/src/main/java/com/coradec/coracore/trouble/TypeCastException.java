package com.coradec.coracore.trouble;

import com.coradec.coracore.annotation.ToString;

/**
 * ​​Indicates an attempt to cast an object to the wrong type.
 */
public class TypeCastException extends BasicException {

    private final Object actual;
    private final Class<?> expectedType;

    public TypeCastException(final Object actual, final Class<?> expectedType) {
        this.actual = actual;
        this.expectedType = expectedType;
    }

    @ToString public Object getActual() {
        return this.actual;
    }

    @ToString public Class<?> getExpectedType() {
        return this.expectedType;
    }
}
