package com.coradec.coratext.ctrl.impl;

import com.coradec.coracore.annotation.Component;
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
@Component
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
