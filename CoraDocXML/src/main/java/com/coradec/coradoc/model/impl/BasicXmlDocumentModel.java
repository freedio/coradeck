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

import com.coradec.coracore.annotation.Nullable;
import com.coradec.coradoc.model.XmlAttributes;
import com.coradec.coradoc.model.XmlDocumentModel;

/**
 * ​​Basic implementation of an XML document model.
 */
public class BasicXmlDocumentModel extends BasicDocumentModel implements XmlDocumentModel {

    @Override public void onEndOfDocument() {

    }

    @Override public void onStartTag(final String name, @Nullable final XmlAttributes attributes,
            final boolean empty) {

    }

    @Override public void onEndTag(final String name) {

    }

    @Override public void onData(final String data) {

    }

    @Override public void onRawData(final String data) {

    }

    @Override public void onProcessingInstruction(final String name, @Nullable final String arg) {

    }

    @Override public void onCharacterReference(final int charCode) {

    }

    @Override public void onEntityReference(final String name) {

    }

    @Override public void onComment(final String comment) {

    }

}
