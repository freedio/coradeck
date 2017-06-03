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

package com.coradec.coratext.ctrl.impl;

import com.coradec.coracore.annotation.Inject;
import com.coradec.coracore.annotation.Nullable;
import com.coradec.coracore.util.ClassUtil;
import com.coradec.coratext.ctrl.TextResolver;
import com.coradec.coratext.model.TextBase;
import com.coradec.coratext.trouble.TextBaseNotFoundException;

import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * ​​Basic implementation of a text resolver.
 */
@SuppressWarnings("ClassHasNoToStringMethod")
public class BasicTextResolver implements TextResolver {

    @Inject private TextBaseFactory textBases;

    @Override
    public String resolve(final @Nullable String context, final String name, final Object... args) {
        try {
            final TextBase textBase = textBases.get(context);
            return textBase.resolve(name, args);
        }
        catch (TextBaseNotFoundException e) {
            StringBuilder collector = new StringBuilder(256);
            collector.append(String.format("Missing text literal %s",
                    context != null ? context.replace('.', '/') + "." + name : name));
            if (args.length > 0) {
                collector.append(//
                        Stream.of(args)
                              .map(ClassUtil::toString)
                              .collect(Collectors.joining(", ", " <", ">")));
            }
            return collector.toString();
        }
    }

}
