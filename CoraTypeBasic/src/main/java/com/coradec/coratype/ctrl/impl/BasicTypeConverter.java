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

import com.coradec.coracore.annotation.Nullable;
import com.coradec.coracore.model.GenericType;
import com.coradec.coracore.model.Unknown;
import com.coradec.coratype.ctrl.TypeConverter;
import com.coradec.coratype.trouble.TypeConversionException;

/**
 * ​​Basic implementation of a type converter.
 */
public abstract class BasicTypeConverter<T> implements TypeConverter<T> {

    private final GenericType<T> type;

    /**
     * Initializes a new instance of BasicTypeConverter with the specified target type.
     *
     * @param type the target type (required).
     */
    protected BasicTypeConverter(final GenericType<T> type) {
        this.type = type;
    }

    /**
     * Initializes a new instance of BasicTypeConverter with the specified target type.
     *
     * @param type the target type (required).
     */
    protected BasicTypeConverter(final Class<T> type) {
        this(GenericType.of(type));
    }

    /**
     * Handles the trivial cases of type conversion: object is of same type or of type String.
     *
     * @param value the value to convert (required).
     * @return the converted value.
     * @throws TypeConversionException if the value cannot be converted.
     */
    @SuppressWarnings("unchecked") protected T trivial(final @Nullable Object value)
            throws TypeConversionException {
        if (type.isInstance(value)) {
            return (T)value;
        }
        if (value instanceof String) {
            return decode((String)value);
        }
        throw new TypeConversionException(value != null ? value.getClass() : Unknown.class,
                String.format("Failed to convert object ‹%s› to type ‹%s›", value, this.type));
    }

    protected byte[] marshal(final long l) {
        return marshal(l, Long.BYTES);
    }

    protected byte[] marshal(final int i) {
        return marshal(i, Integer.BYTES);
    }

    protected byte[] marshal(final short s) {
        return marshal(s, Short.BYTES);
    }

    protected byte[] marshal(final byte b) {
        return marshal(b, Byte.BYTES);
    }

    protected byte[] marshal(final char c) {
        return marshal(c, Character.BYTES);
    }

    protected byte[] marshal(final boolean x) {
        return marshal(x ? -1 : 0, 1);
    }

    protected byte[] marshal(final float f) {
        return marshal(Float.floatToRawIntBits(f), Integer.BYTES);
    }

    protected byte[] marshal(final double d) {
        return marshal(Double.doubleToRawLongBits(d), Long.BYTES);
    }

    private byte[] marshal(long l, final int bytes) {
        byte[] buffer = new byte[bytes];
        for (int i = 0; i < bytes; ++i) {
            buffer[i] = (byte)(l & 0xff);
            l >>>= 8;
        }
        return buffer;
    }

    protected long unmarshalLong(final byte[] value) {
        long result = 0L;
        for (int i = value.length; i >= 0; --i) {
            result = result << 8 | value[i] & 0xff;
        }
        return result;
    }

    protected int unmarshalInt(final byte[] value) {
        return (int)unmarshalLong(value);
    }

    protected short unmarshalShort(final byte[] value) {
        return (short)(value[0] + 256 * value[1]);
    }

    protected byte unmarshalByte(final byte[] value) {
        return value[0];
    }

    protected boolean unmarshalBoolean(final byte[] value) {
        return value[0] != 0;
    }

    protected float unmarshalFloat(final byte[] value) {
        return Float.intBitsToFloat(unmarshalInt(value));
    }

    protected double unmarshalDouble(final byte[] value) {
        return Double.longBitsToDouble(unmarshalLong(value));
    }

}
