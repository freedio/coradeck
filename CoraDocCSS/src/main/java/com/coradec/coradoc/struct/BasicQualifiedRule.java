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

package com.coradec.coradoc.struct;

import static com.coradec.coradoc.state.Match.*;

import com.coradec.coraconf.model.Configuration;
import com.coradec.coraconf.model.ValueMap;
import com.coradec.coracore.annotation.Nullable;
import com.coradec.coracore.trouble.InitializationError;
import com.coradec.coracore.trouble.ObjectInstantiationFailure;
import com.coradec.coracore.util.StringUtil;
import com.coradec.coradoc.model.Declaration;
import com.coradec.coradoc.model.ModifiableDeclaration;
import com.coradec.coradoc.model.ParserToken;
import com.coradec.coradoc.model.QualifiedRule;
import com.coradec.coradoc.model.Selector;
import com.coradec.coradoc.model.Style;
import com.coradec.coradoc.model.StyleDeclaration;
import com.coradec.coradoc.model.impl.BasicStyle;
import com.coradec.coradoc.state.ProcessingState;
import com.coradec.coradoc.token.AtKeyword;
import com.coradec.coradoc.token.BracketClose;
import com.coradec.coradoc.token.BracketOpen;
import com.coradec.coradoc.token.Colon;
import com.coradec.coradoc.token.Comma;
import com.coradec.coradoc.token.CurlyClose;
import com.coradec.coradoc.token.CurlyOpen;
import com.coradec.coradoc.token.Delimiter;
import com.coradec.coradoc.token.EOF;
import com.coradec.coradoc.token.FunctionToken;
import com.coradec.coradoc.token.Identifier;
import com.coradec.coradoc.token.Matcher;
import com.coradec.coradoc.token.ParenthClose;
import com.coradec.coradoc.token.ParenthOpen;
import com.coradec.coradoc.token.Semicolon;
import com.coradec.coradoc.token.StringToken;
import com.coradec.coradoc.token.Whitespace;
import com.coradec.coradoc.trouble.CommaWithoutPrecedingSelectorException;
import com.coradec.coradoc.trouble.DeclarationClassCastException;
import com.coradec.coradoc.trouble.DeclarationClassNotFoundException;
import com.coradec.coradoc.trouble.InvalidDeclarationTokenException;
import com.coradec.coradoc.trouble.MisplacedWhitespaceException;
import com.coradec.coradoc.trouble.SelectorTokenInvalidException;
import com.coradec.coratext.model.LocalizedText;
import com.coradec.coratext.model.Text;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

/**
 * ​​Implementation of a qualified rule.
 */
@SuppressWarnings("ClassHasNoToStringMethod")
public class BasicQualifiedRule extends BasicRule implements QualifiedRule {

    private static final Configuration DECLARATION_MAPPING =
            Configuration.of(BasicQualifiedRule.class);
    static final Text TEXT_SELECTOR_FAILURE = LocalizedText.define("SelectorFailure");

    private @Nullable List<Selector> selectors;
    private @Nullable Map<String, StyleDeclaration> declarations;

    @Override public List<Selector> getSelectors() {
        if (selectors == null) selectors = new PreludeProcessor().process(getPrelude());
        return Collections.unmodifiableList(selectors);
    }

    @Override public Selector getSelector(final int index) throws IndexOutOfBoundsException {
        return getSelectors().get(index);
    }

    @Override public Map<String, StyleDeclaration> getStyleDeclarations() {
        if (declarations == null) declarations =
                new DeclarationsProcessor().process(getSimpleBlock().getComponentValues());
        return Collections.unmodifiableMap(declarations);
    }

    @Override public StyleDeclaration getStyleDeclaration(final String name)
            throws NoSuchElementException {
        return getStyleDeclarations().get(name);
    }

    @Override public int getSpecifity() {
        return specifityOf(getSelectors());
    }

