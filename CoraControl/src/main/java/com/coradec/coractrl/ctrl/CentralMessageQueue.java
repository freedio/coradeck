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

package com.coradec.coractrl.ctrl;

import static com.coradec.coracore.model.Scope.*;

import com.coradec.coracom.model.Message;
import com.coradec.coracom.model.Recipient;
import com.coradec.coracom.trouble.QueueException;
import com.coradec.coracore.annotation.Implementation;
import com.coradec.coractrl.model.MessageQueue;
import com.coradec.coralog.ctrl.impl.Logger;
import com.coradec.coratext.model.LocalizedText;
import com.coradec.coratext.model.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * ​​The central message queue.
 */
@SuppressWarnings({"ClassHasNoToStringMethod", "WeakerAccess", "PackageVisibleField"})
@Implementation(SINGLETON)
public class CentralMessageQueue extends Logger implements MessageQueue {

    static final Text TEXT_FAILED_TO_DELIVER = LocalizedText.define("FailedToDeliver");
    final LinkedList<Message> queue;
    final LinkedList<Message> prioq;
    final Semaphore items, lock;
    final Map<Recipient, MessageProcessor> qmap;
    final Thread queueRunner = new Thread(new QueueRunner(), "MessageQueueProcessor");
    final ExecutorService immediateExecutor;
    final List<Recipient> preProcessors, postProcessors;

    @Override public void schedule(final Runnable code) {
        immediateExecutor.execute(code);
    }

    @SuppressWarnings("unchecked") public CentralMessageQueue() {
        queue = new LinkedList<>();
        prioq = new LinkedList<>();
        items = new Semaphore(0);
        lock = new Semaphore(1);
        qmap = new HashMap<>();
        preProcessors = new ArrayList<>(0);
        postProcessors = new ArrayList<>(0);
        immediateExecutor = Executors.newCachedThreadPool();
        final ScheduledExecutorService delayedExecutor = Executors.newScheduledThreadPool(3);
        Runtime.getRuntime().addShutdownHook(new CmqTerminator());
        queueRunner.start();
    }

    @Override public void inject(final Message message) throws QueueException {
        try {
            if (message.isUrgent()) {
                prioq.addLast(message);
            } else {
                queue.addLast(message);
            }
            debug("CMQ: Enqueued message %s", message);
            message.onEnqueue();
            items.release();
        }
        catch (Exception e) {
            throw new QueueException(e);
        }
    }

    void deliver(final Recipient recipient, final Message message) {
        try {
            debug("CMQ: Delivering message %s to %s", message, recipient);
            message.onDeliver();
            lock.acquire();
            qmap.computeIfAbsent(recipient, MessageProcessor::new).add(message);
            lock.release();
        }
        catch (Exception e) {
            error(e, TEXT_FAILED_TO_DELIVER, message);
        }
    }

    private void process(final Recipient recipient, final Message message) {

    }

    private class MessageProcessor implements Runnable {

        private final Recipient recipient;
        private final AtomicBoolean running;
        private final LinkedList<Message> messages;

        MessageProcessor(final Recipient recipient) {
            this.recipient = recipient;
            running = new AtomicBoolean();
            messages = new LinkedList<>();
            immediateExecutor.execute(this);
        }

        void add(final Message message) {
            messages.add(message);
        }

        @Override public void run() {
            try {
                lock.acquire();
                while (!messages.isEmpty()) {
                    final Message message = messages.remove();
                    lock.release();
                    try {
                        recipient.onMessage(message);
                    }
                    catch (Exception e) {
                        warn(e);
                    }
                    lock.acquire();
                }
                qmap.remove(recipient);
                lock.release();
            }
            catch (InterruptedException e) {
                error(e);
            }
        }

    }

    private class CmqTerminator extends Thread {

        @Override public void run() {
            queueRunner.interrupt();
        }

    }

    private class QueueRunner implements Runnable {

        @Override public void run() {
            while (!Thread.currentThread().isInterrupted() || !queue.isEmpty() && !prioq.isEmpty())
                try {
                    items.acquire();
                    final Message message;
                    if (!prioq.isEmpty()) {
                        message = prioq.removeFirst();
                    } else {
                        message = queue.removeFirst();
                    }
//                    debug("Read Cursor=%d, message = %s", cursor, message);
                    preProcessors.forEach(mp -> deliver(mp, message));
                    final Set<Recipient> recipients = message.getRecipients();
                    if (recipients != null) recipients.forEach(mp -> {
                        try {
                            deliver(mp, message);
                        }
                        catch (Exception e) {
                            try {
                                error(e, TEXT_FAILED_TO_DELIVER, message);
                            }
                            catch (Exception e1) {
                                e.printStackTrace();
                            }
                        }
                    });
                    postProcessors.forEach(mp -> deliver(mp, message));
                }
                catch (InterruptedException e) {
                    break;
                }
//            System.out.printf("QueueRunner died%n");
        }

    }

}
