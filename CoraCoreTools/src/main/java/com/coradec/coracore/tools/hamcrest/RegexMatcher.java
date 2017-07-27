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

package com.coradec.coracore.tools.hamcrest;

import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

/**
 * ​​Implementation of a hamcrest regex matcher.
 */
@SuppressWarnings("ClassHasNoToStringMethod")
public class RegexMatcher extends TypeSafeMatcher<String> {

    private final String pattern;

    public RegexMatcher(final String pattern) {
        this.pattern = pattern;
    }

    @Override protected boolean matchesSafely(final String item) {
        return item.matches(pattern);
    }

    @Override public void describeTo(final Description description) {
        description.appendText("matches \"" + pattern + "\"");
    }

    @Factory public static Matcher<String> matches(String pattern) {
        return new RegexMatcher(pattern);
    }

}
