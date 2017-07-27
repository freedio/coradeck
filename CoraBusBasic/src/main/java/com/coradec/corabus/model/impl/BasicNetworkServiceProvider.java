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

import com.coradec.corabus.model.NetworkServiceProvider;
import com.coradec.corabus.view.BusService;
import com.coradec.corabus.view.NetworkService;
import com.coradec.corabus.view.impl.BasicServiceView;
import com.coradec.coracom.model.Information;
import com.coradec.coracom.model.Voucher;
import com.coradec.coracore.annotation.Nullable;
import com.coradec.coracore.trouble.ServiceNotAvailableException;
import com.coradec.coracore.util.StringUtil;
import com.coradec.corasession.model.Session;
import com.coradec.coratext.model.LocalizedText;
import com.coradec.coratext.model.Text;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

/**
 * ​​Basic implementation of a network service.
 */
public class BasicNetworkServiceProvider extends BasicServiceProviderHub
        implements NetworkServiceProvider {

    static final Text TEXT_CANNOT_DERIVE_SAD = LocalizedText.define("CannotDeriveSocketAddress");
    private static final Text TEXT_NETWORK_CLIENT_NOT_AVAILABLE =
            LocalizedText.define("NetworkClientNotAvailable");

//    private static Map<SocketAddress, Client> clientsBySocket;

    @Override public <S extends BusService> boolean provides(final Session session,
            final Class<? super S> type, final Object... args) {
        if (!type.isAssignableFrom(NetworkService.class)) return false;
        if (args.length > 0) {
            if (args[0] instanceof SocketAddress) return true;
            if (args.length > 1) {
                if (args[0] instanceof InetAddress && args[1] instanceof Integer) return true;
                if (args[0] instanceof String && args[1] instanceof Integer) return true;
            }
        }
        return false;
    }

    @Override
    public <S extends BusService> S getService(final Session session, final Class<? super S> type,
            final Object... args) throws ServiceNotAvailableException {
        if (!provides(session, type, args)) throw new ServiceNotAvailableException(type, args);
        //noinspection unchecked
        return (S)new BasicNetworkService(session, args);
    }

    void createConnection(final BasicNetworkService connector, final SocketAddress socketAddress) {

    }

    @SuppressWarnings("ClassHasNoToStringMethod")
    private class BasicNetworkService extends BasicServiceView implements NetworkService {

        private final SocketAddress socketAddress;

        BasicNetworkService(final Session session, final Object... args) {
            super(session);
            SocketAddress sad = null;
            if (args.length > 0) {
                if (args[0] instanceof SocketAddress) sad = (SocketAddress)args[0];
                if (args.length > 1) {
                    if (args[0] instanceof InetAddress && args[1] instanceof Integer)
                        sad = new InetSocketAddress((InetAddress)args[0], (Integer)args[1]);
                    else if (args[0] instanceof String && args[1] instanceof Integer)
                        sad = new InetSocketAddress((String)args[0], (Integer)args[1]);
                }
            }
            if (sad == null) throw new IllegalArgumentException(
                    TEXT_CANNOT_DERIVE_SAD.resolve(StringUtil.toString(args)));
            registerService(this);
            createConnection(this, socketAddress = sad);
        }

        @Override public void send(final Information info) {
//            inject()
        }

        @Nullable @Override public Voucher<Information> receive() {
            return null; // TODO
        }
    }

}
