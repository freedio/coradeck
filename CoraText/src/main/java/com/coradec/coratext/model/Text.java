package com.coradec.coratext.model;

import com.coradec.coracore.annotation.Nullable;

/**
 * ​Representation of a text literal.
 */
public interface Text {

    /**
     * Returns the (full) name of the text literal.
     * @return the full name of the text literal.
     */
    String getName();

    /**
     * Resolves the text with the specified arguments.
     *
     * @param args the text arguments (optional).
     * @return the resolved text.
     */
    String resolve(@Nullable Object... args);

}
