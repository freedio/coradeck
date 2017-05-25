package com.coradec.corajet.trouble;

import com.coradec.coracore.annotation.Nullable;
import com.coradec.coracore.annotation.ToString;

/**
 * ​​Indicates an attempt to inject an instance for an interface for which no implementation is
 * known.
 */
public class ImplementationNotFoundException extends CoraJetException {

    private final @Nullable String targetClassName;
    private final @Nullable String fieldName;
    private final @Nullable Class<?> type;

    public ImplementationNotFoundException(final String targetClassName, final String fieldName,
                                           final Class<?> fieldType) {
        this.targetClassName = targetClassName;
        this.fieldName = fieldName;
        this.type = fieldType;
    }

    @ToString public @Nullable Class<?> getType() {
        return this.type;
    }

    @ToString public @Nullable String getTargetClassName() {
        return this.targetClassName;
    }

    @ToString public @Nullable String getFieldName() {
        return this.fieldName;
    }
}
