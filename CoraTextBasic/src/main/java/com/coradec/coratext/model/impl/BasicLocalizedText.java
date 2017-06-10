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

package com.coradec.coratext.model.impl;

import com.coradec.coracore.annotation.Implementation;
import com.coradec.coratext.model.LocalizedText;

/**
 * ​​Basic implementation of a localized text.
 */
@SuppressWarnings("ClassHasNoToStringMethod")
@Implementation
public class BasicLocalizedText extends BasicText implements LocalizedText {

    private final String context;
    private final String localName;

    public BasicLocalizedText(final String context, final String name) {
        super(context + "." + name);
        this.context = context;
        this.localName = name;
    }

    @Override public String getName() {
        return context + "." + localName;
    }

    @Override public String resolve(final Object... args) {
        return RESOLVER.resolve(context, localName, args);
    }

}
