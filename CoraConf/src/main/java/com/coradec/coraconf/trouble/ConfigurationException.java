package com.coradec.coraconf.trouble;

import com.coradec.coracore.trouble.BasicException;

/**
 * ​​Base class of all exceptions related to configurations.
 */
public class ConfigurationException extends BasicException {

    /**
     * Initializes a new instance of ConfigurationException with the specified underlying problem
     * and explanation.
     *
     * @param explanation the explanation.
     * @param problem     the underlying problem.
     */
    public ConfigurationException(final String explanation, final Throwable problem) {
        super(explanation, problem);
    }

    /**
     * Initializes a new instance of ConfigurationException.
     */
    public ConfigurationException() {
    }

    /**
     * Initializes a new instance of ConfigurationException with the specified underlying problem.
     *
     * @param problem     the underlying problem.
     */
    public ConfigurationException(final Throwable problem) {
        super(problem);
    }

}
