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

import static com.coradec.coradoc.state.ParserState.*;

import com.coradec.coracore.annotation.Implementation;
import com.coradec.coracore.annotation.Nullable;
import com.coradec.coracore.model.Origin;
import com.coradec.coracore.model.impl.URLigin;
import com.coradec.coradoc.ctrl.XmlParser;
import com.coradec.coradoc.model.Document;
import com.coradec.coradoc.model.XmlAttributes;
import com.coradec.coradoc.model.XmlDocumentModel;
import com.coradec.coradoc.model.impl.BasicXmlAttributes;
import com.coradec.coradoc.model.impl.BasicXmlDocumentModel;
import com.coradec.coradoc.state.ParserState;
import com.coradec.coradoc.trouble.ParseFailure;
import com.coradec.coratext.model.LocalizedText;
import com.coradec.coratext.model.Text;

import java.net.URL;

/**
 * ​​Basic implementation of an XML parser.
 */
@SuppressWarnings("ClassHasNoToStringMethod")
@Implementation
public class BasicXmlParser<M extends XmlDocumentModel> implements XmlParser<M> {

    private static final Text TEXT_FAILED_TO_PARSE = LocalizedText.define("FailedToParse");
    private static final Text TEXT_INVALID_NAME_START_CHARACTER =
            LocalizedText.define("InvalidNameStartCharacter");
    private static final Text TEXT_INVALID_TOKEN = LocalizedText.define("InvalidToken");
    private static final Text TEXT_ANONYMOUS_EMPTY_TAG = LocalizedText.define("AnonymousEmptyTag");
    private static final Text TEXT_MISSING_TAG = LocalizedText.define("MissingTag");
    private static final Text TEXT_ATTRIBUTE_WITHOUT_NAME =
            LocalizedText.define("AttributeWithoutName");
    private static final Text TEXT_PI_WITHOUT_NAME = LocalizedText.define("PIWithoutName");
    private static final Text TEXT_INVALID_HEX_CHAR_REF = LocalizedText.define("InvalidHexCharRef");
    private static final Text TEXT_NON_COMPLIANT_COMMENT =
            LocalizedText.define("NonCompliantComment");
    private static final Text TEXT_ATTRIBUTE_REDEFINITION =
            LocalizedText.define("AttributeRedefinition");

    private ParserState state = INITIAL;
    private XmlDocumentModel model = new BasicXmlDocumentModel() {

    };
    private Origin origin;
    private Document document;

    @Override public XmlParser<M> to(final M model) {
        this.model = model;
        return this;
    }

    @Override public XmlParser<M> from(final URL source) {
        document = Document.from(source);
        origin = new URLigin(source);
        return this;
    }

