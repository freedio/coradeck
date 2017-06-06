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
import com.coradec.corajet.cldr.data.MultiGenericInterface;

/**
 * ​​CarClassLoader and injector test with generic class injection with several type parameters.
 */
@SuppressWarnings("ClassHasNoToStringMethod")
public class Test3 {

    @Inject
    private MultiGenericInterface<Integer, String> cis;
    @Inject
    private MultiGenericInterface<String, Double> csi;

    public static void main(String... args) {
        new Test3().launch();
    }

    private void launch() {
        assertThat(cis.value(13), is(equalTo("13")));
        assertThat(csi.value("3.141592654"), is(equalTo(3.141592654)));
    }

}
