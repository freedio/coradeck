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
import com.coradec.coraconf.model.ValueMap;
import com.coradec.coracore.annotation.Nullable;
import com.coradec.coradoc.cssenum.CssUnit;
import com.coradec.coradoc.cssenum.Distance;
import com.coradec.coradoc.model.IntegerMeasure;
import com.coradec.coradoc.model.Style;
import com.coradec.coradoc.token.IntegerDimension;
import com.coradec.coradoc.token.IntegerPercentage;
import com.coradec.coragui.com.SetExtentCommand;
import com.coradec.coragui.com.SetGaugeCommand;
import com.coradec.coragui.com.SetHeightCommand;
import com.coradec.coragui.com.SetLeftCommand;
import com.coradec.coragui.com.SetOriginCommand;
import com.coradec.coragui.com.SetTopCommand;
import com.coradec.coragui.com.SetVisibilityCommand;
import com.coradec.coragui.com.SetWidthCommand;
import com.coradec.coragui.com.SetupCommand;
import com.coradec.coragui.model.Coordinate;
import com.coradec.coragui.model.Extent;
import com.coradec.coragui.model.Gauge;
import com.coradec.coragui.model.Widget;

import java.awt.*;
import java.util.Map;

/**
 * Swing implementation of a widget.​​
 */
@SuppressWarnings("ClassHasNoToStringMethod")
public class SwingWidget<P extends Component> extends SwingGadget<P> implements Widget<P> {

    private SwingGUI gui;
    private Style style;

    protected SwingWidget(final ValueMap attributes, final P peer, final BusNode node) {
        super(attributes, peer, node);
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

    void setGUI(SwingGUI gui) {
        this.gui = gui;
        gui.getStyle(this).andThen(this::startSetup);
    }

    private void startSetup(Style style) {
        inject(new InternalSetupCommand(style));
    }

    /**
     * Asynchronously sets up the widget's properties from the specified style.
     * <p>
     * Subclasses are supposed to wrap this method early (i.e. invoke the super method as early as
     * possible) and then do their own local setup.
     *
     * @param style the style.
     */
    protected void setupStyle(final Style style) {
        setExtent(new SwingExtent(calcTop(style.getTop(), style.getTopValue()),
                calcLeft(style.getLeft(), style.getLeftValue()),
                calcHeight(style.getHeight(), style.getHeightValue()),
                calcWidth(style.getWidth(), style.getWidthValue())));
    }

    private int calcTop(final Distance top, final @Nullable IntegerMeasure topValue) {
        int result = 0;
        switch (top) {
            case AUTO:
            case INITIAL:
                result = getPeer().getY();
                break;
            case INHERIT:
                result = getParentTop();
                break;
            case EXPLICIT:
                if (topValue instanceof IntegerPercentage)
                    result = (int)(getParentHeight() * topValue.getValue() / 100.0);
                else if (topValue instanceof IntegerDimension)
                    result = dim((IntegerDimension)topValue);
                else if (topValue != null) result = topValue.getValue();
                break;
        }
        return result;
    }

    private int calcLeft(final Distance left, final @Nullable IntegerMeasure leftValue) {
        int result = 0;
        switch (left) {
            case AUTO:
            case INITIAL:
                result = getPeer().getX();
                break;
            case INHERIT:
                result = getParentLeft();
                break;
            case EXPLICIT:
                if (leftValue instanceof IntegerPercentage)
                    result = (int)(getParentWidth() * leftValue.getValue() / 100.0);
                else if (leftValue instanceof IntegerDimension)
                    result = dim((IntegerDimension)leftValue);
                else if (leftValue != null) result = leftValue.getValue();
                break;
        }
        return result;
    }

    private int calcHeight(final Distance height, final @Nullable IntegerMeasure heightValue) {
        int result = 0;
        switch (height) {
            case AUTO:
            case INITIAL:
                result = getPeer().getHeight();
                break;
            case INHERIT:
                result = getParentHeight();
                break;
            case EXPLICIT:
                if (heightValue instanceof IntegerPercentage)
                    result = (int)(getParentHeight() * heightValue.getValue() / 100.0);
                else if (heightValue instanceof IntegerDimension)
                    result = dim((IntegerDimension)heightValue);
                else if (heightValue != null) result = heightValue.getValue();
                break;
        }
        return result;
    }

    private int calcWidth(final Distance width, final @Nullable IntegerMeasure widthValue) {
        int result = 0;
        switch (width) {
            case AUTO:
            case INITIAL:
                result = getPeer().getWidth();
                break;
            case INHERIT:
                result = getParentWidth();
                break;
            case EXPLICIT:
                if (widthValue instanceof IntegerPercentage)
                    result = (int)(getParentWidth() * widthValue.getValue() / 100.0);
                else if (widthValue instanceof IntegerDimension)
                    result = dim((IntegerDimension)widthValue);
                else if (widthValue != null) result = widthValue.getValue();
                break;
        }
        return result;
    }

    private int getParentTop() {
        return getParent().map(Widget::getTop).orElseGet(() -> gui.getScreen().getTop());
    }

    private int getParentLeft() {
        return getParent().map(Widget::getLeft).orElseGet(() -> gui.getScreen().getLeft());
    }

    private int getParentHeight() {
        return getParent().map(Widget::getHeight).orElseGet(() -> gui.getScreen().getHeight());
    }

    private int getParentWidth() {
        return getParent().map(Widget::getWidth).orElseGet(() -> gui.getScreen().getWidth());
    }

    private int dim(final IntegerDimension width) {
        return width.getValue() * unitToPixels(width.getUnit());
    }

    private int unitToPixels(final CssUnit unit) {
        switch (unit) {
            case em:
                return 1;
            case ex:
                return 1;
            case px:
                return 1;
            case cm:
                return 1;
            case mm:
                return 1;
            case in:
                return 1;
            case pt:
                return 1;
            case pc:
                return 1;
            case ch:
                return 1;
            case rem:
                return 1;
            case vh:
                return 1;
            case vw:
                return 1;
            case vmin:
                return 1;
            case vmax:
                return 1;
            default:
                return 1;
        }
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

    private class InternalSetupCommand extends SetPropertyCommand implements SetupCommand {

        private final Style style;

        public InternalSetupCommand(final Style style) {
            this.style = style;
        }

        @Override public void execute() {
            setupStyle(style);
        }
    }
}
