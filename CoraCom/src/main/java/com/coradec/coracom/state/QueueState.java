package com.coradec.coracom.state;

import com.coradec.coracore.model.State;

/**
 * ​Enumeration of delivery states.
 */
public enum QueueState implements State {
    NEW, ENQUEUED, DELIVERED;
}
