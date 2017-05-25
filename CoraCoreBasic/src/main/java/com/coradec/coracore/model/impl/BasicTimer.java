package com.coradec.coracore.model.impl;

import com.coradec.coracore.annotation.Implementation;
import com.coradec.coracore.model.Timer;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * ​​BNasic implementation of a timer.
 */
@SuppressWarnings("ClassHasNoToStringMethod")
@Implementation
public class BasicTimer implements Timer {

    private Instant start = null, stop = null, lap = null;

    public BasicTimer() {
        this.start = lap();
    }

    @Override public Timer stop() {
        stop = lap = lap();
        return this;
    }

    private Instant lap() {
        return Instant.now();
    }

    @Override public long get(final ChronoUnit unit) {
        return unit.between(start, stop != null ? stop : lap != null ? lap : Instant.now());
    }

}
