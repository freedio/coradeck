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

import static com.coradec.coradoc.cssenum.FontSize.*;

import com.coradec.coracore.annotation.Nullable;
import com.coradec.coradoc.cssenum.FontSize;
import com.coradec.coradoc.model.IntegerMeasure;
import com.coradec.coradoc.model.ModifiableDeclaration;
import com.coradec.coradoc.model.ParserToken;
import com.coradec.coradoc.model.Style;
import com.coradec.coradoc.state.ProcessingState;
import com.coradec.coradoc.struct.BasicCssDeclaration;
import com.coradec.coradoc.token.Identifier;
import com.coradec.coradoc.token.IntegerDimension;
import com.coradec.coradoc.token.IntegerPercentage;
import com.coradec.coradoc.token.IntegerValue;
import com.coradec.coradoc.trouble.InvalidDeclarationTokenException;

/**
 * ​​Implementation of the CSS font-size declaration.
 */
@SuppressWarnings("ClassHasNoToStringMethod")
public class FontSizeDeclaration extends BasicCssDeclaration {

    static FontSize getDefault() {
        return FontSize.MEDIUM;
    }

    static @Nullable IntegerMeasure getDefaultValue() {
        return null;
    }

    static boolean isSize(final ParserToken token) {
        return token instanceof Identifier && isValidSizeIdentifier((Identifier)token) ||
               token instanceof IntegerDimension ||
               token instanceof IntegerPercentage;
    }

    private static boolean isValidSizeIdentifier(final Identifier token) {
        try {
            FontSize.valueOf(token.getEnumTag());
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private FontSize size;
    private IntegerMeasure sizeValue;

    /**
     * Initializes a new instance of FontSizeDeclaration from the specified declaration.
     *
     * @param source the declaration.
     */
    public FontSizeDeclaration(final ModifiableDeclaration source) {
        super(source);
    }

    /**
     * Initializes an new instance of FontSizeDeclaration with the specified size token.
     *
     * @param size the size identifier.
     * @throws IllegalArgumentException if the specified token is not a valid font size.
     */
    FontSizeDeclaration(final ParserToken size) {
        super("font-size");
        process(size);
    }

    @Override protected ProcessingState getInitialState() {
        return this::base;
    }

    protected ProcessingState base(final ParserToken token) {
        if (token instanceof Identifier) {
            final String ident = ((Identifier)token).getEnumTag();
            if (ident.equals(EXPLICIT.name())) throw new InvalidDeclarationTokenException(token);
            try {
                this.size = FontSize.valueOf(ident);
            } catch (IllegalArgumentException e) {
                throw new InvalidDeclarationTokenException(token);
            }
        } else if (token instanceof IntegerMeasure) {
            this.sizeValue = (IntegerMeasure)token;
            this.size = EXPLICIT;
        } else if (token instanceof IntegerValue && ((IntegerValue)token).getValue() == 0) {
            this.sizeValue = ZERO_PIXELS;
            this.size = EXPLICIT;
        } else end(token);
        return this::end;
    }

    public FontSize getSize() {
        return size == null ? getDefault() : size;
    }

    @Nullable public IntegerMeasure getValue() {
        return sizeValue;
    }

    @Override public void apply(final Style style) {
        style.setFontSize(getSize(), getValue());
    }
}
