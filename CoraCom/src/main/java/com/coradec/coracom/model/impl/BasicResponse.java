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

import static com.coradec.coracom.state.Answer.*;

import com.coradec.coracom.model.PayloadMessage;
import com.coradec.coracom.model.Recipient;
import com.coradec.coracom.model.Response;
import com.coradec.coracom.state.Answer;
import com.coradec.coracore.annotation.Inject;
import com.coradec.coracore.annotation.NonNull;
import com.coradec.coracore.annotation.Nullable;
import com.coradec.coracore.model.Factory;
import com.coradec.coracore.model.Origin;
import com.coradec.coradir.model.Path;
import com.coradec.coradir.model.TranscendentPath;
import com.coradec.corasession.model.Session;

import java.util.Map;
import java.util.UUID;

/**
 * ​​Basic implementation of a session response.
 */
@SuppressWarnings("ClassHasNoToStringMethod")
public class BasicResponse extends BasicMessage implements Response, PayloadMessage {

    @Inject private static Factory<TranscendentPath> PATH;

    private final UUID reference;
    private final Answer answer;
    private @Nullable byte[] body;
    private @Nullable Throwable failureReason;

    /**
     * Initializes a new instance of BasicResponse with the specified sender, recipient, request
     * reference, answer and either a failure reason (if the answer is "KO"), a response body (if
     * the answer is "OK"), or nothing at all (in any case) in the context of the specified
     * session..
     *
     * @param session   the session context.
     * @param sender    the sender.
     * @param recipient the recipient.
     * @param reference the request reference.
     * @param answer    the answer.
     * @param arg       an additional argument (optional).
     */
    public BasicResponse(final Session session, final Origin sender, final Recipient recipient,
            final UUID reference, final Answer answer, final @Nullable Object arg) {
        super(sender, recipient);
        this.reference = reference;
        this.answer = answer;
        if (answer == OK && arg instanceof byte[]) this.body = (byte[])arg;
        if (answer == KO && arg instanceof Throwable) this.failureReason = (Throwable)arg;
    }

    /**
     * Initializes a new instance of BasicMessage from the specified property map.
     *
     * @param properties the property map.
     */
    public BasicResponse(final Map<String, Object> properties) {
        super(properties);
        this.reference = get(UUID.class, PROP_REFERENCE);
        this.answer = get(Answer.class, PROP_ANSWER);
        this.failureReason = lookup(Throwable.class, PROP_REASON).orElse(null);
        this.body = lookup(byte[].class, PROP_BODY).orElse(null);
    }

    @Override public UUID getReference() {
        return reference;
    }

    @Override public Answer getAnswer() {
        return answer;
    }

    @Override public @Nullable Throwable getFailureReason() {
        return failureReason;
    }

    @Override public @Nullable byte[] getBody() {
        return body == null ? null : body.clone();
    }

    @Override public Path getTarget() {
        return PATH.create(getRecipient().getRecipientId());
    }

    @Override public void setBody(final @NonNull byte[] payload) {
        this.body = payload;
    }

    @Override protected void collect() {
        super.collect();
        set(PROP_REFERENCE, reference);
        set(PROP_ANSWER, answer);
        if (failureReason != null) set(PROP_REASON, failureReason);
    }

}
