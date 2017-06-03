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

import com.coradec.coracore.annotation.Nullable;
import com.coradec.coracore.annotation.ToString;
import com.coradec.coracore.ctrl.RecursiveObjects;
import com.coradec.coracore.model.Tuple;
import com.coradec.coracore.trouble.UnexpectedEndOfDataException;

import java.lang.reflect.Method;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * ​​Static library of object and class utilities.
 */
@SuppressWarnings({"UseOfObsoleteDateTimeApi", "UseOfSystemOutOrSystemErr"})
public class ClassUtil {

    private static final RecursiveObjects registry = RecursiveObjects.getInstance();
    @SuppressWarnings("ArraysAsListWithZeroOrOneArgument")
    private static final Set<String> IGNORED_PROPERTIES = new HashSet<>(Arrays.asList("Class"));
    private static final Class[] VALUE_CLASSES = {
            String.class, Number.class, Date.class, Throwable.class, Boolean.class, Character.class,
            CharSequence.class, Collection.class, Map.class, URL.class,
            };

    public static String toString(final Object o, final @Nullable Object v) {
        if (o == null) return StringUtil.toString(null);
        final Class<?> klass = o.getClass();
        if (klass.isArray()) return arrayOf(klass.getComponentType(), o);
        if (Stream.of(VALUE_CLASSES).anyMatch(c -> c.isInstance(o))) return valueOf(klass, o);
        String result;
        if (registry.contains(o)) return String.format("<@%08x>", System.identityHashCode(o));
        registry.add(o);
        try {
            final String attributes = //
                    Stream.of(klass.getMethods())
                          .filter(method -> method.getName().matches("^(is|get)" + "[A-Z0-9].+") &&
                                            method.isAnnotationPresent(ToString.class))
                          .map(method -> new Tuple(propertyName(method.getName()), method))
                          .map(p -> {
                              final String name = p.get(0);
                              Object value;
                              try {
                                  try {
                                      value = AccessController.doPrivileged(
                                              (PrivilegedExceptionAction<Object>)() -> {
                                                  final Method method = p.get(1);
                                                  method.setAccessible(true);
                                                  return method.invoke(o);
                                              });
                                  }
                                  catch (PrivilegedActionException e) {
                                      throw e.getException();
                                  }
                              }
                              catch (IllegalArgumentException e) {
                                  value = StringUtil.INACCESSIBLE;
                                  System.err.printf("%n>>> Strange getter: %s%n%n", p.get(1));
                              }
                              catch (IllegalAccessException e) {
                                  value = StringUtil.INACCESSIBLE;
                              }
                              catch (Exception e) {
                                  value = StringUtil.FAILS;
                              }
                              return String.format("%s", classNameObject(name, value));
                          })
                          .distinct()
                          .collect(Collectors.joining(" "));
            if (v != null) result =
                    String.format("(%s %s%s)", nameOf(klass), StringUtil.toString(v),
                            attributes.isEmpty() ? StringUtil.EMPTY : " " + attributes);
            else result = String.format("(%s%s)", nameOf(klass),
                    attributes.isEmpty() ? StringUtil.EMPTY : " " + attributes);
        }
        finally

        {
            registry.remove(o);
        }
        return result;
    }

    /**
     * Returns the standard String representation of the specified object.
     *
     * @param o the object.
     * @return the object's string representation.
     */
    public static String toString(final Object o) {
        return toString(o, null);
    }

    private static String arrayOf(final Class<?> componentType, final Object o) {
        return String.format("(%s%s)", nameOf(componentType), StringUtil.toString(o));
    }

    private static String valueOf(final Class<?> type, final Object value) {
        return String.format("(%s %s)", nameOf(type), StringUtil.toString(value));
    }

    public static String nameOf(final Class<?> type) {
        if (type.isMemberClass())
            return String.format("%s.%s", nameOf(type.getEnclosingClass()), type.getSimpleName());
        if (type.isArray()) return String.format("%s[]", nameOf(type.getComponentType()));
        String result = type.getName();
        if (result.matches("^java\\.(lang|util)\\.[^a-z].+")) result = result.substring(10);
        return result;
    }

    private static String classNameObject(final String name, final @Nullable Object object) {
        if (object instanceof Optional) {
            //noinspection unchecked
            return classNameObject(name, ((Optional)object).orElse(null));
        }
        if (object == null) return StringUtil.EMPTY;
        final String className = nameOf(object.getClass());
        String value = StringUtil.toString(object);
        if (!value.startsWith(className + "@"))
            if (Stream.of(VALUE_CLASSES).anyMatch(c -> c.isInstance(object)))
                value = String.format("%s %s", className, value);
            else value =
                    String.format("%s@%08x %s", className, System.identityHashCode(object), value);
        return String.format("(%s: %s)", name, value);
    }

    private static String propertyName(final String methodName) {
        return methodName.replaceFirst("^is", "").replaceFirst("^get", "");
    }

    /**
     * Returns the resource path for the specified class with the specified extension.
     *
     * @param klass     the class.
     * @param extension the extension of the resource file.
     * @return a resource file name (path).
     */
    public static String toResourcePath(final Class klass, String extension) {
        return klass.getName().replace('.', '/') + '.' + extension;
    }

    /**
     * Converts the specified internal class name to an external class name.
     *
     * @param internal the internal class name.
     * @return the external class name.
     */
    public static String toExternal(final String internal) {
        return fromSignature(internal)[0];
    }

