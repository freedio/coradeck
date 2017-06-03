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

package com.coradec.coracore.trouble;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;

import com.coradec.coracore.annotation.ToString;
import org.junit.Test;

public class BasicExceptionTest {

    @Test public void testEmptyMessageWithoutArguments() {
        // Arrange
        BasicException testee = new NoMessageNoArgsException();
        // Act:
        String message = testee.getMessage();
        // Assert:
        assertThat(message, is(""));
    }

    @Test public void testNonEmptyMessageWithoutArguments() {
        // Arrange
        BasicException testee = new MessageNoArgsException();
        // Act:
        String message = testee.getMessage();
        // Assert:
        assertThat(message, is("This is a message"));
    }

    @Test public void testEmptyMessageWithArguments() {
        // Arrange
        BasicException testee = new NoMessageWithArgsException("Test", 42);
        // Act:
        String message = testee.getMessage();
        // Assert:
        assertThat(message, is("(Text: \"Test\", Number: 42)"));
    }

    @Test public void testNonEmptyMessageWithArguments() {
        // Arrange
        BasicException testee = new MessageWithArgsException("Test", 42);
        // Act:
        String message = testee.getMessage();
        // Assert:
        assertThat(message, is("(Text: \"Test\", Number: 42): This is a message"));
    }

    private class NoMessageNoArgsException extends BasicException {

    }

    private class MessageNoArgsException extends BasicException {

        MessageNoArgsException() {
            super("This is a message");
        }
    }

    private class NoMessageWithArgsException extends BasicException {

        private final String text;
        private final int number;
        private final Object internal;

        NoMessageWithArgsException(final String text, final int number) {
            this.text = text;
            this.number = number;
            this.internal = new Object();
        }

        @ToString public String getText() {
            return this.text;
        }

        @ToString public int getNumber() {
            return this.number;
        }

        public Object getInternal() {
            return this.internal;
        }
    }

    private class MessageWithArgsException extends BasicException {

        private final String text;
        private final int number;
        private final Object internal;

        MessageWithArgsException(final String text, final int number) {
            super("This is a message");
            this.text = text;
            this.number = number;
            internal = new Object();
        }

        @ToString public String getText() {
            return this.text;
        }

        @ToString public int getNumber() {
            return this.number;
        }

        public Object getInternal() {
            return this.internal;
        }
    }
}
