package com.coradec.coracore.util;

import com.coradec.coracore.annotation.Inject;
import com.coradec.coracore.ctrl.Factory;
import com.coradec.coracore.model.Timer;

/**
 * ​​Static library of time related utilities.
 */
public final class TimeUtil {

    @Inject private static Factory<Timer> timer;

    private TimeUtil() {
    }

    public static Timer start() {
        return timer.create();
    }

}
