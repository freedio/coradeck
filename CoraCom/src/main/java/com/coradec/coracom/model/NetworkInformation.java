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

package com.coradec.coracom.model;

import com.coradec.coracore.annotation.Nullable;

import java.util.Map;

/**
 * ​Generalize information coming across the wire.
 */
public interface NetworkInformation {

    /**
     * Returns additional attributes by name in their generic form as String.
     *
     * @return the attribute map.
     */
    Map<String, String> getAttributes();

    /**
     * Returns the message body, if any.
     *
     * @return the message body, or {@code null}.
     */
    @Nullable byte[] getBody();

}
