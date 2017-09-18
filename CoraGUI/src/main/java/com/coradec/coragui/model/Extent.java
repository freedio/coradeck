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

/**
 * ​Extent of a widget (sometimes also referred to as bounds).
 */
public interface Extent {

    /**
     * Returns the vertical position of the widget's origin.
     *
     * @return the vertical position.
     */
    int getTop();

    /**
     * Returns the horizontal position of the widget's origin.
     *
     * @return the horizontal position.
     */
    int getLeft();

    /**
     * Returns the height of the widget.
     *
     * @return the height.
     */
    int getHeight();

    /**
     * Returns the width of the widget.
     *
     * @return the width.
     */
    int getWidth();

}
