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

import java.util.Collection;

/**
 * An object sent from a sender to a list of recipients.  If the list of recipients is empty, the
 * message will either be delivered regularly to the sender, if it is also a recipient, or bounce to
 * the sender.  Messages without a valid sender should and will normally be dropped with a log
 * entry.
 */
public interface Message extends Event {

    /**
     * Returns the set of recipients.  The empty set denotes a broadcast message.
     *
     * @return the set of recipients.
     */
    Collection<Recipient> getRecipients();

    /**
     * Returns the recipients as an array.  The empty array denotes a broadcast message.
     *
     * @return the recipient list.
     */
    Recipient[] getRecipientList();

    /**
     * Sets the number of recipients to which the message is to be delivered.
     *
     * @param recipients the number of recipients.
     */
    void setDeliveries(int recipients);

    /**
     * Callback invoked when the message has been delivered to all recipients.
     *
     * @throws IllegalStateException if the current state is not {@link QueueState#ENQUEUED}.
     */
    void onDelivered();

    /**
     * Returns the sender.
     *
     * @return the sender.
     */
    Sender getSender();

    /**
     * Checks whether the message is urgent.
     *
     * @return {@code true} if the message is urgent, {@code false} if not.
     */
    boolean isUrgent();

}
