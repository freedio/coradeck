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

import com.coradec.coracore.annotation.ToString;
import com.coradec.coradoc.model.ParserStructure;
import com.coradec.coradoc.model.ParserToken;
import com.coradec.coradoc.token.BasicParserToken;

import java.util.ArrayList;
import java.util.List;

/**
 * ​​Basic implementation of a parser structure.
 */
@SuppressWarnings("ClassHasNoToStringMethod")
public class BasicParserStructure extends BasicParserToken implements ParserStructure {

    private final List<ParserToken> componentValues;

    protected BasicParserStructure() {
        componentValues = new ArrayList<>();
    }

    /**
     * Returns the component values.
     *
     * @return the component values.
     */
    @ToString public List<ParserToken> getComponentValues() {
        return componentValues;
    }

    /**
     * Adds a component value.
     *
     * @param value the component value.
     */
    public void addComponentValue(final ParserToken value) {
        componentValues.add(value);
    }

    protected void clearComponentValues() {
        componentValues.clear();
    }

}
