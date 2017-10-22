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

import com.coradec.coradoc.cssenum.FontWeight;
import com.coradec.coradoc.model.ModifiableDeclaration;
import com.coradec.coradoc.model.ParserToken;
import com.coradec.coradoc.model.Style;
import com.coradec.coradoc.state.ProcessingState;
import com.coradec.coradoc.struct.BasicCssDeclaration;
import com.coradec.coradoc.token.Identifier;
import com.coradec.coradoc.token.IntegerValue;
import com.coradec.coradoc.trouble.InvalidDeclarationTokenException;

/**
 * ​​Implementation of the CSS font-weight declaration.
 */
@SuppressWarnings("ClassHasNoToStringMethod")
public class FontWeightDeclaration extends BasicCssDeclaration {

    static FontWeight getDefault() {
        return FontWeight.NORMAL;
    }

    static boolean isWeight(final ParserToken token) {
        return token instanceof IntegerValue && isValidNumericWeight((IntegerValue)token) ||
               token instanceof Identifier && isValidWeightIdentifier((Identifier)token);
    }

    private static boolean isValidNumericWeight(final IntegerValue token) {
        try {
            FontWeight.valueOf("_" + token.getRepr());
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private static boolean isValidWeightIdentifier(final Identifier token) {
        final String value = token.getEnumTag();
        try {
            FontWeight.valueOf(value);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private FontWeight weight;

    /**
     * Initializes a new instance of FontWeightDeclaration from the specified declaration.
     *
     * @param source the declaration.
     */
    protected FontWeightDeclaration(final ModifiableDeclaration source) {
        super(source);
    }

    /**
     * Initializes an new instance of FontWeightDeclaration with the specified weight token.
     *
     * @param weight the weight identifier.
     * @throws IllegalArgumentException if the specified token is not a valid font weight.
     */
    protected FontWeightDeclaration(final ParserToken weight) {
        super("font-weight");
        process(weight);
    }

    @Override protected ProcessingState getInitialState() {
        return this::base;
    }

    protected ProcessingState base(final ParserToken token) {
        if (token instanceof Identifier) {
            final String ident = ((Identifier)token).getEnumTag();
            try {
                this.weight = FontWeight.valueOf(ident);
            } catch (IllegalArgumentException e) {
                throw new InvalidDeclarationTokenException(token);
            }
        } else if (token instanceof IntegerValue) {
            this.weight = FontWeight.valueOf("_" + ((IntegerValue)token).getRepr());
        } else end(token);
        return this::end;
    }

    public FontWeight getWeight() {
        return weight == null ? getDefault() : weight;
    }

    @Override public void apply(final Style style) {
        style.setFontWeight(getWeight());
    }
}
