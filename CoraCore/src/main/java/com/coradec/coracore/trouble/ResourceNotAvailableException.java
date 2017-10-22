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

package com.coradec.coracore.trouble;

import com.coradec.coracore.annotation.ToString;

/**
 * ​​Indicates an attempt to request a resource which is not available.
 */
public class ResourceNotAvailableException extends ResourceException {

    private final Class<?> type;
    private final Object[] args;

    public ResourceNotAvailableException(final Class<?> type, final Object... args) {
        this.type = type;
        this.args = args;
    }

    public ResourceNotAvailableException(final Throwable problem, final Class<?> type,
            final Object... args) {
        super(problem);
        this.type = type;
        this.args = args;
    }

    @ToString public Class<?> getType() {
        return type;
    }

    @ToString public Object[] getArgs() {
        return args;
    }

}
