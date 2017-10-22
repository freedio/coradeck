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

import com.coradec.coradoc.model.IntegerMeasure;
import com.coradec.coradoc.model.ModifiableDeclaration;
import com.coradec.coradoc.model.ParserToken;
import com.coradec.coradoc.model.Style;
import com.coradec.coradoc.state.ProcessingState;
import com.coradec.coradoc.struct.BasicCssDeclaration;
import com.coradec.coradoc.token.Identifier;
import com.coradec.coradoc.token.IntegerDimension;
import com.coradec.coradoc.token.IntegerValue;
import com.coradec.coradoc.trouble.InvalidLengthException;

/**
 * ​​Implementation of the CSS padding declaration.
 */
@SuppressWarnings("ClassHasNoToStringMethod")
public class PaddingDeclaration extends BasicCssDeclaration {

    IntegerMeasure left, right, top, bottom;

    /**
     * Initializes a new instance of PaddingDeclaration from the specified declaration.
     *
     * @param source the declaration.
     */
    public PaddingDeclaration(final ModifiableDeclaration source) {
        super(source);
    }

    @Override protected ProcessingState getInitialState() {
        return this::first;
    }

    /**
     * Initializes an new instance of PaddingDeclaration with the specified padding token.
     *
     * @param padding the padding token.
     * @throws IllegalArgumentException if the specified token is not a valid padding.
     */
    private PaddingDeclaration(final ParserToken padding) {
        super("padding");
    }

    private ProcessingState first(final ParserToken token) {
        if (token instanceof Identifier) {
            switch (((Identifier)token).getEnumTag()) {
                case "INITIAL":
                    left = right = top = bottom = ZERO_PIXELS;
                    return this::end;
                case "INHERIT":
                    left = right = top = bottom = null;
                    return this::end;
                default:
                    throw new InvalidLengthException(token);
            }
        }
        left = right = top = bottom = decodeMeasure(token);
        return this::second;
    }

    private ProcessingState second(final ParserToken token) {
        left = right = decodeMeasure(token);
        return this::third;
    }

    private ProcessingState third(final ParserToken token) {
        bottom = decodeMeasure(token);
        return this::fourth;
    }

    private ProcessingState fourth(final ParserToken token) {
        left = decodeMeasure(token);
        return this::end;
    }

    private IntegerMeasure decodeMeasure(final ParserToken token) {
        if (token instanceof IntegerMeasure) return (IntegerDimension)token;
        if (token instanceof IntegerValue && ((IntegerValue)token).getValue() == 0)
            return ZERO_PIXELS;
        else throw new InvalidLengthException(token);
    }

    public IntegerMeasure getTop() {
        return top;
    }

    public IntegerMeasure getLeft() {
        return left;
    }

    public IntegerMeasure getRight() {
        return right;
    }

    public IntegerMeasure getBottom() {
        return bottom;
    }

    @Override public void apply(final Style style) {
        style.setPaddingTop(getTop());
        style.setPaddingLeft(getLeft());
        style.setPaddingRight(getRight());
        style.setPaddingBottom(getBottom());
    }

}
