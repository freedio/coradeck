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
import com.coradec.coracore.model.GenericType;
import com.coradec.coracore.model.State;
import com.coradec.coratype.ctrl.TypeConverter;
import com.coradec.coratype.trouble.TypeConversionException;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * ​​Implementation of a type converter for state sets.
 */
@Implementation(SINGLETON)
public class StateSetConverter extends BasicTypeConverter<Set<State>> {

    public StateSetConverter() {
        super(GenericType.of(Set.class, State.class));
    }

    @Override public Set<State> convert(final Object value) throws TypeConversionException {
        return trivial(value);
    }

    @Override public Set<State> decode(final String value) throws TypeConversionException {
        if (!value.startsWith("(") || !value.endsWith(")"))
            throw new TypeConversionException(value, Set.class);
        final String[] values = value.substring(1, value.length() - 1).split(", ");
        final Set<State> result = new HashSet<>();
        final TypeConverter<State> stateConverter = TypeConverter.to(State.class);
        for (String val : values) {
            result.add(stateConverter.decode(val));
        }
        return result;
    }

    @Override public String encode(final Set<State> value) {
        final TypeConverter<State> stateConverter = TypeConverter.to(State.class);
        return value.stream()
                    .map(stateConverter::encode)
                    .collect(Collectors.joining(", ", "(", ")"));
    }

    @Override public Set<State> unmarshal(final byte[] value) throws TypeConversionException {
        return standardUnmarshal(value);
    }

    @Override public byte[] marshal(final Set<State> value) {
        return standardMarshal(value);
    }

}
