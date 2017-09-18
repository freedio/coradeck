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

import com.coradec.coracore.annotation.ToString;
import com.coradec.coracore.util.ClassUtil;
import com.coradec.coragui.model.Coordinate;
import com.coradec.coragui.model.Extent;
import com.coradec.coragui.model.Gauge;

import java.awt.*;

/**
 * ​​Swing implementation of an extent.
 */
public class SwingExtent implements Extent {

    private final int top;
    private final int left;
    private final int height;
    private final int width;

    public SwingExtent(final Rectangle bounds) {
        top = bounds.y;
        left = bounds.x;
        height = bounds.height;
        width = bounds.width;
    }

    public SwingExtent(final Coordinate origin, final Gauge gauge) {
        top = origin.getTop();
        left = origin.getLeft();
        height = gauge.getHeight();
        width = gauge.getWidth();
    }

    @Override @ToString public int getTop() {
        return top;
    }

    @Override @ToString public int getLeft() {
        return left;
    }

    @Override @ToString public int getHeight() {
        return height;
    }

    @Override @ToString public int getWidth() {
        return width;
    }

    @Override public String toString() {
        return ClassUtil.toString(this);
    }
}
