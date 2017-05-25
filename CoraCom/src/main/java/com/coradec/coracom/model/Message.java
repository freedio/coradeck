package com.coradec.coracom.model;

import com.coradec.coracom.state.QueueState;
import com.coradec.coracore.model.State;

import java.util.Set;

/**
 * ​​Basic interface for all exchanged messages.
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
     * Callback invoked when the message gets enqueued.
     *
     * @throws IllegalStateException if the current state is not {@link QueueState#NEW}.
     */
    void onEnqueue() throws IllegalStateException;

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

}
