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

package com.coradec.coracore.ctrl;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;

import com.coradec.coracore.model.StackFrame;
import com.coradec.corajet.test.CoradeckJUnit4TestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.reflect.Method;

@RunWith(CoradeckJUnit4TestRunner.class)
public class AutoOriginTest extends AutoOrigin {

    @Test public void testHere() throws Exception {
        final StackFrame that = (StackFrame)here();
        assertThat(that.getClassName(), is(equalTo(getClass().getName())));
        assertThat(that.getMethodName(), is(equalTo("testHere")));
        assertThat(that.getFileName(), is(equalTo(getClass().getSimpleName() + ".java")));
        assertThat(that.getLineNumber(), is(37));
    }

    @Test public void testThere() throws Exception {
        final StackFrame that = (StackFrame)there();
        assertThat(that.getClassName(), is(equalTo(Method.class.getName())));
        assertThat(that.getMethodName(), is(equalTo("invoke")));
        assertThat(that.getFileName(), is(equalTo("Method.java")));
        assertThat(that.getLineNumber(), is(498));
    }

    @Test public void testThere1() throws Exception {
        final StackFrame that = (StackFrame)there(Exception.class);
        assertThat(that.getClassName(), is(equalTo(Method.class.getName())));
        assertThat(that.getMethodName(), is(equalTo("invoke")));
        assertThat(that.getFileName(), is(equalTo("Method.java")));
        assertThat(that.getLineNumber(), is(498));
    }

    @Test public void testTthere() throws Exception {
        final StackFrame that = (StackFrame)tthere();
        assertThat(that.getClassName(), is(equalTo(Method.class.getName())));
        assertThat(that.getMethodName(), is(equalTo("invoke")));
        assertThat(that.getFileName(), is(equalTo("Method.java")));
        assertThat(that.getLineNumber(), is(498));
    }

    @Test public void testToString() throws Exception {
        final String that = toString();
        assertThat(that, is(equalTo("(" + getClass().getName() + ")")));
    }

}
