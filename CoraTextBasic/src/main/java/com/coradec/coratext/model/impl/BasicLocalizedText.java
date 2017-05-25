package com.coradec.coratext.model.impl;

import com.coradec.coracore.annotation.Component;
import com.coradec.coratext.model.LocalizedText;

/**
 * ​​Basic implementation of a localized text.
 */
@SuppressWarnings("ClassHasNoToStringMethod")
@Component
public class BasicLocalizedText extends BasicText implements LocalizedText {

    private final String context;
    private final String localName;

    public BasicLocalizedText(final String context, final String name) {
        super(context + "." + name);
        this.context = context;
        this.localName = name;
    }

    @Override public String getName() {
        return context + "." + localName;
    }

    @Override public String resolve(final Object... args) {
        return RESOLVER.resolve(context, localName, args);
    }

}
