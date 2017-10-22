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

import com.coradec.coradoc.cssenum.FontStyle;
import com.coradec.coradoc.model.ModifiableDeclaration;
import com.coradec.coradoc.model.ParserToken;
import com.coradec.coradoc.model.Style;
import com.coradec.coradoc.state.ProcessingState;
import com.coradec.coradoc.struct.BasicCssDeclaration;
import com.coradec.coradoc.token.Identifier;
import com.coradec.coradoc.trouble.InvalidDeclarationTokenException;

/**
 * ​​Implementation of the CSS font style declaration.
 */
@SuppressWarnings("ClassHasNoToStringMethod")
public class FontStyleDeclaration extends BasicCssDeclaration {

    static FontStyle getDefault() {
        return FontStyle.NORMAL;
    }

    static boolean isStyle(final ParserToken token) {
        if (!(token instanceof Identifier)) return false;
        try {
            FontStyle.valueOf(((Identifier)token).getEnumTag());
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private FontStyle style;

    /**
     * Initializes a new instance of FontStyleDeclaration from the specified declaration.
     *
     * @param source the declaration.
     */
    public FontStyleDeclaration(final ModifiableDeclaration source) {
        super(source);
    }

    /**
     * Initializes an new instance of FontStyleDeclaration with the specified style identifier.
     *
     * @param style the style identifier.
     * @throws IllegalArgumentException if the specified identifier does not describe a font style.
     */
    public FontStyleDeclaration(final ParserToken style) throws IllegalArgumentException {
        super("font-style");
        process(style);
    }

    @Override protected ProcessingState getInitialState() {
        return this::base;
    }

    protected ProcessingState base(final ParserToken token) {
        if (token instanceof Identifier) try {
            this.style = FontStyle.valueOf(((Identifier)token).getEnumTag());
        } catch (IllegalArgumentException e) {
            throw new InvalidDeclarationTokenException(token);
        }
        else end(token);
        return this::end;
    }

    public FontStyle getStyle() {
        return style == null ? getDefault() : style;
    }

    @Override public void apply(final Style style) {
        style.setFontStyle(getStyle());
    }
}
