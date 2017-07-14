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

package com.coradec.coracore.util;

import com.coradec.coracore.annotation.Inject;
import com.coradec.coracore.model.Factory;
import com.coradec.coracore.model.StackFrame;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * ​​Static library of execution utilities.
 */
@SuppressWarnings("WeakerAccess")
public final class ExecUtil {

    private static final Pattern INVOC_PATTERN = Pattern.compile("access\\$\\d+");
    private static final Set<String> SYNTHETIC_CLASSES = new HashSet<>(
            Arrays.asList("sun.reflect.DelegatingMethodAccessorImpl",
                    "sun.reflect.NativeMethodAccessorImpl"));
    @Inject private static Factory<StackFrame> STACKFRAME_FACTORY;

    private ExecUtil() {
    }

    /**
     * Returns the current stack frame.
     *
     * @return the current stack frame.
     */
    public static StackFrame getCurrentStackFrame() {
        return getStackFrame(1);
    }

    /**
     * Returns the current stack frame with the specified caller's real class name.
     *
     * @param realClassName the current real class name.
     * @return the current stack frame.
     */
    public static StackFrame getCurrentStackFrame(final String realClassName) {
        return getStackFrame(1, realClassName);
    }

    /**
     * Returns the caller's stack frame.
     *
     * @return the caller's stack frame.
     */
    public static StackFrame getCallerStackFrame() {
        return getStackFrame(2);
    }

    /**
     * Returns the caller's stack frame with the specified caller's real class name.
     *
     * @param realClassName the caller's real class name.
     * @return the caller's stack frame.
     */
    public static StackFrame getCallerStackFrame(final String realClassName) {
        return getStackFrame(2, realClassName);
    }

    private static StackTraceElement extract(final int base, final StackTraceElement[] stackTrace) {
        int level = base;
        StackTraceElement result;
        do {
            result = stackTrace[level++];
        } while (INVOC_PATTERN.matcher(result.getMethodName()).matches() ||
                 SYNTHETIC_CLASSES.contains(result.getClassName()));
        return result;
    }

    /**
     * Returns the caller's caller's stack frame.
     *
     * @return the caller's caller's stack frame.
     */
    public static StackFrame getCallerCallerStackFrame() {
        return getStackFrame(3);
    }

    /**
     * Returns the caller's caller's stack frame with the specified caller's real class name.
     *
     * @param realClassName the caller's caller's real class name.
     * @return the caller's caller's stack frame.
     */
    public static StackFrame getCallerCallerStackFrame(final String realClassName) {
        return getStackFrame(3, realClassName);
    }

    /**
     * Returns the stack frame of the <i>level</i>th stack frame.
     *
     * @param level         the level.
     * @param realClassName the caller's real class name.
     * @return a stack frame.
     */
    public static StackFrame getStackFrame(final int level, final String realClassName) {
        return STACKFRAME_FACTORY.get(extract(level + 2, Thread.currentThread().getStackTrace()),
                realClassName);
    }

    /**
     * Returns the stack frame of the <i>level</i>th stack frame.
     *
     * @param level         the level.
     * @return a stack frame.
     */
    public static StackFrame getStackFrame(final int level) {
        return STACKFRAME_FACTORY.get(extract(level + 2, Thread.currentThread().getStackTrace()));
    }

}
