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

package com.coradec.coradoc.ctrl.impl;

import static com.coradec.coradoc.state.Match.*;

import com.coradec.coracore.annotation.Nullable;
import com.coradec.coradoc.cssenum.CssUnit;
import com.coradec.coradoc.ctrl.CssTokenizer;
import com.coradec.coradoc.model.CssDocument;
import com.coradec.coradoc.model.DocumentModel;
import com.coradec.coradoc.model.ParserToken;
import com.coradec.coradoc.token.AtKeyword;
import com.coradec.coradoc.token.BadStringToken;
import com.coradec.coradoc.token.BadUrlToken;
import com.coradec.coradoc.token.BracketClose;
import com.coradec.coradoc.token.BracketOpen;
import com.coradec.coradoc.token.CDC;
import com.coradec.coradoc.token.CDO;
import com.coradec.coradoc.token.Colon;
import com.coradec.coradoc.token.Column;
import com.coradec.coradoc.token.Comma;
import com.coradec.coradoc.token.CurlyClose;
import com.coradec.coradoc.token.CurlyOpen;
import com.coradec.coradoc.token.Delimiter;
import com.coradec.coradoc.token.EOF;
import com.coradec.coradoc.token.FunctionToken;
import com.coradec.coradoc.token.HashToken;
import com.coradec.coradoc.token.Identifier;
import com.coradec.coradoc.token.IntegerDimension;
import com.coradec.coradoc.token.IntegerPercentage;
import com.coradec.coradoc.token.IntegerValue;
import com.coradec.coradoc.token.Matcher;
import com.coradec.coradoc.token.ParenthClose;
import com.coradec.coradoc.token.ParenthOpen;
import com.coradec.coradoc.token.RealDimension;
import com.coradec.coradoc.token.RealPercentage;
import com.coradec.coradoc.token.RealValue;
import com.coradec.coradoc.token.Semicolon;
import com.coradec.coradoc.token.StringToken;
import com.coradec.coradoc.token.UnicodeRange;
import com.coradec.coradoc.token.UrlToken;
import com.coradec.coradoc.token.Whitespace;
import com.coradec.coradoc.trouble.CssUnitUnknownException;
import com.coradec.coradoc.trouble.EndOfDocumentException;
import com.coradec.coratext.model.LocalizedText;
import com.coradec.coratext.model.Text;

/**
 * ​​Basic implementation of a CSS tokenizer.
 */
@SuppressWarnings("ClassHasNoToStringMethod")
public class BasicCssTokenizer<M extends DocumentModel> implements CssTokenizer {

    private static final Text TEXT_PARSER_FALLTHROUGH = LocalizedText.define("ParserFallThrough");

    private final CssDocument document;
    private final M model;

    public BasicCssTokenizer(final CssDocument document, final M model) {
        this.document = document;
        this.model = model;
    }

