/*
 * Copyright ⓒ 2017 by Coradec GmbH.
 *
 * This file is part of the Coradeck.
 *
 * Coradeck is free software: you can redistribute it under the the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or any later version.
 *
 * Coradeck is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR ANY PARTICULAR PURPOSE.  See the GNU General Public License for further details.
 *
 * The GNU General Public License is available from <http://www.gnu.org/licenses/>.
 *
 * @license GPL-3.0+ <http://spdx.org/licenses/GPL-3.0+>
 *
 * @author Dominik Wezel <dom@coradec.com>
 */

package com.coradec.coracar;

import static com.coradec.coracore.model.Scope.*;

import com.coradec.coracore.model.Timer;
import com.coradec.coracore.util.ClassUtil;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * ​​An implementation.
 */
@com.coradec.coracore.annotation.Implementation(TEMPLATE)
public class Implementation implements Interface {

    private static final AtomicInteger ID = new AtomicInteger(0);

    private final String id;
    private final Timer t;

    public Implementation(Timer t) {
        this.t = t;
        id = "X" + ID.getAndIncrement();
        t.start();
    }

    @Override public String getValue() {
        t.stop();
        return id;
    }

    @Override public String toString() {
        return ClassUtil.toString(this);
    }
}
