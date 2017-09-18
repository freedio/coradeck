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

import static com.coradec.coracore.model.Scope.*;

import com.coradec.corabus.model.BusNode;
import com.coradec.coracom.model.Request;
import com.coradec.coracore.annotation.Implementation;
import com.coradec.coracore.annotation.Register;
import com.coradec.coracore.trouble.UnimplementedOperationException;
import com.coradec.coragui.model.Coordinate;
import com.coradec.coragui.model.Extent;
import com.coradec.coragui.model.Gauge;
import com.coradec.coragui.model.Screen;
import com.coradec.coragui.swing.bus.impl.SwingScreenNode;

import java.awt.*;

/**
 * ​​Swing implementation of a screen.
 */
@Implementation(SINGLETON)
@Register(SwingGUI.class)
public class SwingScreen extends SwingGadget<Toolkit> implements Screen<Toolkit> {

    protected SwingScreen(final String id, final BusNode node) {
        super(id, Toolkit.getDefaultToolkit(), node);
    }

    public SwingScreen(final String id) {
        this(id, new SwingScreenNode());
    }

    @Override public int getLeft() {
        return 0;
    }

    @Override public Request setLeft(final int x) {
        throw new UnimplementedOperationException();
    }

    @Override public int getTop() {
        return 0;
    }

    @Override public Request setTop(final int y) {
        throw new UnimplementedOperationException();
    }

    @Override public int getWidth() {
        return getPeer().getScreenSize().width;
    }

    @Override public Request setWidth(final int w) {
        throw new UnimplementedOperationException();
    }

    @Override public int getHeight() {
        return getPeer().getScreenSize().height;
    }

    @Override public Request setHeight(final int h) {
        throw new UnimplementedOperationException();
    }

    @Override public Coordinate getOrigin() {
        return new SwingCoordinate(0, 0);
    }

    @Override public Request setOrigin(final Coordinate origin) {
        throw new UnimplementedOperationException();
    }

    @Override public Gauge getGauge() {
        return new SwingGauge(getPeer().getScreenSize());
    }

    @Override public Request setGauge(final Gauge gauge) {
        throw new UnimplementedOperationException();
    }

    @Override public Extent getExtent() {
        return new SwingExtent(getOrigin(), getGauge());
    }

    @Override public Request setExtent(final Extent extent) {
        throw new UnimplementedOperationException();
    }

    @Override public boolean isVisible() {
        return true;
    }

    @Override public Request setVisible(final boolean state) {
        throw new UnimplementedOperationException();
    }

}
