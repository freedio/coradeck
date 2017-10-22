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

import com.coradec.coracore.annotation.Nullable;
import com.coradec.coradoc.cssenum.BackgroundSize;
import com.coradec.coradoc.model.IntegerMeasure;
import com.coradec.coradoc.model.ModifiableDeclaration;
import com.coradec.coradoc.model.ParserToken;
import com.coradec.coradoc.model.Style;
import com.coradec.coradoc.state.ProcessingState;
import com.coradec.coradoc.struct.BasicCssDeclaration;
import com.coradec.coradoc.token.Identifier;
import com.coradec.coradoc.token.IntegerValue;

/**
 * ​​Implementation of the CSS background-size declaration.
 */
@SuppressWarnings("ClassHasNoToStringMethod")
public class BackgroundSizeDeclaration extends BasicCssDeclaration {

    static BackgroundSize getDefault() {
        return BackgroundSize.AUTO;
    }

    static boolean isSize(final ParserToken token) {
        if (!(token instanceof Identifier)) return false;
        final String name = ((Identifier)token).getEnumTag();
        if (name.equals("EXPLICIT")) return false;
        try {
            BackgroundSize.valueOf(name);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    static boolean isSizePart(final ParserToken token) {
        return token instanceof IntegerMeasure ||
               token instanceof IntegerValue && ((IntegerValue)token).getValue() == 0;
    }

    private BackgroundSize size;
    private IntegerMeasure width, height;

    /**
     * Initializes a new instance of BackgroundSizeDeclaration from the specified declaration.
     *
     * @param source the declaration.
     */
    protected BackgroundSizeDeclaration(final ModifiableDeclaration source) {
        super(source);
    }

    /**
     * Initializes a new instance of BackgroundSizeDeclaration with the specified size token.
     *
     * @param size the color token.
     * @throws IllegalArgumentException if the specified token is not a valid background size.
     */
    protected BackgroundSizeDeclaration(final ParserToken size) {
        super("background-size");
        process(size);
        if (!isSize(size)) process(new Identifier("auto"));
    }

    BackgroundSizeDeclaration(final ParserToken part1, final ParserToken part2) {
        super("background-size");
        process(part1);
        process(part2);
    }

    @Override protected ProcessingState getInitialState() {
        return this::base;
    }

    private ProcessingState base(final ParserToken token) {
        if (isSize(token)) {
            size = BackgroundSize.valueOf(((Identifier)token).getEnumTag());
            return this::end;
        } else if (isSizePart(token)) {
            if (width == null)
                width = token instanceof IntegerValue ? ZERO_PIXELS : (IntegerMeasure)token;
            else {
                height = token instanceof IntegerValue ? ZERO_PIXELS : (IntegerMeasure)token;
                size = BackgroundSize.EXPLICIT;
                return this::end;
            }
        } else end(token);
        return this::base;
    }

    public BackgroundSize getSize() {
        return size == null ? BackgroundSize.AUTO : size;
    }

    public @Nullable IntegerMeasure getHeight() {
        return height;
    }

    public @Nullable IntegerMeasure getWidth() {
        return width;
    }

    @Override public void apply(final Style style) {
        style.setBackgroundSize(getSize());
        style.setBackgroundHeight(getHeight());
        style.setBackgroundWidth(getWidth());
    }

}
