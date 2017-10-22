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

import static com.coradec.coradoc.cssenum.Distance.*;

import com.coradec.coradoc.cssenum.Distance;
import com.coradec.coradoc.model.IntegerMeasure;
import com.coradec.coradoc.model.ModifiableDeclaration;
import com.coradec.coradoc.model.ParserToken;
import com.coradec.coradoc.state.ProcessingState;
import com.coradec.coradoc.token.Identifier;
import com.coradec.coradoc.token.IntegerValue;
import com.coradec.coradoc.trouble.InvalidDeclarationTokenException;

/**
 * ​​A CSS declaration involving a distance.
 */
public abstract class DistanceCssDeclaration extends BasicCssDeclaration {

    private Distance distance;
    private IntegerMeasure distanceValue;

    protected DistanceCssDeclaration(final ModifiableDeclaration source) {
        super(source);
    }

    protected DistanceCssDeclaration(final String identifier) {
        super(identifier);
    }

    @Override protected ProcessingState getInitialState() {
        return this::base;
    }

    private ProcessingState base(final ParserToken token) {
        if (token instanceof Identifier) {
            final String ident = ((Identifier)token).getEnumTag();
            if (ident.equals(EXPLICIT.name())) throw new InvalidDeclarationTokenException(token);
            try {
                distance = Distance.valueOf(ident);
            } catch (IllegalArgumentException e) {
                throw new InvalidDeclarationTokenException(token);
            }
        } else if (token instanceof IntegerMeasure) {
            distanceValue = (IntegerMeasure)token;
            distance = EXPLICIT;
        } else if (token instanceof IntegerValue && ((IntegerValue)token).getValue() == 0) {
            distanceValue = ZERO_PIXELS;
            distance = EXPLICIT;
        } else end(token);
        return this::end;
    }

    protected Distance getDistance() {
        return distance;
    }

    protected IntegerMeasure getDistanceValue() {
        return distanceValue;
    }

}
