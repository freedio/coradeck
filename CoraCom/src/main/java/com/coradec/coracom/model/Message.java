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
import com.coradec.coracore.model.State;

import java.util.Set;
import java.util.UUID;

/**
 * An object sent from a sender to a list of recipients.  If the list of recipients is empty, the
 * message will either be delivered regularly to the sender, if it is also a recipient, or bounce to
 * the sender.  Messages without a valid sender should and will normally be dropped with a log
 * entry.
 */
public interface Message {

    /**
     * Returns the message state.
     *
     * @return the message state.
     */
    State getState();

    /**
     * Returns the set of recipients.  The empty set denotes a broadcast message.
     *
     * @return the set of recipients.
     */
    Set<Recipient> getRecipients();

    /**
     * Returns the recipients as an array.  The empty array denotes a broadcast message.
     *
     * @return the recipient list.
     */
    Recipient[] getRecipientList();

    /**
     * Returns the sender of the message.
     *
     * @return the sender.
     */
    Sender getSender();

    /**
     * Returns the message ID.
     *
     * @return the message ID.
     */
    UUID getId();

    /**
     * Callback invoked when the message gets enqueued.
     *
     * @throws IllegalStateException if the current state is not {@link QueueState#NEW}.
     */
    void onEnqueue() throws IllegalStateException;

    /**
     * Callback invoked when the message is dispatched from the queue.
     *
     * @throws IllegalStateException if the current state is not {@link QueueState#NEW}.
     */
    void onDispatch() throws IllegalStateException;

    /**
     * Callback invoked when the message gets delivered.
     * <p>
     * This callback will be invoked for each recipient.
     *
     * @throws IllegalStateException if the current state is not {@link QueueState#ENQUEUED}.
     */
    void onDeliver() throws IllegalStateException;

    /**
     * Callback invoked when the message has been delivered to all recipients.
     * <p>
     * This callback will be invoked only once.
     */
    void onDelivered();

    /**
     * Checks if this is an urgent message.
     *
     * @return {@code true} if the message is urgent, {@code false} if not.
     */
    boolean isUrgent();

//    /**
//     * Sets the sender of the message.
//     *
//     * @param sender the sender.
//     * @return this message, for method chaining.
//     */
//    Message from(Sender sender);
//
//    /**
//     * Adds the specified recipients to the list of recipients.
//     *
//     * @param recipients the recipient(s) to add to the recipients list.
//     * @return this message, for method chaining.
//     */
//    Message to(Recipient... recipients);
//
//    /**
//     * Adds the specified recipients to the list of carbon-copy recipients.
//     *
//     * @param recipients the recipient(s) to add to the carbon copy recipients list.
//     * @return this message, for method chaining.
//     */
//    Message cc(Recipient... recipients);

}
