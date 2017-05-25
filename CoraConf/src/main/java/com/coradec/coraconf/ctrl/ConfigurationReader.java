package com.coradec.coraconf.ctrl;

import com.coradec.coraconf.model.AnnotatedProperty;

import java.io.IOException;
import java.util.Set;

/**
 * ​An object that reads a configuration file.
 */
public interface ConfigurationReader {

    /**
     * Returns the parsed properties.
     *
     * @return the parsed properties.
     * @throws IOException if parsing failed.
     */
    Set<AnnotatedProperty> getProperties() throws IOException;
}
