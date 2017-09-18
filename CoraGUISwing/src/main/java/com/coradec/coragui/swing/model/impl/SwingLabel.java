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
import com.coradec.coracore.annotation.Implementation;
import com.coradec.coracore.annotation.Register;
import com.coradec.coragui.model.Label;
import com.coradec.coragui.swing.bus.impl.SwingLabelNode;

import javax.swing.*;

/**
 * ​​Swing implementation of a label.
 */
@SuppressWarnings("ClassHasNoToStringMethod")
@Implementation
@Register(SwingGUI.class)
public class SwingLabel extends SwingWidget<JLabel> implements Label<JLabel> {

    protected SwingLabel(final String id, final BusNode node) {
        super(id, new JLabel(), node);
    }

    public SwingLabel(final String id) {
        this(id, new SwingLabelNode());
    }

}
