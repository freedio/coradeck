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

package com.coradec.coratype.trouble;

import com.coradec.coracore.annotation.Nullable;
import com.coradec.coracore.annotation.ToString;

/**
 * ​​Indicates a failure to cast, decode or convert a type.
 */
public class TypeConversionException extends TypeException {

    private final String representation;
    private final Class<?> fromType;

    public TypeConversionException(final Class<?> fromType) {
        this.fromType = fromType;
        representation = null;
    }

    public TypeConversionException(final Class<?> fromType, final Throwable problem) {
        super(problem);
        this.fromType = fromType;
        representation = null;
    }

    public TypeConversionException(final Class<?> fromType, final String explanation) {
        super(explanation);
        this.fromType = fromType;
        representation = null;
    }

    public TypeConversionException(final String repr, final Class<?> fromType,
            final Throwable problem) {
        super(problem);
        this.representation = repr;
        this.fromType = fromType;
    }

    public TypeConversionException(final String repr, final Class<?> fromType) {
        this.fromType = fromType;
        representation = repr;
    }

    @ToString public Class<?> getFromType() {
        return this.fromType;
    }

    @ToString @Nullable public String getRepresentation() {
        return representation;
    }

}
