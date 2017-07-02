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

package com.coradec.coracom.ctrl.impl;

import static java.util.concurrent.TimeUnit.*;

import com.coradec.coracom.ctrl.MessageQueue;
import com.coradec.coracom.model.Message;
import com.coradec.coracom.model.Recipient;
import com.coradec.coracom.model.Sender;
import com.coradec.coracom.trouble.MessageWithoutSenderException;
import com.coradec.coracom.trouble.QueueException;
import com.coradec.coralog.ctrl.impl.Logger;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * ​​Simple implementation of a message queue for internal purposes.
 */
@SuppressWarnings("ClassHasNoToStringMethod")
public class SimpleMessageQueue extends Logger implements MessageQueue {

    private final BlockingQueue<Message> queue = new LinkedBlockingQueue<>();
    private final AtomicBoolean running = new AtomicBoolean();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @SuppressWarnings("WeakerAccess") public SimpleMessageQueue() {

    }

    @Override public <M extends Message> M inject(final M message) throws QueueException {
        if (message.getSender() == null) throw new MessageWithoutSenderException(message);
        message.onEnqueue();
//        if (message.getClass().getSimpleName().endsWith("ExecuteStateTransitionRequest")) {
//            debug("Enqueuing message %s", message);
//            for (final StackTraceElement element : Thread.currentThread().getStackTrace()) {
//                System.out.println("\tat " + element);
//            }
//        }
        queue.add(message);
        if (!running.getAndSet(true)) {
            startQueueProcessor();
        }
        return message;
    }

    private void startQueueProcessor() {
        executor.execute(() -> {
            while (!Thread.interrupted()) {
                final Message message;
                try {
                    message = queue.poll(1, SECONDS);
                    if (message == null) {
                        break;
                    }
                    Set<Recipient> recipients = message.getRecipients();
                    if (recipients.isEmpty()) {
                        final Sender sender = message.getSender();
                        if (sender instanceof Recipient)
                            recipients = Collections.singleton((Recipient)sender);
                        if (recipients.isEmpty()) {
                            sender.bounce(message);
                            continue;
                        }
                    }
                    for (final Recipient recipient : recipients) {
                        try {
//                            debug("Delivering message %s to %s", message, recipient);
                            recipient.onMessage(message);
                            message.onDeliver();
                        } catch (IllegalStateException e) {
                            e.printStackTrace();
                        }
                    }
                } catch (InterruptedException e) {
                    break;
                }
            }
            running.set(false);
        });
    }

}
