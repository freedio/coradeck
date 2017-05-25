package com.coradec.coralog.model;

import com.coradec.coratext.model.Text;

import java.util.Optional;

/**
 * ​A log entry conveying a problem and an optional explanation for the problem.
 */
public interface ProblemLogEntry extends LogEntry {

    /**
     * Returns the explanatory text literal.
     *
     * @return the explanatory text literal, if any.
     */
    Optional<Text> getText();

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
