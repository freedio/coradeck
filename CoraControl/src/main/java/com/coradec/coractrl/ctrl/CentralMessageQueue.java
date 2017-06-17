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
import static java.util.concurrent.TimeUnit.*;

import com.coradec.coracom.model.Message;
import com.coradec.coracom.model.Recipient;
import com.coradec.coracom.trouble.QueueException;
import com.coradec.coraconf.model.Property;
import com.coradec.coracore.annotation.Implementation;
import com.coradec.coracore.annotation.Nullable;
import com.coradec.coracore.trouble.OperationInterruptedException;
import com.coradec.coractrl.model.MessageQueue;
import com.coradec.coralog.annotate.Staging;
import com.coradec.coralog.ctrl.impl.Logger;
import com.coradec.coratext.model.LocalizedText;
import com.coradec.coratext.model.Text;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * ​​The central message queue.
 */
@SuppressWarnings({"ClassHasNoToStringMethod", "WeakerAccess", "PackageVisibleField"})
@Implementation(SINGLETON)
@Staging
public class CentralMessageQueue extends Logger implements MessageQueue {

    private static final Property<Integer> PROP_MAX_THREADS =
            Property.define("MaxThreads", Integer.class, 20);
    static final Text TEXT_CMQ_INTERRUPTED = LocalizedText.define("CmqInterrupted");
    static final Text TEXT_CMQ_STARTED = LocalizedText.define("CmqStarted");
    static final Text TEXT_WAITING_FOR_LOCK =
            LocalizedText.define("InterruptedWhileWaitingForLock");
    final ExecutorService executor;
    final Queue<Message> cmq;
    final Thread mqp;
    final Semaphore items;
    final Map<Recipient, MessageProcessor> rmq;
    Semaphore rmqLock = new Semaphore(1);

    public CentralMessageQueue() {
        final int nThreads = PROP_MAX_THREADS.value();
        executor = Executors.newFixedThreadPool(nThreads);
        debug("Executor with fixed thread pool of size %d created.", nThreads);
        cmq = new ConcurrentLinkedQueue<>();
        rmq = new HashMap<>();
        (mqp = new Thread(new MessageQueueProcessor(), "MQP")).start();
        items = new Semaphore(0);
    }

    @Override public void schedule(final Runnable code) {
        executor.execute(code);
    }

    @Override public void inject(final Message message) throws QueueException {
//        debug("Injecting message %s", message.getId());
        message.onEnqueue();
        cmq.add(message);
        items.release();
    }

    private class MessageQueueProcessor implements Runnable {

        @Override public void run() {
            info(TEXT_CMQ_STARTED);
            SysControl.onShutdown(mqp::interrupt);
            while (!Thread.interrupted()) {
                try {
                    items.acquire();
                    final Message message = cmq.poll();
                    if (message != null) {
//                        debug("Picked up message %s", message.getId());
                        message.onDispatch();
                        for (final Recipient recipient : message.getRecipientList()) {
                            enqueue(recipient, message);
                        }
                    }
                } catch (InterruptedException e) {
                    break;
                }
            }
            info(TEXT_CMQ_INTERRUPTED);
        }

        private void enqueue(final Recipient recipient, final Message message) {
            try {
                rmqLock.acquire();
                rmq.computeIfAbsent(recipient, MessageProcessor::new).add(message);
                rmqLock.release();
            } catch (InterruptedException e) {
                throw new OperationInterruptedException(TEXT_WAITING_FOR_LOCK.resolve());
            }
        }

    }

    private class MessageProcessor implements Runnable {

        private final Recipient recipient;
        private final LinkedBlockingQueue<Message> messages;
        private final AtomicBoolean running;

        public MessageProcessor(final Recipient recipient) {
            this.recipient = recipient;
            messages = new LinkedBlockingQueue<>();
            running = new AtomicBoolean();
        }

        void add(final Message message) {
//            debug("Adding message for recipient %s, now %d", recipient, messages.size() + 1);
            messages.add(message);
            if (!running.getAndSet(true)) executor.execute(this);
        }

