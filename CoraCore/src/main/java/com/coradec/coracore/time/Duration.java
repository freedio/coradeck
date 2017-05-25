package com.coradec.coracore.time;

import java.util.concurrent.TimeUnit;

/**
 * ​Representation of a duration with a time unit and an amount.
 */
public interface Duration {

    /**
     * Returns the amount.
     *
     * @return the amount.
     */
    long getAmount();

    /**
     * Returns the time unit.
     *
     * @return the time unit.
     */
    TimeUnit getUnit();
}
