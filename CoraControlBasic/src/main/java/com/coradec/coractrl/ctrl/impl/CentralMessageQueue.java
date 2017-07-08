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

package com.coradec.coractrl.ctrl.impl;

import static com.coradec.coracore.model.Scope.*;
import static java.util.concurrent.TimeUnit.*;

import com.coradec.coracom.model.Message;
import com.coradec.coracom.model.Recipient;
import com.coradec.coracom.model.Sender;
import com.coradec.coracom.trouble.MessageUndeliverableException;
import com.coradec.coracom.trouble.MessageWithoutSenderException;
import com.coradec.coracom.trouble.QueueException;
import com.coradec.coraconf.model.Property;
import com.coradec.coracore.annotation.Implementation;
import com.coradec.coracore.annotation.ToString;
import com.coradec.coracore.time.Duration;
import com.coradec.coracore.trouble.OperationInterruptedException;
import com.coradec.coractrl.ctrl.MultiThreadedMessageQueue;
import com.coradec.coractrl.ctrl.SysControl;
import com.coradec.coractrl.trouble.MessageQueueDisabledException;
import com.coradec.coralog.ctrl.impl.Logger;
import com.coradec.coratext.model.LocalizedText;
import com.coradec.coratext.model.Text;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * ​​The central message queue service.
 */
@SuppressWarnings({"ClassHasNoToStringMethod", "WeakerAccess", "PackageVisibleField"})
@Implementation(SINGLETON)
public class CentralMessageQueue extends Logger implements MultiThreadedMessageQueue {

    static final AtomicInteger MP_ID_GEN = new AtomicInteger(0);
    private static final Property<Integer> PROP_HIGH_WATER_MARK =
            Property.define("HighWaterMark", Integer.class, 20);
    private static final Property<Integer> PROP_LOW_WATER_MARK =
            Property.define("LowWaterMark", Integer.class, 3);
    private static final Text TEXT_QUEUE_DISPATCHER_STARTED =
            LocalizedText.define("QueueDispatcherStarted");
    static final Property<Duration> PROP_PATIENCE =
            Property.define("Patience", Duration.class, Duration.of(20, SECONDS));
    private static final Property<Integer> PROP_QQ_SIZE =
            Property.define("QueueQueueSize", Integer.class, 1024);

    private final int lowWaterMark;
    private final int highWaterMark;

    final Map<Recipient, RecipientQueue> queueMap;
    final BlockingQueue<RecipientQueue> queueQueue;
    final Semaphore queues, qman;
    final Queue<MessageProcessor> processors;
    boolean running;
    AtomicInteger maxUsed = new AtomicInteger(0), currentUsed = new AtomicInteger(0);

    public CentralMessageQueue() {
        lowWaterMark = PROP_LOW_WATER_MARK.value();
        highWaterMark = PROP_HIGH_WATER_MARK.value();
        queueMap = new HashMap<>();
        queueQueue = new ArrayBlockingQueue<>(1024);
        queues = new Semaphore(0);
        qman = new Semaphore(1);
        processors = new ConcurrentLinkedQueue<>();
        for (int i = 0; i < lowWaterMark; ++i) {
            startThread();
        }
        running = true;
        SysControl.onShutdown(new ShutMeDown());
    }

    private void startThread() {
        new MessageProcessor().start();
    }

    @Override public <M extends Message> M inject(final M message) throws QueueException {
        if (!running) throw new MessageQueueDisabledException();
        if (message.getSender() == null) throw new MessageWithoutSenderException(message);
        Set<Recipient> recipients = message.getRecipients();
        if (recipients.isEmpty()) {
            final Sender sender = message.getSender();
            if (sender instanceof Recipient) recipients = Collections.singleton((Recipient)sender);
            if (recipients.isEmpty()) throw new MessageUndeliverableException(message);
        }
        message.setDeliveries(recipients.size());
//        debug("Injecting message %s", message);
        try {
            qman.acquire();
            for (Recipient recipient : recipients) {
                message.onEnqueue();
                queueMap.computeIfAbsent(recipient, r -> {
                    final RecipientQueue queue = new RecipientQueue(r);
                    queueQueue.add(queue);
                    queues.release();
                    boost();
                    return queue;
                }).add(message);
            }
        } catch (InterruptedException e) {
            throw new OperationInterruptedException();
        } finally {
            qman.release();
        }
        return message;
    }

