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

import com.coradec.coradoc.cssenum.BackgroundPosition;
import com.coradec.coradoc.cssenum.BackgroundPositionPart;
import com.coradec.coradoc.model.CssPosition;
import com.coradec.coradoc.model.IntegerMeasure;
import com.coradec.coradoc.model.ModifiableDeclaration;
import com.coradec.coradoc.model.ParserToken;
import com.coradec.coradoc.model.Style;
import com.coradec.coradoc.model.impl.BasicCssPosition;
import com.coradec.coradoc.state.ProcessingState;
import com.coradec.coradoc.struct.BasicCssDeclaration;
import com.coradec.coradoc.token.Identifier;
import com.coradec.coradoc.token.IntegerDimension;
import com.coradec.coradoc.token.IntegerPercentage;
import com.coradec.coradoc.token.IntegerValue;
import com.coradec.coradoc.trouble.InvalidDeclarationTokenException;
import com.coradec.coradoc.trouble.PositionInvalidException;

/**
 * ​​Implementation of the CSS background-position declaration.
 */
@SuppressWarnings("ClassHasNoToStringMethod")
public class BackgroundPositionDeclaration extends BasicCssDeclaration {

    private static final CssPosition DEFAULT_POSITION =
            new BasicCssPosition(new IntegerPercentage("0", 0), new IntegerPercentage("0", 0));

    static CssPosition getDefault() {
        return DEFAULT_POSITION;
    }

    static boolean isPosition(final ParserToken token) {
        if (!(token instanceof Identifier)) return false;
        try {
            BackgroundPosition.valueOf(((Identifier)token).getEnumTag());
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    static boolean isPositionPart(final ParserToken token) {
        return token instanceof Identifier && isValidPositionIdentifier((Identifier)token) ||
               token instanceof IntegerDimension ||
               token instanceof IntegerPercentage;
    }

    private static boolean isValidPositionIdentifier(final Identifier token) {
        try {
            BackgroundPositionPart.valueOf(token.getEnumTag());
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private static IntegerPercentage pctFromValue(final int value) {
        return new IntegerPercentage(String.valueOf(value), value);
    }

    private BackgroundPosition position;
    private BackgroundPositionPart horizontalPosition;
    private BackgroundPositionPart verticalPosition;
    private IntegerMeasure horizontalValue;
    private IntegerMeasure verticalValue;

    /**
     * Initializes a new instance of BackgroundPositionDeclaration from the specified declaration.
     *
     * @param source the declaration.
     */
    protected BackgroundPositionDeclaration(final ModifiableDeclaration source) {
        super(source);
    }

    /**
     * Initializes a new instance of BackgroundPositionDeclaration with the specified position
     * token.
     *
     * @param position the position token.
     * @throws IllegalArgumentException if the specified token is not a valid background position.
     */
    protected BackgroundPositionDeclaration(final ParserToken position) {
        super("background-position");
        process(position);
        if (!isPosition(position)) process(new Identifier("center"));
    }

    BackgroundPositionDeclaration(final ParserToken part1, final ParserToken part2) {
        super("background-position");
        process(part1);
        process(part2);
    }

    @Override protected ProcessingState getInitialState() {
        return this::base;
    }

    private ProcessingState base(final ParserToken token) {
        if (isPosition(token)) {
            try {
                position = BackgroundPosition.valueOf(((Identifier)token).getEnumTag());
                return this::end;
            } catch (IllegalArgumentException e) {
                throw new InvalidDeclarationTokenException(token);
            }
        } else if (token instanceof Identifier &&
                   horizontalValue == null &&
                   verticalValue == null) {
            try {
                final BackgroundPositionPart part =
                        BackgroundPositionPart.valueOf(((Identifier)token).getEnumTag());
                if (part.isHorizontal() && horizontalPosition == null) horizontalPosition = part;
                else if (part.isVertical() && verticalPosition == null) verticalPosition = part;
                else throw new PositionInvalidException(token);
            } catch (IllegalArgumentException e) {
                throw new InvalidDeclarationTokenException(token);
            }
        } else if (token instanceof IntegerMeasure &&
                   horizontalPosition == null &&
                   verticalPosition == null) {
            final IntegerMeasure value = (IntegerMeasure)token;
            if (horizontalValue == null) horizontalValue = value;
            else verticalValue = value;
        } else if (token instanceof IntegerValue &&
                   ((IntegerValue)token).getValue() == 0 &&
                   horizontalPosition == null &&
                   verticalPosition == null) {
            if (horizontalValue == null) horizontalValue = ZERO_PIXELS;
            else verticalValue = ZERO_PIXELS;
        } else end(token);
        if (horizontalPosition != null && verticalPosition != null ||
            horizontalValue != null && verticalValue != null) return this::end;
        return this::base;
    }

    public CssPosition getPosition() {
        IntegerMeasure h = horizontalValue != null ? horizontalValue
                                                   : pctFromValue(horizontalPosition.getValue());
        IntegerMeasure v =
                verticalValue != null ? verticalValue : pctFromValue(verticalPosition.getValue());
        return new BasicCssPosition(h, v);
    }

    @Override public void apply(final Style style) {
        style.setBackgroundPosition(getPosition());
    }

}
