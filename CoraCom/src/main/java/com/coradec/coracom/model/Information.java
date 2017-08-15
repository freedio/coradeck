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

package com.coradec.coracom.model;

import com.coradec.coracom.state.QueueState;
import com.coradec.coracore.model.Origin;
import com.coradec.coracore.model.State;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * ​An general information about something.
 */
public interface Information extends Serializable {

    /**
     * Returns the exact timestamp of creation.
     *
     * @return the creation timestamp.
     */
    LocalDateTime getCreatedAt();

    /**
     * Returns the information processing state.
     * <p>
     * When the information is created, it is in state NEW.  During injection into the message
     * queue, it changes to ENQUEUED.  Once the message is picked up by a message processor, it
     * becomes DISPATCHED.  Just before being delivered to the recipients {@link
     * Recipient#onMessage(Message)}  method, it turns to DELIVERED.
     *
     * @return the information state.
     */
    State getState();

    /**
     * Returns the ID of the information.
     *
     * @return the information ID.
     */
    UUID getId();

    /**
     * Returns the origin of the information.
     *
     * @return the origin.
     */
    Origin getOrigin();

    /**
     * Callback invoked when the information gets enqueued.
     *
     * @throws IllegalStateException if the current state is not {@link QueueState#NEW}.
     */
    void onEnqueue() throws IllegalStateException;

    /**
     * Callback invoked when the information is dispatched from the queue.
     *
     * @throws IllegalStateException if the current state is not {@link QueueState#NEW}.
     */
    void onDispatch() throws IllegalStateException;

    /**
     * Callback invoked when the information gets delivered to a recipient.
     *
     * @throws IllegalStateException if the current state is not {@link QueueState#ENQUEUED}.
     */
    void onDeliver() throws IllegalStateException;

    /**
     * Resets the status of the information to NEW so that it can be injected again.
     *
     * @return this information, for method chaining.
     */
    Information renew();

}
