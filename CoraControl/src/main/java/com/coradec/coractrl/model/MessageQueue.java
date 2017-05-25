package com.coradec.coractrl.model;

import com.coradec.coracom.model.Message;
import com.coradec.coracom.trouble.QueueException;

/**
 * ​A queue for messages.
 */
public interface MessageQueue {

    /**
     * Schedules the specified code for execution.
     *
     * @param code the code.
     */
    void schedule(Runnable code);

    /**
     * Injects a message into the queue.
     *
     * @param message the message to inject.
     * @throws QueueException if the message could not be injected.
     */
    void inject(Message message) throws QueueException;

}
