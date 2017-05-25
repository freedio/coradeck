package com.coradec.coracom.model;

/**
 * ​Recipient of a message.
 */
public interface Recipient {

    /**
     * Callback invoked when a new message is ready for processing.
     *
     * @param message the new message.
     */
    void onMessage(Message message);

}
