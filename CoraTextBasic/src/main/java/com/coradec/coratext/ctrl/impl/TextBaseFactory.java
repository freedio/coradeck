/*
 * Copyright ⓒ 2017 by Coradec GmbH.
 *
 * This file is part of the Coradeck.
 *
 * Coradeck is free software: you can redistribute it under the the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or any later version.
 *
 * Coradeck is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR ANY PARTICULAR PURPOSE.  See the GNU General Public License for further details.
 *
 * The GNU General Public License is available from <http://www.gnu.org/licenses/>.
 *
 * @license GPL-3.0+ <http://spdx.org/licenses/GPL-3.0+>
 *
 * @author Dominik Wezel <dom@coradec.com>
 */

package com.coradec.coratext.ctrl.impl;

import com.coradec.coracore.annotation.Inject;
import com.coradec.coracore.annotation.Nullable;
import com.coradec.coracore.ctrl.Factory;
import com.coradec.coratext.model.TextBase;
import com.coradec.coratext.trouble.TextBaseNotFoundException;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages different text bases.
 */
@SuppressWarnings({"ClassHasNoToStringMethod", "WeakerAccess"})
public class TextBaseFactory {

    @Inject private static Factory<TextBase> TEXT_BASE;

    private TextBase dflt;
    private final Map<String, TextBase> textBases;

    protected TextBaseFactory() {
        textBases = createTextBaseMap();
    }

    /**
     * Creates a suitable text base map.
     * @return a new text base map.
     */
    protected Map<String, TextBase> createTextBaseMap() {
        return new ConcurrentHashMap<>();
    }

    /**
     * Returns the text base with the specified name, or the default text base if the name is not
     * specified.
     *
     * @param context the text base name (optional).
     * @return the text base with the specified name.
     */
    public TextBase get(final @Nullable String context) throws TextBaseNotFoundException {
        TextBase result = context == null ? dflt : textBases.get(context);
        if (result == null) {
            if (context == null) result = dflt = TEXT_BASE.get(TextBase.class);
            else textBases.put(context, result = TEXT_BASE.get(TextBase.class, context));
        }
        if (result == null) throw new TextBaseNotFoundException(context);
        return result;
    }

}
