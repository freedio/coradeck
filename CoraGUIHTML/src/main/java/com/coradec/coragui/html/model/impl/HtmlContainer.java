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

package com.coradec.coragui.html.model.impl;

import com.coradec.corabus.model.BusNode;
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
 * ​​HTML implementation of a container.
 */
@SuppressWarnings("ClassHasNoToStringMethod")
public class HtmlContainer<P> extends HtmlWidget<P> implements Container<P> {

    private static final Text TEXT_EXPECTED_HTML_GADGET =
            LocalizedText.define("ExpectedHtmlGadget");

    List<HtmlGadget> elements = new ArrayList<>();

    protected HtmlContainer(final String id, final P peer, final BusNode node) {
        super(id, peer, node);
    }

    @Override public Request add(final Gadget gadget) {
        if (!(gadget instanceof HtmlGadget))
            throw new IllegalArgumentException(TEXT_EXPECTED_HTML_GADGET.resolve(gadget));
        //noinspection unchecked
        return inject(new AddGadgetCommand((HtmlGadget<Component>)gadget));
    }

    @SuppressWarnings("ClassHasNoToStringMethod")
    private class AddGadgetCommand extends BasicCommand {

        private final HtmlGadget<Component> gadget;

        public AddGadgetCommand(final HtmlGadget<Component> gadget) {
            super(HtmlContainer.this, HtmlContainer.this);
            this.gadget = gadget;
        }

        @Override public void execute() {
            elements.add(gadget);
            gadget.setParent(HtmlContainer.this);
//            getPeer().add(gadget.getPeer());
        }

    }

}
