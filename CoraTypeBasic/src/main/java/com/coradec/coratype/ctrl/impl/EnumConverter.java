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
import com.coradec.coracore.util.StringUtil;
import com.coradec.coratype.trouble.TypeConversionException;

/**
 * ​​Implementation of a type converter for enums.
 */
@Implementation(SINGLETON)
public class EnumConverter extends BasicTypeConverter<Enum> {

    public EnumConverter() {
        super(Enum.class);
    }

    @Override public Enum<?> convert(final Object value) throws TypeConversionException {
        return trivial(value);
    }

    @SuppressWarnings("unchecked") @Override public Enum<?> decode(final String value)
            throws TypeConversionException {
        final String[] split = value.split(":");
        if (split.length < 2) throw new TypeConversionException(value, Enum.class);
        try {
            final Class<Enum> type = (Class<Enum>)Class.forName(split[0]);
            return Enum.valueOf(type, split[1]);
        } catch (ClassNotFoundException | IllegalArgumentException e) {
            throw new TypeConversionException(value, Enum.class, e);
        }
    }

    @Override public String encode(final Enum value) {
        return String.format("%s:%s", value.getDeclaringClass().getName(), value.name());
    }

    @Override public Enum unmarshal(final byte[] value) throws TypeConversionException {
        return decode(new String(value, StringUtil.CHARSET));
    }

    @Override public byte[] marshal(final Enum value) {
        return encode(value).getBytes(StringUtil.CHARSET);
    }
}
