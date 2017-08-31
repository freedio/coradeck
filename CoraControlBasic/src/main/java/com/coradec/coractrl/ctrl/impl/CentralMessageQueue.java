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
import com.coradec.coracom.model.Target;
import com.coradec.coracom.model.impl.BasicCommand;
import com.coradec.coracom.trouble.InformationWithoutOriginException;
import com.coradec.coracom.trouble.QueueException;
import com.coradec.coraconf.model.Property;
import com.coradec.coracore.annotation.Implementation;
import com.coradec.coracore.annotation.Internal;
import com.coradec.coracore.annotation.Nullable;
import com.coradec.coracore.annotation.ToString;
import com.coradec.coracore.model.Origin;
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
        implements MultiThreadedMessageQueue, Origin, Recipient {

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
    final Map<Target, TargetQueue> queueMap;
    final BlockingQueue<Deferred> deferredQueue;
    final Queue<TargetQueue> queueQueue;
    final Semaphore queues, qman, iq;
    final Queue<MessageProcessor> processors;
    final Collection<Observer> observers;
    final ReentrantLock obslock = new ReentrantLock();
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
        else {
            try {
                qman.acquire();
                if (info instanceof Message) {
                    info.onEnqueue();
                    dispatchMessage(info, ((Message)info).getRecipient());
                } else dispatchInfo(info, observers);
            } catch (InterruptedException e) {
                throw new OperationInterruptedException();
            } finally {
                qman.release();
            }
        }
        return info;
    }

    private <I extends Information> void dispatchInfo(final I info,
            final Collection<Observer> observers) throws InterruptedException {
        obslock.lock();
        for (Observer observer : observers) {
            dispatchMessage(info, observer);
        }
        obslock.unlock();
    }

    private <I extends Information> void dispatchMessage(final I info, final Target target)
            throws InterruptedException {
        queueMap.computeIfAbsent(target, t -> {
            final TargetQueue queue = new TargetQueue(t);
            queueQueue.add(queue);
            queues.release();
            boost();
            return queue;
        }).add(info);
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

    /**
     * Returns the current value of the shutdown lock count.
     *
     * @return the shutdown lock count.
     */
    @Override public int getShutdownLockCount() {
        return preventShutdown.get();
    }

    /**
     * Forces the shutdown lock count to 0.
     */
    @Override public void clearShutdownLock() {
        preventShutdown.set(0);
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
                out.printf("%-32s: %d%n", rq.getTarget(), rq.size());
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

    void boost() {
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
        if (message instanceof AddSubscriberCommand ||
            message instanceof RemoveSubscriberCommand) {
            ((Command)message).execute();
        } else error(TEXT_CANNOT_PROCESS_MESSAGE, message);
    }

    /**
     * Returns the recipient ID.
     *
     * @return the recipient ID.
     */
    @Override public String getRecipientId() {
        return "CMQ";
    }

    @Override public URI toURI() {
        return URI.create(represent());
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
            do {
                try {
                    if (!queues.tryAcquire(patience.getAmount(), patience.getUnit()))
                        if (isOdd()) break;
                        else continue;
                    final TargetQueue queue;
                    final Target target;
                    try {
                        qman.acquire();
                        queue = queueQueue.remove();
                        target = queue.getTarget();
                        if (queue.isEmpty()) {
                            queueMap.remove(target);
                            continue;
                        }
                    } finally {
                        qman.release();
                    }
                    final Information message = queue.poll();
                    if (message != null) {
                        message.onDeliver();
                        if (target instanceof Recipient && message instanceof Message)
                            ((Recipient)target).onMessage((Message)message);
                        else if (target instanceof Observer) {
                            Observer observer = (Observer)target;
                            if (observer.wants(message) && observer.notify(message))
                                inject(new RemoveSubscriberCommand(observer));
                        }
                        try {
                            qman.acquire();
                            queueQueue.add(queue);
                            queues.release();
                        } finally {
                            qman.release();
                        }
                    }
                } catch (InterruptedException e) {
                    break;
                } catch (Exception e) {
                    error(e);
                }
            } while (true);
            processors.remove(this);
            currentUsed.set(processors.size());
        }
    }

    private class TargetQueue {

        private final Target recipient;
        private final LinkedList<Information> mainQueue = new LinkedList<>();
        private final LinkedList<Information> prioQueue = new LinkedList<>();
        private final ReentrantLock queueLock = new ReentrantLock();

        public TargetQueue(final Target recipient) {
            this.recipient = recipient;
        }

        @ToString public Target getTarget() {
            return recipient;
        }

        void add(final Information info) throws InterruptedException {
            queueLock.lock();
            try {
                (info instanceof Message && ((Message)info).isUrgent() ? prioQueue : mainQueue).add(
                        info);
            } finally {
                queueLock.unlock();
            }
        }

        boolean isEmpty() {
            return size() == 0;
        }

        @Nullable Information poll() {
            queueLock.lock();
            try {
                @Nullable Information result = prioQueue.poll();
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

        Information peek() {
            queueLock.lock();
            try {
                @Nullable Information result = prioQueue.peek();
                if (result == null) result = mainQueue.peek();
                return result;
            } finally {
                queueLock.unlock();
            }
        }
    }

    private class ShutMeDown implements Runnable {

        @Override public void run() {
            while (preventShutdown.get() != 0) {
                debug("Cannot terminate yet: %d locks.", preventShutdown.get());
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    break;
                }
            }
            running = false;
            while (!queueQueue.isEmpty()) Thread.yield();
            scheduler.interrupt();
            processors.forEach(Thread::interrupt);
        }
    }

    @Internal
    private class AddSubscriberCommand extends BasicCommand {

        private final Observer observer;

        public AddSubscriberCommand(final Observer observer) {
            super(CentralMessageQueue.this, CentralMessageQueue.this);
            this.observer = observer;
        }

        @Override public void execute() {
            obslock.lock();
            observers.add(observer);
            obslock.unlock();
            succeed();
        }

        @Override public boolean isUrgent() {
            return true;
        }

    }

    @Internal
    private class RemoveSubscriberCommand extends BasicCommand {

        private final Observer observer;

        public RemoveSubscriberCommand(final Observer observer) {
            super(CentralMessageQueue.this, CentralMessageQueue.this);
            this.observer = observer;
        }

        @Override public void execute() {
            obslock.lock();
            observers.remove(observer);
            obslock.unlock();
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
