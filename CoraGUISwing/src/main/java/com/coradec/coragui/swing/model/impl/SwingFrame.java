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
import com.coradec.coraconf.model.ValueMap;
import com.coradec.coracore.annotation.Implementation;
import com.coradec.coracore.annotation.Register;
import com.coradec.coradoc.model.Style;
import com.coradec.coragui.model.Frame;
import com.coradec.coragui.swing.bus.impl.SwingFrameNode;

import javax.swing.*;

/**
 * ​​Swing implementation of a frame.
 */
@Implementation
@Register(SwingGUI.class)
public class SwingFrame extends SwingContainer<JFrame> implements Frame<JFrame> {

    protected SwingFrame(final ValueMap attributes, BusHub hub) {
        super(attributes, new JFrame(), hub);
    }

    public SwingFrame(final ValueMap attributes) {
        this(attributes, new SwingFrameNode());
    }

    @Override public void discard() {
        getPeer().dispose();
    }

    @Override protected void setupStyle(final Style style) {
        getAttributes().lookup("title").ifPresent(text -> getPeer().setTitle(text));
        getPeer().pack();
        super.setupStyle(style);
    }

}
