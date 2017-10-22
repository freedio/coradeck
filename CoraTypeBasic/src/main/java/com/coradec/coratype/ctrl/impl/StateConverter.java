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
import com.coradec.coracore.model.State;
import com.coradec.coracore.util.StringUtil;
import com.coradec.coratype.trouble.TypeConversionException;

import java.lang.reflect.InvocationTargetException;

/**
 * ​​Implementation of a type converter for enums.
 */
@Implementation(SINGLETON)
public class StateConverter extends BasicTypeConverter<State> {

    public StateConverter() {
        super(State.class);
    }

    @Override public State convert(final Object value) throws TypeConversionException {
        return trivial(value);
    }

    @SuppressWarnings("unchecked") @Override public State decode(final String value)
            throws TypeConversionException {
        final String[] split = value.split(":");
        if (split.length < 2)
            throw new TypeConversionException(value, State.class, "Expected type:name[:ordinal]");
        try {
            final Class<?> type = Class.forName(split[0]);
            if (Enum.class.isAssignableFrom(type))
                return (State)Enum.valueOf((Class<? extends Enum>)type, split[1]);
            if (split.length != 3)
                throw new TypeConversionException(value, State.class, "Expected type:name:ordinal");
            return (State)type.getConstructor(String.class, Integer.TYPE)
                              .newInstance(split[1], Integer.valueOf(split[2]));
        } catch (InvocationTargetException e) {
            throw new TypeConversionException(value, State.class, e.getTargetException());
        } catch (ClassNotFoundException | IllegalArgumentException | IllegalAccessException |
                InstantiationException | NoSuchMethodException e) {
            throw new TypeConversionException(value, State.class, e);
        }
    }

    @Override public String encode(final State value) {
        return String.format("%s:%s:%s", value.getClass().getName(), value.name(), value.ordinal());
    }

    @Override public State unmarshal(final byte[] value) throws TypeConversionException {
        return decode(new String(value, StringUtil.UTF8));
    }

    @Override public byte[] marshal(final State value) {
        return encode(value).getBytes(StringUtil.UTF8);
    }

}
