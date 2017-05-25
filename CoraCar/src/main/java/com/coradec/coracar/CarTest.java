package com.coradec.coracar;

import com.coradec.coracore.annotation.Inject;
import com.coradec.coracore.model.Timer;
import com.coradec.coracore.util.ClassUtil;

import java.time.temporal.ChronoUnit;

/**
 * ​​The actual executable.
 */
public class CarTest {

    @Inject private Interface x;
    @Inject private Interface y;
    @Inject private Timer timer;

    public static void main(String... args) {
        new CarTest().launch(args);
    }

    private void launch(final String... args) {
        System.out.printf("Running class %s%n", ClassUtil.nameOf(CarTest.class));
        System.out.println("My ClassLoader is " + CarTest.class.getClassLoader());
        System.out.printf("The values are %s and %s%n", x.getValue(), y.getValue());
        timer.stop();
        System.out.printf("This took %sns%n", timer.get(ChronoUnit.NANOS));
    }

}
