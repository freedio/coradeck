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

package com.coradec.corajet.trouble;

import com.coradec.coracore.annotation.Nullable;
import com.coradec.coracore.annotation.ToString;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * ​​Indicates an attempt to inject an instance for an interface for which no implementation is
 * known.
 */
public class ImplementationNotFoundException extends CoraJetException {

    private final @Nullable String interfaceName;
    private final List<Type> typeArguments;
    private final Object[] constructorArguments;

    public ImplementationNotFoundException(@Nullable final String interfaceName) {
        this.interfaceName = interfaceName;
        this.typeArguments = Collections.emptyList();
        this.constructorArguments = new Object[0];
    }

    public ImplementationNotFoundException(@Nullable final String interfaceName,
                                           final List<Type> typeArguments,
                                           final Object... constructorArguments) {

        this.interfaceName = interfaceName;
        this.typeArguments = new ArrayList<>(typeArguments);
        this.constructorArguments = constructorArguments.clone();
    }

    @ToString public @Nullable String getInterface() {
        return this.interfaceName;
    }

    @ToString public List<Type> getTypeArguments() {
        return this.typeArguments;
    }

    @ToString public List<Object> getConstructorArguments() {
        return Arrays.asList(this.constructorArguments);
    }
}