    /**
     * Returns the descriptor of the specified class.
     *
     * @param klass the class.
     * @return the descriptor.
     */
    public static String descriptorOf(Class<?> klass) {
        StringBuilder collector = new StringBuilder();
        while (klass.isArray()) {
            collector.append('[');
            klass = klass.getComponentType();
        }
        if (klass == Boolean.TYPE) collector.append('Z');
        else if (klass == Byte.TYPE) collector.append('B');
        else if (klass == Character.TYPE) collector.append('C');
        else if (klass == Short.TYPE) collector.append('S');
        else if (klass == Integer.TYPE) collector.append('I');
        else if (klass == Long.TYPE) collector.append('J');
        else if (klass == Float.TYPE) collector.append('F');
        else if (klass == Double.TYPE) collector.append('D');
        else collector.append('L').append(klass.getName().replace('.', '/')).append(';');
        return collector.toString();
    }

    /**
     * Decodes the specified signature into a return value ([0]) and a parameter list ([1]).
     *
     * @param signature the signature.
     * @return two strings representing the return value and parameter list.
     */
    public static String[] fromSignature(final String signature) {
        final StringBuilder parameters = new StringBuilder();
        final StringBuilder retval = new StringBuilder();
        final StringBuilder suffices = new StringBuilder();
        StringBuilder collector = retval;
        for (int i = 0, is = signature.length(); i < is; ) {
            char c = signature.charAt(i++);
            switch (c) {
                case '(':
                    collector = parameters;
                    continue;
                case ')':
                    collector = retval;
                    continue;
                case 'B':
                    collector.append(" byte");
                    break;
                case 'C':
                    collector.append(" char");
                    break;
                case 'D':
                    collector.append(" double");
                    break;
                case 'F':
                    collector.append(" float");
                    break;
                case 'I':
                    collector.append(" int");
                    break;
                case 'J':
                    collector.append(" long");
                    break;
                case 'L':
                    collector.append(' ');
                    while ((c = signature.charAt(i++)) != ';') {
                        if (c == '<') {
                            i = decodeTypeParameters(signature, collector, i, is);
                        } else {
                            if (c == '/') c = '.';
                            collector.append(c);
                        }
                    }
                    break;
                case 'S':
                    collector.append(" short");
                    break;
                case 'V':
                    collector.append(" void");
                    break;
                case 'Z':
                    collector.append(" boolean");
                    break;
                case '[':
                    suffices.append("[]");
                    continue;
                default:
                    collector.append(' ').append(c).append("???");
            }
            collector.append(suffices);
            suffices.setLength(0);
            if (i < is && signature.charAt(i) != ')') collector.append(',');
        }
        return new String[] {
                retval.length() == 0 ? null : retval.substring(1),
                parameters.length() == 0 ? null : parameters.substring(1)
        };
    }

    private static int decodeTypeParameters(final String signature, final StringBuilder collector,
                                            int offset, final int length) {
        collector.append('<');
        StringBuilder suffices = new StringBuilder();
        while (offset < length) {
            char c = signature.charAt(offset++);
            switch (c) {
                case '>':
                    if (collector.charAt(collector.length() - 1) == ',')
                        collector.setLength(collector.length() - 1);
                    collector.append(c).append(suffices);
                    return offset;
                case 'B':
                    collector.append("byte");
                    break;
                case 'C':
                    collector.append("char");
                    break;
                case 'D':
                    collector.append("double");
                    break;
                case 'F':
                    collector.append("float");
                    break;
                case 'I':
                    collector.append("int");
                    break;
                case 'J':
                    collector.append("long");
                    break;
                case 'L':
                    while ((c = signature.charAt(offset++)) != ';') {
                        if (c == '<') {
                            offset = decodeTypeParameters(signature, collector, offset, length);
                        } else {
                            if (c == '/') c = '.';
                            collector.append(c);
                        }
                    }
                    break;
                case 'S':
                    collector.append("short");
                    break;
                case 'T':
                    while ((c = signature.charAt(offset++)) != ';') {
                        collector.append(c);
                    }
                    break;
                case 'V':
                    collector.append("void");
                    break;
                case 'Z':
                    collector.append("boolean");
                    break;
                case '[':
                    suffices.append("[]");
                    continue;
                default:
                    collector.append(' ').append(c).append("???");
            }
            collector.append(',');
        }
        throw new UnexpectedEndOfDataException(signature);
    }

    /**
     * Returns the internal name of the specified class.
     *
     * @param type the class.
     * @return the internal name.
     */
    public static String internalNameOf(final Class<?> type) {
        return type.getName().replace('.', '/');
    }

    public static Class<?> classForDescriptor(final String desc) throws ClassNotFoundException {
        final String className = toExternal(desc);
        switch (className) {
            case "boolean":
                return Boolean.TYPE;
            case "byte":
                return Byte.TYPE;
            case "short":
                return Short.TYPE;
            case "int":
                return Integer.TYPE;
            case "long":
                return Long.TYPE;
            case "float":
                return Float.TYPE;
            case "double":
                return Double.TYPE;
            default:
                return Class.forName(removeTypeParameters(className));
        }
    }

    private static String removeTypeParameters(final String className) {
        return className.replaceFirst("<.+>", "");
    }

    public static String internalNameOf(final String signature) {
        final String result = removeTypeParameters(signature);
        return result.substring(1, result.length() - 1);
    }
}