    @Override public @Nullable ParserToken next() throws EndOfDocumentException {
        String name, number;
        StringBuilder collector;
        char c;
        try {
            final int blanks = document.skipBlanks();
            if (blanks > 0) return new Whitespace(blanks);
            ParserToken result;
            collector = new StringBuilder();
            c = document.nextChar();
        } catch (EndOfDocumentException e) {
            return new EOF();
        }
        switch (c) {
            case '"':
            case '\'':
                return parseStringToken(c);
            case '#':
                name = String.valueOf(document.readWhile(this::isIdentifierPart));
                return name.isEmpty() ? new Delimiter('#') : new HashToken(name);
            case '$':
                return document.isNext("=") ? new Matcher(SUFFIX) : new Delimiter('$');
            case '(':
                return new ParenthOpen();
            case ')':
                return new ParenthClose();
            case '*':
                return document.isNext("=") ? new Matcher(SUBSTRING) : new Delimiter('*');
            case '-':
                if (document.isNext("->")) return new CDC();
                if (isIdentifierStart(document.peek())) {
                    collector.append('-')
                             .append(document.nextChar())
                             .append(document.readWhile(this::isIdentifierPart));
                    name = collector.toString();
                    collector.setLength(0);
                    return new Identifier(name);
                }
//                break;
            case '+':
                return String.valueOf(document.peek(2)).matches("\\.?[0-9].?") ? parseNumber(c)
                                                                               : new Delimiter(c);
            case '0':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
                return parseNumber(c);
            case ',':
                return new Comma();
            case '.':
                if (document.nextIs(ch -> ch >= '0' && ch <= '9')) {
                    return parseNumber(c);
                }
                return new Delimiter(c);
            case '/':
                if (document.isNext("*")) {
                    model.onComment(String.valueOf(document.readUpto("*/")));
                    return next();
                }
                return new Delimiter(c);
            case ':':
                return new Colon();
            case ';':
                return new Semicolon();
            case '<':
                return document.isNext("!--") ? new CDO() : new Delimiter(c);
            case '@':
                return isIdentifierStart(document.peek(3)) ? new AtKeyword(parseName())
                                                           : new Delimiter(c);
            case '[':
                return new BracketOpen();
            case ']':
                return new BracketClose();
            case '^':
                return document.isNext("=") ? new Matcher(PREFIX) : new Delimiter(c);
            case '{':
                return new CurlyOpen();
            case '}':
                return new CurlyClose();
            case 'u':
            case 'U':
                if (String.valueOf(document.peek(2)).matches("\\+[0-9a-fA-F?]")) {
                    document.nextChar();
                    return parseUnicodeRange();
                }
                document.pushback(c);
                return parseIdentLike();
            case '|':
                if (document.isNext("=")) return new Matcher(DASH);
                if (document.isNext("|")) return new Column();
                return new Delimiter(c);
            case '~':
                if (document.isNext("=")) return new Matcher(INCLUDE);
                return new Delimiter(c);
            default:
                if (isIdentifierStart(c)) {
                    document.pushback(c);
                    return parseIdentLike();
                }
                return new Delimiter(c);
        }
    }

    private ParserToken parseIdentLike() {
        final String name = parseName();
        return document.isNext("(") //
               ? (name.equalsIgnoreCase("url")) //
                 ? parseURL() //
                 : new FunctionToken(name) //
               : new Identifier(name);
    }

    private UrlToken parseURL() {
        final UrlToken result = new UrlToken();
        document.skipBlanks();
        if (document.isFinished()) return result;
        char c = document.nextChar();
        if (c == '"' || c == '\'') {
            final StringToken stringToken = parseStringToken(c);
            if (stringToken instanceof BadStringToken) {
                return cleanupBadURL();
            }
            result.setValue(stringToken.getValue());
            document.skipBlanks();
            if (document.isFinished() || document.isNext(")")) return result;
            return cleanupBadURL();
        }
        StringBuilder collector = new StringBuilder();
        document.pushback(c);
        while (true) {
            c = document.nextChar();
            if (document.isFinished() || c == ')') {
                result.setValue(collector.toString());
                return result;
            }
            if (c == '"' || c == '\'' || c == '(' || isNonPrintable(c)) return cleanupBadURL();
            if (document.isBlank(c)) {
                document.skipBlanks();
            } else if (c == '\\') {
                collector.append(parseEscape());
            } else collector.append(c);
        }
    }

    private boolean isNonPrintable(final Character c) {
        return c >= 0 && c < 9 || c == 11 || c >= 14 && c < 32 || c == 0x7F;
    }

    private BadUrlToken cleanupBadURL() {
        char c;
        while (!document.isFinished()) {
            document.readUntil(ch -> ch == ')' || ch == '\\');
            c = document.nextChar();
            if (c == ')') break;
            parseEscape();
        }
        return new BadUrlToken();
    }

    private ParserToken parseUnicodeRange() {
        StringBuilder collector = new StringBuilder(16);
        collector.append(document.readWhile(this::isHexDigit, 6));
        if (collector.length() < 6)
            collector.append(document.readWhile(c -> c == '?', 6 - collector.length()));
        String mask = collector.toString();
        String begin, end;
        if (mask.contains("?")) {
            begin = mask.replace('?', '0');
            end = mask.replace('?', 'F');
        } else {
            begin = end = mask;
            if (String.valueOf(document.peek(2)).matches("-[0-9a-fA-F]")) {
                document.nextChar();
                end = String.valueOf(document.readWhile(this::isHexDigit));
            }
        }
        return new UnicodeRange(Integer.parseInt(begin, 16), Integer.parseInt(end, 16));
    }

