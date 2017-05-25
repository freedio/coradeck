package com.coradec.coracar;

import com.coradec.coracore.model.Scope;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * ​​An implementation.
 */
@com.coradec.coracore.annotation.Implementation(Scope.TEMPLATE)
public class Implementation implements Interface {

    private static final AtomicInteger ID = new AtomicInteger(0);
    private final String id;

    public Implementation() {
        id = "X" + ID.getAndIncrement();
    }

    @Override public String getValue() {
        return id;
    }

}
