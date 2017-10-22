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

import com.coradec.coradoc.cssenum.FontVariant;
import com.coradec.coradoc.model.ModifiableDeclaration;
import com.coradec.coradoc.model.ParserToken;
import com.coradec.coradoc.model.Style;
import com.coradec.coradoc.state.ProcessingState;
import com.coradec.coradoc.struct.BasicCssDeclaration;
import com.coradec.coradoc.token.Identifier;
import com.coradec.coradoc.trouble.InvalidDeclarationTokenException;

/**
 * ​​Implementation of the CSS font-variant declaration.
 */
@SuppressWarnings("ClassHasNoToStringMethod")
public class FontVariantDeclaration extends BasicCssDeclaration {

    static FontVariant getDefault() {
        return FontVariant.NORMAL;
    }

    static boolean isVariant(final ParserToken token) {
        if (!(token instanceof Identifier)) return false;
        try {
            FontVariant.valueOf(((Identifier)token).getEnumTag());
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private FontVariant variant;

    /**
     * Initializes a new instance of FontVariantDeclaration from the specified declaration.
     *
     * @param source the declaration.
     */
    protected FontVariantDeclaration(final ModifiableDeclaration source) {
        super(source);
    }

    /**
     * Initializes an new instance of FontVariantDeclaration with the specified variant identifier.
     *
     * @param variant the style identifier.
     * @throws IllegalArgumentException if the specified identifier does not describe a font
     *                                  variant.
     */
    protected FontVariantDeclaration(final ParserToken variant) throws IllegalArgumentException {
        super("font-variant");
        process(variant);
    }

    @Override protected ProcessingState getInitialState() {
        return this::base;
    }

    private ProcessingState base(final ParserToken token) {
        if (token instanceof Identifier) try {
            this.variant = FontVariant.valueOf(((Identifier)token).getEnumTag());
        } catch (IllegalArgumentException e) {
            throw new InvalidDeclarationTokenException(token);
        }
        else end(token);
        return this::end;
    }

    public FontVariant getVariant() {
        return variant == null ? getDefault() : variant;
    }

    @Override public void apply(final Style style) {
        style.setFontVariant(getVariant());
    }
}
