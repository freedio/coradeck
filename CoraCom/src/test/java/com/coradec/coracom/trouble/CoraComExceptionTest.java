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

package com.coradec.coracom.trouble;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;

import org.junit.Test;

import java.io.IOException;

public class CoraComExceptionTest {

    @Test public void throwingException1() {
        try {
            throw new CoraComException();
        } catch (Exception e) {
            assertThat(e, is(instanceOf(CoraComException.class)));
            assertThat(e.getCause(), is(nullValue()));
        }
    }

    @Test public void throwingException2() {
        try {
            throw new CoraComException(new IOException());
        } catch (Exception e) {
            assertThat(e, is(instanceOf(CoraComException.class)));
            assertThat(e.getCause(), is(instanceOf(IOException.class)));
        }
    }

}