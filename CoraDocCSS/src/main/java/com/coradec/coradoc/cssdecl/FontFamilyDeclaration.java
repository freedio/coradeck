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
import com.coradec.coradoc.struct.BasicCssDeclaration;
import com.coradec.coradoc.token.Comma;
import com.coradec.coradoc.token.Identifier;
import com.coradec.coradoc.token.StringToken;
import com.coradec.coradoc.trouble.FontFamilyInvalidException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * ​​Implementation of the CSS font-family declaration.
 */
@SuppressWarnings("ClassHasNoToStringMethod")
public class FontFamilyDeclaration extends BasicCssDeclaration {

    static boolean isFamily(final ParserToken token) {
        return token instanceof Identifier || token instanceof StringToken;
    }

    private List<String> families;

    /**
     * Initializes a new instance of FontFamilyDeclaration from the specified declaration.
     *
     * @param source the declaration.
     */
    public FontFamilyDeclaration(final ModifiableDeclaration source) {
        super(source);
    }

    /**
     * Initializes an new instance of FontFamilyDeclaration with the specified family token.
     *
     * @param family the family identifier.
     * @throws IllegalArgumentException if the specified token is not a valid font family.
     */
    FontFamilyDeclaration(final ParserToken family) {
        super("font-family");
        addFamily(family);
    }

    public void addFamily(final ParserToken family) {
        if (family instanceof Identifier) families().add(((Identifier)family).getName());
        else if (family instanceof StringToken) families().add(((StringToken)family).getValue());
        else throw new FontFamilyInvalidException(family);
    }

    private List<String> families() {
        if (families == null) families = new ArrayList<>();
        return families;
    }

    @Override protected ProcessingState getInitialState() {
        return this::base;
    }

    private ProcessingState base(final ParserToken token) {
        if (isFamily(token)) addFamily(token);
        else end(token);
        return this::afterFamily;
    }

    private ProcessingState afterFamily(final ParserToken token) {
        if (token instanceof Comma) return this::base;
        return end(token);
    }

    public List<String> getFamily() {
        return Collections.unmodifiableList(families);
    }

    @Override public void apply(final Style style) {
        style.setFontFamily(getFamily());
    }
}
