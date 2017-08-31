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

import java.io.IOException;

/**
 * ​​A type converter for class {@link Integer}.
 */
@Implementation(SINGLETON)
public class IntegerConverter extends BasicTypeConverter<Integer> {

    public IntegerConverter() {
        super(Integer.class);
    }

    @Override public Integer convert(final Object obj) throws TypeConversionException {
        return obj instanceof Number ? ((Number)obj).intValue() : trivial(obj);
    }

    @Override public Integer decode(final String value) throws TypeConversionException {
        try {
            return Integer.valueOf(value);
        } catch (NumberFormatException e) {
            throw new TypeConversionException(String.class, e);
        }
    }

    /**
     * Encodes the specified value into a string representation that can be decoded using {@link
     * #decode(String)}.
     *
     * @param value the value to encode.
     * @return the encoded object.
     */
    @Override public String encode(final Integer value) {
        return String.valueOf(value);
    }

    @Override public Integer unmarshal(final byte[] value) throws TypeConversionException {
        Unmarshaller unmar = getUnmarshaller(value);
        try {
            return unmar.readInt();
        } catch (IOException e) {
            throw new TypeConversionException(Integer.class, e);
        }
    }

    @Override public byte[] marshal(final Integer value) {
        Marshaller mar = getMarshaller();
        try {
            mar.writeInt(value);
            return mar.get();
        } catch (IOException e) {
            throw new TypeConversionException(Integer.class, e);
        }
    }
}
