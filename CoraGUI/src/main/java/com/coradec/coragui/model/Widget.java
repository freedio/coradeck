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

package com.coradec.coragui.model;

import com.coradec.coracom.model.Request;

/**
 * ​A visible graphical component.
 */
public interface Widget<P> extends Gadget<P> {

    String TOP = "Top";
    String LEFT = "Left";
    String WIDTH = "Width";
    String HEIGHT = "Height";
    String VISIBLE = "Visible";
    String COORDINATE = "Coordinate";
    String GAUGE = "Gauge";
    String EXTENT = "Extent";

    /**
     * Returns the x coordinate of the origin in the context of the specified session.
     *
     * @return the x coordinate.
     */
    int getLeft();

    /**
     * Sets the x coordinate of the widget to the specified value in pixels..
     *
     * @param x the value to set.
     * @return a request to track progress of the operation.
     */
    Request setLeft(int x);

    /**
     * Returns the y coordinate of the origin.
     *
     * @return the y coordinate.
     */
    int getTop();

    /**
     * Sets the y coordinate of the widget to the specified value in pixels.
     *
     * @param y the value to set.
     * @return a request to track progress of the operation.
     */
    Request setTop(int y);

    /**
     * Returns the width of the widget.
     *
     * @return the width.
     */
    int getWidth();

    /**
     * Sets the width of the widget to the specified value in pixels.
     *
     * @param w the value to set.
     * @return a request to track progress of the operation.
     */
    Request setWidth(int w);

    /**
     * Returns the height of the widget.
     *
     * @return the height.
     */
    int getHeight();

    /**
     * Sets the height of the widget to the specified value in pixels.
     *
     * @param h the value to set.
     * @return a request to track progress of the operation.
     */
    Request setHeight(int h);

    /**
     * Returns the coordinate of the widget's origin.
     *
     * @return the coordinate of the origin.
     */
    Coordinate getOrigin();

    /**
     * Sets the coordinate of the widget's origin to the specified value.
     *
     * @param origin the value to set.
     * @return a request to track progress of the operation.
     */
    Request setOrigin(Coordinate origin);

    /**
     * Returns the gauge of the widget.
     *
     * @return the extent.
     */
    Gauge getGauge();

    /**
     * Sets the widget's gauge to the specified value.
     *
     * @param gauge the value to set.
     * @return a request to track progress of the operation.
     */
    Request setGauge(Gauge gauge);

    /**
     * Returns the extent of the widget.
     *
     * @return the extent.
     */
    Extent getExtent();

    /**
     * Sets the widget's extent to the specified value.
     *
     * @param extent the value to set.
     * @return a request to track progress of the operation.
     */
    Request setExtent(Extent extent);

    /**
     * Checks in the context of the specified session if the widget is visible.
     *
     * @return {@code true} if the widget is visible, {@code false} if not.
     */
    boolean isVisible();

    /**
     * Sets the visibility of the widget to the specified value.
     *
     * @param state the state to set.
     * @return a request to track progress of the operation.
     */
    Request setVisible(boolean state);

}