    @Override public XmlParser<M> parse() throws ParseFailure {
        StringBuilder collector = new StringBuilder();
        @Nullable String tag = null, attrName = null, piName = null;
        @Nullable XmlAttributes attributes = null;
        boolean endTag = false, emptyTag = false;
        int radix = 10;
        model.onStartOfDocument(origin);
        final Document document = this.document;
        char c;
        outer:
        while (true) {
            switch (state) {
                case INITIAL:
                    document.skipBlanks();
                    if (document.isFinished()) state = ENDDOC;
                    else if (document.isNext("<")) state = TAG_INIT;
                    else if (document.isNext("&#x")) state = HEX_CHAR_REF;
                    else if (document.isNext("&#")) state = CHAR_REF;
                    else if (document.isNext("&")) state = ENT_REF;
                    else state = PCDATA;
                    break;
                case TAG_INIT:
                    document.skipBlanks();
                    if (document.isNext("!--") && document.isNextNot("-")) state = COMMENT;
                    else if (document.isNext("![CDATA[")) state = CDATA;
                    else if (document.isNext("?")) state = PI;
                    else if (document.isNext("/")) state = ENDTAG;
                    else {
                        c = document.nextChar();
                        if (isNameStart(c)) {
                            collector.append(c);
                            state = TAG;
                        } else throw new ParseFailure(
                                TEXT_INVALID_NAME_START_CHARACTER.resolve(document.nextChar(),
                                        document.getOrigin(), document.getLine(),
                                        document.getColumn()));
                    }
                    break;
                case TAG:
                    collector.append(document.readWhile(this::isNamePart));
                    tag = collector.toString();
                    collector.setLength(0);
                    state = TAG_ATTRS;
                    break;
                case ENDTAG:
                    endTag = true;
                    c = document.nextChar();
                    if (isNameStart(c)) {
                        collector.append(c);
                        state = TAG;
                    } else throw new ParseFailure(
                            TEXT_INVALID_NAME_START_CHARACTER.resolve(document.nextChar(),
                                    document.getOrigin(), document.getLine(),
                                    document.getColumn()));
                    collector.append(document.readWhile(this::isNamePart));
                    tag = collector.toString();
                    collector.setLength(0);
                    state = TAG_EXIT;
                    break;
                case ENDDOC:
                    break outer;
                case PCDATA:
                    document.skipBlanks();
                    collector.append(document.readUntil(ch -> ch == '<' || ch == '&'));
                    model.onData(resolve(normalize(collector.toString())));
                    collector.setLength(0);
                    state = INITIAL;
                    break;
                case PI:
                    c = document.nextChar();
                    if (!isNameStart(c)) throw new ParseFailure(
                            TEXT_INVALID_NAME_START_CHARACTER.resolve(document.nextChar(),
                                    document.getOrigin(), document.getLine(),
                                    document.getColumn()));
                    collector.append(c).append(document.readWhile(this::isNamePart));
                    piName = collector.toString();
                    collector.setLength(0);
                    state = PI_PARA;
//                    break;
                case PI_PARA:
                    document.skipBlanks();
                    collector.append(document.readUpto("?>"));
                    if (piName == null) throw new ParseFailure(
                            TEXT_PI_WITHOUT_NAME.resolve(document.getOrigin(), document.getLine(),
                                    document.getColumn()));
                    model.onProcessingInstruction(piName, collector.toString().trim());
                    piName = null;
                    collector.setLength(0);
                    state = INITIAL;
                    break;
                case HEX_CHAR_REF:
                    radix = 16;
                    state = CHAR_REF;
//                    break;
                case CHAR_REF:
                    collector.append(document.readUpto(";"));
                    final int charCode;
                    try {
                        charCode = Integer.parseInt(collector.toString(), radix);
                    } catch (NumberFormatException e) {
                        throw new ParseFailure(
                                TEXT_INVALID_HEX_CHAR_REF.resolve(collector.toString(),
                                        document.getOrigin(), document.getLine(),
                                        document.getColumn()));
                    }
                    model.onCharacterReference(charCode);
                    collector.setLength(0);
                    radix = 10;
                    state = INITIAL;
                    break;
                case ENT_REF:
                    c = document.nextChar();
                    if (!isNameStart(c)) throw new ParseFailure(
                            TEXT_INVALID_NAME_START_CHARACTER.resolve(document.nextChar(),
                                    document.getOrigin(), document.getLine(),
                                    document.getColumn()));
                    collector.append(c).append(document.readWhile(this::isNamePart));
                    if (!document.isNext(";")) throw new ParseFailure(
                            TEXT_INVALID_TOKEN.resolve(document.nextChar(), document.getOrigin(),
                                    document.getLine(), document.getColumn()));
                    model.onEntityReference(collector.toString());
                    collector.setLength(0);
                    state = INITIAL;
                    break;
                case COMMENT:
                    collector.append(document.readUpto("-->"));
                    String comment = collector.toString();
                    if (comment.endsWith("-") || comment.contains("--")) throw new ParseFailure(
                            TEXT_NON_COMPLIANT_COMMENT.resolve(comment, document.getOrigin(),
                                    document.getLine(), document.getColumn()));
                    model.onComment(comment);
                    collector.setLength(0);
                    state = INITIAL;
                    break;
                case TAG_ATTRS:
                    document.skipBlanks();
                    if (document.isNext("/")) {
                        emptyTag = true;
                        state = TAG_EXIT;
                    } else if (document.isNext(">")) state = TAG_END;
                    else {
                        c = document.nextChar();
                        if (!isNameStart(c)) throw new ParseFailure(
                                TEXT_INVALID_NAME_START_CHARACTER.resolve(document.nextChar(),
                                        document.getOrigin(), document.getLine(),
                                        document.getColumn()));
                        collector.append(c);
                        state = TAG_ATTR_NAME;
                    }
                    break;
                case TAG_ATTR_NAME:
                    collector.append(document.readWhile(this::isNamePart));
                    document.skipBlanks();
                    if (!document.isNext("=")) {
                        throw new ParseFailure(TEXT_INVALID_TOKEN.resolve(document.nextChar(),
                                document.getOrigin(), document.getLine(), document.getColumn()));
                    }
                    attrName = collector.toString();
                    collector.setLength(0);
                    state = TAG_ATTR_VALUE;
//                    break;
                case TAG_ATTR_VALUE:
                    document.skipBlanks();
                    if (!document.isNext("\"")) throw new ParseFailure(
                            TEXT_INVALID_TOKEN.resolve(document.nextChar(), document.getOrigin(),
                                    document.getLine(), document.getColumn()));
                    state = IN_ATTR_VALUE;
//                    break;
                case IN_ATTR_VALUE:
                    collector.append(document.readUpto("\""));
                    if (attrName == null) throw new ParseFailure(
                            TEXT_ATTRIBUTE_WITHOUT_NAME.resolve(document.getOrigin(),
                                    document.getLine(), document.getColumn()));
                    if (attributes == null) attributes = new BasicXmlAttributes();
                    try {
                        attributes.add(attrName, resolve(collector.toString()));
                    } catch (IllegalStateException e) {
                        throw new ParseFailure(
                                TEXT_ATTRIBUTE_REDEFINITION.resolve(attrName, document.getOrigin(),
                                        document.getLine(), document.getColumn()));
                    }
                    attrName = null;
                    collector.setLength(0);
                    state = TAG_ATTRS;
                    break;
                case TAG_EXIT:
                    document.skipBlanks();
                    if (!document.isNext(">")) throw new ParseFailure(
                            TEXT_INVALID_TOKEN.resolve(document.nextChar(), document.getOrigin(),
                                    document.getLine(), document.getColumn()));
                    state = TAG_END;
//                    break;
                case TAG_END:
                    if (tag == null) throw new ParseFailure(
                            TEXT_MISSING_TAG.resolve(document.getOrigin(), document.getLine(),
                                    document.getColumn()));
                    if (endTag) model.onEndTag(tag);
                    else model.onStartTag(tag, attributes, emptyTag);
                    tag = null;
                    attributes = null;
                    endTag = emptyTag = false;
                    state = INITIAL;
                    break;
                case CDATA:
                    collector.append(document.readUpto("]]>"));
                    model.onRawData(normalize(collector.toString()));
                    collector.setLength(0);
                    state = INITIAL;
                    break;
            }
        }
        return this;
    }

