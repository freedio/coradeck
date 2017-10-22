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

package com.coradec.coradoc.cssdecl;

import com.coradec.coradoc.model.ModifiableDeclaration;
import com.coradec.coradoc.model.ParserToken;
import com.coradec.coradoc.model.Style;
import com.coradec.coradoc.struct.DistanceCssDeclaration;

/**
 * ​​Implementation of the CSS width declaration.
 */
@SuppressWarnings("ClassHasNoToStringMethod")
public class WidthDeclaration extends DistanceCssDeclaration {

    /**
     * Initializes a new instance of WidthDeclaration from the specified declaration.
     *
     * @param source the declaration.
     */
    public WidthDeclaration(final ModifiableDeclaration source) {
        super(source);
    }

    /**
     * Initializes an new instance of WidthDeclaration with the specified distance token.
     *
     * @param distance the distance token.
     * @throws IllegalArgumentException if the specified token is not a valid width.
     */
    WidthDeclaration(final ParserToken distance) {
        super("width");
        process(distance);
    }

    @Override public void apply(final Style style) {
        style.setWidth(getDistance(), getDistanceValue());
    }
}
