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
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collection;
import java.util.ConcurrentModificationException;
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
@SuppressWarnings("UseOfObsoleteDateTimeApi")
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
    public static final String[] UNCAPITALIZED = {
            "a", "an", "the", "on", "in", "of", "for", "from", "with", "as", "at", "to", "like",
            "upon", "down", "near", "off", "onto", "unto", "yet", "nor", "so", "if", "once", "than",
            "that", "till", "when", "into", "out", "by", "and", "or", "but", "up", "over",
            };
    public static final Charset UTF8 = Charset.forName("UTF-8");

    private StringUtil() {
    }

    public static String represent(final @Nullable Object o) {
        if (o == null) return NULL_REPR;
        if (o instanceof Representable) return ((Representable)o).represent();
        if (o instanceof byte[]) return ppByteArray((byte[])o);
        if (o.getClass().isArray())
            return ppArray(o, StringUtil::represent, StringUtil::charArrayRepr);
        if (o instanceof CharSequence) return (String)o;
        if (o instanceof Character) return Character.toString((Character)o);
        if (o instanceof Optional<?>) {
            //noinspection unchecked
            return (String)((Optional)o).map(StringUtil::represent).orElse(NULL_REPR);
        }
        if (o instanceof List) //
            do {
                try {
                    return ((List<?>)o).stream()
                                       .map(StringUtil::represent)
                                       .collect(joining(", ", "[", "]"));
                } catch (ConcurrentModificationException e) {
                    // retry
                }
            } while (true);
        if (o instanceof Collection) //
            do {
                try {
                    return ((Collection<?>)o).stream()
                                             .map(StringUtil::represent)
                                             .collect(joining(", ", "(", ")"));
                } catch (ConcurrentModificationException e) {
                    // retry
                }
            } while (true);
        if (o instanceof Map) //
            do {
                try {
                    return ((Map<?, ?>)o).entrySet()
                                         .stream()
                                         .map(entry -> represent(entry.getKey()) +
                                                       ": " +
                                                       represent(entry.getValue()))
                                         .collect(joining(", ", "{", "}"));
                } catch (Exception e) {
                    // retry
                }
            } while (true);
        if (o instanceof Date) return String.format(Locale.UK, "%tFT%<tT.%<tN", (Date)o);
        return o.toString();
    }

    public static String toString(final @Nullable Object o) {
        if (o == null) return NULL_REPR;
        if (o instanceof byte[]) return ppByteArray((byte[])o);
        if (o.getClass().isArray()) return ppArray(o, StringUtil::toString, StringUtil::charArray);
        if (o instanceof CharSequence) return "\"" + o + '"';
        if (o instanceof Character) return "'" + escape((char)o) + "'";
        if (o instanceof Class) return ClassUtil.nameOf((Class<?>)o);
        if (o instanceof Optional<?>) {
            //noinspection unchecked
            return (String)((Optional)o).map(StringUtil::toString).orElse(NULL_REPR);
        }
        if (o instanceof List) //
            return toString((Collection<?>)o, '[',
                    ']');// ((List<?>)o).stream().map(StringUtil::toString).collect(joining(", ",
        // "[", "]"));
        if (o instanceof Collection) //
            return toString((Collection<?>)o, '(', ')');
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

    public static String escape(final char o) {
        final int i = "\f\7\r\n\0\b'\\\"".indexOf(o);
        if (i != -1) return "\\" + "farn0b'\"\\".charAt(i);
        if (o < ' ') return "\\" + Integer.toOctalString(o);
        if (o > 255) return "\\u" + String.format("%04x", (int)o);
        return String.valueOf(o);
    }

    /**
     * Returns a copy of the specified string with each special character (including the specified
     * additional special characters) escaped.
     *
     * @param s        the string.
     * @param toEscape additional characters to escape.
     * @return a fully escaped copy.
     */
    public static String escape(final String s, final String toEscape) {
        StringBuilder collector = new StringBuilder(s.length() * 2);
        for (int i = 0, is = s.length(); i < is; ++i) {
            char c = s.charAt(i);
            int x = toEscape.indexOf(c);
            collector.append(x != -1 ? "\\" + c : escape(c));
        }
        return collector.toString();
    }

    public static char unescape(final char c) {
        final int i = "farn0b'\"\\".indexOf(c);
        if (i != -1) return "\f\7\r\n\0\b\'\"\\".charAt(i);
        return c;
    }

    private static String ppArray(final Object o, Function<Object, String> objArray,
            Function<char[], String> charray) {
        if (o instanceof boolean[]) return Arrays.toString((boolean[])o);
        else if (o instanceof char[]) return charray.apply((char[])o);
        else if (o instanceof byte[]) return toString(o);
        else if (o instanceof short[]) return Arrays.toString((short[])o);
        else if (o instanceof int[]) return Arrays.toString((int[])o);
        else if (o instanceof long[]) return Arrays.toString((long[])o);
        else if (o instanceof float[]) return Arrays.toString((float[])o);
        else if (o instanceof double[]) return Arrays.toString((double[])o);
        return Stream.of((Object[])o).map(objArray).collect(joining(", ", "[", "]"));
    }

    private static String charArray(final char[] cs) {
        StringBuilder collector = new StringBuilder().append("[");
        String separator = "";
        for (char c : cs) {
            collector.append(separator).append(StringUtil.toString(c));
            separator = ", ";
        }
        return collector.append(']').toString();
    }

    private static String charArrayRepr(final char[] cs) {
        StringBuilder collector = new StringBuilder().append("[");
        String separator = "";
        for (char c : cs) {
            collector.append(separator).append(StringUtil.represent(c));
            separator = ", ";
        }
        return collector.append(']').toString();
    }

    /**
     * Formats the specified string according to the English title case rule.
     *
     * @param s the string to format.
     * @return the same string with cases adjusted.
     */
    public static String toTitleCase(final String s) {
        final StringBuilder collector = new StringBuilder();
        boolean inWord = false;
        int off;
        for (int i = 0, is = s.length(); i < is; ++i) {
            char c = s.charAt(i);
            if (inWord) {
                if (Character.isWhitespace(c)) inWord = false;
            } else if (!Character.isWhitespace(c)) {
                c = Character.toUpperCase(c);
                for (final String word : UNCAPITALIZED) {
                    if (i != 0 &&
                        s.startsWith(word, i) &&
                        (off = i + word.length()) < s.length() &&
                        !Character.isLetter(s.charAt(off)) &&
                        s.substring(off).matches(".*\\p{IsAlphabetic}.*")) {
                        c = Character.toLowerCase(c);
                        break;
                    }
                }
                inWord = true;
            }
            collector.append(c);
        }
        return collector.toString();
    }

    public static String toString(final Collection<?> o, final char start, final char end) {
        final Object[] array = o.toArray();
        return Stream.of(array)
                     .map(StringUtil::toString)
                     .collect(joining(", ", String.valueOf(start), String.valueOf(end)));
    }

    public static String toString(final Object[] o, final char start, final char end) {
        return Stream.of(o)
                     .map(StringUtil::toString)
                     .collect(joining(", ", String.valueOf(start), String.valueOf(end)));
    }

    public static String format(final byte[] bytes, final String delimiter) {
        final StringBuilder collector = new StringBuilder(256);
        for (final byte b : bytes) {
            collector.append(delimiter).append(String.format("%02x", b));
        }
        return collector.substring(delimiter.length());
    }

}
