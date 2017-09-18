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

package com.coradec.coragui.swing.model.impl;

import com.coradec.corabus.model.BusNode;
import com.coradec.coracore.annotation.NonNull;
import com.coradec.coracore.annotation.Nullable;
import com.coradec.coractrl.ctrl.impl.BasicAgent;
import com.coradec.coragui.model.Container;
import com.coradec.coragui.model.Gadget;
import com.coradec.coratext.model.LocalizedText;
import com.coradec.coratext.model.Text;

import java.net.URI;
import java.util.Optional;

/**
 * ​​Swing implementation of a gadget.
 *
 * @param <P> the peer type.
 */
@SuppressWarnings("ClassHasNoToStringMethod")
public class SwingGadget<P> extends BasicAgent implements Gadget<P> {

    private static final Text TEXT_EXPECTED_SWING_CONTAINER =
            LocalizedText.define("ExpectedSwingContainer");

    private final P peer;
    private final BusNode node;
    private final String name;
    private @Nullable SwingContainer parent;

    protected SwingGadget(final String id, final P peer, final BusNode node) {
        this.name = id;
        this.peer = peer;
        this.node = node;
    }

    @Override public P getPeer() {
        return peer;
    }

    @Override public String getRecipientId() {
        return node.getRecipientId();
    }

    @Override public URI toURI() {
        return node.toURI();
    }

    @Override public String represent() {
        return node.represent();
    }

    @Override public String getName() {
        return name;
    }

    @Override public Optional<Container> getParent() {
        return Optional.ofNullable(parent);
    }

    @Override public void setParent(@NonNull Container parent) {
        if (!(parent instanceof SwingContainer))
            throw new IllegalArgumentException(TEXT_EXPECTED_SWING_CONTAINER.resolve(parent));
        this.parent = (SwingContainer)parent;
    }
}
