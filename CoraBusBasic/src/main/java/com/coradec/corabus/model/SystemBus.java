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

package com.coradec.corabus.model;

import com.coradec.corabus.model.impl.BasicSystemBus;
import com.coradec.coractrl.com.StartStateMachineRequest;
import com.coradec.corasession.model.Session;

/**
 * ​API of the system bus.
 */
public interface SystemBus extends BusHub {

    /**
     * Creates a suitable system bus.
     *
     * @return the system bus.
     */
    static SystemBus create() {
        return BasicSystemBus.create();
    }

    /**
     * Shuts the system bus down in the context of the specified session.
     *
     * @param session the session context.
     */
    StartStateMachineRequest shutdown(Session session);
}
