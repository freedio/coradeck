package com.coradec.coracore.model;

/**
 * ​​A tuple of objects
 */
public class Tuple {

    private final Object[] values;

    public Tuple(Object... values) {
        this.values = values;
    }

    public <T> T get(int index) {
        return (T)values[index];
    }

}
