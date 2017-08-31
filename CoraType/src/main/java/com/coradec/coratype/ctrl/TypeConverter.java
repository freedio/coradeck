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

package com.coradec.coratype.ctrl;

import com.coradec.coracore.model.Factory;
import com.coradec.coracore.model.GenericFactory;
import com.coradec.coracore.model.GenericType;
import com.coradec.coratype.module.CoraType;
import com.coradec.coratype.trouble.TypeConversionException;

/**
 * Converts object into a particular type.
 *
 * @param <V> the object type.
 */
@SuppressWarnings("unchecked")
public interface TypeConverter<V> {

    Factory<TypeConverter<?>> FACTORY = new GenericFactory(TypeConverter.class);

    @SuppressWarnings("unchecked") static <T> TypeConverter<T> to(GenericType<T> type) {
        return CoraType.find(type);
    }

    @SuppressWarnings("unchecked") static <T> TypeConverter<T> to(Class<T> type) {
        return CoraType.find(GenericType.of(type));
    }

    /**
     * Converts the specified object into an object of the target type, if possible.
     *
     * @param value the object to convert.
     * @return the converted object.
     * @throws TypeConversionException if the type conversion failed.
     */
    V convert(Object value) throws TypeConversionException;

    /**
     * Decodes the specified string into an object of the target type, if possible.
     *
     * @param value the value to decode.
     * @return the decoded object.
     * @throws TypeConversionException if the type conversion failed.
     */
    V decode(String value) throws TypeConversionException;

    /**
     * Encodes the specified value into a string representation that can be decoded using {@link
     * #decode(String)}.
     *
     * @param value the value to encode.
     * @return the encoded object.
     */
    String encode(V value);

    /**
     * Unmarshals the specified byte array into an object of the target type, if possible.
     *
     * @param value the value to unmarshal.
     * @return the decoded object.
     * @throws TypeConversionException if the type conversion failed.
     */
    V unmarshal(byte[] value) throws TypeConversionException;

    /**
     * Marshals the specified value into a byte array that can be unmarshalled using {@link
     * #unmarshal(byte[])}.
     *
     * @param value the value to marshal.
     * @return the encoded object.
     */
    byte[] marshal(V value);

}
