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

import static com.coradec.coradoc.cssenum.CssUnit.*;

import com.coradec.coracore.util.ClassUtil;
import com.coradec.coradoc.model.IntegerMeasure;
import com.coradec.coradoc.model.ModifiableDeclaration;
import com.coradec.coradoc.model.ParserToken;
import com.coradec.coradoc.model.StyleDeclaration;
import com.coradec.coradoc.state.ProcessingState;
import com.coradec.coradoc.token.IntegerDimension;
import com.coradec.coradoc.token.Whitespace;
import com.coradec.coradoc.trouble.InvalidDeclarationTokenException;

/**
 * ​​Basic implementation of a CSS declaration.
 */
public abstract class BasicCssDeclaration implements StyleDeclaration {

    protected static final IntegerMeasure ZERO_PIXELS = new IntegerDimension("0", 0, px);
    private final String identifier;
    private ProcessingState state;

    protected BasicCssDeclaration(final ModifiableDeclaration source) {
        this.identifier = source.getIdentifier();
        this.state = getInitialState();
        source.getBody()
              .stream()
              .filter(token -> !(token instanceof Whitespace))
              .forEach(this::process);
    }

    protected BasicCssDeclaration(String identifier) {
        this.identifier = identifier;
        this.state = getInitialState();
    }

    protected abstract ProcessingState getInitialState();

    @Override public String getIdentifier() {
        return identifier;
    }

    protected void process(final ParserToken token) {
        state = state.process(token);
    }

    @Override public String toString() {
        return ClassUtil.toString(this);
    }

    protected void setState(final ProcessingState state) {
        this.state = state;
    }

    protected ProcessingState end(final ParserToken token) {
        throw new InvalidDeclarationTokenException(token);
    }
}
