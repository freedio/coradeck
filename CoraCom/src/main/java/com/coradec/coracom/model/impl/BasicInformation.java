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

package com.coradec.coracom.model.impl;

import static com.coradec.coracom.state.QueueState.*;

import com.coradec.coracom.model.Information;
import com.coradec.coracore.annotation.ToString;
import com.coradec.coracore.model.Origin;
import com.coradec.coracore.model.State;
import com.coradec.coracore.util.ClassUtil;
import com.coradec.coralog.ctrl.impl.Logger;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * ​​Basic implementation of an information.
 */
public class BasicInformation extends Logger implements Information {

    private final LocalDateTime created;
    private final UUID id;
    private final Origin origin;
    private State state;

    public BasicInformation(Origin origin) {
        this.origin = origin;
        this.created = LocalDateTime.now();
        this.id = UUID.randomUUID();
        this.state = NEW;
    }

    @Override @ToString public LocalDateTime getCreationTimestamp() {
        return created;
    }

    @Override @ToString public State getState() {
        return this.state;
    }

    /**
     * Sets the state of the message.
     *
     * @param state the new state.
     */
    protected void setState(final State state) {
        this.state = state;
    }

    @Override @ToString public UUID getId() {
        return id;
    }

    @Override @ToString public Origin getOrigin() {
        return origin;
    }

    @Override public void onEnqueue() throws IllegalStateException {
        if (state != NEW) throw new IllegalStateException(
                String.format("Information %s has illegal state %s (should be NEW)", this,
                        state.name()));
        setState(ENQUEUED);
    }

    @Override public void onDispatch() throws IllegalStateException {
        if (state != ENQUEUED) throw new IllegalStateException(
                String.format("Information %s has illegal state %s (should be ENQUEUED)", this,
                        state.name()));
        setState(DISPATCHED);
    }

    @Override public void onDeliver() throws IllegalStateException {

    }

    @Override public Information renew() {
        setState(NEW);
        return this;
    }

    @Override public String toString() {
        return ClassUtil.toString(this);
    }

}
