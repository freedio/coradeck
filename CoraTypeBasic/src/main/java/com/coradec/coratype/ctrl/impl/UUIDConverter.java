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
import java.util.UUID;

/**
 * ​​Implementation of a type converter for UUIDs.
 */
@Implementation(SINGLETON)
public class UUIDConverter extends BasicTypeConverter<UUID> {

    public UUIDConverter() {
        super(UUID.class);
    }

    @Override public UUID convert(final Object value) throws TypeConversionException {
        return trivial(value);
    }

    @Override public UUID decode(final String value) throws TypeConversionException {
        return UUID.fromString(value);
    }

    @Override public String encode(final UUID value) {
        return value.toString();
    }

    @Override public UUID unmarshal(final byte[] value) throws TypeConversionException {
        Unmarshaller unmar = getUnmarshaller(value);
        try {
            return new UUID(unmar.readLong(), unmar.readLong());
        } catch (IOException e) {
            throw new TypeConversionException(UUID.class, e);
        }
    }

    @Override public byte[] marshal(final UUID value) {
        Marshaller mar = getMarshaller();
        try {
            mar.writeLong(value.getMostSignificantBits());
            mar.writeLong(value.getLeastSignificantBits());
            return mar.get();
        } catch (IOException e) {
            throw new TypeConversionException(UUID.class, e);
        }
    }
}
