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

package com.coradec.coratype.ctrl.impl;

import static com.coradec.coracore.model.Scope.*;

import com.coradec.coracore.annotation.Implementation;
import com.coradec.coratype.trouble.TypeConversionException;

/**
 * ​​Implementation of an object type converter.
 */
@Implementation(SINGLETON)
public class ObjectConverter extends BasicTypeConverter<Object> {

    public ObjectConverter() {
        super(Object.class);
    }

    @Override public Object convert(final Object obj) throws TypeConversionException {
        return obj;
    }

    @Override public Object decode(final String value) throws TypeConversionException {
        return value;
    }

}
