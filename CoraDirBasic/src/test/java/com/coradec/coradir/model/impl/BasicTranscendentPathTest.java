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

package com.coradec.coradir.model.impl;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;

import com.coradec.coradir.model.Path;
import com.coradec.coradir.trouble.PathEmptyException;
import com.coradec.corajet.test.CoradeckJUnit4TestRunner;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.Collections;

@RunWith(CoradeckJUnit4TestRunner.class)
public class BasicTranscendentPathTest {

    private static final String SEPARATOR = Path.separator();
    BasicTranscendentPath testee;

    @Test public void testEmptyPath() {
        testee = new BasicTranscendentPath("host");
        assertThat(testee.isAbsolute(), is(true));
        assertThat(testee.isEmpty(), is(true));
        assertThat(testee.isName(), is(false));
        assertThat(testee.isTranscendent(), is(true));
        assertThat(testee.isLocalAbsolute(), is(false));
        assertThat(testee.represent(), is("//host/"));
        try {
            testee.head();
            Assert.fail("Expected EmptyPathException");
        } catch (PathEmptyException e) {
            // expected that
        }
        assertThat(testee.tail(), is(equalTo(new EmptyPath())));
        assertThat(testee.localize(), is(equalTo(new EmptyPath())));
        assertThat(testee.toURI("test").toString(), is(equalTo("test://host/")));
        assertThat(testee.getPath(), is(equalTo(Collections.emptyList())));
        assertThat(testee.add("another").represent(), is(equalTo("//host/another")));
        assertThat(testee.transcend(), is(testee));
    }

    @Test public void testSimplePath() {
        testee = new BasicTranscendentPath("host", "name");
        assertThat(testee.isAbsolute(), is(true));
        assertThat(testee.isEmpty(), is(false));
        assertThat(testee.isName(), is(false));
        assertThat(testee.isTranscendent(), is(true));
        assertThat(testee.isLocalAbsolute(), is(false));
        assertThat(testee.represent(), is("//host" + SEPARATOR + "name"));
        assertThat(testee.head(), is("name"));
        assertThat(testee.tail(), is(equalTo(new BasicPath("name"))));
        assertThat(testee.localize(), is(equalTo(new BasicPath("name"))));
        assertThat(testee.toURI("test").toString(), is(equalTo("test://host/name")));
        assertThat(testee.getPath(), is(equalTo(Collections.singletonList("name"))));
        assertThat(testee.add("another").represent(),
                is(equalTo("//host" + SEPARATOR + "name" + SEPARATOR + "another")));
        assertThat(testee.transcend(), is(testee));
    }

    @Test public void testCompoundPath() {
        testee = new BasicTranscendentPath("host", "hello", "world");
        assertThat(testee.isAbsolute(), is(true));
        assertThat(testee.isEmpty(), is(false));
        assertThat(testee.isName(), is(false));
        assertThat(testee.isTranscendent(), is(true));
        assertThat(testee.isLocalAbsolute(), is(false));
        assertThat(testee.represent(), is("//host" + SEPARATOR + "hello" + SEPARATOR + "world"));
        assertThat(testee.head(), is("hello"));
        assertThat(testee.tail(), is(equalTo(new BasicPath("hello", "world"))));
        assertThat(testee.localize(), is(equalTo(new BasicPath("hello", "world"))));
        assertThat(testee.toURI("test").toString(),
                is(equalTo("test://host" + SEPARATOR + "hello" + SEPARATOR + "world")));
        assertThat(testee.getPath(), is(equalTo(Arrays.asList("hello", "world"))));
        assertThat(testee.add("another").represent(), is(equalTo(
                "//host" + SEPARATOR + "hello" + SEPARATOR + "world" + SEPARATOR + "another")));
        assertThat(testee.transcend(), is(testee));
    }

}
