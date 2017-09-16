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

package com.coradec.coradoc.model.impl;

import com.coradec.coracore.model.Origin;
import com.coradec.coracore.util.ClassUtil;
import com.coradec.coradoc.ctrl.Cbuffer;
import com.coradec.coradoc.model.Document;
import com.coradec.coradoc.trouble.DocumentReadFailure;
import com.coradec.coradoc.trouble.EndOfDocumentException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.function.Predicate;

/**
 * ​​Basic implementation of a (generic) document.
 */
public class BasicDocument implements Document {

    private final Origin origin;
    private final BufferedReader source;
    private final Cbuffer pushback = Cbuffer.allocate(65536);
    private int tabwidth = 8;
    private int page = 0, line = 0, column = 0;
    private int lastpage = 0, lastline = 0, lastcolumn = 0;

    /**
     * Initializes a new instance of BasicDocument from the specified origin through the specified
     * source with the specified encoding.
     * <p>
     * The encoding can be changed later; e.g. XML documents may have an explicit encoding specified
     * in their header, which will cause the encoding to change after the prologue has been
     * processed.
     *
     * @param origin   the origin of the document.
     * @param source   the document source.
     * @param encoding the (assumed) encoding of the document.
     */
    public BasicDocument(final Origin origin, final InputStream source, final Charset encoding) {
        this.origin = origin;
        this.source = new BufferedReader(new InputStreamReader(source, encoding));
        pushback.clear();
    }

    @Override public int skipBlanks() {
        int skipped = 0;
        char c;
        try {
            for (c = nextChar(); isBlank(c); c = nextChar()) ++skipped;
            pushback(c);
        } catch (EndOfDocumentException e) {
            // can't skip any further, but don';t report an error.
        }
        return skipped;
    }

    @Override public boolean isNext(final CharSequence next) {
        CharBuffer buffer = CharBuffer.allocate(next.length());
        buffer.clear();
        flood(buffer);
        boolean result = equal(next, buffer);
        if (!result) pushback(buffer);
        else consume(buffer);
        return result;
    }

    @Override public boolean isNextNot(final CharSequence next) {
        CharBuffer buffer = CharBuffer.allocate(next.length());
        buffer.clear();
        flood(buffer);
        boolean result = !equal(next, buffer);
        pushback(buffer);
        return result;
    }

    @Override public char nextChar() throws EndOfDocumentException {
        int c;
        try {
            c = pushback.hasRemaining() ? pushback.get() : source.read();
            if (c == -1) throw new EndOfDocumentException();
            return updatePosition((char)c);
        } catch (IOException e) {
            throw new DocumentReadFailure(e);
        }
    }

    @Override public Origin getOrigin() {
        return origin;
    }

    @Override public int getLine() {
        return line;
    }

    @Override public int getColumn() {
        return column;
    }

    @Override public CharSequence readWhile(final Predicate<Character> valid) {
        StringBuilder collector = new StringBuilder();
        char c;
        for (c = nextChar(); valid.test(c); c = nextChar()) collector.append(c);
        pushback(c);
        return collector;
    }

    @Override public CharSequence readUntil(final Predicate<Character> terminator) {
        StringBuilder collector = new StringBuilder();
        char c;
        for (c = nextChar(); !terminator.test(c); c = nextChar()) collector.append(c);
        pushback(c);
        return collector;
    }

    @Override public CharSequence readUpto(final CharSequence terminator) {
        StringBuilder collector = new StringBuilder();
        char c;
        for (c = nextChar(); !endsWith(collector, terminator); c = nextChar()) collector.append(c);
        pushback(c);
        collector.setLength(collector.length() - terminator.length());
        return collector;
    }

    @Override public int getTabwidth() {
        return tabwidth;
    }

    @Override public void setTabwidth(final int tabwidth) {
        this.tabwidth = tabwidth;
    }

    @Override public boolean isFinished() {
        try {
            return pushback.isEmpty() && source.read() == -1;
        } catch (IOException e) {
            throw new DocumentReadFailure();
        }
    }

    /**
     * Checks if the specified character is to be considered a blank.
     * <p>
     * The standard document assumes that {@link Character#isWhitespace(char)} is sufficient for
     * testing this, but derived document types (such as XmlDocument) may have other criteria for a
     * character being a blank.
     *
     * @param c the character to test.
     * @return {@code true} if the character is to be considered a blank, {@code false} if not.
     */
    protected boolean isBlank(final char c) {
        return Character.isWhitespace(c);
    }

