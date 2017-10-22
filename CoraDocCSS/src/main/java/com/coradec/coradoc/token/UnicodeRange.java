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

package com.coradec.coradoc.token;

import com.coradec.coracore.annotation.ToString;

/**
 * ​​A Unicode range token.
 */
@SuppressWarnings("ClassHasNoToStringMethod")
public class UnicodeRange extends BasicParserToken {

    private final int begin;
    private final int end;

    public UnicodeRange(final int begin, final int end) {
        this.begin = begin;
        this.end = end;
    }

    @ToString public int getBegin() {
        return begin;
    }

    @ToString public int getEnd() {
        return end;
    }

}
