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

package com.coradec.corabus.protocol;

import com.coradec.corabus.model.ServiceProvider;
import com.coradec.coracore.trouble.InitializationError;
import com.coradec.coratext.model.LocalizedText;

/**
 * Handles requirements of a particular protocol.
 */
public interface ProtocolHandler extends ServiceProvider {

    /**
     * Creates a protocol handler for the specified protocol.
     *
     * @param protocol the protocol name.
     * @return a handler.
     */
    static ProtocolHandler fore(String protocol) {
        String handlerName = "com.coradec.corabus.protocol.handler." + protocol + "_Handler";
        try {
            final Class<?> handlerClass = Class.forName(handlerName);
            return (ProtocolHandler)handlerClass.newInstance();
        } catch (Exception e) {
            throw new InitializationError(
                    LocalizedText.define("InvalidProtocolHandler").resolve(handlerName, protocol),
                    e);
        }
    }

}
