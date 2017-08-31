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

import com.coradec.coracore.annotation.NonNull;
import com.coradec.coracore.annotation.Nullable;
import com.coradec.coracore.annotation.ToString;
import com.coradec.coracore.model.GenericType;

/**
 * ​​Indicates a failure to cast, decode or convert a type.
 */
public class TypeConversionException extends TypeException {

    private final String representation;
    private @Nullable Class<?> fromClass;
    private @Nullable GenericType<?> fromType;

    public TypeConversionException(final @NonNull Class<?> fromType) {
        this.fromClass = fromType;
        representation = null;
    }

    public TypeConversionException(final @NonNull Class<?> fromType, final Throwable problem) {
        super(problem);
        this.fromClass = fromType;
        representation = null;
    }

    public TypeConversionException(final @NonNull Class<?> fromType, final String explanation) {
        this(null, fromType, explanation);
    }

    public TypeConversionException(final String repr, final @NonNull Class<?> fromType, final Throwable problem) {
        super(problem);
        this.representation = repr;
        this.fromClass = fromType;
    }

    public TypeConversionException(final String repr, final @NonNull GenericType<?> fromType) {
        this.representation = repr;
        this.fromType = fromType;
    }

    public TypeConversionException(final String repr, final @NonNull Class<?> fromType) {
        this.representation = repr;
        this.fromClass = fromType;
    }

    public TypeConversionException(final @Nullable String repr, final @NonNull Class<?> fromType,
            final String explanation) {
        super(explanation);
        representation = repr;
        fromClass = fromType;
    }

    @Nullable @ToString public GenericType<?> getGenericType() {
        return this.fromType;
    }

    @Nullable @ToString public Class<?> getType() {
        return this.fromClass;
    }

    @ToString @Nullable public String getRepresentation() {
        return representation;
    }

}
