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

package com.coradec.coradoc.model.impl;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;

import com.coradec.coracore.model.impl.URLigin;
import com.coradec.coracore.trouble.ResourceNotFoundException;
import com.coradec.coracore.util.StringUtil;
import com.coradec.coradoc.model.Document;
import com.coradec.coradoc.trouble.EndOfDocumentException;
import com.coradec.corajet.test.CoradeckJUnit4TestRunner;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.net.URL;

@RunWith(CoradeckJUnit4TestRunner.class)
public class BasicDocumentTest {

    @Test public void testDocumentReadThrough() throws IOException {
        final String name = "Testdocument1.txt";
        URL location = getClass().getClassLoader().getResource(name);
        if (location == null) throw new ResourceNotFoundException(name);

        Document testee =
                new BasicDocument(new URLigin(location), location.openStream(), StringUtil.UTF8);
        assertThat(testee.skipBlanks(), is(0)); // no blanks at the beginning
        assertThat(testee.nextChar(), is('A'));
        assertThat(testee.skipBlanks(), is(1));
        assertThat(testee.isNext("simple "), is(true));
        assertThat(testee.readWhile(Character::isAlphabetic).toString(), is(equalTo("one")));
        assertThat(testee.skipBlanks(), is(3));
        assertThat(testee.readUntil(c -> c == '/').toString(), is(equalTo("liner with no CR")));
        assertThat(testee.readUpto(".").toString(), is(equalTo("/LF")));
        // At EOD:
        assertThat(testee.skipBlanks(), is(0));
        try {
            testee.nextChar();
            Assert.fail("Expected EndOfDocumentException");
        } catch (EndOfDocumentException e) {
            // expected that
        }
        try {
            testee.readUntil(c -> c == '.');
            Assert.fail("Expected EndOfDocumentException");
        } catch (EndOfDocumentException e) {
            // expected that
        }
        try {
            testee.readWhile(c -> c == '.');
            Assert.fail("Expected EndOfDocumentException");
        } catch (EndOfDocumentException e) {
            // expected that
        }
    }

}
