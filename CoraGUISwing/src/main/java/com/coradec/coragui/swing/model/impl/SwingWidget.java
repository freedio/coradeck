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
import com.coradec.coracom.model.Request;
import com.coradec.coracom.model.impl.BasicCommand;
import com.coradec.coragui.com.SetExtentCommand;
import com.coradec.coragui.com.SetGaugeCommand;
import com.coradec.coragui.com.SetHeightCommand;
import com.coradec.coragui.com.SetLeftCommand;
import com.coradec.coragui.com.SetOriginCommand;
import com.coradec.coragui.com.SetTopCommand;
import com.coradec.coragui.com.SetVisibilityCommand;
import com.coradec.coragui.com.SetWidthCommand;
import com.coradec.coragui.model.Coordinate;
import com.coradec.coragui.model.Extent;
import com.coradec.coragui.model.Gauge;
import com.coradec.coragui.model.Widget;

import java.awt.*;
import java.util.Map;

/**
 * Swing implementation of a widget.​​
 */
public class SwingWidget<P extends Component> extends SwingGadget<P> implements Widget<P> {

    protected SwingWidget(final String id, final P peer, final BusNode node) {
        super(id, peer, node);
        approve(SetPropertyCommand.class);
    }

    @Override public int getLeft() {
        return getPeer().getX();
    }

    @Override public Request setLeft(final int x) {
        return inject(new InternalSetLeftCommand(x));
    }

    @Override public int getTop() {
        return getPeer().getY();
    }

    @Override public Request setTop(final int y) {
        return inject(new InternalSetTopCommand(y));
    }

    @Override public int getWidth() {
        return getPeer().getWidth();
    }

    @Override public Request setWidth(final int w) {
        return inject(new InternalSetWidthCommand(w));
    }

    @Override public int getHeight() {
        return getPeer().getHeight();
    }

    @Override public Request setHeight(final int h) {
        return inject(new InternalSetHeightCommand(h));
    }

    @Override public Coordinate getOrigin() {
        return toCoordinate(getPeer().getLocation());
    }

    @Override public Request setOrigin(final Coordinate origin) {
        return inject(new InternalSetOriginCommand(origin));
    }

    @Override public Gauge getGauge() {
        return toGauge(getPeer().getSize());
    }

    @Override public Request setGauge(final Gauge gauge) {
        return inject(new InternalSetGaugeCommand(gauge));
    }

    @Override public Extent getExtent() {
        return toExtent(getPeer().getBounds());
    }

    @Override public Request setExtent(final Extent extent) {
        return inject(new InternalSetExtentCommand(extent));
    }

    @Override public boolean isVisible() {
        return getPeer().isVisible();
    }

    @Override public Request setVisible(final boolean state) {
        return inject(new InternalSetVisibilityCommand(state));
    }

    private Coordinate toCoordinate(final Point location) {
        return new SwingCoordinate(location);
    }

    Point fromCoordinate(final Coordinate origin) {
        return new Point(origin.getLeft(), origin.getTop());
    }

    private Gauge toGauge(final Dimension size) {
        return new SwingGauge(size);
    }

    Dimension fromGauge(final Gauge gauge) {
        return new Dimension(gauge.getWidth(), gauge.getHeight());
    }

    private Extent toExtent(final Rectangle bounds) {
        return new SwingExtent(bounds);
    }

    Rectangle fromExtent(final Extent extent) {
        return new Rectangle(extent.getLeft(), extent.getTop(), extent.getWidth(),
                extent.getHeight());
    }

    private abstract class SetPropertyCommand extends BasicCommand {

        public SetPropertyCommand() {
            super(SwingWidget.this, SwingWidget.this);
        }

        SetPropertyCommand(final Map<String, Object> properties) {
            super(properties);
        }

    }

    @SuppressWarnings("ClassHasNoToStringMethod")
    private class InternalSetLeftCommand extends SetPropertyCommand implements SetLeftCommand {

        private final int left;

        InternalSetLeftCommand(final int left) {
            this.left = left;
        }

        @Override public void execute() {
            final P peer = getPeer();
            peer.setLocation(left, peer.getY());
        }

    }

    @SuppressWarnings("ClassHasNoToStringMethod")
    private class InternalSetTopCommand extends SetPropertyCommand implements SetTopCommand {

        private final int top;

        InternalSetTopCommand(final int top) {
            this.top = top;
        }

        @Override public void execute() {
            final P peer = getPeer();
            peer.setLocation(peer.getX(), top);
        }

    }

    @SuppressWarnings("ClassHasNoToStringMethod")
    private class InternalSetWidthCommand extends SetPropertyCommand implements SetWidthCommand {

        private final int w;

        InternalSetWidthCommand(final int w) {
            this.w = w;
        }

        @Override public void execute() {
            final P peer = getPeer();
            peer.setSize(w, peer.getHeight());
        }

    }

    @SuppressWarnings("ClassHasNoToStringMethod")
    private class InternalSetHeightCommand extends SetPropertyCommand implements SetHeightCommand {

        private final int h;

        InternalSetHeightCommand(final int h) {
            this.h = h;
        }

        @Override public void execute() {
            final P peer = getPeer();
            peer.setSize(peer.getWidth(), h);
        }

    }

    @SuppressWarnings("ClassHasNoToStringMethod")
    private class InternalSetVisibilityCommand extends SetPropertyCommand
            implements SetVisibilityCommand {

        private final boolean state;

        InternalSetVisibilityCommand(final boolean state) {
            this.state = state;
        }

        @Override public void execute() {
            getPeer().setVisible(state);
        }

    }

    @SuppressWarnings("ClassHasNoToStringMethod")
    private class InternalSetOriginCommand extends SetPropertyCommand implements SetOriginCommand {

        private final Coordinate origin;

        InternalSetOriginCommand(final Coordinate origin) {
            this.origin = origin;
        }

        @Override public void execute() {
            getPeer().setLocation(fromCoordinate(origin));
        }

    }

    @SuppressWarnings("ClassHasNoToStringMethod")
    private class InternalSetGaugeCommand extends SetPropertyCommand implements SetGaugeCommand {

        private final Gauge gauge;

        InternalSetGaugeCommand(final Gauge gauge) {
            this.gauge = gauge;
        }

        @Override public void execute() {
            getPeer().setSize(fromGauge(gauge));
        }

    }

    @SuppressWarnings("ClassHasNoToStringMethod")
    private class InternalSetExtentCommand extends SetPropertyCommand implements SetExtentCommand {

        private final Extent extent;

        InternalSetExtentCommand(final Extent extent) {
            this.extent = extent;
        }

        @Override public void execute() {
            getPeer().setBounds(fromExtent(extent));
        }

    }

}
