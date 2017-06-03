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

import com.coradec.coracore.ctrl.Factory;
import com.coradec.coracore.model.GenericFactory;
import com.coradec.coracore.model.Type;
import com.coradec.coratype.trouble.TypeConversionException;

/**
 * ​A type converter for a particular target class.
 */
public interface TypeConverter<V> {

    Factory<TypeConverter<?>> FACTORY = new GenericFactory<>(TypeConverter.class);

    @SuppressWarnings("unchecked") static <T> TypeConverter<T> to(Type<T> type) {
        return (TypeConverter<T>)FACTORY.get(type);
    }

    /**
     * Converts the specified object into an object of the target type, if possible.
     *
     * @param obj the object to convert.
     * @return the converted object.
     * @throws TypeConversionException if the type conversion failed.
     */
    V convert(Object obj) throws TypeConversionException;

    /**
     * Decodes the specified string into an object of the target type, if possible.
     *
     * @param value the value to decode.
     * @return the decoded object.
     * @throws TypeConversionException if the type conversion failed.
     */
    V decode(String value) throws TypeConversionException;

}
