package com.coradec.coracore.model;

import java.time.temporal.ChronoUnit;

/**
 * ​Representation of a t9mer.
 */
public interface Timer {

    /**
     * Stops the timer and registers the time.
     *
     * @return this timer, for method chaining.
     */
    Timer stop();

    /**
     * Returns the time of the last lap in the specified time unit.
     *
     * @param unit the time unit.
     * @return the amount of time since the last lap in the specified time unit.
     */
    long get(ChronoUnit unit);

}
