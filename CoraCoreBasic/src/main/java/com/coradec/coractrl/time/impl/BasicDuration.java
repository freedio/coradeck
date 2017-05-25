package com.coradec.coractrl.time.impl;

import com.coradec.coracore.annotation.Implementation;
import com.coradec.coracore.time.Duration;

import java.util.concurrent.TimeUnit;

/**
 * ​​Basic implementation of a duration.
 */
@Implementation
public class BasicDuration implements Duration {

    private final long amount;
    private final TimeUnit unit;

    public BasicDuration(long amount, TimeUnit unit) {
        this.amount = amount;
        this.unit = unit;
    }

    public long getAmount() {
        return amount;
    }

    public TimeUnit getUnit() {
        return unit;
    }

    @Override public String toString() {
        return String.format("%d %s", getAmount(), getUnit());
    }
}
