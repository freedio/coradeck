package com.coradec.coracore.trouble;

import com.coradec.coracore.annotation.ToString;

import java.util.Collection;

/**
 * ​​An exception with multiple reasons
 */
public class MultiException extends BasicException {

    private final Collection<Exception> causes;

    public MultiException(final Collection<Exception> causes) {
        this.causes = causes;
    }

    @ToString public Collection<Exception> getCauses() {
        return this.causes;
    }

}
