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
    @SuppressWarnings("WeakerAccess") protected BasicTypeConverter(final GenericType<T> type) {
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

}
