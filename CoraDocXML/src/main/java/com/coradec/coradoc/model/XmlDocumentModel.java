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

package com.coradec.coradoc.model;

import com.coradec.coracore.annotation.Nullable;
import com.coradec.coracore.model.Origin;

/**
 * A model for building a structured document.
 */
public interface XmlDocumentModel extends DocumentModel {

    /**
     * Callback invoked before the document starts.
     *
     * @param document the document origin.
     */
    void onStartOfDocument(Origin document);

    /**
     * Callback invoked before the document ends.
     */
    void onEndOfDocument();

    /**
     * Callback invoked when a start tag has been encountered.
     * <p>
     * Note that a tag may be marked as not empty, but there may still be no content between the
     * start and end tag.  The {@code empty} argument just tells whether a trailing slash has been
     * detected in the start tag.
     *
     * @param name       the fully qualified name of the tag
     * @param attributes the attributes, if any.
     * @param empty      {@code true} if the tag is empty, {@code false} if it may or may not have
     */
    void onStartTag(String name, @Nullable XmlAttributes attributes, boolean empty);

    /**
     * Callback invoked when an end tag has been encountered.
     *
     * @param name the fully qualified name of the tag.
     */
    void onEndTag(String name);

    /**
     * Callback invoked when a block of PCDATA has been encountered.
     * <p>
     * Several blocks of contiguous data can follow each other without interruption.
     *
     * @param data the data.
     */
    void onData(String data);

    /**
     * Callback invoked when a block of CDATA has been encountered.
     *
     * @param data the data.
     */
    void onRawData(String data);

    /**
     * Callback invoked when the processing instruction with the specified name and parameter was
     * encountered.
     *
     * @param name the name of the PI.
     * @param arg  the (amorphous) argument of the PI.
     */
    void onProcessingInstruction(String name, @Nullable String arg);

    /**
     * Callback invoked when a character reference with the specified code was encountered.
     *
     * @param charCode the character code.
     */
    void onCharacterReference(int charCode);

    /**
     * Callback invoked when an entity reference with the specified name was encountered.
     *
     * @param name the entity name.
     */
    void onEntityReference(String name);

    /**
     * Callback invoked when the specified comment was encountered.
     *
     * @param comment the comment.
     */
    void onComment(String comment);
}
