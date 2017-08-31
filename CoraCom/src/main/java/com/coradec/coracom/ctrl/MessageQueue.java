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

package com.coradec.coracom.ctrl;

import com.coradec.coracom.model.Information;
import com.coradec.coracom.trouble.QueueException;

/**
 * ​A queue for messages.
 */
public interface MessageQueue {

    /**
     * Injects an information into the queue.
     *
     * @param info the information to inject.
     * @return the injected information.
     * @throws QueueException if the information could not be injected.
     */
    <I extends Information> I inject(I info) throws QueueException;

    /**
     * Subscribes the specified observer for information from the queue.
     *
     * @param observer the observer.
     */
    void subscribe(Observer observer);

    /**
     * Unsubscribes the specified observer for information from the queue.
     *
     * @param observer the observer to unsubscribe.
     */
    void unsubscribe(Observer observer);

    /**
     * Prevents the message queue from shutting down while an important process is running.
     */
    void preventShutdown();

    /**
     * Allows the message queue to shut down because an important process has finished.
     */
    void allowShutdown();

    /**
     * Returns the current value of the shutdown lock count.
     *
     * @return the shutdown lock count.
     */
    int getShutdownLockCount();

    /**
     * Forces the shutdown lock count to 0.
     */
    void clearShutdownLock();
}
