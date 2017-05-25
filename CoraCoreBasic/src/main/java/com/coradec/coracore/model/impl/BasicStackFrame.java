package com.coradec.coracore.model.impl;

import com.coradec.coracore.annotation.Implementation;
import com.coradec.coracore.annotation.Nullable;
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

    public BasicStackFrame(final StackTraceElement frame, final String realClassName) {
        this(frame);
        this.realClassName = realClassName;
    }

    @Override public String getClassName() {
        return className;
    }

    @Override public String getClassFileName() {
        return getClassName().replace('.', '/');
    }

    private String getMethodName() {
        return this.methodName;
    }

    private String getFileName() {
        return this.fileName;
    }

    private int getLineNumber() {
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
