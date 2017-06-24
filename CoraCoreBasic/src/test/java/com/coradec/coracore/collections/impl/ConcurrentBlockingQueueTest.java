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

package com.coradec.coracore.collections.impl;

import static java.util.concurrent.TimeUnit.*;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.*;

import com.coradec.coracore.trouble.CapacityExhaustedException;
import org.junit.Test;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

@SuppressWarnings({
                          "UnnecessaryLocalVariable", "ForLoopReplaceableByForEach", "WeakerAccess",
                          "PackageVisibleField"
                  })
public class ConcurrentBlockingQueueTest {

    private static final String ELEMENT = "This is a test";
    private static final int CAPACITY = 1000;
    static final Random RANDOM = new Random();
    static final AtomicInteger IDGEN = new AtomicInteger(0);
    static final BitSet BITS = new BitSet(5000);
    static final AtomicInteger NUMBERS = new AtomicInteger(0);
    static Semaphore DONE = new Semaphore(0);

    final BlockingQueue<String> testee = new ArrayBlockingQueue<>(CAPACITY);

    @Test public void addingSingleElementShouldSucceed() {
        testee.add(ELEMENT);
        assertThat(testee.size(), is(1));
        assertThat(testee.remove(), is(equalTo(ELEMENT)));
        assertThat(testee.isEmpty(), is(true));
    }

    @Test public void addingToCapacityShouldSucceed() {
        fillToCapacity();
        assertThat(testee.size(), is(CAPACITY));
    }

    @Test public void addingAndRemovingBeyondCapacityShouldSucceed() {
        for (int i = 0, is = 2 * CAPACITY; i < is; ++i) {
            testee.add("Element " + i);
            assertThat(testee.remove(), is(equalTo("Element " + i)));
        }
        assertThat(testee.isEmpty(), is(true));
    }

    @Test public void addingBeyondCapacityShouldFail() {
        try {
            for (int i = 0, is = 2 * CAPACITY; i < is; ++i) {
                testee.add(ELEMENT);
            }
            fail("Should have thrown " + CapacityExhaustedException.class.getSimpleName());
        } catch (IllegalStateException e) {
            // expected that
        }
        assertThat(testee.size(), is(CAPACITY));
    }

    @Test public void offeringSingleElementShouldSucceed() {
        assertThat(testee.offer(ELEMENT), is(true));
        assertThat(testee.size(), is(1));
        assertThat(testee.poll(), is(equalTo(ELEMENT)));
        assertThat(testee.isEmpty(), is(true));
    }

    @Test public void offeringToCapacityShouldSucceed() {
        boolean result = true;
        for (int i = 0, is = CAPACITY; i < is; ++i) {
            result &= testee.offer(ELEMENT);
        }
        assertThat(testee.size(), is(CAPACITY));
        assertThat(result, is(true));
    }

    @Test public void offeringAndPollingBeyondCapacityShouldSucceed() {
        for (int i = 0, is = 2 * CAPACITY; i < is; ++i) {
            assertThat(testee.offer("Element " + i), is(true));
            assertThat(testee.poll(), is(equalTo("Element " + i)));
        }
        assertThat(testee.isEmpty(), is(true));
    }

    @Test public void offeringBeyondCapacityShouldSucceed() {
        int entries = 0;
        for (int i = 0, is = 2 * CAPACITY; i < is; ++i) {
            if (testee.offer(ELEMENT)) ++entries;
        }
        assertThat(testee.size(), is(CAPACITY));
    }

    @Test public void puttingSingleElementShouldSucceed() throws InterruptedException {
        testee.put(ELEMENT);
        assertThat(testee.size(), is(1));
        assertThat(testee.take(), is(equalTo(ELEMENT)));
        assertThat(testee.isEmpty(), is(true));
    }

    @Test public void puttingToCapacityShouldSucceed() throws InterruptedException {
        for (int i = 0, is = CAPACITY; i < is; ++i) {
            testee.put(ELEMENT);
        }
        assertThat(testee.size(), is(CAPACITY));
    }

    @Test public void puttingAndTakingBeyondCapacityShouldSucceed() throws InterruptedException {
        for (int i = 0, is = 2 * CAPACITY; i < is; ++i) {
            testee.put("Element " + i);
            assertThat(testee.take(), is(equalTo("Element " + i)));
        }
        assertThat(testee.isEmpty(), is(true));
    }