    private int specifityOf(final List<Selector> selectors) {
        return getSelectors().stream().map(selector -> {
            if (selector instanceof TypeSelector || selector instanceof PseudoElement) return 1;
            if (selector instanceof IdSelector) return 10000;
            if (selector instanceof ClassSelector ||
                selector instanceof AttributeSelector ||
                selector instanceof PseudoClass) return 100;
            if (selector instanceof DerivedSelector) {
                return specifityOf(((DerivedSelector)selector).getCascade());
            }
            return 0;
        }).reduce((c1, c2) -> c1 + c2).orElse(0);
    }

    @Override public Style toStyle() {
        final Style result = new BasicStyle();
        getStyleDeclarations().values().forEach(declaration -> declaration.apply(result));
        return result;
    }

    @Override public boolean matches(final List<String> path, final ValueMap attributes) {
        return getSelectors().stream().anyMatch(selector -> selector.matches(path, attributes));
    }

    /**
     * Checks if the specified parser token represents whitespace.
     *
     * @param token the parser token.
     * @return {@code true} if the parser token represents whitespace, {@code false} if not.
     */
    boolean isWhiteSpace(final ParserToken token) {
        return token instanceof Whitespace;
    }

    /**
     * Checks if the specified parser token represents a delimiter with the specified value.
     *
     * @param token the parser token.
     * @param value the delimiter value.
     * @return {@code true} if the parser token represents a delimiter with the specified value,
     * {@code false} if not.
     */
    boolean isDelimiter(final ParserToken token, final char value) {
        return token instanceof Delimiter && ((Delimiter)token).getDelimiter() == value;
    }

    /**
     * Checks if the specified parser token represents an identifier.
     *
     * @param token the parser token.
     * @return {@code true}f the parser token represents an identifier, {@code false} if not.
     */
    boolean isIdentifier(final ParserToken token) {
        return token instanceof Identifier;
    }

    /**
     * Checks if the specified parser token represents a string.
     *
     * @param token the parser token.
     * @return {@code true} if the parser token represents a string, {@code false} if not.
     */
    boolean isString(final ParserToken token) {
        return token instanceof StringToken;
    }

    /**
     * Checks if the specified parser token is a semicolon.
     *
     * @param token the parser token.
     * @return {@code true} if the token is a semicolon, {@code false} if not.
     */
    boolean isSemicolon(final ParserToken token) {
        return token instanceof Semicolon;
    }

    /**
     * Checks if the specified parser token is an opening curly brace.
     *
     * @param token the parser token.
     * @return {@code true} if the parser token is an opening curly brace, {@code false} if not.
     */
    boolean isCurlyOpen(final ParserToken token) {
        return token instanceof CurlyOpen;
    }

    /**
     * Checks if the specified parser token is EOF.
     *
     * @param token the parser token.
     * @return {@code true} if the parser token is EOF, {@code false} if not.
     */
    boolean isEOF(final ParserToken token) {
        return token instanceof EOF;
    }

    /**
     * Casts the specified raw declaration to one of the CSS declarations.
     *
     * @param declaration the raw declaration/
     * @return a CSS declaration.
     */
    @SuppressWarnings("unchecked") Declaration cast(final ModifiableDeclaration declaration) {
        final String identifier = declaration.getIdentifier();
        String className = DECLARATION_MAPPING.lookup(String.class, identifier)
                                              .orElseThrow(
                                                      () -> new DeclarationClassNotFoundException(
                                                              identifier));

        Class<? extends Declaration> klass;
        try {
            klass = (Class<? extends Declaration>)Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new InitializationError("Class not found: " + className);
        }
        try {
            if (!Declaration.class.isAssignableFrom(klass))
                throw new DeclarationClassCastException(identifier, klass);
            @SuppressWarnings("JavaReflectionMemberAccess") Constructor<? extends Declaration>
                    constructor = klass.getConstructor(ModifiableDeclaration.class);
            return constructor.newInstance(declaration);
        } catch (InvocationTargetException e) {
            throw new ObjectInstantiationFailure(klass, e.getTargetException());
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException e) {
            throw new ObjectInstantiationFailure(klass, e);
        }
    }

    @SuppressWarnings("ClassHasNoToStringMethod")
    private class PreludeProcessor {

        private final List<Selector> items = new ArrayList<>();
        private Selector current;

