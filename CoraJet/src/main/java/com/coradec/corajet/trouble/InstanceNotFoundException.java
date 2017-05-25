package com.coradec.corajet.trouble;

import com.coradec.coracore.annotation.ToString;
import com.coradec.coracore.model.Type;

/**
 * ​​
 */
public class InstanceNotFoundException extends CoraJetException {

    private final Type<?> type;
    private final Object[] args;

    public InstanceNotFoundException(final Type<?> type, final Object... args) {
        this.type = type;
        this.args = args;
    }

    @ToString public Type<?> getType() {
        return this.type;
    }

    @ToString public Object[] getArgs() {
        return this.args;
    }
}
