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
import com.coradec.coradoc.model.CssDocument;

import java.io.InputStream;
import java.nio.charset.Charset;

/**
 * Basic implementation of a CSS source document.​​
 */
public class BasicCssDocument extends BasicDocument implements CssDocument {

    public BasicCssDocument(final Origin origin, final InputStream inputStream,
            final Charset encoding) {
        super(origin, inputStream, encoding);
    }

    @Override public int skipBlanks() {
        // skip comments also, because they can appear everywhere where blanks can appear.
        int skipped = super.skipBlanks();
        while (isNext("/*")) {
            skipped += 2 + readUpto("*/").length() + super.skipBlanks();
        }
        return skipped;
    }

}
