package com.coradec.coracom.model.impl;

import static com.coradec.coracom.state.QueueState.*;

import com.coradec.coracom.model.Message;
import com.coradec.coracom.model.Recipient;
import com.coradec.coracom.model.Sender;
import com.coradec.coracore.annotation.ToString;
import com.coradec.coracore.model.State;
import com.coradec.coracore.util.ClassUtil;
import com.coradec.coracore.util.CollectionUtil;
import com.coradec.coracore.annotation.Implementation;
import com.coradec.coralog.ctrl.impl.Logger;

import java.util.HashSet;
import java.util.Set;

/**
 * ​​Basic implementation of a message.
 */
@Implementation
public class BasicMessage extends Logger implements Message {

    private final Sender sender;
    private final Set<Recipient> recipients;
    private State state;

    /**
     * Initializes a new instance of BasicMessage with the specified sender and set of recipients.
     *
     * @param sender     the sender.
     * @param recipients the recipients.
     */
    private BasicMessage(final Sender sender, final Set<Recipient> recipients) {
        this.sender = sender;
        this.recipients = recipients;
        this.state = NEW;
    }

    /**
     * Initializes a new instance of BasicMessage with the specified sender and list of recipients.
     *
     * @param sender     the sender.
     * @param recipients the list of recipients
     */
    public BasicMessage(final Sender sender, Recipient... recipients) {
        this(sender, new HashSet<>(CollectionUtil.setOf(recipients)));
    }

    @Override @ToString public State getState() {
        return this.state;
    }

    protected void setState(final State state) {
        this.state = state;
    }

    @Override @ToString public Set<Recipient> getRecipients() {
        return recipients;
    }

    @Override public Recipient[] getRecipientList() {
        return recipients.toArray(new Recipient[recipients.size()]);
    }

    @Override @ToString public Sender getSender() {
        return sender;
    }

    @Override public void onEnqueue() throws IllegalStateException {
        if (state != NEW) throw new IllegalStateException(state.name());
        state = ENQUEUED;
    }

    @Override public void onDeliver() throws IllegalStateException {
        if (state != ENQUEUED) throw new IllegalStateException(state.name());
        state = DELIVERED;
    }

    @Override public void onDelivered() {
        // for subclasses to override
    }

    @Override @ToString public boolean isUrgent() {
        return false;
    }

    @Override public String toString() {
        return ClassUtil.toString(this);
    }

}
