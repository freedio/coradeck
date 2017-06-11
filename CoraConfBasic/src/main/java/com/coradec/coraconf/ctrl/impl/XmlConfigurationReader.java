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

import com.coradec.coraconf.model.AnnotatedProperty;
import com.coradec.coraconf.trouble.ConfigurationException;
import com.coradec.coracore.annotation.ToString;
import com.coradec.coracore.util.ClassUtil;
import com.coradec.coracore.util.StringUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.net.URL;
import java.util.Iterator;
import java.util.Optional;

/**
 * ​​A configuration file reader for XML-style configurations.
 */
@SuppressWarnings("ClassHasNoToStringMethod")
public class XmlConfigurationReader extends BasicConfigurationReader {

    private Document document;
    private NodeList properties;
    private Iterator<AnnotatedProperty> iterator;

    /**
     * Initializes a new instance of XmlConfigurationReader for the configuration with the specified
     * context located at the specified resource.
     *
     * @param context  the configuration context.
     * @param resource the resource.
     */
    public XmlConfigurationReader(final String context, final URL resource) {
        super(context, resource);
    }

    @Override protected void open() throws IOException {
        try {
            this.iterator = new NodeIterator(DocumentBuilderFactory.newInstance()
                                                                   .newDocumentBuilder()
                                                                   .parse(getResourceAsStream()));
        }
        catch (ParserConfigurationException | SAXException e) {
            throw new ConfigurationException(getContext(), e);
        }
    }

    @Override protected void close() throws IOException {
    }

    @Override protected Optional<AnnotatedProperty> getNextProperty() {
        return Optional.ofNullable(this.iterator.hasNext() ? this.iterator.next() : null);
    }

    private class NodeIterator implements Iterator<AnnotatedProperty> {

        private final NodeList properties;
        private int cursor;

        NodeIterator(final Document document) {
            this.properties = document.getElementsByTagName("property");
            this.cursor = 0;
        }

        @Override public boolean hasNext() {
            return this.cursor < this.properties.getLength();
        }

        @ToString public NodeList getProperties() {
            return this.properties;
        }

        @ToString public int getCursor() {
            return this.cursor;
        }

        @Override public AnnotatedProperty next() {
            final Element node = (Element)this.properties.item(this.cursor++);
            final String name = node.getAttribute("name");
            final String type = node.getAttribute("type");
            final String content = node.getTextContent();
            final String value = extract(node.getElementsByTagName("value"));
            final String comment = extract(node.getElementsByTagName("comment"));
            return createPropertyFrom(name, type, value, comment);
        }

        private String extract(final NodeList value) {
            final StringBuilder collector = new StringBuilder();
            String prefix = "";
            for (int i = 0, is = value.getLength(); i < is; ++i) {
                collector.append(prefix).append(value.item(i));
                prefix = StringUtil.NEWLINE;
            }
            return collector.toString();
        }

        @Override public String toString() {
            return ClassUtil.toString(this);
        }
    }

}