    @Override @ToString public int getLowWaterMark() {
//        if (lowWaterMark == 0) lowWaterMark = PROP_LOW_WATER_MARK.value();
        return lowWaterMark;
    }

    @Override @ToString public int getHighWaterMark() {
//        if (highWaterMark == 0) highWaterMark = PROP_HIGH_WATER_MARK.value();
        return highWaterMark;
    }

    @Override public int getMaxUsed() {
        return maxUsed.get();
    }

    @Override public void resetUsage() {
        processors.forEach(Thread::interrupt);
        maxUsed.set(0);
    }

    private void boost() {
        final int qsize = queueQueue.size();
        if (qsize > processors.size() && processors.size() < highWaterMark) startThread();
    }

    private class MessageProcessor extends Thread {

        MessageProcessor() {
            super("MessageProcessor-" + MP_ID_GEN.getAndIncrement());
            processors.add(this);
            currentUsed.set(processors.size());
            maxUsed.set(Integer.max(maxUsed.get(), currentUsed.get()));
        }

        @Override public void run() {
            final Duration patience = PROP_PATIENCE.value();
//            debug("%s starting (patience = %s).", getName(), patience);
            do {
                try {
                    if (!queues.tryAcquire(patience.getAmount(), patience.getUnit()))
                        if (isOdd()) break;
                        else continue;
                    final RecipientQueue queue;
                    final Recipient recipient;
                    try {
                        qman.acquire();
                        queue = queueQueue.remove();
                        recipient = queue.getRecipient();
                        if (queue.isEmpty()) {
                            queueMap.remove(recipient);
                            continue;
                        }
                    } finally {
                        qman.release();
                    }
                    final Message message = queue.poll();
//                    debug("%s: Acquired %s", getName(), message);
                    if (message != null) {
//                        debug("Delivering message %s to recipient %s", message, recipient);
                        recipient.onMessage(message);
                        message.onDeliver();
//                        debug("%s: Delivered %s", getName(), message);
                        try {
                            qman.acquire();
//                            debug("%s: Returning queue queue", getName());
                            queueQueue.add(queue);
                            queues.release();
                        } finally {
                            qman.release();
                        }
                    }
                } catch (InterruptedException e) {
//                    debug("%s interrupted.", getName());
                    break;
                } catch (Exception e) {
                    error(e);
                }
            } while (true);
//            debug("%s terminated.", getName());
            processors.remove(this);
            currentUsed.set(processors.size());
        }
    }

    boolean isOdd() {
        return processors.size() > lowWaterMark;
    }

    private class RecipientQueue {

        private final Recipient recipient;
        private final Queue<Message> stdQ;
        private final Queue<Message> prioQ;

        public RecipientQueue(final Recipient recipient) {
            this.recipient = recipient;
            this.stdQ = new ConcurrentLinkedQueue<>();
            this.prioQ = new ConcurrentLinkedQueue<>();
        }

        @ToString public Recipient getRecipient() {
            return recipient;
        }

        void add(final Message message) {
            (message.isUrgent() ? prioQ : stdQ).add(message);
        }

        boolean isEmpty() {
            return stdQ.isEmpty() && prioQ.isEmpty();
        }

        Message poll() {
            return (prioQ.isEmpty() ? stdQ : prioQ).poll();
        }
    }

    private class ShutMeDown implements Runnable {

        @Override public void run() {
            running = false;
            while (!queueQueue.isEmpty()) Thread.yield();
            processors.forEach(Thread::interrupt);
        }
    }
}
