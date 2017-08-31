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

import com.coradec.coracore.util.NetworkUtil;
import com.coradec.coradir.model.Path;
import com.coradec.coradir.trouble.CannotTranscendRelativePathException;
import com.coradec.corajet.test.CoradeckJUnit4TestRunner;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.Collections;

@RunWith(CoradeckJUnit4TestRunner.class)
public class BasicPathTest {

    private static final String HOSTNAME = NetworkUtil.getCanonicalHostName();

    private BasicPath testee;

    @Test public void testEmptyPath() {
        testee = new BasicPath();
        assertThat(testee.isAbsolute(), is(true));
        assertThat(testee.isEmpty(), is(false));
        assertThat(testee.isName(), is(true));
        assertThat(testee.isTranscendent(), is(false));
        assertThat(testee.isLocalAbsolute(), is(true));
        assertThat(testee.represent(), is(""));
        assertThat(testee.head(), is(""));
        assertThat(testee.tail().isEmpty(), is(true));
        assertThat(testee.localize(), is(instanceOf(EmptyPath.class)));
        assertThat(testee.toURI("test").toString(), is(equalTo("test:/")));
        assertThat(testee.getPath(), is(equalTo(Collections.singletonList(""))));
        assertThat(testee.add("another").represent(), is(equalTo("/another")));
        assertThat(testee.transcend().represent(), is(equalTo("//" + HOSTNAME + Path.separator())));
    }

    @Test public void testNamePath() {
        final String name = "name";
        testee = new BasicPath(name);
        assertThat(testee.isAbsolute(), is(false));
        assertThat(testee.isEmpty(), is(false));
        assertThat(testee.isName(), is(true));
        assertThat(testee.isTranscendent(), is(false));
        assertThat(testee.isLocalAbsolute(), is(false));
        assertThat(testee.represent(), is(name));
        assertThat(testee.head(), is(name));
        assertThat(testee.tail().isEmpty(), is(true));
        assertThat(testee.localize(), is(testee));
        assertThat(testee.toURI("test").toString(), is(equalTo("test:" + name)));
        assertThat(testee.getPath(), is(equalTo(Collections.singletonList(name))));
        assertThat(testee.add("another").represent(),
                is(equalTo(name + Path.separator() + "another")));
        try {
            testee.transcend();
            Assert.fail("Expected " + CannotTranscendRelativePathException.class.getName());
        } catch (CannotTranscendRelativePathException e) {
            // expected that
        }
    }

    @Test public void testAbsolutePath() {
        testee = new BasicPath("", "name");
        assertThat(testee.isAbsolute(), is(true));
        assertThat(testee.isEmpty(), is(false));
        assertThat(testee.isName(), is(false));
        assertThat(testee.isTranscendent(), is(false));
        assertThat(testee.isLocalAbsolute(), is(true));
        assertThat(testee.represent(), is(Path.separator() + "name"));
        assertThat(testee.head(), is(""));
        assertThat(testee.tail(), is(equalTo(new BasicPath("name"))));
        assertThat(testee.localize(), is(equalTo(new BasicPath("name"))));
        assertThat(testee.toURI("test").toString(), is(equalTo("test:/name")));
        assertThat(testee.getPath(), is(equalTo(Arrays.asList("", "name"))));
        assertThat(testee.add("another").represent(),
                is(equalTo(Path.separator() + "name" + Path.separator() + "another")));
        assertThat(testee.transcend().represent(),
                is(equalTo("//" + HOSTNAME + Path.separator() + "name")));
    }

    @Test public void testRelativeCompoundPath() {
        testee = new BasicPath("hello", "world");
        assertThat(testee.isAbsolute(), is(false));
        assertThat(testee.isEmpty(), is(false));
        assertThat(testee.isName(), is(false));
        assertThat(testee.isTranscendent(), is(false));
        assertThat(testee.isLocalAbsolute(), is(false));
        assertThat(testee.represent(), is("hello" + Path.separator() + "world"));
        assertThat(testee.head(), is("hello"));
        assertThat(testee.tail(), is(equalTo(new BasicPath("world"))));
        assertThat(testee.localize(), is(testee));
        assertThat(testee.toURI("test").toString(),
                is(equalTo("test:hello" + Path.separator() + "world")));
        assertThat(testee.getPath(), is(equalTo(Arrays.asList("hello", "world"))));
        assertThat(testee.add("another").represent(),
                is(equalTo("hello" + Path.separator() + "world" + Path.separator() + "another")));
        try {
            testee.transcend();
            Assert.fail("Expected " + CannotTranscendRelativePathException.class.getName());
        } catch (CannotTranscendRelativePathException e) {
            // expected that
        }
    }

    @Test public void testAbsoluteCompoundPath() {
        testee = new BasicPath("", "hello", "world");
        assertThat(testee.isAbsolute(), is(true));
        assertThat(testee.isEmpty(), is(false));
        assertThat(testee.isName(), is(false));
        assertThat(testee.isTranscendent(), is(false));
        assertThat(testee.isLocalAbsolute(), is(true));
        assertThat(testee.represent(), is(Path.separator() + "hello" + Path.separator() + "world"));
        assertThat(testee.head(), is(""));
        assertThat(testee.tail(), is(equalTo(new BasicPath("hello", "world"))));
        assertThat(testee.localize(), is(equalTo(new BasicPath("hello", "world"))));
        assertThat(testee.toURI("test").toString(),
                is(equalTo("test:" + Path.separator() + "hello" + Path.separator() + "world")));
        assertThat(testee.getPath(), is(equalTo(Arrays.asList("", "hello", "world"))));
        assertThat(testee.add("another").represent(), is(equalTo(Path.separator() +
                                                                 "hello" +
                                                                 Path.separator() +
                                                                 "world" +
                                                                 Path.separator() +
                                                                 "another")));
        assertThat(testee.transcend().represent(), is(equalTo(
                "//" + HOSTNAME + Path.separator() + "hello" + Path.separator() + "world")));
    }

}
