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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.Date;

/**
 * ​​Implementation of a local date converter.
 */
@SuppressWarnings("UseOfObsoleteDateTimeApi")
@Implementation(SINGLETON)
public class LocalDateConverter extends BasicTypeConverter<LocalDate> {

    public LocalDateConverter() {
        super(LocalDate.class);
    }

    @Override public LocalDate convert(final Object obj) throws TypeConversionException {
        if (obj instanceof LocalDateTime) return ((LocalDateTime)obj).toLocalDate();
        if (obj instanceof Date)
            return ((Date)obj).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        return trivial(obj);
    }

    @Override public LocalDate decode(final String value) throws TypeConversionException {
        try {
            return LocalDate.parse(value);
        } catch (DateTimeParseException e) {
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
    @Override public String encode(final LocalDate value) {
        return String.valueOf(value);
    }

    @Override public LocalDate unmarshal(final byte[] value) throws TypeConversionException {
        Unmarshaller unmar = getUnmarshaller(value);
        try {
            return LocalDate.ofEpochDay(unmar.readLong());
        } catch (IOException e) {
            throw new TypeConversionException(LocalDate.class);
        }
    }

    @Override public byte[] marshal(final LocalDate value) {
        Marshaller mar = getMarshaller();
        try {
            mar.writeLong(value.toEpochDay());
            return mar.get();
        } catch (IOException e) {
            throw new TypeConversionException(LocalDate.class);
        }
    }

}
