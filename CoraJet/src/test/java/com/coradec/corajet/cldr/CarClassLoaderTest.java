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

package com.coradec.corajet.cldr;

import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * ​​Tests the CAR class loader and various injection scenarios.
 * <p>
 * Assumptions: the class path is already set up to include the entire test directory.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class CarClassLoaderTest {

    private static final String PROP_CLASS_PATH = "java.class.path";

    @Ignore @Test public void aa_showClassPath() {
//        System.getProperties().forEach((key, value) -> System.out.printf("%s: %s%n", key, value));
        final String classPath = System.getProperty(PROP_CLASS_PATH);
        System.out.println("ClassPath:");
        for (final String element : classPath.split(File.pathSeparator)) {
            System.out.printf("• %s%n", element);
        }
    }

    @Test public void ab_testImplementationInjector()
            throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException,
                   IllegalAccessException {
        // Arrange:
        System.setProperty("syslog.level", "INFORMATION");
        final CarClassLoader classLoader = new CarClassLoader();
        // Act:
        final Class<?> test1 = classLoader.findClass("com.coradec.corajet.cldr.ctrl.Test1");
        final Method main = test1.getMethod("main", String[].class);
        main.invoke(test1, (Object)new String[0]);
        // Assert:
        // no exception being thrown means success
    }

    @Test public void ac_testGenericInjector()
            throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException,
                   IllegalAccessException {
        // Arrange:
        System.setProperty("syslog.level", "INFORMATION");
        final CarClassLoader classLoader = new CarClassLoader();
        // Act:
        final Class<?> test = classLoader.findClass("com.coradec.corajet.cldr.ctrl.Test2");
        final Method main = test.getMethod("main", String[].class);
        main.invoke(test, (Object)new String[0]);
        // Assert:
        // no exception being thrown means success
    }

    @Test public void ad_testMultiGenericInjector()
            throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException,
                   IllegalAccessException {
        // Arrange:
        System.setProperty("syslog.level", "INFORMATION");
        final CarClassLoader classLoader = new CarClassLoader();
        // Act:
        final Class<?> test = classLoader.findClass("com.coradec.corajet.cldr.ctrl.Test3");
        final Method main = test.getMethod("main", String[].class);
        main.invoke(test, (Object)new String[0]);
        // Assert:
        // no exception being thrown means success
    }

    @Test public void ae_testFactoryInjector()
            throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException,
                   IllegalAccessException {
        // Arrange:
        System.setProperty("syslog.level", "DEBUG");
        final CarClassLoader classLoader = new CarClassLoader();
        // Act:
        final Class<?> test = classLoader.findClass("com.coradec.corajet.cldr.ctrl.Test4");
        final Method main = test.getMethod("main", String[].class);
        main.invoke(test, (Object)new String[0]);
        // Assert:
        // no exception being thrown means success
    }

}