    /**
     * Resolves the specified string, replacing entity and character references with their values.
     *
     * @param s the string to resolve.
     * @return the resolved string.
     */
    private String resolve(final String s) {
        return s; // todo
    }

    /**
     * Returns the specified string, normalized to line-ends consisting of only \n;
     *
     * @param s the string to normalize.
     * @return the normalized string.
     */
    private String normalize(final String s) {
        return s.replaceAll("\r\n", "\n").replaceAll("\n\r", "\n").replaceAll("\r", "\n");
    }

    private boolean isNameStart(final char c) {
        return c == ':' ||
               c >= 'A' && c <= 'Z' ||
               c == '_' ||
               c >= 'a' && c <= 'z' ||
               c >= 0xC0 && c < 0x300 && c != 0xD7 && c != 0xF7 ||
               c >= 0x370 && c < 0x2000 && c != 0x37E ||
               c == 0x200C ||
               c == 0x200D ||
               c >= 0x2070 && c < 0x2190 ||
               c >= 0x2C00 && c < 0x2FF0 ||
               c > 0x3000 && c < 0xD800 ||
               c >= 0xF900 && c < 0xFDD0 ||
               c >= 0xFDF0 && c < 0xFFFE;
    }

    private boolean isNamePart(final char c) {
        return c == '-' ||
               c == '.' ||
               c >= '0' && c <= '9' ||
               c == 0xB7 ||
               c >= 0x300 && c < 0x370 ||
               c == 0x203F ||
               c == 0x2040 ||
               isNameStart(c);
    }

    @Override public M getModel() {
        //noinspection unchecked
        return (M)model;
    }
}