    /**
     * Pushes the specified character back to the input.
     *
     * @param c the character.
     */
    private void pushback(final char c) {
        pushback.compact();
        pushback.open(1);
        pushback.put(c);
        pushback.rewind();
        reset();
    }

    /**
     * Pushes the specified buffer back to the input.
     *
     * @param buffer the buffer.
     */
    private void pushback(final CharBuffer buffer) {
        pushback.compact();
        pushback.open(buffer.remaining());
        pushback.put(buffer);
        pushback.rewind();
        reset();
    }

    /**
     * Pushes the specified character sequence back to the input.
     *
     * @param sequence the character sequence.
     */
    private void pushback(final CharSequence sequence) {
        pushback.compact();
        pushback.put(sequence);
        pushback.flip();
        reset();
    }

    /**
     * Floods the specified buffer with characters from the pushback and the underlying document.
     *
     * @param buffer the buffer.
     * @throws DocumentReadFailure if the document could not be read.
     */
    private void flood(final CharBuffer buffer) throws DocumentReadFailure {
        mark();
        final int available = Integer.min(pushback.length(), buffer.remaining());
        char[] xfer = new char[available];
        pushback.get(xfer);
        buffer.put(xfer);
        if (buffer.hasRemaining()) try {
            if (source.read(buffer) == -1) throw new EndOfDocumentException();
        } catch (IOException e) {
            throw new DocumentReadFailure(e);
        }
        buffer.flip();
    }

    private void mark() {
        lastpage = page;
        lastline = line;
        lastcolumn = column;
    }

    private void reset() {
        page = lastpage;
        line = lastline;
        column = lastcolumn;
    }

    /**
     * Updates the line and column number according to the specified character.
     * <p>
     * The following characters have a special meaning beyond just incrementing the column number by
     * 1: <ul> <li>CR (\r) sets the column to 0.</li> <li>LF (\n) sets the column to 0 and
     * increments the line number.</li> <li>VT (\t) sets the column to the next multiple of
     * <i>tabwidth</i>.</li> <li>FF (\f) sets the line and column to 0 and increments the page
     * number.</li> <li>BS (\b) decrements the column number (even if it gets negative).</li>
     * <li>BEL (\a) does not change the column number.</li> </ul>
     *
     * @param c the character.
     * @return the character.
     */
    private char updatePosition(final char c) {
        lastpage = page;
        lastline = line;
        lastcolumn = column;
        switch (c) {
            case '\r':
                column = 0;
                break;
            case '\n':
                column = 0;
                ++line;
                break;
            case '\t':
                column += tabwidth - column % tabwidth;
                break;
            case '\f':
                column = 0;
                line = 0;
                ++page;
                break;
            case '\b':
                --column;
            case '\7':
                break;
            default:
                ++column;
        }
        return c;
    }

    private void consume(final CharBuffer buffer) {
        while (buffer.hasRemaining()) updatePosition(buffer.get());
    }

    /**
     * Checks if the first character sequences ends with the second one.
     *
     * @param b1 the first character sequence,
     * @param b2 the second character sequence.
     * @return {@code true} if the first characters sequence ends with the second one, else {@code
     * false}.
     */
    private boolean endsWith(final CharSequence b1, final CharSequence b2) {
        final int l1 = b1.length(), l2 = b2.length();
        return l1 >= l2 && equal(b1.subSequence(l1 - l2, l1), b2);
    }

    @Override public String toString() {
        return ClassUtil.toString(this);
    }

    /**
     * Compares the specified character sequences for equality.
     *
     * @param b1 the first character sequence.
     * @param b2 the second character sequence.
     * @return {@code true} if both character sequences are equal in length and contents.
     */
    private boolean equal(final CharSequence b1, final CharSequence b2) {
        return b1.length() == b2.length() && sameChars(b1.length(), b1, b2);
    }

    /**
     * Checks if the specified character sequences contain the same characters in the same order up
     * to the specified length.
     *
     * @param length the length.
     * @param b1     the first character sequence.
     * @param b2     the second character sequence.
     * @return {@code true} if both character sequences have the same characters in the same order
     * up to the specified length.
     */
    private boolean sameChars(final int length, final CharSequence b1, final CharSequence b2) {
        for (int i = 0; i < length; ++i) if (b1.charAt(i) != b2.charAt(i)) return false;
        return true;
    }

}
