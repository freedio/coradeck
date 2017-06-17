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

package com.coradec.corajet.test;

import com.coradec.corajet.cldr.CarClassLoader;
import com.coradec.corajet.cldr.Syslog;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;

/**
 * ​​A JUnit 4 test runner for coradeck modules.
 */
public class CoradeckJUnit4TestRunner extends BlockJUnit4ClassRunner {

    private static CarClassLoader CLASS_LOADER;
    private static Class<? extends Annotation> TEST_ANNOTATION;

    private static CarClassLoader getClassLoader() {
        if (CLASS_LOADER == null) CLASS_LOADER = new CarClassLoader();
        return CLASS_LOADER;
    }

    @SuppressWarnings("unchecked") private static Class<? extends Annotation> getTestAnnotation()
            throws ClassNotFoundException {
        if (TEST_ANNOTATION == null) TEST_ANNOTATION =
                (Class<? extends Annotation>)getClassLoader().findClass("org.junit.Test");
        return TEST_ANNOTATION;
    }

    /**
     * Creates a CoradeckJUnit4TestRunner to run {@code klass}
     *
     * @param klass the class to test.
     * @throws InitializationError if the test class is malformed.
     */
    public CoradeckJUnit4TestRunner(final Class<?> klass)
            throws InitializationError, ClassNotFoundException {
        super(getClassLoader().findClass(klass.getName()));
        try {
            final Field syslog_level = klass.getField("SYSLOG_LEVEL");
            final String level = String.valueOf(syslog_level.get(null));
            Syslog.setLevel(level);
            if (level.equals("DEBUG") || level.equals("TRACE"))
                getClassLoader().showImpplementations();
        } catch (IllegalAccessException | NoSuchFieldException e) {
            // static field SYSLOG_LEVEL undefined/not accessible ⇒ don't change current log level
        }
    }

    /**
     * Adds to {@code errors} for each method annotated with {@code @Test}that
     * is not a public, void instance method with no arguments.
     */

    protected void validateTestMethods(List<Throwable> errors) {
        try {
            validatePublicVoidNoArgMethods(getTestAnnotation(), false, errors);
        } catch (ClassNotFoundException e) {
            errors.add(e);
        }
    }

    /**
     * Returns the methods that run tests. Default implementation returns all
     * methods annotated with {@code @Test} on this class and superclasses that
     * are not overridden.
     */
    protected List<FrameworkMethod> computeTestMethods() {
        try {
            return getTestClass().getAnnotatedMethods(getTestAnnotation());
        } catch (ClassNotFoundException e) {
            return Collections.emptyList();
        }
    }

}