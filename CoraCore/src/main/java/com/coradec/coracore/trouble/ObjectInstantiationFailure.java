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

import com.coradec.coracore.annotation.Nullable;
import com.coradec.coracore.annotation.ToString;

import java.lang.reflect.InvocationTargetException;

/**
 * ​​Indicates a failure to create an instance of a particular class.
 */
public class ObjectInstantiationFailure extends BasicException {

    private final Class<?> type;

    public ObjectInstantiationFailure(final Class<?> type, final @Nullable Throwable problem) {
        super(problem instanceof InvocationTargetException
              ? ((InvocationTargetException)problem).getTargetException() : problem);
        this.type = type;
    }

    public ObjectInstantiationFailure(final Class<?> type) {
        this.type = type;
    }

    public ObjectInstantiationFailure(final Class<?> type, final String explanation) {
        super(explanation);
        this.type = type;
    }

    @ToString public Class<?> getType() {
        return this.type;
    }

}
