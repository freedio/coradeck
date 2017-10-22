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
import com.coradec.coradoc.state.ProcessingState;
import com.coradec.coradoc.struct.ColorCssDeclaration;

/**
 * ​​Implementation of the CSS background-color declaration.
 */
public class BackgroundColorDeclaration extends ColorCssDeclaration {

    /**
     * Initializes a new instance of BackgroundColorDeclaration from the specified declaration.
     *
     * @param source the declaration.
     */
    protected BackgroundColorDeclaration(final ModifiableDeclaration source) {
        super(source);
    }

    /**
     * Initializes a new instance of BackgroundColorDeclaration with the specified color token.
     *
     * @param color the color token.
     * @throws IllegalArgumentException if the specified token is not a valid background color.
     */
    protected BackgroundColorDeclaration(final ParserToken color) {
        super("background-color");
        process(color);
    }

    @Override protected ProcessingState getInitialState() {
        return this::processColor;
    }

    @Override public void apply(final Style style) {
        style.setBackgroundColor(getColor());
    }
}
