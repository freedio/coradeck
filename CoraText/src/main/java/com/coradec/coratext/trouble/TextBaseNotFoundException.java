package com.coradec.coratext.trouble;

import com.coradec.coracore.annotation.Nullable;
import com.coradec.coracore.annotation.ToString;

/**
 * ​​Indicates an attempt to access a text base which does not exist.
 */
public class TextBaseNotFoundException extends CoraTextException {

    private final String context;

    public TextBaseNotFoundException(final @Nullable String context) {
        this.context = context;
    }

    @ToString public String getContext() {
        return this.context != null ? this.context : "<default>";
    }

}
