package com.coradec.coralog.model;

import com.coradec.coracore.model.Origin;

import java.time.LocalDateTime;

/**
 * ​An entry that can be written to a log.
 */
public interface LogEntry {

    /**
     * Returns the origin of the log entry.
     *
     * @return the origin of the log entry.
     */
    Origin getOrigin();

    /**
     * Returns the log level.
     *
     * @return the log level.
     */
    LogLevel getLevel();

    /**
     * Returns the creation timestamp of the log entry.
     *
     * @return the timestamp.
     */
    LocalDateTime getTimestamp();
}
