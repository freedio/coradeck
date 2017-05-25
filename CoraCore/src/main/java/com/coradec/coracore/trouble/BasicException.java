package com.coradec.coracore.trouble;

import com.coradec.coracore.annotation.ToString;
import com.coradec.coracore.util.StringUtil;

import java.lang.reflect.InvocationTargetException;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * ​​Basic implementation of a core exception.
 */
public class BasicException extends RuntimeException {

    private static final Set<String> IRRELEVANT = new HashSet<>(
            Arrays.asList("getClass", "getMessage", "getLocalizedMessage", "getSuppressed",
                    "getStackTrace"));

    /**
     * Initializes a new instance of BasicException.
     */
    protected BasicException() {
    }

    /**
     * Initializes a new instance of BasicException with the specified underlying problem.
     *
     * @param problem the underlying problem.
     */
    public BasicException(final Throwable problem) {
        super(problem);
    }

    /**
     * Initializes a new instance of BasicException with the specified explanation.
     *
     * @param explanation the explanation text.
     */
    protected BasicException(final String explanation) {
        super(explanation);
    }

    /**
     * Initializes a new instance of BasicException with the specified underlying problem and
     * explanation.
     *
     * @param explanation the explanation.
     * @param problem     the underlying problem.
     */
    protected BasicException(final String explanation, final Throwable problem) {
        super(explanation, problem);
    }

    @Override public String getMessage() {
        final String message = Optional.ofNullable(super.getMessage()).orElse("");
//        System.err.printf("Message: \"%s\"%n", message);
        final String props = //
                Stream.of(getClass().getMethods())
//                      .peek(method -> System.err.printf("Input Argument: \"%s\"%n",
//                              method.getName()))
                      .filter(method -> !IRRELEVANT.contains(method.getName()))
                      .filter(method -> (method.getName().startsWith("get") ||
                                         method.getName().startsWith("is")) &&
                                        method.getParameterCount() == 0)
                      .filter(method -> method.isAnnotationPresent(ToString.class))
                      .map(method -> {
//                          System.err.printf("Valid Argument: \"%s\"%n", method.getName());
                          final Object value;
                          String result = null;
                          final String propertyName = extractPropertyName(method.getName());
                          try {
                              try {
                                  value = AccessController.doPrivileged(
                                          (PrivilegedExceptionAction<Object>)() -> {
                                              method.setAccessible(true);
                                              return method.invoke(BasicException.this);
                                          });
                              }
                              catch (PrivilegedActionException e) {
                                  throw e.getException();
                              }
                              if (value != null) result = String.format("%s: %s", propertyName,
                                      StringUtil.toString(value));
                          }
                          catch (IllegalAccessException e) {
                              result = String.format("%s: %s", propertyName, "<inaccessible>");
                          }
                          catch (InvocationTargetException e) {
                              result = String.format("%s: %s", propertyName, "<faulty>");
                          }
                          catch (Exception e) {
                              result = String.format("%s: %s", propertyName, "<faulty>");
                          }
                          return result;
                      })
                      .filter(Objects::nonNull)
                      .collect(Collectors.joining(", "));
        StringBuilder result = new StringBuilder(message.length() + props.length() + 10);
        if (!props.isEmpty()) result.append('(').append(props).append(')');
        if (!props.isEmpty() && !message.isEmpty()) result.append(": ");
        result.append(message);
        return result.toString();
    }

    private String extractPropertyName(final String name) {
        return name.startsWith("get") ? name.substring(3)
                                      : name.startsWith("is") ? name.substring(2) : name;
    }
}
