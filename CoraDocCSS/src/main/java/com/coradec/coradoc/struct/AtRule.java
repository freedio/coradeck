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

import com.coradec.coracore.annotation.Nullable;
import com.coradec.coradoc.model.Declaration;
import com.coradec.coradoc.model.ParserToken;
import com.coradec.coradoc.token.AtKeyword;

import java.util.List;

/**
 * An @-Rule.
 */
@SuppressWarnings("ClassHasNoToStringMethod")
public class AtRule extends BasicRule implements Declaration {

    private final AtKeyword keyword;
    private final List<ParserToken> prelude;
    private final @Nullable SimpleBlock simpleBlock;

    public AtRule(final AtKeyword keyword, final List<ParserToken> prelude,
            @Nullable final SimpleBlock simpleBlock) {
        this.keyword = keyword;
        this.prelude = prelude;
        this.simpleBlock = simpleBlock;
    }

    @Override public String getIdentifier() {
        return keyword.getName();
    }

}
