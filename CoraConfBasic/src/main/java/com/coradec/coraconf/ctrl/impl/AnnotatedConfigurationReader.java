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

package com.coradec.coraconf.ctrl.impl;

import com.coradec.coraconf.model.AnnotatedProperty;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Optional;

/**
 * Configurable reader for property files in the standard property file format, as known from Java
 * .properties files or Unix configuration files.
 */
@SuppressWarnings("WeakerAccess")
public abstract class AnnotatedConfigurationReader extends BasicConfigurationReader {

//    private static final Log LOG = UniLog.forClass(AnnotatedConfigurationReader.class);

    protected String SEPARATORS() {
        return ":=";
    }

    protected String COMMENTS() {
        return "#!";
    }

    protected char ESCAPE_CHAR() {
        return '\\';
    }

    protected String OPENINGQUOTES() {
        return "'\"“‘„‚";
    }

    protected String CLOSINGQUOTES() {
        return "'\"”’“‘";
    }

    protected static final char NUL = '\0';
    protected static final char CR = '\r';
    protected static final char LF = '\n';

    protected String UNESCAPED() {
        return "abfnrt\\'\"0";
    }

    protected String ESCAPED() {
        return "\7\b\f\n\r\t\\'\"\0";
    }

    protected static final String NEWLINE = System.getProperty("line.separator");

    private BufferedReader reader;
    private final StringBuilder fileComment = new StringBuilder();

    public AnnotatedConfigurationReader(final String context, final URL resource) {
        super(context, resource);
    }

    public Optional<String> getFileComment() {
        return Optional.ofNullable(
                this.fileComment.length() == 0 ? null : this.fileComment.toString());
    }

    @Override protected void open() throws IOException {
        this.reader = new BufferedReader(new InputStreamReader(getResourceAsStream()));
    }

    @Override protected void close() throws IOException {
        this.reader.close();
    }

    @Override protected Optional<AnnotatedProperty> getNextProperty() {
        final BufferedReader reader = this.reader;
        try {
            boolean escaped = false, octescape = false, skipSpaces = false;
            int quoted = -1, hexescape = 0, octEscape = 0, hexEscape = 0;
            char last = NUL;
            final StringBuilder comment = new StringBuilder(), value = new StringBuilder(), key =
                    new StringBuilder();
            StringBuilder collector = key;
            for (int ic = reader.read(); ic != -1; ic = reader.read()) {
                char c = (char)ic;
                if (skipSpaces && Character.isWhitespace(c)) continue;
                skipSpaces = false;
                if (hexescape > 0) {
                    int digit = -1;
                    if (c >= 'A' && c < 'G') digit = c - 'A' + 10;
                    else if (c >= 'a' && c < 'g') digit = c - 'a' + 10;
                    else if (c >= '0' && c <= '9') digit = c - '0';
                    if (digit == -1) {
                        collector.append((char)hexEscape);
                        hexescape = 0;
                    } else {
                        hexEscape = 16 * hexEscape + digit;
                        if (--hexescape == 0) collector.append((char)hexEscape);
                        continue;
                    }
                } else if (octescape) {
                    if (c >= '0' && c < '8') {
                        final int digit = c - '0';
                        if (8 * octEscape + digit < 256) {
                            octEscape = 8 * octEscape + digit;
                            continue;
                        }
                    }
                    collector.append((char)octEscape);
                    octescape = false;
                }
                if (escaped) {
                    escaped = false;
                    if (c == '\r' || c == '\n') {
                        skipSpaces = true;
                    } else if (c >= '0' && c < '8') {
                        octescape = true;
                        octEscape = c - '0';
                    } else if (c == 'x' || c == 'X') {
                        hexescape = 2;
                        hexEscape = 0;
                    } else if (c == 'u' || c == 'U') {
                        hexescape = 4;
                        hexEscape = 0;
                    } else {
                        final int i = UNESCAPED().indexOf(c);
                        if (i != -1) c = ESCAPED().charAt(i);
                        collector.append(c);
                    }
                    continue;
                }
                if (c == ESCAPE_CHAR()) {
                    escaped = true;
                } else if (COMMENTS().indexOf(c) != -1 && collector.toString().trim().isEmpty()) {
                    collector = comment;
                    skipSpaces = true;
                } else if (quoted != -1) {
                    if (c == CLOSINGQUOTES().charAt(quoted)) quoted = -1;
                    else collector.append(c);
                } else if ((quoted = OPENINGQUOTES().indexOf(c)) != -1) {
                    continue;
                } else if (collector == key && SEPARATORS().indexOf(c) != -1) {
                    collector = value;
                } else if (c == CR || c == LF && last != CR) {
                    if ((collector == comment || collector.length() == 0) && last == LF) {
                        this.fileComment.append(comment);
                        comment.setLength(0);
                    }
                    if (collector == comment) collector.append(NEWLINE);
                    if (key.length() != 0 && value.length() != 0) {
                        String _key = key.toString().trim();
                        final String _value = trimValue(value.toString());
                        String _type = String.class.getName();
                        if (_key.startsWith("(")) {
                            final int d = _key.indexOf(") ");
                            if (d != -1) {
                                _type = _key.substring(1, d).trim();
                                _key = _key.substring(d + 2).trim();
                            }
                        }
                        final String _comment =
                                comment.length() == 0 ? null : comment.toString().trim();
                        return Optional.of(createPropertyFrom(_key, _type, _value, _comment));
                    }
                    collector = key;
                } else {
                    collector.append(c);
                }
                last = c;
            }
        }
        catch (final IOException e) {
            discloseStringExtensions().warn(here(), e, "Failed to read from ‹%s›", getResource());
        }
        return Optional.empty();
    }

    /**
     * Trims the value field according to the rules.
     *
     * @param value the value.
     * @return the trimmed value.
     */
    protected String trimValue(final String value) {
        return value.trim();
    }

}
