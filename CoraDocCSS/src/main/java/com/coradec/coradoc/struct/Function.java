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

import com.coradec.coracore.annotation.Nullable;
import com.coradec.coracore.annotation.ToString;
import com.coradec.coradoc.model.ParserToken;
import com.coradec.coradoc.state.ProcessingState;
import com.coradec.coradoc.token.Comma;
import com.coradec.coradoc.token.Delimiter;
import com.coradec.coradoc.token.Semicolon;
import com.coradec.coradoc.token.Whitespace;
import com.coradec.coradoc.trouble.CssSyntaxException;
import com.coradec.coradoc.trouble.InvalidArgumentSeparatorException;
import com.coradec.coradoc.trouble.InvalidFunctionArgumentException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * ​​A function block.
 */
@SuppressWarnings("ClassHasNoToStringMethod")
public class Function extends SimpleBlock {

    private final String name;
    private @Nullable List<ParserToken> arguments;

    public Function(final String name, final List<ParserToken> tokens) {
        super(tokens);
        this.name = name;
    }

    @ToString public String getName() {
        return name;
    }

    public String getIdentifier() {
        return name.toUpperCase().replace('-', '_');
    }

    public List<ParserToken> getArguments() {
        if (arguments == null) arguments = new ArgumentsProcessor().process(getComponentValues());
        return Collections.unmodifiableList(arguments);
    }

    private class ArgumentsProcessor {

        private final List<ParserToken> arguments = new ArrayList<>();
        private boolean separatorLast = false;

        List<ParserToken> process(final List<ParserToken> tokens) {
            ProcessingState state = this::argument;
            for (final ParserToken token : tokens) {
                state = state.process(token);
            }
            if (separatorLast) {
                throw new CssSyntaxException();
            }
            clearComponentValues();
            return arguments;
        }

        private ProcessingState argument(final ParserToken token) {
            separatorLast = false;
            if (token instanceof Delimiter ||
                token instanceof Comma ||
                token instanceof Semicolon) {
                throw new InvalidFunctionArgumentException(token);
            }
            if (token instanceof Whitespace) return this::argument;
            arguments.add(token);
            return this::afterArgument;
        }

        private ProcessingState afterArgument(final ParserToken token) {
            separatorLast = true;
            if (token instanceof Whitespace) return this::afterArgument;
            if (token instanceof Comma) return this::argument;
            throw new InvalidArgumentSeparatorException(token);
        }
    }
}