        @Override public void run() {
            @Nullable Message message;
            try {
                message = messages.poll(2, SECONDS);
            } catch (InterruptedException e) {
                message = null;
            }
//            debug("Processing message for recipient %s", recipient);
            if (message == null) {
                try {
                    rmqLock.acquire();
                    rmq.remove(recipient);
                    rmqLock.release();
                    return;
                } catch (InterruptedException e) {
                    throw new OperationInterruptedException(TEXT_WAITING_FOR_LOCK.resolve());
                }
            }
            recipient.onMessage(message);
            if (!messages.isEmpty()) executor.execute(this);
            else running.set(false);
        }
    }

//    static final Text TEXT_FAILED_TO_DELIVER = LocalizedText.define("FailedToDeliver");
//    final ConcurrentLinkedQueue<Message> queue;
//    final ConcurrentLinkedQueue<Message> prioq;
//    final Semaphore items, lock;
//    final Map<Recipient, MessageProcessor> qmap;
//    final Thread queueRunner = new Thread(new QueueRunner(), "MessageQueueProcessor");
//    final ExecutorService immediateExecutor;
//    final List<Recipient> preProcessors, postProcessors;
//
//    @Override public void schedule(final Runnable code) {
//        immediateExecutor.execute(code);
//    }
//
//    @SuppressWarnings("unchecked") public CentralMessageQueue() {
//        queue = new ConcurrentLinkedQueue<>();
//        prioq = new ConcurrentLinkedQueue<>();
//        items = new Semaphore(0);
//        lock = new Semaphore(1);
//        qmap = new HashMap<>();
//        preProcessors = new ArrayList<>(0);
//        postProcessors = new ArrayList<>(0);
//        immediateExecutor = Executors.newCachedThreadPool();
//        final ScheduledExecutorService delayedExecutor = Executors.newScheduledThreadPool(3);
//        Runtime.getRuntime().addShutdownHook(new CmqTerminator());
//        queueRunner.start();
//    }
//
//    @Override public void inject(final Message message) throws QueueException {
//        try {
//            if (message.isUrgent()) {
//                prioq.add(message);
//            } else {
//                queue.add(message);
//            }
//            debug("CMQ: Enqueued message %s", message);
//            message.onEnqueue();
//            items.release();
//        }
//        catch (Exception e) {
//            throw new QueueException(e);
//        }
//    }
//
//    void deliver(final Recipient recipient, final Message message) {
//        try {
//            debug("CMQ: Delivering message %s to %s", message, recipient);
//            message.onDeliver();
//            lock.acquire();
//            qmap.computeIfAbsent(recipient, MessageProcessor::new).add(message);
//            lock.release();
//        }
//        catch (Exception e) {
//            error(e, TEXT_FAILED_TO_DELIVER, message);
//        }
//    }
//
//    private void process(final Recipient recipient, final Message message) {
//
//    }
//
//    private class MessageProcessor implements Runnable {
//
//        private final Recipient recipient;
//        private final AtomicBoolean running;
//        private final ConcurrentLinkedQueue<Message> messages;
//
//        MessageProcessor(final Recipient recipient) {
//            this.recipient = recipient;
//            running = new AtomicBoolean();
//            messages = new ConcurrentLinkedQueue<>();
//            immediateExecutor.execute(this);
//        }
//
//        void add(final Message message) {
//            messages.add(message);
//        }
//
//        @Override public void run() {
//            try {
//                lock.acquire();
//                while (!messages.isEmpty()) {
//                    final Message message = messages.remove();
//                    lock.release();
//                    try {
//                        recipient.onMessage(message);
//                    }
//                    catch (Exception e) {
//                        warn(e);
//                    }
//                    lock.acquire();
//                }
//                qmap.remove(recipient);
//                lock.release();
//            }
//            catch (InterruptedException e) {
//                error(e);
//            }
//        }
//
//    }
//
//    private class CmqTerminator extends Thread {
//
//        @Override public void run() {
//            queueRunner.interrupt();
//        }
//
//    }
//
//    private class QueueRunner implements Runnable {
//
//        @Override public void run() {
//            while (!Thread.currentThread().isInterrupted() || !queue.isEmpty() && !prioq
// .isEmpty())
//                try {
//                    items.acquire();
//                    final Message message;
//                    if (!prioq.isEmpty()) {
//                        message = prioq.poll();
//                    } else {
//                        message = queue.remove();
//                    }
//                    preProcessors.forEach(mp -> deliver(mp, message));
//                    final Set<Recipient> recipients = message.getRecipients();
//                    if (recipients != null) recipients.forEach(mp -> {
//                        try {
//                            deliver(mp, message);
//                        }
//                        catch (Exception e) {
//                            try {
//                                error(e, TEXT_FAILED_TO_DELIVER, message);
//                            }
//                            catch (Exception e1) {
//                                e.printStackTrace();
//                            }
//                        }
//                    });
//                    postProcessors.forEach(mp -> deliver(mp, message));
//                }
//                catch (InterruptedException e) {
//                    break;
//                }
////            System.out.printf("QueueRunner died%n");
//        }
//
//    }

}
