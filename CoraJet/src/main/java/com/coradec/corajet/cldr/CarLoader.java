/*
 * Copyright ⓒ 2017 by Coradec GmbH.
 *
 * This file is part of the Coradeck.
 *
 * Coradeck is free software: you can redistribute it under the the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or any later version.
 *
 * Coradeck is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR ANY PARTICULAR PURPOSE.  See the GNU General Public License for further details.
 *
 * The GNU General Public License is available from <http://www.gnu.org/licenses/>.
 *
 * @license GPL-3.0+ <http://spdx.org/licenses/GPL-3.0+>
 *
 * @author Dominik Wezel <dom@coradec.com>
 */

package com.coradec.corajet.cldr;

import java.io.File;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

/**
 * ​​Implementation of the Comprehensive ARchive loader.
 */
public class CarLoader {

    public static void main(String... args) {
        System.exit(new CarLoader().launch(args));
    }

    private int launch(final String[] args) {
        try {
            Syslog.info("Boot loader starting ...");
            final String jarFileName = System.getProperty("java.class.path");
            if (!jarFileName.endsWith(".jar") ||
                jarFileName.indexOf(File.pathSeparatorChar) != -1) {
                throw new IllegalStateException("Not a CAR boot loader context!");
            }
            CarClassLoader loader = new CarClassLoader();
            JarFile jarFile = new JarFile(jarFileName);
            final Manifest manifest = jarFile.getManifest();
            final Attributes mainAttributes = manifest.getMainAttributes();
            final String applicationClassPath = mainAttributes.getValue("Class-Path");
            Syslog.info("Class-Path: %s", applicationClassPath);
            final String applicationClassName = mainAttributes.getValue("Application");
            Syslog.info("Application: %s", applicationClassName);
            final Class<?> application = loader.findClass(applicationClassName);
            final Method method = application.getMethod("main", String[].class);
            Syslog.info("Invoking %s.%s%s", application.getName(), method,
                    Arrays.toString(args).replaceFirst("^\\[", "(").replaceFirst("]$", ")"));
            method.invoke(null, (Object)args);
            return 0;
        }
        catch (Exception e) {
            e.printStackTrace();
            return 1;
        }

    }

}
