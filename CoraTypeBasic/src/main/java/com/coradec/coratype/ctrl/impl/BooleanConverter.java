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
 * ​​Implementation of a type converter for booleans.
 */
@Implementation(SINGLETON)
public class BooleanConverter extends BasicTypeConverter<Boolean> {

    public BooleanConverter() {
        super(Boolean.class);
    }

    @Override public Boolean convert(final Object value) throws TypeConversionException {
        if (value == null) return false;
        if (value instanceof Number) return ((Number)value).intValue() != 0;
        return trivial(value);
    }

    @Override public Boolean decode(final String value) throws TypeConversionException {
        return Boolean.valueOf(value);
    }

    @Override public String encode(final Boolean value) {
        return value.toString();
    }

    @Override public Boolean unmarshal(final byte[] value) throws TypeConversionException {
        return value.length > 0 && value[0] != 0;
    }

    @Override public byte[] marshal(final Boolean value) {
        return new byte[] {value ? (byte)-1 : (byte)0};
    }

}
