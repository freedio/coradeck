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

import com.coradec.coracore.annotation.Implementation;
import com.coradec.coracore.annotation.NonNull;
import com.coradec.coracore.annotation.Nullable;
import com.coradec.coracore.collections.Ring;
import com.coradec.coracore.trouble.CapacityExhaustedException;
import com.coradec.coracore.trouble.OperationInterruptedException;
import com.coradec.coracore.util.ClassUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * ​A fixed array implementation of a ring.
 * <p>
 * ArrayRing tolerates {@code null} elements and is thread safe, but supports only queue
 * operations.
 */
@SuppressWarnings({
                          "PackageVisibleField", "WeakerAccess", "unchecked",
                          "ForLoopReplaceableByForEach"
                  })
@Implementation
public class ArrayRing<T> implements Ring<T> {

    private static final Object RETRIEVED = new Object();
    private final T[] ring;
    private final AtomicInteger in;
    private final AtomicInteger out;
    final Semaphore items = new Semaphore(0, true);
    private final Semaphore slots = new Semaphore(0, true);
    private final int capacity;

    public ArrayRing(int capacity) {
        this.capacity = capacity;
        ring = (T[])new Object[capacity];
        Arrays.fill(ring, RETRIEVED);
        slots.release(capacity);
        in = new AtomicInteger(-1);
        out = new AtomicInteger(-1);
    }

    @Override public boolean add(final T t) throws CapacityExhaustedException {
        try {
            if (slots.tryAcquire(0, MILLISECONDS)) {
                insertItem(t);
                return true;
            }
            throw new CapacityExhaustedException();
        } catch (InterruptedException e) {
            throw new OperationInterruptedException();
        }
    }

    @Override public boolean offer(@NonNull final T t) {
        try {
            if (slots.tryAcquire(0, MILLISECONDS)) {
                insertItem(t);
                return true;
            }
            return false;
        } catch (InterruptedException e) {
            throw new OperationInterruptedException();
        }
    }

    @Override public T remove() throws NoSuchElementException {
        try {
            if (items.tryAcquire(0, MILLISECONDS)) {
                return retrieveItem();
            }
            throw new NoSuchElementException();
        } catch (InterruptedException e) {
            throw new OperationInterruptedException();
        }
    }

    @Override public @Nullable T poll() {
        try {
            if (items.tryAcquire(0, MILLISECONDS)) {
                return retrieveItem();
            }
            return null;
        } catch (InterruptedException e) {
            throw new OperationInterruptedException();
        }
    }

    @Override public T element() throws NoSuchElementException {
        try {
            if (items.tryAcquire(0, MILLISECONDS)) {
                final T result = ring[getItemNumber()];
                items.release();
                return result;
            }
            throw new NoSuchElementException();
        } catch (InterruptedException e) {
            throw new OperationInterruptedException();
        }
    }

    @Override public @Nullable T peek() {
        try {
            if (items.tryAcquire(0, MILLISECONDS)) {
                final T result = ring[getItemNumber()];
                items.release();
                return result;
            }
            return null;
        } catch (InterruptedException e) {
            throw new OperationInterruptedException();
        }
    }

    @Override public void put(final T t) throws InterruptedException {
        slots.acquire();
        insertItem(t);
    }

    @Override public boolean offer(final T t, final long timeout, @NonNull final TimeUnit unit)
            throws InterruptedException {
        if (slots.tryAcquire(timeout, unit)) {
            insertItem(t);
            return true;
        }
        return false;
    }

    @Override public T take() throws InterruptedException {
        items.acquire();
        return retrieveItem();
    }

    @Override public @Nullable T poll(final long timeout, @NonNull final TimeUnit unit)
            throws InterruptedException {
        if (items.tryAcquire(timeout, unit)) {
            return retrieveItem();
        }
        return null;
    }

    @Override public int remainingCapacity() {
        return slots.availablePermits();
    }

    @Override public boolean remove(final Object o) {
        throw new UnsupportedOperationException();
    }

    @Override public boolean containsAll(@NonNull final Collection<?> c)
            throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    @Override public boolean addAll(@NonNull final Collection<? extends T> c)
            throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    @Override public boolean removeAll(@NonNull final Collection<?> c)
            throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    @Override public boolean retainAll(@NonNull final Collection<?> c)
            throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    @Override public void clear() {
        slots.drainPermits();
        items.drainPermits();
        in.set(-1);
        out.set(-1);
        Arrays.fill(ring, RETRIEVED);
        slots.release(capacity);
    }

    @Override public int size() {
        return items.availablePermits();
    }

    @Override public boolean isEmpty() {
        return size() == 0;
    }

    @Override public boolean contains(final Object o) {
        return stream().anyMatch(o::equals);
    }

    @NonNull @Override public Iterator<T> iterator() {
        return new RingIterator();
    }

    @NonNull @Override public Object[] toArray() {
        List<Object> objects = new ArrayList<>();
        for (final Iterator<T> it = iterator(); it.hasNext(); ) {
            objects.add(it.next());
        }
        return objects.toArray();
    }

    @SuppressWarnings("SuspiciousToArrayCall") @Override
    public @NonNull <U> U[] toArray(@NonNull final U[] a) {
        List<Object> objects = new ArrayList<>();
        for (final Iterator<T> it = iterator(); it.hasNext(); ) {
            objects.add(it.next());
        }
        return objects.toArray(a);
    }

    @Override public int drainTo(@NonNull final Collection<? super T> c) {
        return drainTo(c, Integer.MAX_VALUE);
    }

    @Override public int drainTo(@NonNull final Collection<? super T> c, final int maxElements) {
        int count = 0;
        if (c == this) throw new IllegalArgumentException();
        while (items.availablePermits() > 0 && count < maxElements) {
            c.add(remove());
            ++count;
        }
        return count;
    }

    private void insertItem(final T t) {
        final int slot = nextIn();
        ring[slot] = t;
        items.release();
    }

    private T retrieveItem() {
        final int slot = nextOut();
        final T result = ring[slot];
        slots.release();
        return result;
    }

    /**
     * Returns the slot number of the next addition.
     *
     * @return the next input slot number.
     */
    public int getSlotNumber() {
        return (in.get() + 1) % capacity;
    }

    /**
     * Returns the slot number of the next entry.
     *
     * @return the next output slot number.
     */
    public int getItemNumber() {
        return (out.get() + 1) % capacity;
    }

    private int nextIn() {
        return in.accumulateAndGet(1, (left, right) -> (left + right) % capacity);
    }

    private int nextOut() {
        return out.accumulateAndGet(1, (left, right) -> (left + right) % capacity);
    }

    private class RingIterator implements Iterator<T> {

        @Override public boolean hasNext() {
            return items.availablePermits() > 0;
        }

        @Override public T next() {
            return ArrayRing.this.remove();
        }

    }

    @Override public String toString() {
        return ClassUtil.toString(this);
    }
}
