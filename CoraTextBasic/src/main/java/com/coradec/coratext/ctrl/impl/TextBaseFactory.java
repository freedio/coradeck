package com.coradec.coratext.ctrl.impl;

import com.coradec.coracore.annotation.Component;
import com.coradec.coracore.annotation.Inject;
import com.coradec.coracore.annotation.Nullable;
import com.coradec.coracore.ctrl.Factory;
import com.coradec.coratext.model.TextBase;
import com.coradec.coratext.trouble.TextBaseNotFoundException;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages different text bases.
 */
@SuppressWarnings("ClassHasNoToStringMethod")
@Component
public class TextBaseFactory {

    @Inject private static Factory<TextBase> TEXT_BASE;

    private TextBase dflt;
    private final Map<String, TextBase> textBases;

    private TextBaseFactory() {
        textBases = createTextBaseMap();
    }

    /**
     * Creates a suitable text base map.
     * @return a new text base map.
     */
    protected Map<String, TextBase> createTextBaseMap() {
        return new ConcurrentHashMap<>();
    }

    /**
     * Returns the text base with the specified name, or the default text base if the name is not
     * specified.
     *
     * @param context the text base name (optional).
     * @return the text base with the specified name.
     */
    public TextBase get(final @Nullable String context) throws TextBaseNotFoundException {
        TextBase result = context == null ? dflt : textBases.get(context);
        if (result == null) {
            if (context == null) result = dflt = TEXT_BASE.get(TextBase.class);
            else textBases.put(context, result = TEXT_BASE.get(TextBase.class, context));
        }
        if (result == null) throw new TextBaseNotFoundException(context);
        return result;
    }

}
