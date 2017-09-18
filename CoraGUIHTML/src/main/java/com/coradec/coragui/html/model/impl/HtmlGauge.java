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

import com.coradec.coracore.util.ClassUtil;
import com.coradec.coragui.model.Gauge;

/**
 * ​​HTML representation of a gauge.
 */
public class HtmlGauge implements Gauge {

    private final int height;
    private final int width;

    public HtmlGauge(final int height, final int width) {
        this.height = height;
        this.width = width;
    }

    @Override public int getHeight() {
        return height;
    }

    @Override public int getWidth() {
        return width;
    }

    @Override public String toString() {
        return ClassUtil.toString(this);
    }
}
