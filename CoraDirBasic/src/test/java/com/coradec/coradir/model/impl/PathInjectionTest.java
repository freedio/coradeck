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

import com.coradec.coracore.annotation.Inject;
import com.coradec.coracore.model.Factory;
import com.coradec.coradir.model.Path;
import com.coradec.coradir.model.TranscendentPath;
import com.coradec.corajet.test.CoradeckJUnit4TestRunner;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.net.URI;

/**
 * ​​Test path injection.
 */
@RunWith(CoradeckJUnit4TestRunner.class)
public class PathInjectionTest {

    private static final String HOSTNAME = "www.example.www";
    @Inject private static Factory<Path> PATH;
    @Inject private static Factory<TranscendentPath> TRANSPATH;
    @Inject private static Factory<EmptyPath> EMPTYPATH;

    @Test public void testTranscendentPathInjection1() {
        Path path = PATH.create(HOSTNAME, new String[] {"a", "b", "c"});
        assertThat(path, is(instanceOf(TranscendentPath.class)));
        TranscendentPath tpath = (TranscendentPath)path;
        assertThat(tpath.getHostname(), is(equalTo(HOSTNAME)));
        assertThat(tpath.localize(), is(equalTo(PATH.create("a", "b", "c"))));
        assertThat(tpath.toURI(), is(equalTo(URI.create("//www.example.www/a/b/c"))));
    }

    @Test public void testTranscendentPathInjection2() {
        Path path = TRANSPATH.create(HOSTNAME, "a", "b", "c");
        assertThat(path, is(instanceOf(TranscendentPath.class)));
        TranscendentPath tpath = (TranscendentPath)path;
        assertThat(tpath.getHostname(), is(equalTo(HOSTNAME)));
        assertThat(tpath.localize(), is(equalTo(PATH.create("a", "b", "c"))));
        assertThat(tpath.toURI(), is(equalTo(URI.create("//www.example.www/a/b/c"))));
    }

    @Test public void testTranscendentPathInjection3() {
        try {
            Path path = TRANSPATH.create(null, "a", "b", "c");
            Assert.fail("Expected ImplementationNotFoundException");
        } catch (Exception e) {
            // expected that
        }
    }

    @Test public void testTranscendentPathInjection4() {
        Path path = TRANSPATH.create(HOSTNAME);
        assertThat(path, is(instanceOf(TranscendentPath.class)));
        TranscendentPath tpath = (TranscendentPath)path;
        assertThat(tpath.getHostname(), is(equalTo(HOSTNAME)));
        assertThat(tpath.localize(), is(equalTo(EMPTYPATH.create())));
        assertThat(tpath.toURI(), is(equalTo(URI.create("//www.example.www/"))));
    }

}
