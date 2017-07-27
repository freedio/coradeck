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

import com.coradec.coracom.com.RequestCompleteEvent;
import com.coradec.coracom.model.Information;
import com.coradec.coracom.model.Recipient;
import com.coradec.coracom.model.Request;
import com.coradec.coracom.model.Sender;
import com.coradec.coracom.model.SerialMultiRequest;
import com.coradec.coracore.annotation.Implementation;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;

/**
 * ​​Basic implementation of a serial multi-request.
 */
@SuppressWarnings("ClassHasNoToStringMethod")
@Implementation
public class BasicSerialMultiRequest extends BasicRequest implements SerialMultiRequest {

    private final Queue<Request> requests = new ConcurrentLinkedQueue<>();
    private final Semaphore sync = new Semaphore(0);

    /**
     * Initializes a new instance of BasicSerialMultiRequest with the specified sender and list of
     * recipients.
     *
     * @param sender     the sender.
     * @param recipients the list of recipients
     */
    public BasicSerialMultiRequest(final Sender sender, final Recipient... recipients) {
        super(sender, recipients);
    }

    /**
     * Initializes a new instance of BasicSerialMultiRequest with the specified sender, list of
     * recipients and a couple of sub-requests.
     *
     * @param requests   the requests to execute (in the order of the list).
     * @param sender     the sender.
     * @param recipients the list of recipients
     */
    public BasicSerialMultiRequest(final List<Request> requests, final Sender sender,
            final Recipient... recipients) {
        super(sender, recipients);
        this.requests.addAll(requests);
    }

    @Override public void process() {
        final Request next = requests.poll();
        if (next == null) succeed();
        else {
            next.reportCompletionTo(this);
            if (next.getState() == NEW) inject(next);
        }
    }

    @Override public boolean notify(final Information info) {
        if (!isComplete()) {
            final Request request = ((RequestCompleteEvent)info).getRequest();
            if (request.isSuccessful()) {
                if (requests.isEmpty()) succeed();
                else process();
            } else if (request.isCancelled()) cancel();
            else if (request.isFailed()) fail(request.getProblem());
        } else return super.notify(info);
        return true;
    }

}
