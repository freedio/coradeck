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

import com.coradec.coradoc.model.ParserToken;
import com.coradec.coradoc.model.Rule;

import java.util.ArrayList;
import java.util.List;

/**
 * ​​Basic implementation of a rule.
 */
@SuppressWarnings("ClassHasNoToStringMethod")
public class BasicRule extends BasicParserStructure implements Rule {

    private final List<ParserToken> prelude;
    private SimpleBlock simpleBlock;

    public BasicRule() {
        prelude = new ArrayList<>();
    }

    /**
     * Returns the prelude.
     *
     * @return the prelude.
     */
    protected List<ParserToken> getPrelude() {
        return prelude;
    }

    /**
     * Clears the prelude.  This method is invoked from subclasses to remove the tokens in the
     * prelude once they has been processed.
     */
    protected void clearPrelude() {
        prelude.clear();
    }

    /**
     * Returns the rule body.
     *
     * @return the rule body.
     */
    protected SimpleBlock getSimpleBlock() {
        return simpleBlock;
    }

    /**
     * Clears the simple block.  This method is invoked from subclasses to remove the simple block
     * once it has been processed.
     */
    protected void clearSimpleBlock() {
        simpleBlock = null;
    }

    /**
     * Sets the rule block.
     *
     * @param ruleBlock the rule block.
     */
    public void setSimpleBlock(final SimpleBlock ruleBlock) {
        this.simpleBlock = ruleBlock;
    }

    /**
     * Adds a component value to the prelude.
     *
     * @param value the component value.
     */
    public void addComponentValue(final ParserToken value) {
        prelude.add(value);
    }

}
