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

import com.coradec.coracore.annotation.Nullable;
import com.coradec.coracore.model.Origin;
import com.coradec.coracore.model.impl.URLigin;
import com.coradec.coracore.trouble.ResourceNotFoundException;
import com.coradec.coradoc.ctrl.XmlParser;
import com.coradec.coradoc.model.XmlAttributes;
import com.coradec.coradoc.model.XmlDocumentModel;
import com.coradec.coradoc.model.impl.BasicXmlDocumentModel;
import com.coradec.coradoc.trouble.StartEndTagMismatchException;
import com.coradec.corajet.test.CoradeckJUnit4TestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

@RunWith(CoradeckJUnit4TestRunner.class)
public class BasicXmlParserTest {

    @Test public void testSimpleXmlDocumentWithDummyModel() {
        final String documentName = "SimpleXmlDocument.xml";
        final URL source = getClass().getClassLoader().getResource(documentName);
        if (source == null) throw new ResourceNotFoundException(documentName);
        BasicXmlDocumentModel model = new BasicXmlDocumentModel();
        XmlParser<BasicXmlDocumentModel> testee = new BasicXmlParser<>();

        testee.from(source).to(model).parse();
    }

    @Test public void testSimpleDocumentWithExplicitModel() {
        final String documentName = "SimpleXmlDocument.xml";
        final URL source = getClass().getClassLoader().getResource(documentName);
        if (source == null) throw new ResourceNotFoundException(documentName);
        TestDocModel model = new TestDocModel();
        XmlParser<TestDocModel> testee = new BasicXmlParser<>();

        testee.from(source).to(model).parse();

        assertThat(model.getDocument(), is(equalTo(new URLigin(source))));
        assertThat(model.isTagStackEmpty(), is(true));
        assertThat(model.getTags().size(), is(4));
        assertThat(model.getProcessingInstructions().size(), is(1));
    }

    private class TestDocModel implements XmlDocumentModel {

        private Origin document;
        private final Stack<String> tagStack;
        private final Map<String, XmlAttributes> tags;
        private final List<String> data;
        private final List<String> entityRefs;
        private final List<Integer> characterRefs;
        private final List<String> comments;
        private final Map<String, String> procInstrs;

        TestDocModel() {
            tagStack = new Stack<>();
            tags = new HashMap<>();
            data = new ArrayList<>();
            entityRefs = new ArrayList<>();
            characterRefs = new ArrayList<>();
            comments = new ArrayList<>();
            procInstrs = new HashMap<>();
        }

        @Override public void onStartOfDocument(final Origin document) {
            this.document = document;
        }

        @Override public void onEndOfDocument() {

        }

        @Override
        public void onStartTag(final String name, @Nullable final XmlAttributes attributes,
                final boolean empty) {
            if (!empty) tagStack.push(name);
            tags.put(name, attributes);
        }

        @Override public void onEndTag(final String name) {
            final String start = tagStack.pop();
            if (!name.equals(start)) throw new StartEndTagMismatchException(start, name);
        }

        @Override public void onData(final String data) {
            this.data.add(data);
        }

        @Override public void onRawData(final String data) {
            this.data.add(data);
        }

        @Override
        public void onProcessingInstruction(final String name, @Nullable final String arg) {
            procInstrs.put(name, arg);
        }

        @Override public void onCharacterReference(final int charCode) {
            characterRefs.add(charCode);
        }

        @Override public void onEntityReference(final String name) {
            entityRefs.add(name);
        }

        @Override public void onComment(final String comment) {
            comments.add(comment);
        }

        @Override public Origin getDocument() {
            return document;
        }

        boolean isTagStackEmpty() {
            return tagStack.isEmpty();
        }

        Map<String, XmlAttributes> getTags() {
            return tags;
        }

        Map<String, String> getProcessingInstructions() {
            return procInstrs;
        }

    }
}
