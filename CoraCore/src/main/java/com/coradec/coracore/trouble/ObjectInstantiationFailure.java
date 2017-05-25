package com.coradec.coracore.trouble;

import com.coradec.coracore.annotation.ToString;

/**
 * ​​Indicates a failure to create an instance of a particular class.
 */
public class ObjectInstantiationFailure extends BasicException {

    private final Class<?> type;

    public ObjectInstantiationFailure(final Class<?> type, final Throwable problem) {
        super(problem);
        this.type = type;
    }

    @ToString public Class<?> getType() {
        return this.type;
    }

}