    private boolean isHexDigit(final char c) {
        return "0123456789ABCDEFabcdef".indexOf(c) != -1;
    }

    /**
     * Parses numeric token or dimension..
     * <p>
     * Use this only after making sure that indeed a numeric token comes next.
     *
     * @param c the first character of the number (may also be a sign prefix).
     * @return the identifier.
     */
    private ParserToken parseNumber(final char c) {
        StringBuilder collector = new StringBuilder(64).append(c);
        boolean integer = c != '.';
        if (document.nextIs(this::isDigit)) collector.append(document.readWhile(this::isDigit));
        if (document.isNext(".") && isDigit((document.peek()))) {
            collector.append('.').append(document.readWhile(this::isDigit));
            integer = false;
        }
        if (String.valueOf(document.peek(3)).trim().matches("[Ee][+-]?[0-9].?")) {
            collector.append(document.nextChar())
                     .append(document.readWhile(ch -> ch == '+' || ch == '-'))
                     .append(document.readWhile(this::isDigit));
            integer = false;
        }
        String repr = String.valueOf(collector);
        collector.setLength(0);
        if (isIdentifierStart(document.peek(3))) {
            String unit$ = parseName();
            CssUnit unit;
            try {
                unit = CssUnit.valueOf(unit$);
            } catch (IllegalArgumentException e) {
                throw new CssUnitUnknownException(unit$);
            }
            return integer //
                   ? new IntegerDimension(repr, Integer.parseInt(repr), unit)
                   : new RealDimension(repr, Double.parseDouble(repr), unit);
        } else if (document.isNext("%"))
            return integer ? new IntegerPercentage(repr, Integer.parseInt(repr))
                           : new RealPercentage(repr, Double.parseDouble(repr));
        else return integer ? new IntegerValue(repr, Integer.parseInt(repr))
                            : new RealValue(repr, Double.parseDouble(repr));
    }

    /**
     * Parses an identifier.
     * <p>
     * Use this only after making sure that indeed an identifier comes next.
     *
     * @return the identifier.
     */
    private String parseName() {
        return String.valueOf(document.nextChar()) + document.readWhile(this::isIdentifierPart);
    }

    /**
     * Parses an escape code point.
     *
     * @return the code point
     */
    private char parseEscape() {
        char c = document.nextChar();
        if (document.isFinished()) return '\ufffd';
        if (isHexDigit(document.peek())) {
            final CharSequence sequence = document.readWhile(this::isHexDigit, 6);
            if (document.nextIs(Character::isWhitespace)) document.nextChar();
            int cp = Integer.parseInt(sequence.toString(), 16);
            if (cp == 0 || cp > 0x10ffff || cp >= 0xd800 && cp < 0xe000) cp = 0xfffd;
            return (char)cp;
        }
        return c;
    }

    private boolean isIdentifierStart(final CharSequence next3) {
        return isIdentifierStart(next3.charAt(0)) ||
               next3.charAt(0) == '-' && isIdentifierStart(next3.charAt(1)) ||
               next3.charAt(0) == '\\' && next3.charAt(1) != '\n';
    }

    private boolean isIdentifierStart(final char c) {
        return c >= 'A' && c <= 'Z' || c >= 'a' && c <= 'z' || c == '_' || c > 0x80;
    }

    private boolean isIdentifierPart(final char c) {
        return isIdentifierStart(c) || c >= '0' && c <= '9' || c == '-';
    }

    private StringToken parseStringToken(final char c) {
        StringBuilder collector = new StringBuilder();
        while (true) {
            collector.append(document.readWhile(ch -> ch != c && "\r\n\\".indexOf(ch) == -1));
            char ch = document.nextChar();
            if (ch == '\n') return new BadStringToken(collector.toString());
            if (ch != '\\') return new StringToken(collector.toString());
            if (!document.isFinished()) {
                if (document.isNext("\\\n") || document.isNext("\\\r")) continue;
                collector.append(parseEscape());
            }
        }
    }

    private boolean isDigit(final Character ch) {
        return ch >= '0' && ch <= '9';
    }

}
