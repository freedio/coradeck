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

import com.coradec.corabus.protocol.ProtocolHandler;
import com.coradec.coracom.model.ParallelMultiRequest;
import com.coradec.coracom.model.Request;
import com.coradec.coraconf.model.Property;
import com.coradec.coracore.annotation.Inject;
import com.coradec.coracore.annotation.Nullable;
import com.coradec.coracore.model.Factory;
import com.coradec.coracore.model.GenericType;
import com.coradec.corasession.model.Session;

import java.util.Collections;
import java.util.List;

/**
 * ​​The network component of the bus.
 */
public class Network extends BasicHub {

    private static final Property<List<String>> PROP_ENABLED_PROTOCOLS =
            Property.define("EnabledProtocols", GenericType.of(List.class, String.class),
                    Collections.singletonList("CMP"));
    @Inject private static Factory<ParallelMultiRequest> MULTIREQUEST;

    @Override protected @Nullable Request onInitialize(final Session session) {
        final Request request = super.onInitialize(session);
        PROP_ENABLED_PROTOCOLS.value().forEach(proto -> {
            add(session, proto + "-handler", createHandler(proto));
            add(session, proto + "-server", new NetworkServer(proto));
        });
        add(session, "client", new NetworkClient());
        return request;
    }

    private ProtocolHandler createHandler(final String protocol) {
        return ProtocolHandler.fore(protocol);
    }

}
