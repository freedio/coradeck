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
import com.coradec.coracom.model.MultiRequest;
import com.coradec.coracom.model.Recipient;
import com.coradec.coracom.model.Request;
import com.coradec.coracom.model.SerialMultiRequest;
import com.coradec.coracore.annotation.Implementation;
import com.coradec.coracore.annotation.Internal;
import com.coradec.coracore.model.Origin;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;

/**
 * ​​Basic implementation of a serial multi-request.
 */
@SuppressWarnings("ClassHasNoToStringMethod")
@Implementation
@Internal
public class BasicSerialMultiRequest extends BasicRequest implements SerialMultiRequest {

    private final Queue<Request> requests = new ConcurrentLinkedQueue<>();
    private final Semaphore sync = new Semaphore(0);

    /**
     * Initializes a new instance of BasicSerialMultiRequest with the specified sender and
     * recipient.
     *
     * @param sender    the sender.
     * @param recipient the recipient.
     */
    public BasicSerialMultiRequest(final Origin sender, final Recipient recipient) {
        this(sender, recipient, Collections.emptyList());
    }

    /**
     * Initializes a new instance of BasicSerialMultiRequest with the specified sender, recipient
     * and a couple of sub-requests.
     *
     * @param sender    the sender.
     * @param recipient the recipient.
     * @param requests  the requests to execute (in the order of the list).
     */
    public BasicSerialMultiRequest(final Origin sender, final Recipient recipient,
            final List<Request> requests) {
        super(sender, recipient);
        this.requests.addAll(requests);
    }

    @Override public MultiRequest process() {
        if (!isComplete()) {
            final Request next = requests.poll();
            if (next == null) succeed();
            else {
                next.reportCompletionTo(this);
                if (next.getState() == NEW) inject(next);
            }
        }
        return this;
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

    @Override public Request andThen(final Request request) {
        return request == null ? this : isComplete() ? new BasicSerialMultiRequest(getOrigin(),
                getRecipient(), Arrays.asList(this, request)) : addRequest(request);
    }

    private Request addRequest(final Request request) {
        requests.add(request);
        return request;
    }

}
