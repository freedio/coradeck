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

import com.coradec.corabus.model.BusHub;
import com.coradec.coracom.model.Request;
import com.coradec.coracom.model.impl.BasicCommand;
import com.coradec.coragui.model.Container;
import com.coradec.coragui.model.Gadget;
import com.coradec.coratext.model.LocalizedText;
import com.coradec.coratext.model.Text;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * ​​Swing implementation of a container.
 */
@SuppressWarnings("ClassHasNoToStringMethod")
public class SwingContainer<P extends java.awt.Container> extends SwingWidget<P>
        implements Container<P> {

    private static final Text TEXT_EXPECTED_SWING_GADGET =
            LocalizedText.define("ExpectedSwingGadget");

    List<SwingGadget> elements = new ArrayList<>();

    protected SwingContainer(final String id, final P peer, final BusHub hub) {
        super(id, peer, hub);
        approve(AddGadgetCommand.class);
    }

    @Override public Request add(Gadget gadget) {
        if (!(gadget instanceof SwingGadget))
            throw new IllegalArgumentException(TEXT_EXPECTED_SWING_GADGET.resolve(gadget));
        //noinspection unchecked
        return inject(new AddGadgetCommand((SwingGadget<Component>)gadget));
    }

    @SuppressWarnings("ClassHasNoToStringMethod")
    private class AddGadgetCommand extends BasicCommand {

        private final SwingGadget<Component> gadget;

        public AddGadgetCommand(final SwingGadget<Component> gadget) {
            super(SwingContainer.this, SwingContainer.this);
            this.gadget = gadget;
        }

        @Override public void execute() {
            elements.add(gadget);
            gadget.setParent(SwingContainer.this);
            getPeer().add(gadget.getPeer());
        }

    }

}
