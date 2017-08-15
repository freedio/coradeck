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

package com.coradec.coracore.util;

import static com.coradec.coracore.tools.hamcrest.RegexMatcher.*;
import static org.hamcrest.MatcherAssert.*;

import org.junit.Test;

/**
 * ​​Tests the network utilities.
 */
public class NetworkUtilTest {

    @Test public void testLocalMachineId() {
        final String expected =
                NetworkUtil.isOnline() ? "[0-9a-fA-F]{2}(-[0-9a-fA-F]{2}){5,}" : "^localhost$";
        System.out.printf("Local system id: %s", NetworkUtil.getLocalMachineId());
        assertThat(NetworkUtil.getLocalMachineId(), matches(expected));
    }

    @Test public void testLocalHostAddress() {
        NetworkUtil.getLocalAddress();
    }

}
