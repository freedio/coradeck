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

/**
 * ​An object that observes (and gets notified about) state changes in another object.
 */
public interface Observer {

    /**
     * Notifies the observer of the specified information.
     * <p>
     * <span style="color:yellow; background:red">Important Note:</span> Notifications are
     * asynchronous by virtue and thus can subtly infect an otherwise clean environment with
     * concurrency.  This will not be a problem if you strictly keep from making modifications on
     * the caller thread, instead using the message queue to trigger modifications on a message
     * processor thread.
     *
     * @param info the information.
     * @return {@code true} if the observer is to be removed after the notification ("one-shot
     * trigger"), {@code false} if it continues to observe.
     */
    boolean notify(Information info);

    /**
     * Checks if the observer wants the specified information.  This can be a very cheap operation
     * as opposed to marshal an information and sending it over the wire only to find that the
     * observer is not interested in this kind of info.
     *
     * @param info the information.
     * @return {@code true} if the observer should be notified about the information.
     */
    boolean wants(Information info);

}
