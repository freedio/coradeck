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

import com.coradec.coradoc.cssenum.BorderStyle;
import com.coradec.coradoc.model.ModifiableDeclaration;
import com.coradec.coradoc.model.ParserToken;
import com.coradec.coradoc.model.Style;
import com.coradec.coradoc.state.ProcessingState;
import com.coradec.coradoc.struct.BasicCssDeclaration;
import com.coradec.coradoc.token.Identifier;
import com.coradec.coradoc.trouble.InvalidDeclarationTokenException;

/**
 * ​​Implementation of the CSS border-style declaration.
 */
@SuppressWarnings("ClassHasNoToStringMethod")
public class BorderStyleDeclaration extends BasicCssDeclaration {

    static boolean isStyle(final ParserToken token) {
        if (!(token instanceof Identifier)) return false;
        try {
            BorderStyle.valueOf(((Identifier)token).getEnumTag());
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private BorderStyle topStyle, leftStyle, rightStyle, bottomStyle;

    /**
     * Initializes a new instance of BorderStyleDeclaration from the specified declaration.
     *
     * @param source the declaration.
     */
    protected BorderStyleDeclaration(final ModifiableDeclaration source) {
        super(source);
    }

    /**
     * Initializes an new instance of BorderStyleDeclaration with the specified style identifier.
     *
     * @param style the style identifier.
     * @throws IllegalArgumentException if the specified token is not a valid border style.
     */
    protected BorderStyleDeclaration(final ParserToken style) {
        super("border-style");
        process(style);
    }

    @Override protected ProcessingState getInitialState() {
        return this::first;
    }

    private ProcessingState first(final ParserToken token) {
        final BorderStyle s = decodeStyle(token);
        topStyle = leftStyle = rightStyle = bottomStyle = s;
        return this::second;
    }

    private ProcessingState second(final ParserToken token) {
        final BorderStyle s = decodeStyle(token);
        leftStyle = rightStyle = s;
        return this::third;
    }

    private ProcessingState third(final ParserToken token) {
        bottomStyle = decodeStyle(token);
        return this::fourth;
    }

    private ProcessingState fourth(final ParserToken token) {
        leftStyle = decodeStyle(token);
        return this::end;
    }

    private BorderStyle decodeStyle(final ParserToken token) {
        if (token instanceof Identifier) {
            try {
                return BorderStyle.valueOf(((Identifier)token).getEnumTag());
            } catch (IllegalArgumentException e) {
                // fall through
            }
        }
        throw new InvalidDeclarationTokenException(token);
    }

    public BorderStyle getTopStyle() {
        return topStyle;
    }

    public BorderStyle getLeftStyle() {
        return leftStyle;
    }

    public BorderStyle getRightStyle() {
        return rightStyle;
    }

    public BorderStyle getBottomStyle() {
        return bottomStyle;
    }

    @Override public void apply(final Style style) {
        style.setBorderTopStyle(getTopStyle());
        style.setBorderLeftStyle(getLeftStyle());
        style.setBorderRightStyle(getRightStyle());
        style.setBorderBottomStyle(getBottomStyle());
    }

}
