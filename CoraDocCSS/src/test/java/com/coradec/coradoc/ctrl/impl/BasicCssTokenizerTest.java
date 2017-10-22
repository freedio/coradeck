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

package com.coradec.coradoc.ctrl.impl;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;

import com.coradec.coracore.model.Origin;
import com.coradec.coracore.model.impl.URLigin;
import com.coradec.coracore.trouble.ResourceNotFoundException;
import com.coradec.coracore.util.StringUtil;
import com.coradec.coradoc.model.CssDocument;
import com.coradec.coradoc.model.DocumentModel;
import com.coradec.coradoc.model.ParserToken;
import com.coradec.coradoc.model.impl.BasicCssDocument;
import com.coradec.coradoc.model.impl.BasicDocumentModel;
import com.coradec.coradoc.trouble.EndOfDocumentException;
import com.coradec.corajet.test.CoradeckJUnit4TestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@RunWith(CoradeckJUnit4TestRunner.class)
public class BasicCssTokenizerTest {

    @Test public void testTokenizer() throws IOException {
        final String resource = getClass().getSimpleName() + "_testTokenizer.data";
        URL source = getClass().getClassLoader().getResource(resource);
        if (source == null) throw new ResourceNotFoundException(resource);
        InputStream in = source.openStream();
        final CssDocument document = new BasicCssDocument(new URLigin(source), in, StringUtil.UTF8);
        final DocumentModel model = new TestModel();
        BasicCssTokenizer testee = new BasicCssTokenizer<>(document, model);
        List<ParserToken> result = new ArrayList<>();

        while (!document.isFinished()) {
            final ParserToken token;
            try {
                token = testee.next();
            } catch (EndOfDocumentException e) {
                break;
            }
            System.out.printf("Token: %s%n", token);
            result.add(token);
        }

        assertThat(result.size(), is(239));
        final String expected = getClass().getSimpleName() + "_testTokenizer.expected";
        source = getClass().getClassLoader().getResource(expected);
        if (source == null) throw new ResourceNotFoundException(expected);
        BufferedReader proofReader =
                new BufferedReader(new InputStreamReader(source.openStream(), StringUtil.UTF8));
        for (ParserToken token : result) {
            String proof = proofReader.readLine();
            assertThat(token.getClass().getName(), is(equalTo(proof)));
        }
        proofReader.close();
    }

    private class TestModel extends BasicDocumentModel {

        @Override public void onStartOfDocument(final Origin document) {

        }

        @Override public void onEndOfDocument() {

        }

        @Override public void onComment(final String comment) {

        }
    }
}
