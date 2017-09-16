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

package com.coradec.coracom.model.impl;

import com.coradec.coracom.model.Command;
import com.coradec.coracom.model.Recipient;
import com.coradec.coracore.model.Origin;

import java.util.Map;

/**
 * ​​Basic implementation of a command.
 */
public abstract class BasicCommand extends BasicRequest implements Command {

    /**
     * Initializes a new instance of BasicCommand with the specified sender and recipient.
     *
     * @param sender    the sender.
     * @param recipient the recipient.
     */
    protected BasicCommand(final Origin sender, final Recipient recipient) {
        super(sender, recipient);
    }

    /**
     * Initializes a new instance of BasicMessage from the specified property map.
     *
     * @param properties the property map.
     */
    public BasicCommand(final Map<String, Object> properties) {
        super(properties);
    }

}
