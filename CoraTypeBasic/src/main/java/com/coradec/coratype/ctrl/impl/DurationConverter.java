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
import static java.lang.Integer.*;
import static java.util.concurrent.TimeUnit.*;

import com.coradec.coracore.annotation.Implementation;
import com.coradec.coracore.time.Duration;
import com.coradec.coratype.trouble.TypeConversionException;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * ​​Implementation of a duration type converter.
 */
@Implementation(SINGLETON)
public class DurationConverter extends BasicTypeConverter<Duration> {

    private static final String[] UNITS = {"ns", "μs", "us", "ms", "s", "m", "h", "d"};
    private static final TimeUnit[] TIMEUNITS = {
            NANOSECONDS, MICROSECONDS, MICROSECONDS, MILLISECONDS, SECONDS, MINUTES, HOURS, DAYS
    };

    public DurationConverter() {
        super(Duration.class);
    }

    @Override public Duration convert(final Object value) throws TypeConversionException {
        return trivial(value);
    }

    @Override public Duration decode(final String value) throws TypeConversionException {
        return parse(value);
    }

    /**
     * Encodes the specified value into a string representation that can be decoded using {@link
     * #decode(String)}.
     *
     * @param value the value to encode.
     * @return the encoded object.
     */
    @Override public String encode(final Duration value) {
        final TimeUnit unit = value.getUnit();
        String u = "";
        for (int i = 0, is = TIMEUNITS.length; i < is; ++i) {
            if (unit == TIMEUNITS[i]) u = UNITS[i];
        }
        return String.format("%d%s", value.getAmount(), u);
    }

    @Override public Duration unmarshal(final byte[] value) throws TypeConversionException {
        Unmarshaller unmar = getUnmarshaller(value);
        try {
            return Duration.of(unmar.readLong(), MILLISECONDS);
        } catch (IOException e) {
            throw new TypeConversionException(Duration.class);
        }
    }

    @Override public byte[] marshal(final Duration value) {
        Marshaller mar = getMarshaller();
        try {
            mar.writeLong(value.toMillis());
            return mar.get();
        } catch (IOException e) {
            throw new TypeConversionException(Duration.class);
        }
    }

    /**
     * Parses a simple duration with one amount and a time unit (ns, μs/us, ms, s, m, h, d),
     * optionally separated with spaces.
     *
     * @param value the duration representation to parse.
     * @return a duration.
     * @throws TypeConversionException if the representation failed to be parsed.
     */
    private Duration parse(final String value) throws TypeConversionException {
        for (int i = 0, is = min(TIMEUNITS.length, TIMEUNITS.length); i < is; ++i) {
            if (value.endsWith(UNITS[i])) {
                final long amount;
                try {
                    amount = Long.parseLong(
                            value.substring(0, value.length() - UNITS[i].length()).trim());
                } catch (NumberFormatException e) {
                    throw new TypeConversionException(value, Duration.class, e);
                }
                return Duration.of(amount, TIMEUNITS[i]);
            }
        }
        throw new TypeConversionException(value, java.time.Duration.class);
    }

}
