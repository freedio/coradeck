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

import com.coradec.corabus.view.BusService;
import com.coradec.corabus.view.EchoService;
import com.coradec.coracore.trouble.ServiceNotAvailableException;
import com.coradec.corasession.model.Session;
import com.coradec.corasession.view.impl.BasicView;

/**
 * Provides the EchoService.
 */
public class EchoServiceProvider extends BasicServiceProvider {

    @Override public <S extends BusService> boolean provides(final Session session,
            final Class<? super S> type, final Object... args) {
        return EchoService.class.isAssignableFrom(type);
    }

    @Override
    public <S extends BusService> S getService(final Session session, final Class<? super S> type,
            final Object... args) throws ServiceNotAvailableException {
        if (!provides(session, type, args)) throw new ServiceNotAvailableException(type, args);
        //noinspection unchecked
        return (S)new BasicEchoService(session);
    }

    private class BasicEchoService extends BasicView implements EchoService {

        public BasicEchoService(final Session session) {
            super(session);
        }

        @Override public String echo(final String input) {
            return "hello".equalsIgnoreCase(input) ? "World" : input.toUpperCase();
        }
    }

}
