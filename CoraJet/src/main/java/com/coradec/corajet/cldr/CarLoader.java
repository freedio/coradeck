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
            Syslog.info("Class-Path: %s%n", applicationClassPath);
            final String applicationClassName = mainAttributes.getValue("Application");
            Syslog.info("Application: %s%n", applicationClassName);
            final Class<?> application = loader.findClass(applicationClassName);
            final Method method = application.getMethod("main", String[].class);
            Syslog.info("Invoking %s.%s%s%n", application.getName(), method,
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
