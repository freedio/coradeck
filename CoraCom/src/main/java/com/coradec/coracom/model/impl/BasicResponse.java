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

import com.coradec.coracom.model.Recipient;
import com.coradec.coracom.model.Response;
import com.coradec.coracom.model.Sender;
import com.coradec.coracom.state.Answer;
import com.coradec.coracore.annotation.Nullable;
import com.coradec.corasession.model.Session;

import java.util.UUID;

/**
 * ​​Basic implementation of a session response.
 */
@SuppressWarnings("ClassHasNoToStringMethod")
public class BasicResponse extends BasicMessage implements Response {

    private final UUID reference;
    private final Answer answer;
    private @Nullable Throwable problem;
    private @Nullable byte[] body;

    public BasicResponse(final Session session, final UUID reference, final Answer answer,
            final @Nullable Object arg, final Sender sender, final Recipient... recipients) {
        super(sender, recipients);
        this.reference = reference;
        this.answer = answer;
        if (answer == KO && arg instanceof Throwable) problem = (Throwable)arg;
        else if (arg instanceof byte[]) body = (byte[])arg;
    }

    @Override public UUID getReference() {
        return reference;
    }

    @Override public Answer getAnswer() {
        return answer;
    }

    @Override public @Nullable Throwable getFailureReason() {
        return problem;
    }

    @Override public @Nullable byte[] getBody() {
        return body == null ? null : body.clone();
    }

}
