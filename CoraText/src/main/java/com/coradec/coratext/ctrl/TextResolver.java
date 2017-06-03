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

package com.coradec.coratext.ctrl;

import com.coradec.coracore.annotation.Nullable;

/**
 * API of a text resolver.​
 */
public interface TextResolver {

    /**
     * Resolves the text literal with the specified name in the specified optional context, using
     * the specified arguments to fill in template variables.
     *
     * @param context the context (optional).
     * @param name    the full literal name.
     * @param args    arguments to fit into the template text.
     * @return the resolved text.
     */
    String resolve(final @Nullable String context, final String name, final Object[] args);

}
