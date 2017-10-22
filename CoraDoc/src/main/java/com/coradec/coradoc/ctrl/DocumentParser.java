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

package com.coradec.coradoc.ctrl;

import com.coradec.coradoc.model.DocumentModel;
import com.coradec.coradoc.trouble.ParseFailure;

import java.net.URL;

/**
 * An object capable of parsing a structured document.
 *
 * @param <M> the document model type.
 */
public interface DocumentParser<M extends DocumentModel> {

    /**
     * Adds the specified document model to handle markup events.
     *
     * @param model the model.
     * @return this parser.
     */
    DocumentParser<M> to(M model);

    /**
     * Adds the specified URL document source.
     *
     * @param source the document source.
     * @return this parser.
     */
    DocumentParser<M> from(URL source);

    /**
     * Parses the document to the model.
     *
     * @throws ParseFailure if the document could not be completely parsed.
     */
    DocumentParser<M> parse() throws ParseFailure;

    /**
     * Returns the model.
     * <p>
     * When invoked after a call to {@link #parse()}, the complete model will be returned.
     *
     * @return the document model.
     */
    M getModel();

}
