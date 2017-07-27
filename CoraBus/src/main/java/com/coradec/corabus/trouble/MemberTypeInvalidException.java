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

package com.coradec.corabus.trouble;

import com.coradec.corabus.model.BusNode;
import com.coradec.coracore.annotation.ToString;

/**
 * ​​Indicates an unexpected member type.
 */
public class MemberTypeInvalidException extends MembershipException {

    private final Class<? extends BusNode> expected;
    private final Class<? extends BusNode> actual;

    public MemberTypeInvalidException(final Class<? extends BusNode> expected,
            final Class<? extends BusNode> actual) {
        this.expected = expected;
        this.actual = actual;
    }

    @ToString public Class<? extends BusNode> getExpected() {
        return expected;
    }

    @ToString public Class<? extends BusNode> getActual() {
        return actual;
    }

}
