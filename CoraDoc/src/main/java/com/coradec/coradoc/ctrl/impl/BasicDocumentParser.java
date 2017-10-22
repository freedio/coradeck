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

import com.coradec.coracore.model.Origin;
import com.coradec.coracore.model.impl.URLigin;
import com.coradec.coradoc.ctrl.DocumentParser;
import com.coradec.coradoc.model.Document;
import com.coradec.coradoc.model.DocumentModel;

import java.net.URL;

/**
 * ​​Basic implementation of a document parser.
 */
public abstract class BasicDocumentParser<M extends DocumentModel> implements DocumentParser<M> {

    private Origin origin;
    private Document document;
    private M model;

    @Override public DocumentParser<M> to(final M model) {
        this.model = model;
        return this;
    }

    @Override public DocumentParser<M> from(final URL source) {
        document = createDocument(source);
        origin = new URLigin(source);
        return this;
    }

    /**
     * Creates a new suitable document from the specified source.
     *
     * @param source the source.
     * @return the document.
     */
    protected Document createDocument(final URL source) {
        return Document.from(source);
    }

    @Override public M getModel() {
        return model;
    }

    protected void startDocument() {
        model.onStartOfDocument(origin);
    }

    protected Document getDocument() {
        return document;
    }
}
