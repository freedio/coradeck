package com.coradec.coracore.util;

import com.coradec.coracore.annotation.Component;
import com.coradec.coracore.ctrl.Factory;
import com.coradec.coracore.model.StackFrame;
import com.coradec.coracore.annotation.Inject;

import java.net.URI;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * ​​Static library of execution utilities.
 */
@Component
public final class ExecUtil {

    private static final Pattern INVOC_PATTERN = Pattern.compile("access\\$\\d+");
    private static final Set<String> SYNTHETIC_CLASSES =
            new HashSet<>(Arrays.asList("sun.reflect.DelegatingMethodAccessorImpl",
                    "sun.reflect.NativeMethodAccessorImpl"));
    @Inject private static Factory<StackFrame> stackFrame = new Factory<StackFrame>() {

        @Override public StackFrame get(final Object... args) {
            return new StackFrame() {

                @Override public String getClassName() {
                    return ((StackTraceElement)args[0]).getClassName();
                }

                @Override public String getClassFileName() {
                    return ((StackTraceElement)args[0]).getClassName().replace('.', '/');
                }

                @Override public URI toURI() {
                    return null;
                }

                @Override public String represent() {
                    return null;
                }
            };
        }

        @Override public StackFrame create(final Object... args) {
            return null;
        }
    };

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
        return stackFrame.get(extract(level + 2, Thread.currentThread().getStackTrace()),
                realClassName);
    }

    /**
     * Returns the stack frame of the <i>level</i>th stack frame.
     *
     * @param level         the level.
     * @return a stack frame.
     */
    public static StackFrame getStackFrame(final int level) {
        return stackFrame.get(extract(level + 2, Thread.currentThread().getStackTrace()));
    }

}
