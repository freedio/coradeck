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

package com.coradec.coradoc.ctrl;

import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;

/**
 * ​​A versatile character buffer.
 */
public final class CharBuffer implements CharSequence {

    int position;
    int limit;
    int capacity;
    int mark = -1;
    char[] array;

    private CharBuffer(final int capacity) {
        this.capacity = capacity;
        array = new char[capacity];
    }

    private CharBuffer(final char[] array, final int position, final int limit,
            final int capacity) {
        this.position = position;
        this.limit = limit;
        this.capacity = capacity;
        this.array = array;
    }

    /**
     * Allocates a character buffer with the specified capacity.
     *
     * @param capacity the buffer capacity.
     * @return a character buffer.
     */
    public static CharBuffer allocate(final int capacity) {
        return new CharBuffer(capacity);
    }

    /**
     * Clears the buffer by setting its position and limit to 0.
     */
    public void clear() {
        position = limit = 0;
        mark = -1;
    }

    /**
     * Checks if the character buffer has any remaining characters.
     *
     * @return {@code true} if the buffer is not empty, {@code false} if it's empty.
     */
    public boolean hasRemaining() {
        return remaining() > 0;
    }

    /**
     * Returns the number of remaining characters in the buffer.
     *
     * @return the number of remaining characters in the buffer.
     */
    private int remaining() {
        return limit - position;
    }

    /**
     * Returns the next character from the buffer.
     *
     * @return the next character.
     * @throws BufferUnderflowException if the buffer is empty.
     */
    public char get() throws BufferUnderflowException {
        if (!hasRemaining()) throw new BufferUnderflowException();
        return array[position++];
    }

    /**
     * Returns the current position.
     *
     * @return the current position.
     */
    public int position() {
        return position;
    }

    public CharBuffer position(final int i) {
        position = i;
        return this;
    }

    /**
     * Compacts the buffer by moving the current contents down to position 0, then setting the limit
     * to the length of the contents and the position to 0.  The mark is discarded.
     */
    public void compact() {
        if (position > 0 && limit > position) {
            System.arraycopy(array, position, array, 0, remaining());
        }
        limit -= position;
        position = 0;
    }

    /**
     * Prepares the buffer for reading the contents by setting the milit to the current position and
     * the position to 0.  The mark is discarded.
     */
    public void flip() {
        limit = position;
        position = 0;
        mark = -1;
    }

    /**
     * Opens the buffer at the current position by the specified amount of slots by moving the
     * remaining part upwards.  If the mark is greater than the current position, it is incremented
     * by the amount.  The limit is incremented by the specified amount.
     *
     * @param amount the spread.
     * @throws BufferOverflowException if the amount is too big and would push the contents beyond
     *                                 the capacity.
     */
    public void open(final int amount) throws BufferOverflowException {
        if (capacity - limit < amount) throw new BufferOverflowException();
        if (hasRemaining())
            System.arraycopy(array, position, array, position + amount, remaining());
        if (mark > position) mark += amount;
        limit += amount;
    }

    /**
     * Writes the specified character to the current position.
     *
     * @param c the character.
     */
    public void put(final char c) {
        if (!hasRemaining()) throw new BufferOverflowException();
        array[position++] = c;
        if (limit < position) limit = position;
    }

    /**
     * Copies the contents of the specified buffer to this buffer at the current position.   If
     * there are more chars remaining in the source buffer than in this buffer, that is, if
     * src.remaining() > remaining(), then no chars are transferred and a BufferOverflowException is
     * thrown.
     *
     * @param buffer the buffer to copy.
     * @throws BufferOverflowException if the specified buffer doesn't fit into this buffer.
     */
    public void put(final java.nio.CharBuffer buffer) throws BufferOverflowException {
//        if (buffer == this) throw new IllegalArgumentException();
        final int toCopy = buffer.remaining();
        if (remaining() < toCopy) throw new BufferOverflowException();
        if (buffer.hasArray()) {
            System.arraycopy(buffer.array(), buffer.arrayOffset(), array, position, toCopy);
            position += toCopy;
            buffer.position(buffer.position() + toCopy);
        } else for (int i = 0; i < toCopy; ++i) array[position++] = buffer.get();
        if (limit < position) limit = position;
    }

    /**
     * Copies the specified string to the buffer at the current position.
     *
     * @param s the string to copy.
     * @throws BufferOverflowException if the string doesn't fit into the buffer.
     */
    public void put(final String s) throws BufferOverflowException {
        final int toCopy = s.length();
        if (remaining() < toCopy) throw new BufferOverflowException();
        System.arraycopy(s.toCharArray(), 0, array, position, toCopy);
        position += toCopy;
        if (limit < position) limit = position;
    }

    public void put(final CharSequence sequence) {
        final int toCopy = sequence.length();
        if (remaining() < toCopy) throw new BufferOverflowException();
        for (int i = 0; i < toCopy; ++i) {
            array[position++] = sequence.charAt(i);
        }
        if (limit < position) limit = position;
    }

    @Override public int length() {
        return remaining();
    }

    @Override public char charAt(final int index) {
        return array[index];
    }

    @Override public CharSequence subSequence(final int start, final int end) {
        return new CharBuffer(array, position + start, position + end, capacity);
    }

    /**
     * Copies characters from this buffer's current position into the specified array.
     *
     * @param buffer the buffer to fill.
     */
    public void get(final char[] buffer) {
        final int toCopy = buffer.length;
        if (toCopy > remaining()) throw new BufferUnderflowException();
        System.arraycopy(array, position, buffer, 0, toCopy);
        position += toCopy;
    }

    @Override public String toString() {
        return new String(array, position, remaining());
    }

    public void rewind() {
        position = 0;
    }

    public boolean isEmpty() {
        return !hasRemaining();
    }

}
