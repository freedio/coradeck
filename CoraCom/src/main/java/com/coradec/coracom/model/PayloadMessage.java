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

/**
 * ​A message with a payload (body).
 */
public interface PayloadMessage extends Message {

    String PROP_BODY = "Body";

    /**
     * Returns the message body.
     *
     * @return the message body.
     */
    @Nullable byte[] getBody();

    /**
     * Sets the message body to the specified payload.
     *
     * @param payload the payload.
     */
    void setBody(byte[] payload);

}
