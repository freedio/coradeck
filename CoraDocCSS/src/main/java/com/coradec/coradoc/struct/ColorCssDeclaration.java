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

package com.coradec.coradoc.struct;

import com.coradec.coradoc.cssenum.ColorCode;
import com.coradec.coradoc.cssenum.ColorFunction;
import com.coradec.coradoc.cssenum.ColorType;
import com.coradec.coradoc.model.ModifiableDeclaration;
import com.coradec.coradoc.model.ParserToken;
import com.coradec.coradoc.state.ProcessingState;
import com.coradec.coradoc.token.HashToken;
import com.coradec.coradoc.token.Identifier;
import com.coradec.coradoc.trouble.ColorFunctionInvalidException;
import com.coradec.coradoc.trouble.ColorValueInvalidException;

import java.awt.*;

/**
 * ​​An abstract CSS color specification.
 */
public abstract class ColorCssDeclaration extends BasicCssDeclaration {

    protected static boolean isColor(final ParserToken token) {
        return token instanceof HashToken && isValidColorValue((HashToken)token) ||
               token instanceof Function && isValidColorFunction((Function)token) ||
               token instanceof Identifier && isValidColorName((Identifier)token);
    }

    private static boolean isValidColorValue(final HashToken token) {
        return token.getName().matches("[0-9A-Fa-f]{6}");
    }

    private static boolean isValidColorFunction(final Function token) {
        try {
            ColorFunction.valueOf(token.getIdentifier());
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private static boolean isValidColorName(final Identifier token) {
        try {
            ColorCode.valueOf(token.getEnumTag());
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private Color color;
    private ColorType colorType;

    public ColorCssDeclaration(final ModifiableDeclaration source) {
        super(source);
    }

    public ColorCssDeclaration(final String identifier) {
        super(identifier);
    }

    public Color getColor() {
        return color;
    }

    protected void setColor(final long color) {
        this.color = new Color((int)(color >>> 16 & 0xff), (int)(color >>> 8 & 0xff),
                (int)(color & 0xff), (int)(color >>> 24 & 0xff));
    }

    protected void setColorType(final ColorType colorType) {
        this.colorType = colorType;
    }

    protected long evalColorValue(final String name) {
        if (name.matches("[0-9A-Fa-f]{6}")) {
            return Long.parseLong(name, 16);
        }
        throw new ColorValueInvalidException('#' + name);
    }

    protected long evalColorFunction(final Function token) {
        ColorFunction colorFunction;
        try {
            colorFunction = ColorFunction.valueOf(token.getIdentifier());
        } catch (IllegalArgumentException e) {
            throw new ColorFunctionInvalidException(token);
        }
        return colorFunction.apply(token.getArguments());
    }

    protected ProcessingState processColor(final ParserToken token) {
        if (token instanceof HashToken) setColor(evalColorValue(((HashToken)token).getName()));
        else if (token instanceof Function) setColor(evalColorFunction((Function)token));
        else if (token instanceof Identifier) {
            ColorCode colorCode = ColorCode.valueOf(((Identifier)token).getEnumTag());
            setColor(colorCode.getValue());
        } else end(token);
        return this::end;
    }

}
