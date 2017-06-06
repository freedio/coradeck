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

package com.coradec.corajet.cldr.ctrl;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;

import com.coradec.coracore.annotation.Inject;
import com.coradec.corajet.cldr.data.GenericInterface;

/**
 * ​​CarClassLoader and injector test with generic class injection.
 */
@SuppressWarnings("ClassHasNoToStringMethod")
public class Test2 {

    @Inject
    private GenericInterface<Byte> b;
    @Inject
    private GenericInterface<Short> s;
    @Inject
    private GenericInterface<Float> f;
    @Inject
    private GenericInterface<Double> d;
    @Inject
    private GenericInterface<String> x;

    public static void main(String... args) {
        new Test2().launch();
    }

    private void launch() {
        assertThat(b.value(), is(equalTo((byte)42)));
        assertThat(s.value(), is(equalTo((short)4711)));
        assertThat(f.value(), is(equalTo(2.71828184f)));
        assertThat(d.value(), is(equalTo(Math.PI)));
        assertThat(x.value(), is(equalTo("Hello, World!")));
    }

}
