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

import com.coradec.coracom.ctrl.Observer;
import com.coradec.coracom.model.Command;
import com.coradec.coracom.model.Deferred;
import com.coradec.coracom.model.Information;
import com.coradec.coracom.model.Message;
import com.coradec.coracom.model.Recipient;
import com.coradec.coracom.model.Request;
import com.coradec.coracom.model.Sender;
import com.coradec.coracom.model.impl.BasicCommand;
import com.coradec.coracom.trouble.InformationWithoutOriginException;
import com.coradec.coracom.trouble.MessageUndeliverableException;
import com.coradec.coracom.trouble.QueueException;
import com.coradec.coraconf.model.Property;
import com.coradec.coracore.annotation.Attribute;
import com.coradec.coracore.annotation.Implementation;
import com.coradec.coracore.annotation.Nullable;
import com.coradec.coracore.annotation.ToString;
import com.coradec.coracore.time.Duration;
import com.coradec.coracore.trouble.OperationInterruptedException;
import com.coradec.coractrl.ctrl.MultiThreadedMessageQueue;
import com.coradec.coractrl.ctrl.SysControl;
import com.coradec.coractrl.trouble.MessageQueueDisabledException;
import com.coradec.coralog.ctrl.impl.Logger;
import com.coradec.coratext.model.LocalizedText;
import com.coradec.coratext.model.Text;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

/**
 * ​​The central message queue service.
 */
