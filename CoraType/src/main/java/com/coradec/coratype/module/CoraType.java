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

package com.coradec.coratype.module;

import com.coradec.coracore.model.DynamicFactory;
import com.coradec.coracore.model.GenericType;
import com.coradec.coratype.ctrl.TypeConverter;
import com.coradec.coratype.trouble.TypeConversionException;

/**
 * ​​The type service façade
 */
@SuppressWarnings("ClassHasNoToStringMethod")
public final class CoraType {

    private static final CoraType INSTANCE = new CoraType();
    private static final TypeConverter<Object> DUMMY_CONVERTER = new DummyConverter();

    public static <X> TypeConverter<X> find(final GenericType<X> type) {
        return INSTANCE.findConverter(type);
    }

    private final DynamicFactory<TypeConverter<?>> converter = new DynamicFactory<>();

    private CoraType() {
    }

    @SuppressWarnings("unchecked")
    private <X> TypeConverter<X> findConverter(final GenericType<X> type) {
        if (type.getRawType() == Object.class) return (TypeConverter<X>)DUMMY_CONVERTER;
        return (TypeConverter<X>)converter.of(TypeConverter.class, type).get();
    }

    private static class DummyConverter implements TypeConverter<Object> {

        @Override public Object convert(final Object value) throws TypeConversionException {
            return value;
        }

        @Override public Object decode(final String value) throws TypeConversionException {
            return value;
        }

        @Override public String encode(final Object value) {
            return String.valueOf(value);
        }

        @Override public Object unmarshal(final byte[] value) throws TypeConversionException {
            return value;
        }

        @Override public byte[] marshal(final Object value) {
            return new byte[0];
        }
    }
}
