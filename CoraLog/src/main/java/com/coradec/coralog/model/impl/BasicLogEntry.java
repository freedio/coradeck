package com.coradec.coralog.model.impl;

import com.coradec.coracore.annotation.ToString;
import com.coradec.coracore.model.Origin;
import com.coradec.coracore.util.ClassUtil;
import com.coradec.coralog.model.LogEntry;
import com.coradec.coralog.model.LogLevel;

import java.time.LocalDateTime;

/**
 * ​​Basic implementation of a log entry.
 */
public class BasicLogEntry implements LogEntry {

    private LocalDateTime timestamp;
    private final Origin origin;
    private final LogLevel level;

    public BasicLogEntry(final Origin origin, final LogLevel level) {
        this.timestamp = LocalDateTime.now();
        this.origin = origin;
        this.level = level;
    }

    @Override @ToString public Origin getOrigin() {
        return this.origin;
    }

    @Override @ToString public LogLevel getLevel() {
        return this.level;
    }

    @Override @ToString public LocalDateTime getTimestamp() {
        return timestamp;
    }

    @Override public String toString() {
        return ClassUtil.toString(this);
    }
}
