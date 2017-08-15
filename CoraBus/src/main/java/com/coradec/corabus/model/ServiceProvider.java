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

import com.coradec.corabus.view.BusService;
import com.coradec.coracore.trouble.ServiceNotAvailableException;
import com.coradec.corasession.model.Session;

/**
 * ​A bus node providing a service.
 */
public interface ServiceProvider extends BusNode {

    /**
     * Checks if the provider provides a service of the specified type with the specified
     * parameters.
     *
     * @param session the session context.
     * @param type    the type.
     * @param args    parameters to select the service.
     * @return {@code true} if the provider provides this type of service, {@code false} if not.
     */
    <S extends BusService> boolean provides(final Session session, Class<? super S> type,
            Object... args);

    /**
     * Returns the service of the specified type with the specified service parameters.
     *
     * @param <S>     the service type.
     * @param session the session context.
     * @param type    the service type selector.
     * @param args    the service parameters.   @return a service of the specified type.
     */
    <S extends BusService> S getService(final Session session, Class<? super S> type,
            Object... args) throws ServiceNotAvailableException;

}
