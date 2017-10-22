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

import com.coradec.coracore.model.impl.URLigin;
import com.coradec.coracore.util.StringUtil;
import com.coradec.coradoc.model.impl.BasicCssDocument;
import com.coradec.coradoc.trouble.DocumentReadFailure;

import java.io.IOException;
import java.net.URL;

/**
 * A source cascaded style-sheet.​​
 */
public interface CssDocument extends Document {

    /**
     * Creates a CSS document from the specified source.
     *
     * @param source the source.
     * @return the document.
     */
    static CssDocument from(URL source) {
        try {
            return new BasicCssDocument(new URLigin(source), source.openStream(), StringUtil.UTF8);
        } catch (IOException e) {
            throw new DocumentReadFailure(source.toString(), e);
        }
    }

}
