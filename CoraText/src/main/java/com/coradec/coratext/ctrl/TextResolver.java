package com.coradec.coratext.ctrl;

import com.coradec.coracore.annotation.Nullable;

/**
 * API of a text resolver.​
 */
public interface TextResolver {

    /**
     * Resolves the text literal with the specified name in the specified optional context, using
     * the specified arguments to fill in template variables.
     *
     * @param context the context (optional).
     * @param name    the full literal name.
     * @param args    arguments to fit into the template text.
     * @return the resolved text.
     */
    String resolve(final @Nullable String context, final String name, final Object[] args);

}
