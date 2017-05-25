package com.coradec.coratext.model;

import static com.coradec.coracore.util.ExecUtil.*;

import com.coradec.coracore.ctrl.Factory;
import com.coradec.coracore.model.GenericFactory;

/**
 * ​​A text literal to be resolved in a specific locale.
 */
public interface LocalizedText extends Text {

    Factory<Text> text = new GenericFactory<>(Text.class);

    /**
     * Defines a new text literal in the caller context.
     *
     * @param name the name of the literal.
     * @return a new localized text literal.
     */
    static Text define(final String name) {
        return text.get(LocalizedText.class, getCallerStackFrame().getClassFileName(),
                name);
    }

}
