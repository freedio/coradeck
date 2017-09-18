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
import com.coradec.coracore.util.ClassUtil;
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

import java.util.Map;

/**
 * ​​HTML implementation of a widget.
 */
public class HtmlWidget<P> extends HtmlGadget<P> implements Widget<P> {

    int left;
    int top;
    int width;
    int height;
    boolean visible;

    protected HtmlWidget(final String id, final P peer, final BusNode node) {
        super(id, peer, node);
    }

    @Override public int getLeft() {
        return left;
    }

    @Override public Request setLeft(final int x) {
        return inject(new InternalSetLeftCommand(x));
    }

    @Override public int getTop() {
        return top;
    }

    @Override public Request setTop(final int y) {
        return inject(new InternalSetTopCommand(y));
    }

    @Override public int getWidth() {
        return width;
    }

    @Override public Request setWidth(final int w) {
        return inject(new InternalSetWidthCommand(w));
    }

    @Override public int getHeight() {
        return height;
    }

    @Override public Request setHeight(final int h) {
        return inject(new InternalSetHeightCommand(h));
    }

    @Override public Coordinate getOrigin() {
        return new HtmlCoordinate(top, left);
    }

    @Override public Request setOrigin(final Coordinate origin) {
        return inject(new InternalSetOriginCommand(origin));
    }

    @Override public Gauge getGauge() {
        return new HtmlGauge(height, width);
    }

    @Override public Request setGauge(final Gauge gauge) {
        return inject(new InternalSetGaugeCommand(gauge));
    }

    @Override public Extent getExtent() {
        return new HtmlExtent(top, left, height, width);
    }

    @Override public Request setExtent(final Extent extent) {
        return inject(new InternalSetExtentCommand(extent));
    }

    @Override public boolean isVisible() {
        return visible;
    }

    @Override public Request setVisible(final boolean state) {
        return inject(new InternalSetVisibilityCommand(state));
    }

    private abstract class SetPropertyCommand extends BasicCommand {

        public SetPropertyCommand() {
            super(HtmlWidget.this, HtmlWidget.this);
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
            HtmlWidget.this.left = left;
        }

    }

    @SuppressWarnings("ClassHasNoToStringMethod")
    private class InternalSetTopCommand extends SetPropertyCommand implements SetTopCommand {

        private final int top;

        public InternalSetTopCommand(final int top) {
            this.top = top;
        }

        @Override public void execute() {
            HtmlWidget.this.top = top;
        }
    }

    @SuppressWarnings("ClassHasNoToStringMethod")
    private class InternalSetWidthCommand extends SetPropertyCommand implements SetWidthCommand {

        private final int width;

        public InternalSetWidthCommand(final int width) {
            this.width = width;
        }

        @Override public void execute() {
            HtmlWidget.this.width = width;
        }

    }

    @SuppressWarnings("ClassHasNoToStringMethod")
    private class InternalSetHeightCommand extends SetPropertyCommand implements SetHeightCommand {

        private final int height;

        public InternalSetHeightCommand(final int height) {
            this.height = height;
        }

        @Override public void execute() {
            HtmlWidget.this.height = height;
        }

    }

    @SuppressWarnings("ClassHasNoToStringMethod")
    private class InternalSetOriginCommand extends SetPropertyCommand implements SetOriginCommand {

        private final Coordinate origin;

        public InternalSetOriginCommand(final Coordinate origin) {
            this.origin = origin;
        }

        @Override public void execute() {
            HtmlWidget.this.top = origin.getTop();
            HtmlWidget.this.left = origin.getLeft();
        }

    }

    @SuppressWarnings("ClassHasNoToStringMethod")
    private class InternalSetGaugeCommand extends SetPropertyCommand implements SetGaugeCommand {

        private final Gauge gauge;

        public InternalSetGaugeCommand(final Gauge gauge) {
            this.gauge = gauge;
        }

        @Override public void execute() {
            HtmlWidget.this.height = gauge.getHeight();
            HtmlWidget.this.width = gauge.getWidth();
        }

    }

    @SuppressWarnings("ClassHasNoToStringMethod")
    private class InternalSetExtentCommand extends SetPropertyCommand implements SetExtentCommand {

        private final Extent extent;

        public InternalSetExtentCommand(final Extent extent) {
            this.extent = extent;
        }

        @Override public void execute() {
            HtmlWidget.this.top = extent.getTop();
            HtmlWidget.this.left = extent.getLeft();
            HtmlWidget.this.height = extent.getHeight();
            HtmlWidget.this.width = extent.getWidth();
        }
    }

    @SuppressWarnings("ClassHasNoToStringMethod")
    private class InternalSetVisibilityCommand extends SetPropertyCommand
            implements SetVisibilityCommand {

        private final boolean state;

        public InternalSetVisibilityCommand(final boolean state) {
            this.state = state;
        }

        @Override public void execute() {
            visible = state;
        }

    }

    @Override public String toString() {
        return ClassUtil.toString(this);
    }
}
