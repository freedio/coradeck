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

package com.coradec.corabus.model.impl;

import com.coradec.corabus.model.NetworkComponent;
import com.coradec.coracom.model.Request;
import com.coradec.coracore.annotation.Nullable;
import com.coradec.coracore.trouble.InitializationError;
import com.coradec.corasession.model.Session;
import com.coradec.coratext.model.Text;

import java.io.IOException;
import java.nio.channels.Selector;

/**
 * B​ase class of all network components.
 */
public abstract class AbstractNetworkComponent extends BasicHub implements NetworkComponent {

    private Selector selector;
    private final Text disconnectionText;

    public AbstractNetworkComponent(final Text disconnectionText) {
        this.disconnectionText = disconnectionText;
    }

    @Override protected @Nullable Request onInitialize(final Session session) {
        final @Nullable Request request = super.onInitialize(session);
        try {
            selector = Selector.open();
        } catch (IOException e) {
            throw new InitializationError(e);
        }
        return request;
    }

    protected Selector getSelector() {
        return selector;
    }

}
