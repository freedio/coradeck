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

import com.coradec.coradir.trouble.CannotTranscendEmptyPathException;
import com.coradec.coradir.trouble.PathEmptyException;
import com.coradec.corajet.test.CoradeckJUnit4TestRunner;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Collections;

@RunWith(CoradeckJUnit4TestRunner.class)
public class EmptyPathTest {

    private EmptyPath testee;

    @Test public void testEmptyPath() {
        testee = new EmptyPath();
        assertThat(testee.isAbsolute(), is(false));
        assertThat(testee.isEmpty(), is(true));
        assertThat(testee.isName(), is(false));
        assertThat(testee.isTranscendent(), is(false));
        assertThat(testee.isLocalAbsolute(), is(false));
        assertThat(testee.represent(), is("."));
        try {
            testee.head();
            Assert.fail("Expected PathEmptyException");
        } catch (PathEmptyException e) {
            // expected that
        }
        try {
            testee.tail();
            Assert.fail("Expected PathEmptyException");
        } catch (PathEmptyException e) {
            // expected that
        }
        assertThat(testee.localize(), is(testee));
        assertThat(testee.toURI("test").toString(), is("test:."));
        assertThat(testee.getPath(), is(equalTo(Collections.emptyList())));
        assertThat(testee.add("another").represent(), is(equalTo("another")));
        try {
            testee.transcend();
            Assert.fail("Expected " + CannotTranscendEmptyPathException.class.getName());
        } catch (CannotTranscendEmptyPathException e) {
            // expected that
        }
    }

}
