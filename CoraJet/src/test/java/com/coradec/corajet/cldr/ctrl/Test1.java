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

package com.coradec.corajet.cldr.ctrl;

import com.coradec.coracore.annotation.Inject;
import com.coradec.coracore.model.Timer;

import java.time.temporal.ChronoUnit;

/**
 * CarClassLoader and injector test with simple injection.
 */
@SuppressWarnings("ClassHasNoToStringMethod")
public class Test1 {

    @Inject
    private Timer timer;

    public static void main(String[] args) {
        new Test1().launch();
    }

    private void launch() {
        timer.start();
        try {
            Thread.sleep(123);
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
        timer.stop();
        System.out.printf("Timer stopped after %s ms.%n", timer.get(ChronoUnit.MILLIS));
    }

}
