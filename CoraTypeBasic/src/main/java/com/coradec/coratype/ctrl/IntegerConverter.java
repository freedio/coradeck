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

package com.coradec.coratype.ctrl;

import static com.coradec.coracore.model.Scope.*;

import com.coradec.coracore.annotation.Implementation;
import com.coradec.coratype.ctrl.impl.BasicTypeConverter;
import com.coradec.coratype.trouble.TypeConversionException;

/**
 * ​​A type converter for class {@link Integer}.
 */
@Implementation(SINGLETON)
public class IntegerConverter extends BasicTypeConverter<Integer> {

    public IntegerConverter() {
        super(Integer.class);
    }

    @Override public Integer convert(final Object obj) throws TypeConversionException {
        return obj instanceof Number ? ((Number)obj).intValue() : trivial(obj);
    }

    @Override public Integer decode(final String value) throws TypeConversionException {
        try {
            return Integer.valueOf(value);
        } catch (NumberFormatException e) {
            throw new TypeConversionException(String.class, e);
        }
    }
}
