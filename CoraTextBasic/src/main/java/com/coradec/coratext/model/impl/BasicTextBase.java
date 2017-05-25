package com.coradec.coratext.model.impl;

import com.coradec.coraconf.ctrl.ConfigurationReaderFactory;
import com.coradec.coraconf.model.AnnotatedProperty;
import com.coradec.coraconf.model.Configuration;
import com.coradec.coraconf.trouble.ConfigurationException;
import com.coradec.coracore.annotation.Component;
import com.coradec.coracore.annotation.Inject;
import com.coradec.coracore.annotation.NonNull;
import com.coradec.coracore.annotation.Nullable;
import com.coradec.coracore.annotation.ToString;
import com.coradec.coracore.ctrl.Factory;
import com.coradec.coracore.util.ClassUtil;
import com.coradec.coratext.ctrl.impl.ApplicationTextBase;
import com.coradec.coratext.model.TextBase;
import com.coradec.coratext.trouble.TextBaseNotFoundException;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.Set;

/**
 * ​​Basic implementation of a text base.
 */
@Component
public class BasicTextBase implements TextBase {

    @Inject private static Factory<ApplicationTextBase> APPLICATION_TEXT_BASE;

    private static final String TEXT_FILE_PATTERN =
            System.getProperty("com.coradec.infra.text.model.TextBase.FileTemplate", "%s.text");

    private final @Nullable String context;
    private final @Nullable URL baseFile;
    private @Nullable Configuration<String> literals;

    /**
     * Initializes a new instance of BasicTextBase with the specified context.
     *
     * @param context the context.
     * @throws TextBaseNotFoundException if no text base was found for the specified context.
     */
    public BasicTextBase(final @NonNull String context) throws TextBaseNotFoundException {
        this.context = context;
        final String contxt = String.format(TEXT_FILE_PATTERN, context.replace('.', '/'));
        baseFile = getClass().getClassLoader().getResource(contxt);
        if (baseFile == null) throw new TextBaseNotFoundException(contxt);
    }

    /**
     * Initializes a new instance of BasicTextBase without a context.
     */
    public BasicTextBase() {
        this.context = null;
        this.baseFile = null;
    }

    @Override @ToString public Optional<String> getContext() {
        return Optional.ofNullable(this.context);
    }

    @ToString public Optional<URL> getBaseFile() {
        return Optional.ofNullable(this.baseFile);
    }

    private Configuration<String> getLiterals() {
        if (literals == null) literals = loadLiterals();
        return this.literals;
    }

    private Configuration<String> loadLiterals() {
        if (baseFile == null || context == null) {
            return APPLICATION_TEXT_BASE.get();
        } else {
            try {
                final Set<AnnotatedProperty> properties =
                        ConfigurationReaderFactory.createParser(context, baseFile, ".text")
                                                  .getProperties();
                return Configuration.of(String.class).add(properties);
            }
            catch (IOException e) {
                throw new ConfigurationException(e);
            }
        }
    }

    @Override public String resolve(final String name, final Object... args) {
        final String result = getLiterals().lookup(name).orElse(//
                (context == null) ? String.format("<Unknown text literal %s>", name)
                                  : String.format("<Unknown text literal %s.%s>", context, name));
        return String.format(result, args);
    }

    @Override public String toString() {
        return ClassUtil.toString(this);
    }

}
