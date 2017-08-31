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
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;

/**
 * ​​Implementation of a type converter for LocalDateTime.
 */
@SuppressWarnings("UseOfObsoleteDateTimeApi")
@Implementation(SINGLETON)
public class LocalDateTimeConverter extends BasicTypeConverter<LocalDateTime> {

    /**
     * Initializes a new instance of LocalDateTimeTypeConverter.
     */
    public LocalDateTimeConverter() {
        super(LocalDateTime.class);
    }

    /**
     * Converts the specified object into an object of the target type, if possible.
     *
     * @param value the object to convert.
     * @return the converted object.
     * @throws TypeConversionException if the type conversion failed.
     */
    @Override public LocalDateTime convert(final Object value) throws TypeConversionException {
        if (value instanceof Date) {
            return LocalDateTime.ofInstant(Instant.ofEpochMilli(((Date)value).getTime()),
                    ZoneId.systemDefault());
        }
        if (value instanceof LocalDate) {
            return LocalDateTime.of((LocalDate)value, LocalTime.of(0, 0, 0));
        }
        if (value instanceof Calendar) {
            return LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(((Calendar)value).getTime().getTime()),
                    ZoneId.systemDefault());
        }
        return trivial(value);
    }

    /**
     * Decodes the specified string into an object of the target type, if possible.
     *
     * @param value the value to decode.
     * @return the decoded object.
     * @throws TypeConversionException if the type conversion failed.
     */
    @Override public LocalDateTime decode(final String value) throws TypeConversionException {
        return LocalDateTime.parse(value);
    }

    /**
     * Encodes the specified value into a string representation that can be decoded using {@link
     * #decode(String)}.
     *
     * @param value the value to encode.
     * @return the encoded object.
     */
    @Override public String encode(final LocalDateTime value) {
        return value.toString();
    }

    /**
     * Unmarshals the specified byte array into an object of the target type, if possible.
     *
     * @param value the value to unmarshal.
     * @return the decoded object.
     * @throws TypeConversionException if the type conversion failed.
     */
    @Override public LocalDateTime unmarshal(final byte[] value) throws TypeConversionException {
        Unmarshaller unmar = getUnmarshaller(value);
        try {
            return LocalDateTime.of(unmar.readInt(), Month.of(unmar.readInt()), unmar.readInt(),
                    unmar.readInt(), unmar.readInt(), unmar.readInt(), unmar.readInt());
        } catch (IOException e) {
            throw new TypeConversionException(LocalDateTime.class, e);
        }
    }

    /**
     * Marshals the specified value into a byte array that can be unmarshalled using {@link
     * #unmarshal(byte[])}.
     *
     * @param value the value to marshal.
     * @return the encoded object.
     */
    @Override public byte[] marshal(final LocalDateTime value) {
        Marshaller mar = getMarshaller();
        try {
            mar.writeInt(value.getYear());
            mar.writeInt(value.getMonthValue());
            mar.writeInt(value.getDayOfMonth());
            mar.writeInt(value.getHour());
            mar.writeInt(value.getMinute());
            mar.writeInt(value.getSecond());
            mar.writeInt(value.getNano());
            return mar.get();
        } catch (IOException e) {
            throw new TypeConversionException(LocalDateTime.class, e);
        }
    }

}
