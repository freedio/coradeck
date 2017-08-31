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

/**
 * An object sent from a sender to a list of recipients.  If the list of recipients is empty, the
 * message will either be delivered regularly to the sender, if it is also a recipient, or bounce to
 * the sender.  Messages without a valid sender should and will normally be dropped with a log
 * entry.
 */
public interface Message extends Event {

    String PROP_SENDER = "Sender";
    String PROP_RECIPIENT = "Recipient";
    String PROP_URGENT = "Urgent";
    String PROP_RECIPIENT_RESOLVER = "-RecipientResolver";

    /**
     * Returns the recipient.
     *
     * @return the recipient.
     */
    Recipient getRecipient();

    /**
     * Checks whether the message is urgent.
     *
     * @return {@code true} if the message is urgent, {@code false} if not.
     */
    boolean isUrgent();

    /**
     * Sets the recipient.
     *
     * @param recipient the new recipient.
     */
    void setRecipent(Recipient recipient);

}
