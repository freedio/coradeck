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

package com.coradec.coradoc.model;

import com.coradec.coracore.model.Origin;
import com.coradec.coracore.model.impl.URLigin;
import com.coradec.coracore.util.StringUtil;
import com.coradec.coradoc.model.impl.BasicDocument;
import com.coradec.coradoc.trouble.DocumentException;
import com.coradec.coradoc.trouble.DocumentReadFailure;
import com.coradec.coradoc.trouble.EndOfDocumentException;

import java.io.IOException;
import java.net.URL;
import java.util.function.Predicate;

/**
 * A generic document.
 */
public interface Document {

    static Document from(URL source) throws DocumentException {
        try {
            return new BasicDocument(new URLigin(source), source.openStream(), StringUtil.UTF8);
        } catch (IOException e) {
            throw new DocumentReadFailure(source.toString(), e);
        }
    }

    /**
     * Skips characters up to the next non-blank or EOD.
     *
     * @return the number of blanks skipped.
     */
    int skipBlanks();

    /**
     * Checks if the next upcoming characters equal the specified character sequence.
     * <p>
     * If the next upcoming characters match the specified character sequence, they are removed (the
     * cursor jumps beyond them to the next character).
     *
     * @param next the next characters expected.
     * @return {@code true} if the next characters are expected, {@code false} if other characters
     * were encountered.
     */
    boolean isNext(CharSequence next);

    /**
     * Checks if the next upcoming character matches the specified rule (without consuming it).
     *
     * @param rule the rule the next character must match to comply.
     * @return {@code true} if the next character matches the rule, {@code false} if not.
     */
    boolean nextIs(Predicate<Character> rule);

    /**
     * Checks if the next upcoming characters differ from the specified character sequence.
     *
     * @param next the next characters NOT expected.
     * @return {@code true} if the next characters did not occur, {@code false} if they occurred.
     */
    boolean isNextNot(CharSequence next);

    /**
     * Returns the next character of the document and consumes it.
     *
     * @return the next character.
     */
    char nextChar() throws EndOfDocumentException;

    /**
     * Returns the next character of the document without consuming it.
     *
     * @return the next character.
     */
    char peek() throws EndOfDocumentException;

    /**
     * Returns the next <i>n</i> characters of the document without consuming them.
     *
     * @param n the number of character to look ahead.
     * @return next next <i>n</i> characters.
     * @throws EndOfDocumentException if the end of the document was reached before the next
     *                                <i>n</i> characters.
     */
    CharSequence peek(int n) throws EndOfDocumentException;

    /**
     * Returns the origin of the document.
     *
     * @return the document's origin.
     */
    Origin getOrigin();

    /**
     * Returns the current line number in the document.
     *
     * @return the current line.
     */
    int getLine();

    /**
     * Returns the current column on the current line in the document.
     *
     * @return the current column.
     */
    int getColumn();

    /**
     * Reads characters from the document while the document still has characters conforming to the
     * specified predicate.
     *
     * @param valid the predicate for valid characters.
     * @return a sequence of characters valid according to the specified predicate.
     */
    CharSequence readWhile(Predicate<Character> valid);

    /**
     * Reads characters from the document while the document still has characters conforming to the
     * specified predicate, but stop when the total number of valid characters reaches the specified
     * limit.
     *
     * @param valid the predicate for valid characters.
     * @param limit the maxmum number of characters to read.
     * @return a sequence of characters valid according to the specified predicate.
     */
    CharSequence readWhile(Predicate<Character> valid, int limit);

    /**
     * Reads characters from the document until the predicate returns {@code true}.
     *
     * @param terminator the predicate for terminator characters.
     * @return a sequence of characters valid according to the specified predicate.
     */
    CharSequence readUntil(Predicate<Character> terminator);

    /**
     * Reads characters from the document up to the specified terminator sequence, then swallows the
     * terminator encountered.
     *
     * @param terminator the terminator sequence.
     * @return a sequence of characters.
     */
    CharSequence readUpto(CharSequence terminator);

    /**
     * Returns the tabulation width of the document.
     *
     * @return the tabulation width of the document.
     */
    int getTabwidth();

    /**
     * Sets the tabulation width of the document to the specified value.
     *
     * @param tabwidth the new tabulation width.
     */
    void setTabwidth(int tabwidth);

    /**
     * Checks if the document is at its end.
     *
     * @return {@code true} if no more characters can be read from the document, {@code false} if
     * there are more characters to read.
     */
    boolean isFinished();

    /**
     * Checks if the document deems the specified character as whitespace.
     *
     * @param c the character.
     * @return {@code true} if the document deems the character as whitespace, {@code false} if not.
     */
    boolean isBlank(char c);

    /**
     * Pushes the specified character back to the document.
     *
     * @param c the character to push bacl.
     */
    void pushback(char c);

}
