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

package com.coradec.corabus.com.impl;

import com.coradec.coracom.model.Message;
import com.coradec.coracom.model.Recipient;
import com.coradec.coracore.annotation.Inject;
import com.coradec.coracore.model.Factory;
import com.coradec.coracore.trouble.UnimplementedOperationException;
import com.coradec.coracore.util.ClassUtil;
import com.coradec.coradir.model.TranscendentPath;
import com.coradec.coralog.ctrl.impl.Logger;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * ​​Implementation of a "fake" recipient from an URI representing a node on another network
 * segment.
 */
public class ExternalRecipient extends Logger implements Recipient {

    @Inject private static Factory<TranscendentPath> PATH;

    private final URI uri;

    public ExternalRecipient(final String hostName, final URI uri) {
        if (uri.getHost() == null) try {
            this.uri = new URI(uri.getScheme(), uri.getUserInfo(), hostName, uri.getPort(),
                    uri.getPath(), uri.getQuery(), uri.getFragment());
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(String.format("Invalid URI: %s", uri), e);
        }
        else this.uri = uri;
        debug("External recipient with URI %s", this.uri);
    }

    @Override public void onMessage(final Message message) {
        throw new UnimplementedOperationException();
    }

    @Override public String getRecipientId() {
        return PATH.create(uri).represent();
    }

    @Override public String toString() {
        return ClassUtil.toString(this);
    }
}
