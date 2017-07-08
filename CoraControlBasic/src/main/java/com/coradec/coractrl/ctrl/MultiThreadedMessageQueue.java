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

package com.coradec.coractrl.ctrl;

import com.coradec.coracom.ctrl.MessageQueue;

/**
 * ​A message queue running multiple threads to dispatch messages.
 */
public interface MultiThreadedMessageQueue extends MessageQueue {

    /**
     * Returns the minimum number of threads running, a.k.a. low water mark.
     *
     * @return the low water mark.
     */
    int getLowWaterMark();

    /**
     * Returns the maximum number of threads running, a.k.a. high water mark.
     *
     * @return the high water mark.
     */
    int getHighWaterMark();

    /**
     * Returns the maximum number of processors used.
     *
     * @return the maximum number of processors used.
     */
    int getMaxUsed();

    /**
     * Resets the maximum number of processors used.
     */
    void resetUsage();
}
