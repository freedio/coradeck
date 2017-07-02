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

import static com.coradec.coracore.tools.hamcrest.RegexMatcher.*;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;

import com.coradec.coracore.model.State;
import com.coradec.coracore.trouble.ResourceFileNotFoundException;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.jar.Manifest;

public class StringUtilTest {

    @SuppressWarnings("PublicField")
    private static class TestState implements State {

        static TestState TESTSTATE = new TestState("TEST", 0);

        private final String name;
        private final int rank;

        TestState(final String name, final int rank) {
            this.name = name;
            this.rank = rank;
        }

        @Override public String name() {
            return name;
        }

        @Override public int ordinal() {
            return rank;
        }

        @Override public String toString() {
            return ClassUtil.toString(this);
        }
    }

    private static Map<?, ?> createMap(final Object... data) {
        final Map<Object, Object> result = new HashMap<>();
        for (int i = 0, is = data.length; i < is; i += 2) {
            result.put(data[i], data[i + 1]);
        }
        return result;
    }

    private static final Object[][] testData = {
            {"Athabasca", "\"Athabasca\""}, //
            {123, "123"}, {12.5f, "12.5"}, //
            {123.456, "123.456"}, //
            {Optional.empty(), StringUtil.NULL_REPR}, //
            {Optional.empty(), StringUtil.NULL_REPR}, //
            {Optional.of("abc"), "\"abc\""}, {'ä', "'ä'"}, //
            {
                    Arrays.asList("a", "b", "c"), "[\"a\", \"b\", \"c\"]"
            }, {
                    new HashSet<>(Arrays.asList("a", "b", "c")), "(\"a\", \"b\", \"c\")"
            }, {
                    createMap("a", 1, "b", 12, "C", 123), "{\"a\": 1, \"b\": 12, \"C\": 123}"
            }, {
                    new Boolean[] {true, false, true, false}, "[true, false, true, false]"
            }, {
                    new boolean[] {true, false, true, false}, "[true, false, true, false]"
            }, {
                    new Character[] {'a', 'b', 'c'}, "['a', 'b', 'c']"
            }, {
                    new char[] {'a', 'b', 'c'}, "['a', 'b', 'c']"
            }, {
                    new Byte[] {1, 10, 100}, "[1, 10, 100]"
            }, {
                    new byte[] {1, 10, 100}, "[01, 0a, 64]"
            }, {
                    TestState.TESTSTATE, "TEST(0)", "Test"
            }
    };

    @Test public void testConstants() {
        assertThat(String.valueOf(StringUtil.EMPTY), is(equalTo("")));
        assertThat(String.valueOf(StringUtil.NULL_REPR), is(equalTo("NIL")));
        assertThat(String.valueOf(StringUtil.NEWLINE),
                is(equalTo(System.getProperty("line.separator"))));
        assertThat(String.valueOf(StringUtil.INACCESSIBLE), is(equalTo("<inaccessible>")));
        assertThat(String.valueOf(StringUtil.FAILS), is(equalTo("<fails>")));
        assertThat(String.valueOf(StringUtil.BLANK), is(equalTo("")));
    }

    @Test public void testRepresent() throws Exception {
        for (final Object[] testDatum : testData) {
            final Object repr = testDatum.length > 2 ? testDatum[2] : testDatum[1];
            assertThat(StringUtil.represent(testDatum[0]), is(equalTo(repr)));
        }
    }

    @Test public void testToString() throws Exception {
        for (final Object[] testDatum : testData) {
            assertThat(StringUtil.toString(testDatum[0]), is(equalTo(testDatum[1])));
        }
        final InputStream in = getClass().getClassLoader()
                                         .getResourceAsStream(
                                                 "com/coradec/coracore/util/MANIFEST.MF.data");
        if (in == null)
            throw new ResourceFileNotFoundException("com/coradec/coracore/util/MANIFEST.MF.data");
        Manifest manifest = new Manifest(in);
        assertThat(StringUtil.toString(manifest), is(equalTo(
                "Mainfest{Archiver-Version: \"Coradec Carchiver\", Implementation-Classes: \"com" +
                ".coradec.coracom.model.impl.BasicEvent com.coradec.coracom.model.impl" +
                ".BasicMessage com.coradec.coracom.model.impl.BasicRequest\", Manifest-Version: " +
                "\"1.0\", Dependencies: \"com.coradec.coradeck:coracore:jar:0.3 com.coradec" +
                ".coradeck:coralog:jar:0.3 com.coradec.coradeck:corasession:jar:0.3\"}")));
        assertThat(StringUtil.toString(List.class.getTypeParameters()[0]),
                matches("name=\"E\", genericDeclaration=\\(Class List\\), bounds=\\(java\\.lang\\" +
                        ".reflect\\.Type\\[Object\\]\\), annotatedBounds=\\(java\\.lang\\" +
                        ".reflect\\.AnnotatedType\\[sun\\.reflect\\.annotation\\" +
                        ".AnnotatedTypeFactory\\$AnnotatedTypeBaseImpl@[0-9a-f]+\\]\\)"));
        assertThat(StringUtil.toString(ArrayList.class.getTypeParameters()[0]),
                matches("name=\"E\", genericDeclaration=\\(Class ArrayList\\), bounds=\\(java\\" +
                        ".lang\\.reflect\\.Type\\[Object\\]\\), annotatedBounds=\\(java\\.lang\\" +
                        ".reflect\\.AnnotatedType\\[sun\\.reflect\\.annotation\\" +
                        ".AnnotatedTypeFactory\\$AnnotatedTypeBaseImpl@[0-9a-f]+\\]\\)"));
        assertThat(StringUtil.toString(ArrayList.class.getGenericSuperclass()),
                matches("owner=NIL, rawType=AbstractList, actualTypeArgs=\\[name=\"E\", " +
                        "genericDeclaration=\\(Class ArrayList\\), bounds=\\(java.lang.reflect" +
                        ".Type\\[Object\\]\\), annotatedBounds=\\(java.lang.reflect" +
                        ".AnnotatedType\\[sun.reflect.annotation" +
                        ".AnnotatedTypeFactory\\$AnnotatedTypeBaseImpl@[0-9a-f]+\\]\\)\\]"));
    }

    @Test public void testToTitleCase() throws Exception {
        final URL resource = getClass().getClassLoader()
                                       .getResource("com/coradec/coracore/util/StringUtilTest" +
                                                    ".toTitleCase.data");
        if (resource == null) throw new ResourceFileNotFoundException(
                "com/coradec/coracore/util/StringUtilTest.toTitleCase.data");
        BufferedReader in = new BufferedReader(
                new InputStreamReader(resource.openStream(), Charset.forName("UTF-8")));
        for (String line = in.readLine(); line != null; line = in.readLine()) {
            final String[] split = line.split("\\|");
            assertThat(StringUtil.toTitleCase(split[0]), is(equalTo(split[1])));
        }
    }

}
