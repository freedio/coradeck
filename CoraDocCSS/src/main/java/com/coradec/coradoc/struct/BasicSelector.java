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
import com.coradec.coradoc.model.Selector;

/**
 * ​​Basic implementation of a selector.
 */
@SuppressWarnings("ClassHasNoToStringMethod")
public abstract class BasicSelector extends BasicParserStructure implements Selector {

    private final String name;

    public BasicSelector(final String name) {
        this.name = name;
    }

    @Override @ToString public String getName() {
        return name;
    }

    @Override public Selector createDescendant(final String name) {
        return new DescendantSelector(this, name);
    }

    @Override public Selector createChild(final String name) {
        return new ChildSelector(this, name);
    }

    @Override public Selector createNeighbor(final String name) {
        return new NeighborSelector(this, name);
    }

    @Override public Selector createSibling(final String name) {
        return new SiblingSelector(this, name);
    }

}