        protected void add(final Selector selector) {
            items.add(selector);
        }

        private void flush() {
        }

        List<Selector> process(final List<ParserToken> prelude) {
            ProcessingState state = this::initial;
            List<ParserToken> processed = new ArrayList<>();
            for (final ParserToken token : prelude) {
                try {
                    state = state.process(token);
                } catch (RuntimeException e) {
                    error(e, TEXT_SELECTOR_FAILURE, StringUtil.toString(current.getName()),
                            processed.stream()
                                     .map(ParserToken::toString)
                                     .collect(Collectors.joining(" ")));
                    throw e;
                }
                processed.add(token);
            }
            if (current != null) items.add(current);
            clearPrelude();
            return items;
        }

        protected boolean isComma(final ParserToken token) {
            return token instanceof Comma;
        }

        private boolean isMatcher(final ParserToken token) {
            return token instanceof Matcher;
        }

        com.coradec.coradoc.state.ProcessingState initial(ParserToken token) {
            if (isWhiteSpace(token)) return this::initial;
            if (isDelimiter(token, ':')) return this::pseudoClass;
            if (isComma(token)) throw new CommaWithoutPrecedingSelectorException();
            if (isDelimiter(token, '*')) {
                current = new UniversalSelector();
                return this::typeSelector;
            }
            if (isIdentifier(token)) {
                current = new TypeSelector(((Identifier)token).getName());
                return this::typeSelector;
            }
            if (isDelimiter(token, '.')) return this::classSelector;
            if (isDelimiter(token, '#')) return this::idSelector;
            if (isDelimiter(token, '[')) return this::attributeSelector;
            throw new SelectorTokenInvalidException(token);
        }

        private com.coradec.coradoc.state.ProcessingState pseudoClass(ParserToken token) {
            if (isWhiteSpace(token)) throw new MisplacedWhitespaceException();
            if (isDelimiter(token, ':')) return this::pseudoElement;
            if (isIdentifier(token)) {
                current = new PseudoClass(((Identifier)token).getName());
                return this::afterSelector;
            }
            throw new SelectorTokenInvalidException(token);
        }

        private com.coradec.coradoc.state.ProcessingState typeSelector(final ParserToken token) {
            if (isWhiteSpace(token)) return this::typeSelector;
            if (isDelimiter(token, '>')) return this::childSelector;
            if (isDelimiter(token, '+')) return this::neighborSelector;
            if (isDelimiter(token, '~')) return this::siblingSelector;
            if (isDelimiter(token, '*')) {
                current = current.createDescendant("*");
                return this::typeSelector;
            }
            if (isIdentifier(token)) {
                current = current.createDescendant(((Identifier)token).getName());
                return this::typeSelector;
            }
            if (isComma(token)) {
                add(current);
                current = null;
                return this::initial;
            }
            throw new SelectorTokenInvalidException(token);
        }

        private com.coradec.coradoc.state.ProcessingState classSelector(final ParserToken token) {
            if (isWhiteSpace(token)) throw new MisplacedWhitespaceException();
            if (isIdentifier(token)) {
                current = new ClassSelector(((Identifier)token).getName());
                return this::afterSelector;
            }
            throw new SelectorTokenInvalidException(token);
        }

        private com.coradec.coradoc.state.ProcessingState idSelector(final ParserToken token) {
            if (isWhiteSpace(token)) throw new MisplacedWhitespaceException();
            if (isIdentifier(token)) {
                current = new IdSelector(((Identifier)token).getName());
                return this::afterSelector;
            }
            throw new SelectorTokenInvalidException(token);
        }

        private com.coradec.coradoc.state.ProcessingState attributeSelector(
                final ParserToken token) {
            if (isWhiteSpace(token)) throw new MisplacedWhitespaceException();
            if (isIdentifier(token)) {
                current = new AttributeSelector(((Identifier)token).getName());
                return this::inAttributeSelector;
            }
            throw new SelectorTokenInvalidException(token);
        }

