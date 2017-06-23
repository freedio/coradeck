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

import static java.util.stream.Collectors.*;

import com.coradec.coracore.annotation.Nullable;
import com.coradec.coracore.model.Representable;
import com.coradec.coracore.model.State;

import java.lang.reflect.AnnotatedType;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.jar.Manifest;
import java.util.stream.Stream;

/**
 * ​​Static library of String utilities.
 */
@SuppressWarnings({"UseOfObsoleteDateTimeApi", "WeakerAccess"})
public final class StringUtil {

    public static final String EMPTY = "";
    public static final String NEWLINE = System.getProperty("line.separator");
    public static final String NULL_REPR = "NIL";
    public static final Object INACCESSIBLE = new Object() {

        @Override public String toString() {
            return "<inaccessible>";
        }
    };
    public static final Object FAILS = new Object() {

        @Override public String toString() {
            return "<fails>";
        }
    };
    public static final Object BLANK = new Object() {

        @Override public String toString() {
            return "";
        }
    };

    private StringUtil() {
    }

    public static String represent(final @Nullable Object o) {
        if (o == null) return NULL_REPR;
        if (o instanceof Representable) return ((Representable)o).represent();
        if (o instanceof byte[]) return ppByteArray((byte[])o);
        if (o.getClass().isArray()) return arrayRepr(o);
        if (o instanceof CharSequence) return "\"" + o + '"';
        if (o instanceof Character) return "'" + escape((char)o) + "'";
        if (o instanceof Optional<?>) {
            //noinspection unchecked
            return (String)((Optional)o).map(StringUtil::represent).orElse(NULL_REPR);
        }
        if (o instanceof List) //
            return ((List<?>)o).stream()
                               .map(StringUtil::represent)
                               .collect(joining(", ", "[", "]"));
        if (o instanceof Collection) //
            return ((Collection<?>)o).stream()
                                     .map(StringUtil::represent)
                                     .collect(joining(", ", "(", ")"));
        if (o instanceof Map) //
            return ((Map<?, ?>)o).entrySet()
                                 .stream()
                                 .map(entry -> represent(entry.getKey()) +
                                               ": " +
                                               represent(entry.getValue()))
                                 .collect(joining(", ", "{", "}"));
        if (o instanceof Date) return String.format(Locale.UK, "%tFT%<tT.%<tN", (Date)o);
        return o.toString();
    }

    public static String toString(final @Nullable Object o) {
        if (o == null) return NULL_REPR;
        if (o instanceof byte[]) return ppByteArray((byte[])o);
        if (o.getClass().isArray()) return array(o);
        if (o instanceof CharSequence) return "\"" + o + '"';
        if (o instanceof Character) return "'" + escape((char)o) + "'";
        if (o instanceof Class) return ClassUtil.nameOf((Class<?>)o);
        if (o instanceof Optional<?>) {
            //noinspection unchecked
            return (String)((Optional)o).map(StringUtil::toString).orElse(NULL_REPR);
        }
        if (o instanceof List) //
            return ((List<?>)o).stream().map(StringUtil::toString).collect(joining(", ", "[", "]"));
        if (o instanceof Collection) //
            return ((Collection<?>)o).stream()
                                     .map(StringUtil::toString)
                                     .collect(joining(", ", "(", ")"));
        if (o instanceof Map) //
            return ((Map<?, ?>)o).entrySet()
                                 .stream()
                                 .map(entry -> toString(entry.getKey()) +
                                               ": " +
                                               toString(entry.getValue()))
                                 .collect(joining(", ", "{", "}"));
        if (o instanceof Date) return String.format(Locale.UK, "%tFT%<tT.%<tN", (Date)o);
        if (o instanceof Manifest) {
            return ((Manifest)o).getMainAttributes()
                                .entrySet()
                                .stream()
                                .map(entry -> toString(entry.getKey()) +
                                              ": " +
                                              toString(entry.getValue()))
                                .collect(joining(", ", "Mainfest{", "}"));
        }
        if (o instanceof State) {
            return String.format("%s(%d)", ((State)o).name(), ((State)o).ordinal());
        }
        if (o instanceof TypeVariable) {
            TypeVariable<?> typeVar = (TypeVariable<?>)o;
            final String name = typeVar.getName();
            final GenericDeclaration genericDeclaration = typeVar.getGenericDeclaration();
            final Type[] bounds = typeVar.getBounds();
            final AnnotatedType[] annotatedBounds = typeVar.getAnnotatedBounds();
            return String.format("name=%s, genericDeclaration=%s, bounds=%s, annotatedBounds=%s",
                    toString(name), ClassUtil.toString(genericDeclaration, genericDeclaration),
                    ClassUtil.toString(bounds, bounds),
                    ClassUtil.toString(annotatedBounds, annotatedBounds));
        }
        if (o instanceof ParameterizedType) {
            ParameterizedType type = (ParameterizedType)o;
            final Type ownerType = type.getOwnerType();
            final Type rawType = type.getRawType();
            final Type[] actualTypeArguments = type.getActualTypeArguments();
            return String.format("owner=%s, rawType=%s, actualTypeArgs=%s",
                    StringUtil.toString(ownerType), StringUtil.toString(rawType),
                    StringUtil.toString(actualTypeArguments));
        }
        return o.toString();
    }

    private static String ppByteArray(final byte[] o) {
        final StringBuilder buffer = new StringBuilder(3 * o.length + 8).append('[');
        String prefix = "";
        for (byte b : o) {
            buffer.append(prefix).append(String.format("%02x", b));
            prefix = ", ";
        }
        return buffer.append(']').toString();
    }

    private static String escape(final char o) {
        final int i = "\f\r\n\0\b\7".indexOf(o);
        if (i != -1) return "\\" + "frn0ba".charAt(i);
        if (o < ' ') return "\\" + Integer.toOctalString(o);
        if (o > 126) return "\\u" + String.format("%04x", (int)o);
        return String.valueOf(o);
    }

    private static String ppArray(final Object o, Function<Object, String> objArray) {
        if (o instanceof boolean[]) return Arrays.toString((boolean[])o);
        else if (o instanceof char[]) return charray((char[])o);
        else if (o instanceof byte[]) return toString(o);
        else if (o instanceof short[]) return Arrays.toString((short[])o);
        else if (o instanceof int[]) return Arrays.toString((int[])o);
        else if (o instanceof long[]) return Arrays.toString((long[])o);
        else if (o instanceof float[]) return Arrays.toString((float[])o);
        else if (o instanceof double[]) return Arrays.toString((double[])o);
        return Stream.of((Object[])o).map(objArray).collect(joining(", ", "[", "]"));
    }

    private static String array(final Object o) {
        return ppArray(o, StringUtil::toString);
    }

    private static String arrayRepr(final Object o) {
        return ppArray(o, StringUtil::represent);
    }

    private static String charray(final char[] cs) {
        StringBuilder collector = new StringBuilder().append("[");
        String separator = "";
        for (char c : cs) {
            collector.append(separator).append(StringUtil.toString(c));
            separator = ", ";
        }
        return collector.append(']').toString();
    }
}
