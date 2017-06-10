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

package com.coradec.coralog.ctrl.impl;

import static com.coradec.coralog.model.LogLevel.*;

import com.coradec.coracore.annotation.Implementation;
import com.coradec.coralog.annotate.Production;
import com.coradec.coralog.annotate.Staging;
import com.coradec.coralog.ctrl.ClassLog;
import com.coradec.coralog.model.LogLevel;

/**
 * ​​Basic implementation of a class log.
 */
@SuppressWarnings("ClassHasNoToStringMethod")
@Implementation
public class BasicClassLog extends BasicLog implements ClassLog {

    private static LogLevel getInitialLevel(Class<?> klass) {
        if (klass.isAnnotationPresent(Production.class)) return ALERT;
        if (klass.isAnnotationPresent(Staging.class)) return INFORMATION;
        return ALL;
    }

    @SuppressWarnings("FieldCanBeLocal") private final Class<?> klass;

    public BasicClassLog(final Class<?> klass) {
        super(getInitialLevel(klass));
        this.klass = klass;
    }

}
