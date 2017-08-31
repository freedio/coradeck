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

import com.coradec.coracore.annotation.Implementation;
import com.coradec.coracore.model.Scope;
import com.coradec.coracore.util.StringUtil;
import com.coradec.coratype.trouble.TypeConversionException;

/**
 * ​​Implementation of a type converter for strings.
 */
@Implementation(value = Scope.SINGLETON)
public class StringConverter extends BasicTypeConverter<String> {

    public StringConverter() {
        super(String.class);
    }

    @Override public String convert(final Object value) throws TypeConversionException {
        return String.valueOf(value);
    }

    @Override public String decode(final String value) throws TypeConversionException {
        return value;
    }

    @Override public String encode(final String value) {
        return value;
    }

    @Override public String unmarshal(final byte[] value) throws TypeConversionException {
        return new String(value, StringUtil.CHARSET);
    }

    @Override public byte[] marshal(final String value) {
        return value.getBytes(StringUtil.CHARSET);
    }
}
