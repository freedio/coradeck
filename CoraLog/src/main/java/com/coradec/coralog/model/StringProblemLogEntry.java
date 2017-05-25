package com.coradec.coralog.model;

import java.util.Optional;

/**
 * ​A log entry conveying a problem and an optional explanation for the problem.
 */
public interface StringProblemLogEntry {

    /**
     * Returns the text template.
     *
     * @return the text template.
     */
    Optional<String> getTemplate();

    /**
     * Returns the problem.
     *
     * @return the problem.
     */
    Throwable getProblem();

    /**
     * Returns the explanation.
     *
     * @return the explanation, if present
     */
    Optional<String> getExplanation();

}
