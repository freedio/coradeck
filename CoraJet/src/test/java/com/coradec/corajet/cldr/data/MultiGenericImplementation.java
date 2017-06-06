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

package com.coradec.corajet.cldr.data;

import com.coradec.coratype.trouble.TypeConversionException;

import java.lang.reflect.Modifier;
import java.util.stream.Stream;

/**
 * ​​Implementation of a MultiGenericInterface.
 */
@SuppressWarnings("ClassHasNoToStringMethod")
@com.coradec.coracore.annotation.Implementation
public class MultiGenericImplementation<F, T> implements MultiGenericInterface<F, T> {

    private final Class<F> from;
    private final Class<T> to;

    public MultiGenericImplementation(Class<F> from, Class<T> to) {
        this.from = from;
        this.to = to;
    }

    @Override public T value(final F input) {
        if (to == String.class) return to.cast(String.valueOf(input));
        if (from == String.class) {
            Exception trouble[] = new Exception[1];
            final T result = //
                    Stream.of(to.getMethods())
                          .filter(m -> m.getName().matches("^(valueOf|fromString)$") &&
                                       to.isAssignableFrom(m.getReturnType()) &&
                                       m.getParameterCount() == 1 &&
                                       m.getParameterTypes()[0] == String.class &&
                                       Modifier.isStatic(m.getModifiers()))
                          .findAny()
                          .map(m -> {
                              try {
                                  return to.cast(m.invoke(null, input));
                              }
                              catch (Exception e) {
                                  trouble[0] = e;
                                  return null;
                              }
                          })
                          .orElseThrow(() -> new TypeConversionException(input.getClass()));
            if (trouble[0] != null) throw new TypeConversionException(input.getClass(), trouble[0]);
            return result;
        } else throw new TypeConversionException(input.getClass(),
                String.format("Don't know how to convert from %s to %s", from.getName(),
                        to.getName()));
    }
}
