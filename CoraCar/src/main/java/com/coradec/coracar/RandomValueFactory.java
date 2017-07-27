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

package com.coradec.coracar;

import com.coradec.coracore.annotation.Nullable;
import com.coradec.coracore.model.Factory;

import java.util.Random;

/**
 * ​​A factory for random values of a particular type./
 */
public class RandomValueFactory implements Factory<Object> {

    private static final Random RANDOM = new Random();

    public RandomValueFactory() {
    }

    @Override @Nullable public Object get(final Object... args) {
        if (args[0] == String.class) return "Hello Key";
        if (args[0] == Integer.class) return RANDOM.nextInt();
        return null;
    }

    @Override @Nullable public Object create(final Object... args) {
        if (args[0] == String.class) return "Hello Value";
        if (args[0] == Integer.class) return RANDOM.nextInt();
        return null;
    }
}
