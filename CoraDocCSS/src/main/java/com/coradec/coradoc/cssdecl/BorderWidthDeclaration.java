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

import static com.coradec.coradoc.cssenum.BorderWidth.*;

import com.coradec.coracore.annotation.Nullable;
import com.coradec.coradoc.cssenum.BorderWidth;
import com.coradec.coradoc.model.ModifiableDeclaration;
import com.coradec.coradoc.model.ParserToken;
import com.coradec.coradoc.model.Style;
import com.coradec.coradoc.state.ProcessingState;
import com.coradec.coradoc.struct.BasicCssDeclaration;
import com.coradec.coradoc.token.Identifier;
import com.coradec.coradoc.token.IntegerDimension;
import com.coradec.coradoc.trouble.InvalidDeclarationTokenException;

/**
 * ​​Implementation of the CSS border-width declaration.
 */
@SuppressWarnings("ClassHasNoToStringMethod")
public class BorderWidthDeclaration extends BasicCssDeclaration {

    static boolean isWidth(final ParserToken token) {
        return token instanceof IntegerDimension ||
               token instanceof Identifier && isValidBorderWidth((Identifier)token);
    }

    private static boolean isValidBorderWidth(final Identifier token) {
        final String ident = token.getEnumTag();
        if (ident.equals(EXPLICIT.name())) return false;
        try {
            BorderWidth.valueOf(ident);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private BorderWidth topWidth, leftWidth, rightWidth, bottomWidth;
    private IntegerDimension topValue, leftValue, rightValue, bottomValue;

    /**
     * Initializes a new instance of BorderWidthDeclaration from the specified declaration.
     *
     * @param source the declaration.
     */
    protected BorderWidthDeclaration(final ModifiableDeclaration source) {
        super(source);
    }

    /**
     * Initializes an new instance of BorderWidthDeclaration with the specified width token.
     *
     * @param width the width token.
     * @throws IllegalArgumentException if the specified token is not a valid border width.
     */
    BorderWidthDeclaration(final ParserToken width) {
        super("border-width");
        process(width);
    }

    @Override protected ProcessingState getInitialState() {
        return this::first;
    }

    private ProcessingState first(final ParserToken token) {
        BorderWidth w = decodeWidth(token);
        IntegerDimension v = decodeValue(token);
        topWidth = leftWidth = rightWidth = bottomWidth = w;
        topValue = leftValue = rightValue = bottomValue = v;
        return this::second;
    }

    private ProcessingState second(final ParserToken token) {
        BorderWidth w = decodeWidth(token);
        IntegerDimension v = decodeValue(token);
        leftWidth = rightWidth = w;
        leftValue = rightValue = v;
        return this::third;
    }

    private ProcessingState third(final ParserToken token) {
        BorderWidth w = decodeWidth(token);
        IntegerDimension v = decodeValue(token);
        bottomWidth = w;
        bottomValue = v;
        return this::fourth;
    }

    private ProcessingState fourth(final ParserToken token) {
        BorderWidth w = decodeWidth(token);
        IntegerDimension v = decodeValue(token);
        leftWidth = w;
        leftValue = v;
        return this::end;
    }

    private BorderWidth decodeWidth(final ParserToken token) {
        if (token instanceof Identifier) {
            final String ident = ((Identifier)token).getEnumTag();
            if (ident.equals(EXPLICIT.name())) throw new InvalidDeclarationTokenException(token);
            try {
                return BorderWidth.valueOf(ident);
            } catch (IllegalArgumentException e) {
                // fall through
            }
        } else if (token instanceof IntegerDimension) return EXPLICIT;
        throw new InvalidDeclarationTokenException(token);
    }

    private @Nullable IntegerDimension decodeValue(final ParserToken token) {
        return token instanceof IntegerDimension ? (IntegerDimension)token : null;
    }

    @Override public void apply(final Style style) {
        style.setBorderTopWidth(topWidth, topValue);
        style.setBorderLeftWidth(leftWidth, leftValue);
        style.setBorderRightWidth(rightWidth, rightValue);
        style.setBorderBottomWidth(bottomWidth, bottomValue);
    }
}
