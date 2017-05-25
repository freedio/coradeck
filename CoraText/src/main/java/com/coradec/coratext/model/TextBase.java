package com.coradec.coratext.model;

import java.util.Optional;

/**
 * ​Representation of a text base.
 */
public interface TextBase {

    /**
     * Returns the context.
     * <p>
     * If no context is defined, the text base is called the default text base.
     *
     * @return the context, if defined.
     */
    Optional<String> getContext();

    /**
     * Resolves the text literal with the specified name using the specified arguments to fill in
     * template variables.
     *
     * @param name the text literal name.
     * @param args arguments to fill the gaps.
     * @return the resolved text literal.
     */
    String resolve(String name, Object... args);

}
