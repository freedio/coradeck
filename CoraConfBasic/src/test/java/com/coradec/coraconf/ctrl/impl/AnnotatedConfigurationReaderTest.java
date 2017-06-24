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

package com.coradec.coraconf.ctrl.impl;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;

import com.coradec.coraconf.model.AnnotatedProperty;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(com.coradec.corajet.test.CoradeckJUnit4TestRunner.class)
public class AnnotatedConfigurationReaderTest {

    private final AnnotatedConfigurationReader testee1;
    private final AnnotatedConfigurationReader testee2;

    {
        testee1 = new AnnotatedConfigurationReader( //
                "context", Thread.currentThread()
                                 .getContextClassLoader()
                                 .getResource(
                                         "com/coradec/coraconf/ctrl/impl/AnnotatedConfigurationReaderTest" +
                                         ".dataset1")) {

        };
        testee2 = new AnnotatedConfigurationReader( //
                "context", Thread.currentThread()
                                 .getContextClassLoader()
                                 .getResource("com/coradec/coraconf/ctrl/impl" +
                                              "/AnnotatedConfigurationReaderTest" +
                                              ".dataset2")) {

        };
    }

    @Test public void testSEPARATORS() throws Exception {
        assertThat(testee1.SEPARATORS(), is(equalTo(":=")));
    }

    @Test public void testCOMMENTS() throws Exception {
        assertThat(testee1.COMMENTS(), is(equalTo("#!")));
    }

    @Test public void testESCAPE_CHAR() throws Exception {
        assertThat(testee1.ESCAPE_CHAR(), is(equalTo('\\')));
    }

    @Test public void testOPENINGQUOTES() throws Exception {
        assertThat(testee1.OPENINGQUOTES(), is(equalTo("'\"“‘„‚")));
    }

    @Test public void testCLOSINGQUOTES() throws Exception {
        assertThat(testee1.CLOSINGQUOTES(), is(equalTo("'\"”’“‘")));
    }

    @Test public void testUNESCAPED() throws Exception {
        assertThat(testee1.UNESCAPED(), is(equalTo("abfnrt\\'\"0")));
    }

    @Test public void testESCAPED() throws Exception {
        assertThat(testee1.ESCAPED(), is(equalTo("\7\b\f\n\r\t\\'\"\0")));
    }

    @Test public void testGetNonEmptyFileComment() throws Exception {
        testee1.open();
        try {
            final AnnotatedProperty v1 = testee1.getNextProperty().orElse(null);
            assertThat(v1, is(not(nullValue())));
            assertThat(v1.getAnnotation(), is(nullValue()));
            assertThat(v1.getName(), is(equalTo("property1")));
            assertThat(v1.getRawValue(), is(equalTo("value1")));
        } finally {
            testee1.close();
        }
        assertThat(testee1.getFileComment().orElse(null), is(equalTo(
                "Test dataset for first testee; the other dataset has no file comment on purpose" +
                "." +
                System.getProperty("line.separator"))));
    }

    @Test public void testGetEmptyFileComment() throws Exception {
        testee2.open();
        try {
            final AnnotatedProperty v1 = testee2.getNextProperty().orElse(null);
            assertThat(v1, is(not(nullValue())));
            assertThat(v1.getAnnotation(), is(nullValue()));
            assertThat(v1.getName(), is(equalTo("property1")));
            assertThat(v1.getRawValue(), is(equalTo("value1")));
        } finally {
            testee2.close();
        }
        assertThat(testee2.getFileComment().orElse(null), is(nullValue()));
    }

    @Test public void testGetNextProperty() throws Exception {
        testee1.open();
        try {
            final AnnotatedProperty v1 = testee1.getNextProperty().orElse(null);
            assertThat(v1, is(not(nullValue())));
            assertThat(v1.getAnnotation(), is(nullValue()));
            assertThat(v1.getName(), is(equalTo("property1")));
            assertThat(v1.getRawValue(), is(equalTo("value1")));
            final AnnotatedProperty v2 = testee1.getNextProperty().orElse(null);
            assertThat(v2, is(not(nullValue())));
            assertThat(v2.getAnnotation(), is(nullValue()));
            assertThat(v2.getName(), is(equalTo("property2")));
            assertThat(v2.getRawValue(), is(equalTo("\u003b\u00ca\21\u1234")));
            // TODO final octescape does not seem to work (try "abc\21")
        } finally {
            testee1.close();
        }
    }

}
