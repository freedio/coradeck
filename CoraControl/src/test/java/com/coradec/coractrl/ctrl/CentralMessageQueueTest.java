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

package com.coradec.coractrl.ctrl;

import com.coradec.corajet.cldr.CarClassLoader;
import org.junit.Ignore;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class CentralMessageQueueTest {

    @Ignore @Test public void test1()
            throws ClassNotFoundException, InvocationTargetException, IllegalAccessException,
                   NoSuchMethodException {
        System.setProperty("syslog.level", "INFORMATION");
        CarClassLoader loader = new CarClassLoader();
        final Class<?> test = loader.findClass(getClass().getName() + "1");
        final Method main = test.getMethod("main", String[].class);
        main.invoke(test, (Object)new String[0]);
    }

}
