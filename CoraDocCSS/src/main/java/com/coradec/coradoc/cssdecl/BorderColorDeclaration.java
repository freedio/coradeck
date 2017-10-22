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

import java.awt.*;

/**
 * ​​Implementation of the CSS border-color declaration.
 */
@SuppressWarnings("ClassHasNoToStringMethod")
public class BorderColorDeclaration extends ColorCssDeclaration {

    private Color topColor, leftColor, rightColor, bottomColor;

    /**
     * Initializes a new instance of BorderColorDeclaration from the specified declaration.
     *
     * @param source the declaration.
     */
    public BorderColorDeclaration(final ModifiableDeclaration source) {
        super(source);
    }

    /**
     * Initializes an new instance of BorderColorDeclaration with the specified color token.
     *
     * @param color the color token.
     * @throws IllegalArgumentException if the specified token is not a valid border color.
     */
    public BorderColorDeclaration(final ParserToken color) {
        super("border-color");
        process(color);
    }

    @Override protected ProcessingState getInitialState() {
        return this::first;
    }

    private ProcessingState first(final ParserToken token) {
        processColor(token);
        topColor = leftColor = rightColor = bottomColor = getColor();
        return this::second;
    }

    private ProcessingState second(final ParserToken token) {
        processColor(token);
        leftColor = rightColor = getColor();
        return this::third;
    }

    private ProcessingState third(final ParserToken token) {
        processColor(token);
        bottomColor = getColor();
        return this::fourth;

    }

    private ProcessingState fourth(final ParserToken token) {
        processColor(token);
        leftColor = getColor();
        return this::end;
    }

    private Color getTopColor() {
        return topColor;
    }

    private Color getLeftColor() {
        return leftColor;
    }

    private Color getRightColor() {
        return rightColor;
    }

    private Color getBottomColor() {
        return bottomColor;
    }

    @Override public void apply(final Style style) {
        style.setBorderTopColor(getTopColor());
        style.setBorderLeftColor(getTopColor());
        style.setBorderRightColor(getTopColor());
        style.setBorderBottomColor(getTopColor());
    }
}
