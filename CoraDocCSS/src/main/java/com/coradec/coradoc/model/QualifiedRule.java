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

package com.coradec.coradoc.model;

import com.coradec.coraconf.model.ValueMap;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * ​A qualified CSS rule
 */
public interface QualifiedRule extends Rule {

    /**
     * Returns a collection of selectors.
     *
     * @return the selectors of the rule.
     */
    Collection<Selector> getSelectors();

    /**
     * Returns the selector with the specified index.
     *
     * @param index the index.
     * @return a selecotr.
     * @throws IndexOutOfBoundsException if the selector index is below zero or greater than the
     *                                   number of available selectors.
     */
    Selector getSelector(int index) throws IndexOutOfBoundsException;

    /**
     * Returns the mapping of declarations.
     *
     * @return the declaration map.
     */
    Map<String, StyleDeclaration> getStyleDeclarations();

    /**
     * Returns the declaration with the specified name.
     *
     * @param name the name.
     * @return the declaration.
     * @throws NoSuchElementException if the declaration table has no declaration for the specified
     *                                name.
     */
    Declaration getStyleDeclaration(String name) throws NoSuchElementException;

    /**
     * Returns the specifity of the rule.
     *
     * @return the specifity.
     */
    int getSpecifity();

    /**
     * Returns a style from the rule.
     *
     * @return a style.
     */
    Style toStyle();

    /**
     * Checks if the rule applies to the specified path and attribute set.
     *
     * @param path       the path.
     * @param attributes the attribute set.
     * @return {@code true} if the rule applies, {@code false} if not.
     */
    boolean matches(List<String> path, ValueMap attributes);

}