        private com.coradec.coradoc.state.ProcessingState inAttributeSelector(
                final ParserToken token) {
            if (isWhiteSpace(token)) throw new MisplacedWhitespaceException();
            AttributeSelector as = (AttributeSelector)current;
            if (isDelimiter(token, ']')) {
                as.match(PRESENCE);
                return this::afterSelector;
            }
            if (isMatcher(token)) {
                as.match(((Matcher)token).getMatch());
                return this::afterMatcher;
            }
            throw new SelectorTokenInvalidException(token);
        }

        private com.coradec.coradoc.state.ProcessingState afterMatcher(final ParserToken token) {
            if (isWhiteSpace(token)) throw new MisplacedWhitespaceException();
            AttributeSelector as = (AttributeSelector)current;
            if (isString(token)) {
                as.setValue(((StringToken)token).getValue());
                return this::afterMatcherValue;
            }
            if (isIdentifier(token)) {
                as.setValue(((Identifier)token).getName());
                return this::afterMatcherValue;
            }
            throw new SelectorTokenInvalidException(token);
        }

        private com.coradec.coradoc.state.ProcessingState afterMatcherValue(
                final ParserToken token) {
            if (isWhiteSpace(token)) throw new MisplacedWhitespaceException();
            if (isDelimiter(token, ']')) return this::afterSelector;
            throw new SelectorTokenInvalidException(token);
        }

        private com.coradec.coradoc.state.ProcessingState childSelector(final ParserToken token) {
            if (isWhiteSpace(token)) return this::childSelector;
            if (isDelimiter(token, '*')) {
                current = current.createChild("*");
                return this::typeSelector;
            }
            if (isIdentifier(token)) {
                current = current.createChild(((Identifier)token).getName());
                return this::typeSelector;
            }
            throw new SelectorTokenInvalidException(token);
        }

        private com.coradec.coradoc.state.ProcessingState neighborSelector(
                final ParserToken token) {
            if (isWhiteSpace(token)) return this::neighborSelector;
            if (isDelimiter(token, '*')) {
                current = current.createNeighbor("*");
                return this::typeSelector;
            }
            if (isIdentifier(token)) {
                current = current.createNeighbor(((Identifier)token).getName());
                return this::typeSelector;
            }
            throw new SelectorTokenInvalidException(token);
        }

        private com.coradec.coradoc.state.ProcessingState siblingSelector(final ParserToken token) {
            if (isWhiteSpace(token)) return this::siblingSelector;
            if (isDelimiter(token, '*')) {
                current = current.createSibling("*");
                return this::typeSelector;
            }
            if (isIdentifier(token)) {
                current = current.createSibling(((Identifier)token).getName());
                return this::typeSelector;
            }
            throw new SelectorTokenInvalidException(token);
        }

        private com.coradec.coradoc.state.ProcessingState afterSelector(final ParserToken token) {
            if (token instanceof Whitespace) return this::afterSelector;
            throw new SelectorTokenInvalidException(token);
        }

        private com.coradec.coradoc.state.ProcessingState pseudoElement(final ParserToken token) {
            if (isWhiteSpace(token)) throw new MisplacedWhitespaceException();
            if (isIdentifier(token)) {
                current = new PseudoElement(((Identifier)token).getName());
                return this::afterSelector;
            }
            throw new SelectorTokenInvalidException(token);
        }
    }

    private class DeclarationsProcessor {

        private final List<Declaration> declarations = new ArrayList<>();
        private AtRuleProcessor atRuleProcessor;
        private DeclarationProcessor declarationProcessor;
        private boolean terminated = false;

        Map<String, StyleDeclaration> process(final List<ParserToken> componentValues) {
            ProcessingState state = this::initial;
            for (final ParserToken token : componentValues) {
                state = state.process(token);
            }
            while (!terminated) state.process(new EOF());
            clearSimpleBlock();
            return declarations.stream()
                               .filter(declaration -> declaration instanceof StyleDeclaration)
                               .map(declaration -> (StyleDeclaration)declaration)
                               .collect(Collectors.toMap(Declaration::getIdentifier, d -> d));
        }

        private boolean isAtKeyword(final ParserToken token) {
            return token instanceof AtKeyword;
        }

