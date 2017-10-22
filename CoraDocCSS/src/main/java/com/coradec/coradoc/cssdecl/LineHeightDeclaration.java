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

import static com.coradec.coradoc.cssenum.LineHeight.*;

import com.coradec.coracore.annotation.Nullable;
import com.coradec.coradoc.cssenum.LineHeight;
import com.coradec.coradoc.model.IntegerMeasure;
import com.coradec.coradoc.model.ModifiableDeclaration;
import com.coradec.coradoc.model.ParserToken;
import com.coradec.coradoc.model.Style;
import com.coradec.coradoc.state.ProcessingState;
import com.coradec.coradoc.struct.BasicCssDeclaration;
import com.coradec.coradoc.token.Identifier;
import com.coradec.coradoc.token.IntegerToken;
import com.coradec.coradoc.token.IntegerValue;
import com.coradec.coradoc.token.NumericToken;
import com.coradec.coradoc.token.RealValue;
import com.coradec.coradoc.trouble.InvalidDeclarationTokenException;

/**
 * ​​Implementation of the CSS line-height declaration.
 */
@SuppressWarnings("ClassHasNoToStringMethod")
public class LineHeightDeclaration extends BasicCssDeclaration {

    static LineHeight getDefault() {
        return LineHeight.NORMAL;
    }

    static NumericToken getDefaultValue() {
        return new RealValue("1.0", 1.0);
    }

    static boolean isLineHeight(final ParserToken token) {
        return token instanceof Identifier && isValidLineHeightIdentifier((Identifier)token) ||
               token instanceof IntegerMeasure ||
               token instanceof RealValue;
    }

    private static boolean isValidLineHeightIdentifier(final Identifier token) {
        try {
            LineHeight.valueOf(token.getEnumTag());
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private LineHeight lineHeight;
    private NumericToken value;

    protected LineHeightDeclaration(final ModifiableDeclaration source) {
        super(source);
    }

    /**
     * Initializes an new instance of LineHeightDeclaration with the specified height token.
     *
     * @param height the height identifier.
     * @throws IllegalArgumentException if the specified token is not a valid line height.
     */
    protected LineHeightDeclaration(final ParserToken height) {
        super("line-height");
        process(height);
    }

    @Override protected ProcessingState getInitialState() {
        return this::base;
    }

    protected ProcessingState base(final ParserToken token) {
        if (token instanceof Identifier) {
            final String ident = ((Identifier)token).getEnumTag();
            if (ident.equals(EXPLICIT.name()) || ident.equals(FACTOR.name()))
                throw new InvalidDeclarationTokenException(token);
            try {
                this.lineHeight = LineHeight.valueOf(ident);
            } catch (IllegalArgumentException e) {
                throw new InvalidDeclarationTokenException(token);
            }
        } else if (token instanceof IntegerMeasure) {
            value = (IntegerToken)token;
            lineHeight = EXPLICIT;
        } else if (token instanceof IntegerValue && ((IntegerValue)token).getValue() == 0) {
            value = (IntegerValue)ZERO_PIXELS;
            lineHeight = EXPLICIT;
        } else if (token instanceof RealValue) {
            value = (RealValue)token;
            lineHeight = FACTOR;
        } else end(token);
        return this::end;
    }

    public LineHeight getHeight() {
        return lineHeight == null ? getDefault() : lineHeight;
    }

    public @Nullable NumericToken getValue() {
        return value;
    }

    @Override public void apply(final Style style) {
        style.setLineHeight(getHeight(), getValue());
    }
}
