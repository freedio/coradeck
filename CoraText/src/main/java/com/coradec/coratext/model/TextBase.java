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

package com.coradec.coratext.model;

import java.util.Optional;

/**
 * ​Representation of a text base.
 */
public interface TextBase {

    /**
     * Returns the context, if present.
     * <p>
     * If no context is defined, the text base is called the default text base.
     *
     * @return the context, if defined.
     */
    Optional<String> getContext();

    /**
     * Resolves the text literal with the specified name using the specified arguments to fill in
     * template variables.
     *
     * @param name the text literal name.
     * @param args arguments to fill the gaps.
     * @return the resolved text literal.
     */
    String resolve(String name, Object... args);

    /**
     * Looks up the text literal with the specified name.
     *
     * @param name the text literal name.
     * @return the unresolved text literal.
     */
    String lookup(String name);

}
