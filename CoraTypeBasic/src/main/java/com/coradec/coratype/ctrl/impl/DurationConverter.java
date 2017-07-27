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

import java.util.concurrent.TimeUnit;

/**
 * ​​Implementation of a duration type converter.
 */
@Implementation(SINGLETON)
public class DurationConverter extends BasicTypeConverter<Duration> {

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
     * Parses a simple duration with one amount and a time unit (ns, μs/us, ms, s, m, h, d),
     * optionally separated with spaces.
     *
     * @param value the duration representation to parse.
     * @return a duration.
     * @throws TypeConversionException if the representation failed to be parsed.
     */
    private Duration parse(final String value) throws TypeConversionException {
        String[] units = {"ns", "μs", "us", "ms", "s", "m", "h", "d"};
        TimeUnit[] timeunits = {
                NANOSECONDS, MICROSECONDS, MICROSECONDS, MILLISECONDS, SECONDS, MINUTES, HOURS, DAYS
        };
        for (int i = 0, is = min(timeunits.length, timeunits.length); i < is; ++i) {
            if (value.endsWith(units[i])) {
                final long amount;
                try {
                    amount = Long.parseLong(
                            value.substring(0, value.length() - units[i].length()).trim());
                } catch (NumberFormatException e) {
                    throw new TypeConversionException(value, Duration.class, e);
                }
                return Duration.of(amount, timeunits[i]);
            }
        }
        throw new TypeConversionException(value, java.time.Duration.class);
    }

}