    @Test public void puttingBeyondCapacityShouldGetLockedInfinitely() throws InterruptedException {
        for (int i = 0, is = CAPACITY; i < is; ++i) {
            testee.put(ELEMENT);
        }
        new Thread(() -> {
            try {
                Thread.sleep(1000);
                testee.remove();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
        long then = System.currentTimeMillis();
        testee.put(ELEMENT);
        assertThat(System.currentTimeMillis() - then - 1000 >= 0, is(true));
    }

    @Test public void TimedOfferingSingleElementShouldSucceedImmediately()
            throws InterruptedException {
        final long then = System.currentTimeMillis();
        assertThat(testee.offer(ELEMENT, 1, SECONDS), is(true));
        assertThat(testee.size(), is(1));
        assertThat(testee.poll(1, SECONDS), is(equalTo(ELEMENT)));
        assertThat(testee.isEmpty(), is(true));
        assertThat(System.currentTimeMillis() - then < 250, is(true));
    }

    @Test public void TimedOfferingToCapacityShouldSucceedImmediately()
            throws InterruptedException {
        final long then = System.currentTimeMillis();
        boolean result = true;
        for (int i = 0, is = CAPACITY; i < is; ++i) {
            result &= testee.offer(ELEMENT, 1, SECONDS);
        }
        assertThat(testee.size(), is(CAPACITY));
        assertThat(result, is(true));
        assertThat(System.currentTimeMillis() - then < 100, is(true));
    }

    @Test public void TimedOfferingAndPollingBeyondCapacityShouldSucceedImmediately()
            throws InterruptedException {
        final long then = System.currentTimeMillis();
        for (int i = 0, is = 2 * CAPACITY; i < is; ++i) {
            assertThat(testee.offer("Element " + i, 1, SECONDS), is(true));
            assertThat(testee.poll(1, SECONDS), is(equalTo("Element " + i)));
        }
        assertThat(testee.isEmpty(), is(true));
        assertThat(System.currentTimeMillis() - then < 1000, is(true));
    }

    @Test public void TimedOfferingBeyondCapacityShouldFailWithinTimeFrame()
            throws InterruptedException {
        final long then = System.currentTimeMillis();
        for (int i = 0, is = CAPACITY; i < is; ++i) {
            testee.put(ELEMENT);
        }
        assertThat(testee.offer(ELEMENT, 100, MILLISECONDS), is(false));
        assertThat(testee.size(), is(CAPACITY));
        assertThat(System.currentTimeMillis() - then >= 100, is(true));
    }

    @Test public void TimedOfferingBeyondCapacityShouldSucceedWhenInsertingDelayed()
            throws InterruptedException {
        for (int i = 0, is = CAPACITY; i < is; ++i) {
            testee.put(ELEMENT);
        }
        final long then = System.currentTimeMillis();
        new Thread(() -> {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            testee.remove();
        }).start();
        assertThat(testee.offer(ELEMENT, 1, SECONDS), is(true));
        assertThat(testee.size(), is(CAPACITY));
        assertThat(System.currentTimeMillis() - then >= 100, is(true));
        assertThat(System.currentTimeMillis() - then < 150, is(true));
    }

    @Test public void removingFromEmptyRingShouldFail() {
        try {
            testee.remove();
            fail("Expected " + NoSuchElementException.class.getSimpleName());
        } catch (NoSuchElementException e) {
            // expected that
        }
    }

    @Test public void pollingFromEmptyRingShouldSucceed() {
        assertThat(testee.poll(), is(nullValue()));
    }

    @Test public void TimedPollingFromEmptyRingShouldSucceed() throws InterruptedException {
        final long then = System.currentTimeMillis();
        assertThat(testee.poll(100, MILLISECONDS), is(nullValue()));
        assertThat(System.currentTimeMillis() - then >= 100, is(true));
    }

    @Test public void elementFromEmptyRingShouldFail() {
        try {
            testee.element();
            fail("Expected " + NoSuchElementException.class.getSimpleName());
        } catch (NoSuchElementException e) {
            // expected that
        }
    }

    @Test public void peekFromEmptyRingShouldSucceed() {
        assertThat(testee.peek(), is(nullValue()));
    }

    @Test public void elementFromRingWithDataShouldSucceed() {
        testee.add(ELEMENT);
        assertThat(testee.element(), is(equalTo(ELEMENT)));
        assertThat(testee.size(), is(1));
        assertThat(testee.element(), is(testee.remove()));
    }

    @Test public void peekFromRingWithDataShouldSucceed() {
        testee.add(ELEMENT);
        assertThat(testee.peek(), is(equalTo(ELEMENT)));
        assertThat(testee.size(), is(1));
        assertThat(testee.peek(), is(testee.remove()));
    }

    @Test public void peekFromFullRingShouldSucceed() {
        fillToCapacity();
        assertThat(testee.peek(), is(equalTo(ELEMENT)));
        assertThat(testee.size(), is(CAPACITY));
        assertThat(testee.peek(), is(testee.remove()));
    }

    @Test public void peekFromFullWrappedRingShouldSucceed() {
        fillToCapacity();
        testee.remove();
        testee.add(ELEMENT);
        assertThat(testee.peek(), is(equalTo(ELEMENT)));
        assertThat(testee.size(), is(CAPACITY));
        assertThat(testee.peek(), is(testee.remove()));
    }

    @Test public void offeringAndPollingBeyondCapacityThroughHalfFullRingShouldSucceed() {
        final AtomicInteger idgen = new AtomicInteger(0);
        for (int i = 0, is = CAPACITY / 2; i < is; ++i) {
            testee.add(ELEMENT);
        }
        for (int i = 0, is = 2 * CAPACITY; i < is; ++i) {
            assertThat(testee.offer(ELEMENT), is(true));
            assertThat(testee.poll(), is(equalTo(ELEMENT)));
        }
        assertThat(testee.size(), is(CAPACITY / 2));
    }

    @Test public void remainingCapacityShouldReportCorrectAmount() {
        assertThat(testee.remainingCapacity(), is(CAPACITY));
        for (int i = 0; i < 100; ++i) {
            testee.add(ELEMENT);
        }
        assertThat(testee.remainingCapacity(), is(CAPACITY - 100));
        for (int i = 0; i < 100; ++i) {
            testee.remove();
        }
        assertThat(testee.remainingCapacity(), is(CAPACITY));
        for (int i = 0; i < CAPACITY; ++i) {
            testee.add(ELEMENT);
        }
        assertThat(testee.remainingCapacity(), is(0));
    }

    @Test public void clearShouldSucceed() {
        fillToCapacity();
        testee.clear();
        assertThat(testee.isEmpty(), is(true));
        assertThat(testee.size(), is(0));
        testee.add(ELEMENT);
        assertThat(testee.isEmpty(), is(false));
        assertThat(testee.size(), is(1));
        testee.remove();
        assertThat(testee.isEmpty(), is(true));
        assertThat(testee.size(), is(0));
    }

    @Test public void interruptedTimedOfferAtCapacityShouldFail() {
        final Thread currentThread = Thread.currentThread();
        fillToCapacity();
        final long then = System.currentTimeMillis();
        interruptAfter100(currentThread);
        try {
            testee.offer(ELEMENT, 1, SECONDS);
            fail("Should have been interrupted");
        } catch (InterruptedException e) {
            // expected that
        } finally {
            assertThat(testee.size(), is(CAPACITY));
            assertThat(System.currentTimeMillis() - then >= 100, is(true));
            assertThat(System.currentTimeMillis() - then < 150, is(true));
        }
    }

    @Test public void interruptedPutAtCapacityShouldFail() {
        final Thread currentThread = Thread.currentThread();
        fillToCapacity();
        final long then = System.currentTimeMillis();
        interruptAfter100(currentThread);
        try {
            testee.put(ELEMENT);
            fail("Should have been interrupted");
        } catch (InterruptedException e) {
            // expected that
        } finally {
            assertThat(testee.size(), is(CAPACITY));
            assertThat(System.currentTimeMillis() - then >= 100, is(true));
            assertThat(System.currentTimeMillis() - then < 150, is(true));
        }
    }

    @Test public void interruptedTimedPollShouldFail() {
        final Thread currentThread = Thread.currentThread();
        final long then = System.currentTimeMillis();
        interruptAfter100(currentThread);
        try {
            testee.poll(1, SECONDS);
            fail("Should have been interrupted");
        } catch (InterruptedException e) {
            // expected that
        } finally {
            assertThat(testee.isEmpty(), is(true));
            assertThat(System.currentTimeMillis() - then >= 100, is(true));
            assertThat(System.currentTimeMillis() - then < 150, is(true));
        }
    }

    @Test public void interruptedTakeShouldFail() {
        final Thread currentThread = Thread.currentThread();
        final long then = System.currentTimeMillis();
        interruptAfter100(currentThread);
        try {
            testee.take();
            fail("Should have been interrupted");
        } catch (InterruptedException e) {
            // expected that
        } finally {
            assertThat(testee.isEmpty(), is(true));
            assertThat(System.currentTimeMillis() - then >= 100, is(true));
            assertThat(System.currentTimeMillis() - then < 150, is(true));
        }
    }

    @Test public void containsWithContainedShouldFindIt() {
        testee.add(ELEMENT);
        assertThat(testee.contains(ELEMENT), is(true));
    }

    @Test public void containsWithNotContainedShouldNotFindIt() {
        testee.add(ELEMENT);
        assertThat(testee.contains("XXX"), is(false));
    }

    @Test public void iteratorShouldSucceed() {
        for (int i = 0, is = CAPACITY / 2; i < is; ++i) {
            testee.add("Element " + i);
        }
        int n = 0;
        for (final Iterator<String> it = testee.iterator(); it.hasNext(); ) {
            assertThat(it.next(), is(equalTo("Element " + n++)));
        }
        assertThat(n, is(CAPACITY / 2));
    }

    @Test public void toArrayShouldSucceed() {
        Object[] array = testee.toArray();
        assertThat(array, is(not(nullValue())));
        assertThat(array.length, is(0));
        fillToCapacityCounted();
        array = testee.toArray();
        assertThat(array, is(not(nullValue())));
        assertThat(array.length, is(CAPACITY));
        for (int i = 0, is = array.length; i < is; ++i) {
            assertThat(array[i], is(equalTo("Element " + i)));
        }

    }

    @Test public void toTypedArrayShouldSucceed() {
        String[] array = testee.toArray(new String[0]);
        assertThat(array, is(not(nullValue())));
        assertThat(array.length, is(0));
        fillToCapacityCounted();
        array = testee.toArray(new String[CAPACITY]);
        assertThat(array, is(not(nullValue())));
        assertThat(array.length, is(CAPACITY));
        for (int i = 0, is = array.length; i < is; ++i) {
            assertThat(array[i], is(equalTo("Element " + i)));
        }

    }

    @Test public void drainToShouldSucceed() {
        fillToCapacity();
        for (int i = 0, is = CAPACITY / 2; i < is; ++i) {
            testee.remove();
        }
        List<String> sink = new ArrayList<>();
        testee.drainTo(sink);
        assertThat(sink.size(), is(CAPACITY / 2));
        assertThat(sink.stream().allMatch(ELEMENT::equals), is(true));
    }

    @Test public void limitedDrainToShouldSucceed() {
        fillToCapacity();
        List<String> sink = new ArrayList<>();
        testee.drainTo(sink, 15);
        assertThat(sink.size(), is(15));
        assertThat(sink.stream().allMatch(ELEMENT::equals), is(true));
    }

    @Test public void testConcurrency() throws InterruptedException {
        long elapsed = System.currentTimeMillis();
        final int agents = 10;
        ExecutorService exec = Executors.newFixedThreadPool(agents);
        for (int i = 0, is = 5; i < is; ++i) {
            exec.execute(new Producer());
        }
        for (int i = 0, is = 5; i < is; ++i) {
            exec.execute(new Consumer());
        }
        DONE.acquire(agents);
        elapsed = System.currentTimeMillis() - elapsed;
        final int values = IDGEN.get();
        System.out.printf("%d items written and retrieved in %d ms ⇒ %.3f μs per item", values,
                elapsed, (double)elapsed / values * 1000.0);
        assertThat(BITS.cardinality(), is(values));
    }

    private void interruptAfter100(final Thread currentThread) {
        new Thread(() -> {
            try {
                Thread.sleep(100);
                currentThread.interrupt();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void fillToCapacity() {
        for (int i = 0, is = CAPACITY; i < is; ++i) {
            testee.add(ELEMENT);
        }
    }

    private void fillToCapacityCounted() {
        for (int i = 0, is = CAPACITY; i < is; ++i) {
            testee.add("Element " + i);
        }
    }

    private class Producer implements Runnable {

        @Override public void run() {
            for (int i = 0, is = RANDOM.nextInt(10000); i < is; ++i) {
                try {
                    testee.put(String.valueOf(IDGEN.getAndIncrement()));
                } catch (InterruptedException e) {
                    break;
                }
            }
            DONE.release();
        }
    }

    private class Consumer implements Runnable {

        @Override public void run() {
            while (true) {
                try {
                    final String value = testee.poll(5, SECONDS);
                    if (value == null) break;
                    final int index = Integer.valueOf(value);
                    synchronized (BITS) {
                        BITS.set(index);
                    }
                } catch (InterruptedException e) {
                    break;
                }
            }
            DONE.release();
        }
    }

}
