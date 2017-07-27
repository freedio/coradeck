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

package com.coradec.coracore.model.impl;

import com.coradec.coracore.annotation.Implementation;
import com.coradec.coracore.annotation.Nullable;
import com.coradec.coracore.annotation.ToString;
import com.coradec.coracore.model.StackFrame;

import java.net.URI;

/**
 * Basic implementation of a stack frame.​​
 */
@SuppressWarnings("ClassHasNoToStringMethod")
@Implementation
public class BasicStackFrame extends BasicOrigin implements StackFrame {

    private final String className;
    private final String methodName;
    private final String fileName;
    private final int lineNumber;
    private @Nullable String realClassName;

    public BasicStackFrame() {
        this(Thread.currentThread().getStackTrace()[2]);
    }

    public BasicStackFrame(final StackTraceElement frame) {
        className = frame.getClassName();
        methodName = frame.getMethodName();
        fileName = frame.getFileName();
        lineNumber = frame.getLineNumber();
    }

    public BasicStackFrame(final StackTraceElement frame, @Nullable final String realClassName) {
        this(frame);
        this.realClassName = realClassName;
    }

    @Override @ToString public String getClassName() {
        return className;
    }

    @Override public String getClassFileName() {
        return getClassName().replace('.', '/');
    }

    @ToString public String getMethodName() {
        return this.methodName;
    }

    @ToString public String getFileName() {
        return this.fileName;
    }

    @ToString public int getLineNumber() {
        return this.lineNumber;
    }

    @Override public URI toURI() {
        final String className = getClassName();
        final String methodName = getMethodName();
        final String fileName = getFileName();
        final int lineNumber = getLineNumber();
        if (realClassName == null || realClassName.equals(className)) return URI.create(
                String.format("javacode:at_%s.%s%%28%s%%3A%d%%29", className, methodName, fileName,
                        lineNumber));
        else return URI.create(
                String.format("javacode:at_%s.%s%%28%s%%3A%d%%29_in_%s", className, methodName,
                        fileName, lineNumber, realClassName));
    }

    @Override public String represent() {
        final String className = getClassName();
        final String methodName = getMethodName();
        final String fileName = getFileName();
        final int lineNumber = getLineNumber();
        if (realClassName == null || realClassName.equals(className))
            return String.format("at %s.%s(%s:%d)", className, methodName, fileName, lineNumber);
        else return String.format("at %s.%s(%s:%d) in %s", className, methodName, fileName,
                lineNumber, realClassName);
    }
}
