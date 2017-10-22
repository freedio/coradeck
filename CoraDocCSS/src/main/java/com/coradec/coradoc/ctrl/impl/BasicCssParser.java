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

import com.coradec.coracore.annotation.Nullable;
import com.coradec.coradoc.ctrl.CssParser;
import com.coradec.coradoc.model.CssDocument;
import com.coradec.coradoc.model.CssDocumentModel;
import com.coradec.coradoc.model.Document;
import com.coradec.coradoc.model.ParserToken;
import com.coradec.coradoc.struct.AtRule;
import com.coradec.coradoc.struct.BasicQualifiedRule;
import com.coradec.coradoc.struct.Function;
import com.coradec.coradoc.struct.SimpleBlock;
import com.coradec.coradoc.token.AtKeyword;
import com.coradec.coradoc.token.BracketClose;
import com.coradec.coradoc.token.BracketOpen;
import com.coradec.coradoc.token.CDC;
import com.coradec.coradoc.token.CDO;
import com.coradec.coradoc.token.CurlyClose;
import com.coradec.coradoc.token.CurlyOpen;
import com.coradec.coradoc.token.EOF;
import com.coradec.coradoc.token.FunctionToken;
import com.coradec.coradoc.token.ParenthClose;
import com.coradec.coradoc.token.ParenthOpen;
import com.coradec.coradoc.token.Semicolon;
import com.coradec.coradoc.token.Whitespace;
import com.coradec.coradoc.trouble.ParseFailure;
import com.coradec.coratext.model.LocalizedText;
import com.coradec.coratext.model.Text;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * ​​Basic implementation of a CSS parser.
 */
@SuppressWarnings("ClassHasNoToStringMethod")
public class BasicCssParser<M extends CssDocumentModel> extends BasicDocumentParser<M>
        implements CssParser<M> {

    private static final Text TEXT_CHILD_SELECTOR_WITHOUT_PREDECESSOR =
            LocalizedText.define("ChildSelectorWithoutPredecessor");
    private static final Text TEXT_NEIGHBOR_SELECTOR_WITHOUT_PREDECESSOR =
            LocalizedText.define("NeighborSelectorWithoutPredecessor");
    private static final Text TEXT_ADDITIONAL_SELECTOR_WITHOUT_PREDECESSOR =
            LocalizedText.define("AdditionalSelectorWithoutPredecessor");
    private static final Text TEXT_SIBLING_SELECTOR_WITHOUT_PREDECESSOR =
            LocalizedText.define("SiblingSelectorWithoutPredecessor");
    private static final Text TEXT_INVALID_ATTRIBUTE_OPERATOR =
            LocalizedText.define("InvalidAttributeOperator");
    private static final Text TEXT_ATTRIBUTE_FILTER_WITHOUT_ATTRIBUTE_NAME =
            LocalizedText.define("AttributeFilterWithoutAttributeName");
    private static final Text TEXT_COLON_EXPECTED = LocalizedText.define("ColonExpected");
    private BasicCssTokenizer<M> tokenizer;

    @Override public CssParser<M> to(final M model) {
        return (CssParser<M>)super.to(model);
    }

    @Override public CssParser<M> from(final URL source) {
        return (CssParser<M>)super.from(source);
    }

    @Override protected Document createDocument(final URL source) {
        return CssDocument.from(source);
    }

    @Override protected CssDocument getDocument() {
        return (CssDocument)super.getDocument();
    }

    @Override public CssParser<M> parse() throws ParseFailure {
        tokenizer = new BasicCssTokenizer<>(getDocument(), getModel());
        M model = getModel();
        model.onStartOfDocument(getDocument().getOrigin());
        boolean topLevel = true;
        for (ParserToken token = tokenizer.next(); token != null; token = tokenizer.next()) {
            if (!(token instanceof Whitespace) &&
                !(token instanceof CDO) &&
                !(token instanceof CDC)) {
                if (token instanceof EOF) return this;
                else if (token instanceof AtKeyword) model.onAtRule(parseAtRule((AtKeyword)token));
                else {
                    final BasicQualifiedRule qualifiedRule = parseQualifiedRule(token);
                    if (qualifiedRule != null) model.onQualifiedRule(qualifiedRule);
                }
            }
        }
        model.onEndOfDocument();
        return this;
    }

    private @Nullable BasicQualifiedRule parseQualifiedRule(final ParserToken initial) {
        final BasicQualifiedRule result = new BasicQualifiedRule();
        for (ParserToken token = initial; token != null; token = tokenizer.next()) {
            if (token instanceof EOF) return null;
            if (token instanceof CurlyOpen) {
                result.setSimpleBlock(parseSimpleBlock(token, CurlyClose.class));
                break;
            }
            result.addComponentValue(parseComponentValue(token));
        }
        return result;
    }

    /**
     * Consumes an @-Rule from the CSS.
     *
     * @param keyword the @-keyword
     * @return the @-Rule.
     */
    private AtRule parseAtRule(final AtKeyword keyword) {
        final List<ParserToken> prelude = new ArrayList<>();
        SimpleBlock simpleBlock = null;
        for (ParserToken token = tokenizer.next(); token != null; token = tokenizer.next()) {
            if (token instanceof Semicolon) break;
            if (token instanceof CurlyOpen) {
                simpleBlock = parseSimpleBlock(token, CurlyClose.class);
                break;
            }
            prelude.add(parseComponentValue(token));
        }
        return new AtRule(keyword, prelude, simpleBlock);
    }

    private ParserToken parseComponentValue(final ParserToken intro) {
        if (intro instanceof CurlyOpen) return parseSimpleBlock(intro, CurlyClose.class);
        else if (intro instanceof BracketOpen) return parseSimpleBlock(intro, BracketClose.class);
        else if (intro instanceof ParenthOpen) return parseSimpleBlock(intro, ParenthClose.class);
        else if (intro instanceof FunctionToken) return parseFunction((FunctionToken)intro);
        return intro;
    }

    private Function parseFunction(final FunctionToken intro) {
        return new Function(intro.getName(), parseSimpleBlockBody(ParenthClose.class));
    }

    private SimpleBlock parseSimpleBlock(final ParserToken opener,
            final Class<? extends ParserToken> terminator) {
        return new SimpleBlock(parseSimpleBlockBody(terminator));
    }

    private List<ParserToken> parseSimpleBlockBody(final Class<? extends ParserToken> terminator) {
        List<ParserToken> result = new ArrayList<>();
        for (ParserToken token = tokenizer.next();
             token != null && !terminator.isInstance(token);
             token = tokenizer.next()) {
            result.add(parseComponentValue(token));
        }
        return result;
    }

}
