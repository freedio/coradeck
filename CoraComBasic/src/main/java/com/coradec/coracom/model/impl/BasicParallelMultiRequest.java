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
import com.coradec.coracom.model.ParallelMultiRequest;
import com.coradec.coracom.model.Recipient;
import com.coradec.coracom.model.Request;
import com.coradec.coracom.model.Sender;
import com.coradec.coracore.annotation.Implementation;
import com.coradec.coracore.annotation.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * ​​Basic implementation of a parallel multi-request.
 */
@SuppressWarnings("ClassHasNoToStringMethod")
@Implementation
public class BasicParallelMultiRequest extends BasicRequest implements ParallelMultiRequest {

    private final Queue<Request> requests = new ConcurrentLinkedQueue<>();
    private final Semaphore sync = new Semaphore(0);
    private final AtomicInteger count = new AtomicInteger();

    /**
     * Initializes a new instance of BasicParallelMultiRequest with the specified sender and list of
     * recipients.
     *
     * @param sender     the sender.
     * @param recipients the list of recipients
     */
    public BasicParallelMultiRequest(final Sender sender, final Recipient... recipients) {
        super(sender, recipients);
    }

    /**
     * Initializes a new instance of BasicParallelMultiRequest with the specified sender, list of
     * recipients and a couple of sub-requests.
     *
     * @param requests   the requests to execute.
     * @param sender     the sender.
     * @param recipients the list of recipients
     */
    public BasicParallelMultiRequest(final List<Request> requests, final Sender sender,
            final Recipient... recipients) {
        super(sender, recipients);
        this.requests.addAll(requests);
    }

    @Override public BasicParallelMultiRequest process() {
        if (!isComplete()) {
            if (requests.isEmpty()) succeed();
            else {
//            debug("Unleashing %d request(s).", requests.size());
                requests.forEach(this::launchRequest);
            }
        }
        return this;
    }

    @Override public boolean notify(final Information info) {
        if (!isComplete()) {
//            debug("Processing %s", info);
//            debug("Subrequest %d of %d processed.", requests.size() - count.get() + 1,
//                    requests.size());
            final Request request = ((RequestCompleteEvent)info).getRequest();
            if (request.isSuccessful()) {
                if (count.decrementAndGet() == 0) succeed();
            } else if (request.isCancelled()) cancel();
            else if (request.isFailed()) fail(request.getProblem());
        } // else super.notify(info);
        return true;
    }

    @Override public Request and(@Nullable final Request request) {
        return request == null ? this : isComplete() ? new BasicParallelMultiRequest(
                Arrays.asList(this, request), getSender(), getRecipientList())
                                                     : addRequest(request);
    }

    private Request addRequest(final Request request) {
        requests.add(request);
        launchRequest(request);
        return request;
    }

    private void launchRequest(final Request request) {
        //                debug("State: %s", request.getState());
        request.reportCompletionTo(this);
        count.incrementAndGet();
        if (request.getState() == NEW) inject(request);
    }

}