        private com.coradec.coradoc.state.ProcessingState initial(final ParserToken token) {
            if (isWhiteSpace(token)) return this::initial;
            if (isSemicolon(token)) return this::initial;
            if (isEOF(token)) {
                terminated = true;
                return this::initial;
            }
            if (isAtKeyword(token)) return atKeyword((AtKeyword)token);
            if (isIdentifier(token)) return identifier((Identifier)token);
            return this::badToken;
        }

        private com.coradec.coradoc.state.ProcessingState atKeyword(final AtKeyword token) {
            atRuleProcessor = new AtRuleProcessor(token);
            return this::atRule;
        }

        private com.coradec.coradoc.state.ProcessingState atRule(final ParserToken token) {
            if (atRuleProcessor.process(token)) {
                declarations.add(atRuleProcessor.getRule());
                return this::initial;
            }
            return this::atRule;
        }

        private com.coradec.coradoc.state.ProcessingState identifier(final Identifier token) {
            declarationProcessor = new DeclarationProcessor(token);
            return this::declaration;
        }

        private com.coradec.coradoc.state.ProcessingState declaration(final ParserToken token) {
            if (declarationProcessor.process(token)) {
                declarations.add(declarationProcessor.getDeclaration());
                return this::initial;
            }
            return this::declaration;
        }

        private com.coradec.coradoc.state.ProcessingState badToken(final ParserToken token) {
            return isSemicolon(token) || isEOF(token) ? this::initial : this::badToken;
        }
    }

    private class DeclarationProcessor {

        private final Identifier identifier;
        private ProcessingState state = this::initial;
        private boolean terminated;
        private ModifiableDeclaration declaration;

        public DeclarationProcessor(final Identifier identifier) {
            this.identifier = identifier;
        }

        boolean process(final ParserToken token) {
            state = state.process(token);
            return terminated;
        }

        private boolean isColon(final ParserToken token) {
            return token instanceof Colon;
        }

        private com.coradec.coradoc.state.ProcessingState initial(final ParserToken token) {
            if (isWhiteSpace(token)) return this::initial;
            if (isColon(token)) {
                declaration = new BasicModifiableDeclaration(identifier);
                return this::declarationBody;
            }
            throw new InvalidDeclarationTokenException(token);
        }

        private com.coradec.coradoc.state.ProcessingState declarationBody(final ParserToken token) {
            if (isSemicolon(token) || isEOF(token)) {
                terminated = true;
                return this::terminal;
            }
            declaration.add(token);
            return this::declarationBody;
        }

        private com.coradec.coradoc.state.ProcessingState terminal(final ParserToken token) {
            return this::terminal;
        }

        Declaration getDeclaration() {
            return cast(declaration);
        }
    }

    private class AtRuleProcessor {

        private final AtKeyword keyword;
        private final List<ParserToken> prelude = new ArrayList<>();
        private ProcessingState state = this::initial;
        private boolean terminated = false;
        private SimpleBlockProcessor simpleBlockProcessor;
        private SimpleBlock simpleBlock;

        public AtRuleProcessor(final AtKeyword keyword) {
            this.keyword = keyword;
        }

        boolean process(final ParserToken token) {
            state = state.process(token);
            return terminated;
        }

        private com.coradec.coradoc.state.ProcessingState initial(final ParserToken token) {
            if (isSemicolon(token)) {
                terminated = true;
                return this::initial;
            }
            if (isCurlyOpen(token)) {
                simpleBlockProcessor = new SimpleBlockProcessor(CurlyClose.class);
                return this::simpleBlock;
            }
            prelude.add(token);
            return this::initial;
        }

        private com.coradec.coradoc.state.ProcessingState simpleBlock(final ParserToken token) {
            if (simpleBlockProcessor.process(token)) {
                simpleBlock = simpleBlockProcessor.getSimpleBlock();
                terminated = true;
                return this::initial;
            }
            return this::simpleBlock;
        }

        Declaration getRule() {
            return new AtRule(keyword, prelude, simpleBlock);
        }

    }

    private class SimpleBlockProcessor {