@SuppressWarnings({"ClassHasNoToStringMethod", "PackageVisibleField"})
@Implementation(SINGLETON)
public class CentralMessageQueue extends Logger
        implements MultiThreadedMessageQueue, Sender, Recipient {

    static final AtomicInteger MP_ID_GEN = new AtomicInteger(0);

    private static final Text TEXT_MESSAGE_BOUNCED = LocalizedText.define("MessageBounced");
    static final Text TEXT_OBSERVER_NASTY = LocalizedText.define("ObserverNasty");
    private static final Text TEXT_CANNOT_PROCESS_MESSAGE =
            LocalizedText.define("CannotProcessMessage");

    private static final Property<Integer> PROP_HIGH_WATER_MARK =
            Property.define("HighWaterMark", Integer.class, 20);
    private static final Property<Integer> PROP_LOW_WATER_MARK =
            Property.define("LowWaterMark", Integer.class, 3);
    static final Property<Duration> PROP_PATIENCE =
            Property.define("Patience", Duration.class, Duration.of(20, SECONDS));

    private final int lowWaterMark;
    private final int highWaterMark;
    final Map<Recipient, RecipientQueue> queueMap;
    final BlockingQueue<Deferred> deferredQueue;
    final Queue<RecipientQueue> queueQueue;
    final Semaphore queues, qman, iq;
    final Queue<MessageProcessor> processors;
    final Collection<Observer> observers;
    final Thread scheduler = new Scheduler();
    boolean running;
    AtomicInteger maxUsed = new AtomicInteger(0);
    AtomicInteger currentUsed = new AtomicInteger(0);
    AtomicInteger preventShutdown = new AtomicInteger(0);

    public CentralMessageQueue() {
        lowWaterMark = PROP_LOW_WATER_MARK.value();
        highWaterMark = PROP_HIGH_WATER_MARK.value();
        queueMap = new HashMap<>();
        queueQueue = new ConcurrentLinkedQueue<>();
        deferredQueue = new PriorityBlockingQueue<>(3,
                (o1, o2) -> (int)(o1.getExecutionTime() - o2.getExecutionTime()));
        queues = new Semaphore(0);
        iq = new Semaphore(0);
        qman = new Semaphore(1);
        processors = new ConcurrentLinkedQueue<>();
        observers = new LinkedList<>();
        for (int i = 0; i < lowWaterMark; ++i) {
            startThread();
        }
        running = true;
        scheduler.start();
        SysControl.onShutdown(new ShutMeDown());
    }

    private void startThread() {
        new MessageProcessor().start();
    }

    @Override public <I extends Information> I inject(final I info) throws QueueException {
        if (!running) {
            final MessageQueueDisabledException dead = new MessageQueueDisabledException();
            if (info instanceof Request) ((Request)info).fail(dead);
            throw dead;
        }
        if (info.getOrigin() == null) throw new InformationWithoutOriginException(info);
        if (info instanceof Deferred && !isDue((Deferred)info)) scheduleDeferred((Deferred)info);
        else if (info instanceof Message) {
            Message message = (Message)info;
            Collection<Recipient> recipients = message.getRecipients();
            if (recipients.isEmpty()) {
                final Sender sender = message.getSender();
                if (sender instanceof Recipient)
                    recipients = Collections.singleton((Recipient)sender);
                if (recipients.isEmpty()) throw new MessageUndeliverableException(message);
            }
            message.setDeliveries(recipients.size());
            try {
                qman.acquire();
                for (Recipient recipient : recipients) {
                    info.onEnqueue();
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
        } else {
            inject(new DispatchInfoCommand(info));
        }
        return info;
    }

    boolean isDue(final Deferred info) {
        return info.getExecutionTime() <= System.currentTimeMillis();
    }

    /**
     * Schedules the specified information for deferred injection.
     *
     * @param info the deferred information.
     */
    private void scheduleDeferred(final Deferred info) {
        deferredQueue.add(info);
        final Deferred next = deferredQueue.peek();
        if (next != null) {
            scheduler.interrupt();
        }
    }

    @Override public void preventShutdown() {
        preventShutdown.incrementAndGet();
    }

    @Override public void allowShutdown() {
        preventShutdown.decrementAndGet();
    }

    @Override public void subscribe(final Observer observer) {
        inject(new AddSubscriberCommand(observer));
    }

    @Override public void unsubscribe(final Observer observer) {
        inject(new RemoveSubscriberCommand(observer));
    }

    @Override @ToString public int getLowWaterMark() {
        return lowWaterMark;
    }

    @Override @ToString public int getHighWaterMark() {
        return highWaterMark;
    }

    @Override public int getMaxWorkerCount() {
        return maxUsed.get();
    }

    @Override public int getActiveWorkerCount() {
        return processors.size();
    }

    @Override public void resetUsage() {
        processors.forEach(Thread::interrupt);
        maxUsed.set(0);
    }

    @Override public void dumpStats() {
        try (StringWriter collector = new StringWriter(4096); PrintWriter out = new PrintWriter(
                collector)) {
            out.println("--- Central message queue statistics ---");
            out.printf("        Number of active workers: %d of %d%n", getActiveWorkerCount(),
                    getMaxWorkerCount());
            out.printf("                 High water mark: %d%n", getHighWaterMark());
            out.printf("                  Low water mark: %d%n", getLowWaterMark());
            out.printf("               Active recipients: %d%n", queueQueue.size());
            out.printf("                         Running? %s%n", String.valueOf(running));
            out.printf(" Unhandled requests by recipient: %n");
            queueQueue.forEach(rq -> {
                out.printf("%-32s: %d%n", rq.getRecipient(), rq.size());
                out.printf("                     ==> Request: %s%n", rq.peek());
            });
            out.println("--- Stack dumps of active workers ---");
            processors.forEach(mp -> {
                out.printf("Worker %s:%n", mp.getName());
                for (final StackTraceElement ste : mp.getStackTrace()) {
                    out.printf("\tat %s.%s(%s:%d)%n", ste.getClassName(), ste.getMethodName(),
                            ste.getFileName(), ste.getLineNumber());
                }

            });
            out.close();
            debug(collector.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void boost() {
        final int qsize = queueQueue.size();
        if (qsize > processors.size() && processors.size() < highWaterMark) startThread();
    }

    boolean isOdd() {
        return processors.size() > lowWaterMark;
    }

    @Override public String represent() {
        return getClass().getSimpleName();
    }

    @Override public void onMessage(final Message message) {
        if (message instanceof DispatchInfoCommand ||
            message instanceof AddSubscriberCommand ||
            message instanceof RemoveSubscriberCommand) {
            ((Command)message).execute();
        } else error(TEXT_CANNOT_PROCESS_MESSAGE, message);
    }

    @Override public URI toURI() {
        return URI.create(represent());
    }

    @Override public void bounce(final Message message) {
        error(TEXT_MESSAGE_BOUNCED, message);
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
                        message.onDeliver();
                        recipient.onMessage(message);
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

    private class RecipientQueue {

        private final Recipient recipient;
        private final LinkedList<Message> mainQueue = new LinkedList<>();
        private final LinkedList<Message> prioQueue = new LinkedList<>();
        private final ReentrantLock queueLock = new ReentrantLock();

        public RecipientQueue(final Recipient recipient) {
            this.recipient = recipient;
        }

        @ToString public Recipient getRecipient() {
            return recipient;
        }

        void add(final Message message) throws InterruptedException {
            queueLock.lock();
            try {
                (message.isUrgent() ? prioQueue : mainQueue).add(message);
            } finally {
                queueLock.unlock();
            }
        }

        boolean isEmpty() {
            return size() == 0;
        }

        @Nullable Message poll() {
            queueLock.lock();
            try {
                @Nullable Message result = prioQueue.poll();
                if (result == null) result = mainQueue.poll();
                return result;
            } finally {
                queueLock.unlock();
            }
        }

        int size() {
            queueLock.lock();
            try {
                return prioQueue.size() + mainQueue.size();
            } finally {
                queueLock.unlock();
            }
        }

        Message peek() {
            queueLock.lock();
            try {
                @Nullable Message result = prioQueue.peek();
                if (result == null) result = mainQueue.peek();
                return result;
            } finally {
                queueLock.unlock();
            }
        }
    }

    private class ShutMeDown implements Runnable {

        @Override public void run() {
            while (preventShutdown.get() != 0) Thread.yield();
            running = false;
            while (!queueQueue.isEmpty()) Thread.yield();
            scheduler.interrupt();
            processors.forEach(Thread::interrupt);
        }
    }

    private class DispatchInfoCommand extends BasicCommand {

        private final Information info;

        public DispatchInfoCommand(final Information info) {
            super(CentralMessageQueue.this);
            this.info = info;
        }

        @Override public void execute() {
            for (final Observer observer : observers)
                try {
                    if (observer.wants(info)) {
                        if (observer.notify(info)) inject(new RemoveSubscriberCommand(observer));
                    }
                } catch (Exception e) {
                    error(e, TEXT_OBSERVER_NASTY, observer);
                }
            succeed();
        }

        @ToString @Attribute public Information getInfo() {
            return info;
        }
    }

    private class AddSubscriberCommand extends BasicCommand {

        private final Observer observer;

        public AddSubscriberCommand(final Observer observer) {
            super(CentralMessageQueue.this);
            this.observer = observer;
        }

        @Override public void execute() {
            observers.add(observer);
            succeed();
        }

        @Override public boolean isUrgent() {
            return true;
        }

    }

    private class RemoveSubscriberCommand extends BasicCommand {

        private final Observer observer;

        public RemoveSubscriberCommand(final Observer observer) {
            super(CentralMessageQueue.this);
            this.observer = observer;
        }

        @Override public void execute() {
            observers.remove(observer);
            succeed();
        }

        @Override public boolean isUrgent() {
            return true;
        }

    }

    private class Scheduler extends Thread {

        @Override public void run() {
            while (running) {
                try {
                    final Deferred deferred = deferredQueue.take();
                    if (deferred == null) continue;
                    if (isDue(deferred)) {
                        inject(deferred);
                    } else {
                        deferredQueue.put(deferred);
                    }
                    Thread.sleep(deferred.getExecutionTime() - System.currentTimeMillis());
                } catch (InterruptedException e) {
                    // simply continue
                }
            }
        }
    }
}
