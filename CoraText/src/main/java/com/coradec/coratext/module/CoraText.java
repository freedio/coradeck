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

package com.coradec.coratext.module;

import com.coradec.coracore.model.Factory;
import com.coradec.coracore.model.GenericFactory;
import com.coradec.coratext.model.LocalizedText;
import com.coradec.coratext.model.Text;

/**
 * ​​Implementation of a text loader.
 */
@SuppressWarnings("ClassHasNoToStringMethod")
public final class CoraText {

    private static final CoraText INSTANCE = new CoraText();

    /**
     * Defines a new text literal in the caller context.
     *
     * @param name the name of the literal.
     * @return a new localized text literal.
     */
    public static Text localized(final String context, final String name) {
        return INSTANCE.createLocalizedText(context, name);
    }

    private final Factory<LocalizedText> text = new GenericFactory<>(LocalizedText.class);

    private CoraText() {
    }

    private Text createLocalizedText(final String context, final String name) {
        return text.get(context, name);
    }

}
