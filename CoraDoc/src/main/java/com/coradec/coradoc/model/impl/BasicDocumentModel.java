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

import com.coradec.coracore.model.Origin;
import com.coradec.coracore.util.ClassUtil;
import com.coradec.coradoc.model.DocumentModel;

/**
 * ​​Basic implementation of a document model.
 */
public class BasicDocumentModel implements DocumentModel {

    private Origin document;

    @Override public void onStartOfDocument(final Origin document) {
        this.document = document;
    }

    @Override public void onEndOfDocument() {

    }

    @Override public void onComment(final String comment) {

    }

    @Override public Origin getDocument() {
        return document;
    }

    @Override public String toString() {
        return ClassUtil.toString(this);
    }
}
