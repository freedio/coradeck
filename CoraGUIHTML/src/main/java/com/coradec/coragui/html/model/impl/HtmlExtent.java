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
import com.coradec.coragui.model.Extent;

/**
 * ​​HTML representation of an extent.
 */
public class HtmlExtent implements Extent {

    private final int top;
    private final int left;
    private final int height;
    private final int width;

    public HtmlExtent(final int top, final int left, final int height, final int width) {
        this.top = top;
        this.left = left;
        this.height = height;
        this.width = width;
    }

    @Override public int getTop() {
        return top;
    }

    @Override public int getLeft() {
        return left;
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
