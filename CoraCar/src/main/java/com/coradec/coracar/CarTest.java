/*
 * Copyright ⓒ 2017 by Coradec GmbH.
 *
 * This file is part of the Coradeck.
 *
 * Coradeck is free software: you can redistribute it under the the terms of the GNU General
 * Public License as published by the Free Software Foundation, either version 3 of the License,
 * or any later version.
 *
 * Coradeck is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR ANY PARTICULAR PURPOSE.  See the
 * GNU General Public License for further details.
 *
 * The GNU General Public License is available from <http://www.gnu.org/licenses/>.
 *
 * @license GPL-3.0+ <http://spdx.org/licenses/GPL-3.0+>
 * @author Dominik Wezel <dom@coradec.com>
 *
 */

package com.coradec.coracar;

import com.coradec.coracore.annotation.Inject;
import com.coradec.coracore.model.Timer;
import com.coradec.coracore.util.ClassUtil;

import java.time.temporal.ChronoUnit;

/**
 * ​​The actual executable.
 */
@SuppressWarnings({"ClassHasNoToStringMethod", "UseOfSystemOutOrSystemErr"})
public class CarTest {

    @Inject
    private static Interface z;
    @Inject
    private static GenericInterface<String, Integer> a;

    @Inject private Interface x;
    @Inject
    private GenericInterface<Integer, String> y;
    @Inject private Timer timer;

    public static void main(String... args) {
        new CarTest().launch(args);
    }

    private void launch(final String... args) {
        System.out.printf("Running class %s%n", ClassUtil.nameOf(CarTest.class));
        System.out.println("My ClassLoader is " + CarTest.class.getClassLoader());
        System.out.printf("The values are %s and %s%n", x.getValue(), z.getValue());
        System.out.printf("The generic values are %s=%d and %d=%s%n", a.getKey(), a.getValue(),
                y.getKey(), y.getValue());
        timer.stop();
        System.out.printf("This took %sns%n", timer.get(ChronoUnit.NANOS));
    }

}