        private final Class<? extends ParserToken> terminal;
        private final List<ParserToken> content = new ArrayList<>();
        private ProcessingState state = this::initial;
        private boolean terminated = false;
        private ComponentValueProcessor componentValueProcessor;

        public SimpleBlockProcessor(final Class<? extends ParserToken> terminal) {
            this.terminal = terminal;
        }

        boolean process(final ParserToken token) {
            state = state.process(token);
            return terminated;
        }

        private com.coradec.coradoc.state.ProcessingState initial(final ParserToken token) {
            if (isEOF(token) || terminal.isInstance(token)) {
                terminated = true;
                return this::initial;
            }
            componentValueProcessor = new ComponentValueProcessor();
            return componentValue(token);
        }

        private com.coradec.coradoc.state.ProcessingState componentValue(final ParserToken token) {
            if (componentValueProcessor.process(token)) {
                content.add(componentValueProcessor.getValue());
                return this::initial;
            }
            return this::componentValue;
        }

        SimpleBlock getSimpleBlock() {
            return new SimpleBlock(content);
        }
    }

    private class ComponentValueProcessor {

        private ProcessingState state = this::initial;
        private boolean terminated = false;
        private ParserToken value;
        private SimpleBlockProcessor simpleBlockProcessor;
        private FunctionProcessor functionProcessor;

        boolean process(final ParserToken token) {
            state = state.process(token);
            return terminated;
        }

        private com.coradec.coradoc.state.ProcessingState initial(final ParserToken token) {
            if (token instanceof CurlyOpen) {
                simpleBlockProcessor = new SimpleBlockProcessor(CurlyClose.class);
                return this::simpleBlock;
            }
            if (token instanceof BracketOpen) {
                simpleBlockProcessor = new SimpleBlockProcessor(BracketClose.class);
                return this::simpleBlock;
            }
            if (token instanceof ParenthOpen) {
                simpleBlockProcessor = new SimpleBlockProcessor(ParenthClose.class);
                return this::simpleBlock;
            }
            if (token instanceof FunctionToken) {
                functionProcessor = new FunctionProcessor((FunctionToken)token);
                return this::function;
            }
            value = token;
            terminated = true;
            return this::terminal;
        }

        private com.coradec.coradoc.state.ProcessingState simpleBlock(final ParserToken token) {
            if (simpleBlockProcessor.process(token)) {
                value = simpleBlockProcessor.getSimpleBlock();
                terminated = true;
                return this::initial;
            }
            return this::simpleBlock;
        }

        private com.coradec.coradoc.state.ProcessingState function(final ParserToken token) {
            if (functionProcessor.process(token)) {
                value = functionProcessor.getFunction();
                terminated = true;
                return this::initial;
            }
            return this::function;
        }

        private com.coradec.coradoc.state.ProcessingState terminal(final ParserToken token) {
            return this::terminal;
        }

        ParserToken getValue() {
            return value;
        }
    }

    private class FunctionProcessor {

        private final List<ParserToken> arguments = new ArrayList<>();
        private final FunctionToken token;
        private ProcessingState state = this::initial;
        private boolean terminated = false;
        private ComponentValueProcessor componentValueProcessor;

        public FunctionProcessor(final FunctionToken token) {
            this.token = token;
        }

        boolean process(final ParserToken token) {
            state = state.process(token);
            return terminated;
        }

        private com.coradec.coradoc.state.ProcessingState initial(final ParserToken token) {
            if (isEOF(token) || token instanceof ParenthClose) {
                terminated = true;
                return this::terminal;
            }
            componentValueProcessor = new ComponentValueProcessor();
            return componentValue(token);
        }

        private com.coradec.coradoc.state.ProcessingState terminal(final ParserToken token) {
            return this::terminal;
        }

        private com.coradec.coradoc.state.ProcessingState componentValue(final ParserToken token) {
            if (componentValueProcessor.process(token)) {
                arguments.add(componentValueProcessor.getValue());
                return this::initial;
            }
            return this::componentValue;
        }

        ParserToken getFunction() {
            return new Function(token.getName(), arguments);
        }

    }

}
