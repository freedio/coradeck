package com.coradec.coralog.ctrl;

import com.coradec.coralog.model.LogEntry;

/**
 * Facade of the log output system.
 */
public interface LogFacility {

    /**
     * Logs the specified entry to the logging system.
     *
     * @param entry the entry to log.
     */
    void log(LogEntry entry);

}
