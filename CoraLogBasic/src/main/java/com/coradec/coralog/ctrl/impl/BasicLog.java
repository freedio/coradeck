package com.coradec.coralog.ctrl.impl;

import static com.coradec.coralog.model.LogLevel.*;

import com.coradec.coracore.annotation.Component;
import com.coradec.coracore.annotation.Inject;
import com.coradec.coracore.annotation.Nullable;
import com.coradec.coracore.annotation.ToString;
import com.coradec.coracore.model.Origin;
import com.coradec.coralog.ctrl.Log;
import com.coradec.coralog.ctrl.LogFacility;
import com.coradec.coralog.ctrl.LogWithStringExtensions;
import com.coradec.coralog.model.LogEntry;
import com.coradec.coralog.model.LogLevel;
import com.coradec.coralog.model.impl.BasicLogEntry;
import com.coradec.coralog.model.impl.BasicProblemLogEntry;
import com.coradec.coralog.model.impl.BasicStringLogEntry;
import com.coradec.coralog.model.impl.BasicStringProblemLogEntry;
import com.coradec.coralog.model.impl.BasicTextLogEntry;
import com.coradec.coratext.model.Text;

/**
 * ​​Basic implementation of a log.
 */
@Component
public abstract class BasicLog implements Log {

    private LogLevel level;
    @Inject private LogFacility logging;

    public BasicLog(final LogLevel level) {
        this.level = level;
    }

    @Override @ToString public LogLevel getLevel() {
        return this.level;
    }

    public void setLevel(final LogLevel level) {
        this.level = level;
    }

    @Override public boolean logsAt(final LogLevel level) {
        return level.atLeast(getLevel());
    }

    @Override public void log(final LogEntry entry) {
        logging.log(entry);
    }

    @Override public void warn(final Origin origin, final @Nullable Throwable problem,
                               final @Nullable Text text, final @Nullable Object[] textArgs) {
        if (logsAt(WARNING)) {
            log((problem != null) //
                ? new BasicProblemLogEntry(origin, WARNING, problem, text, textArgs)
                : text != null ? new BasicTextLogEntry(origin, WARNING, text, textArgs)
                               : new BasicLogEntry(origin, WARNING));
        }
    }

    @Override public void warn(final Origin origin, final Throwable problem) {
        if (logsAt(WARNING)) {
            log(new BasicProblemLogEntry(origin, WARNING, problem, null));
        }
    }

    @Override public void error(final Origin origin, final @Nullable Throwable problem,
                                final @Nullable Text text, final Object... textArgs) {
        if (logsAt(ERROR)) {
            log((problem != null) //
                ? new BasicProblemLogEntry(origin, ERROR, problem, text, textArgs)
                : text != null ? new BasicTextLogEntry(origin, ERROR, text, textArgs)
                               : new BasicLogEntry(origin, ERROR));
        }
    }

    @Override public void error(final Origin origin, final Throwable problem) {
        if (logsAt(ERROR)) {
            log(new BasicProblemLogEntry(origin, ERROR, problem, null));
        }
    }

    @Override public void debug(final Origin origin, final String text, final Object... textArgs) {
        if (logsAt(LogLevel.DEBUG)) {
            log(new BasicStringLogEntry(origin, DEBUG, text, textArgs));
        }
    }

    @Override public LogWithStringExtensions discloseStringExtensions() {
        return new InternalLogWithStringExtensions();
    }

    private class InternalLogWithStringExtensions implements LogWithStringExtensions {

        @Override public boolean logsAt(final LogLevel level) {
            return BasicLog.this.logsAt(level);
        }

        @Override public void log(final LogEntry entry) {
            BasicLog.this.log(entry);
        }

        @Override public LogLevel getLevel() {
            return BasicLog.this.getLevel();
        }

        @Override public void warn(final Origin origin, final @Nullable Throwable problem,
                                   final @Nullable Text text, final @Nullable Object[] textArgs) {
            BasicLog.this.warn(origin, problem, text, textArgs);
        }

        @Override public void warn(final Origin origin, final Throwable problem) {
            BasicLog.this.warn(origin, problem);
        }

        @Override public void error(final Origin origin, final @Nullable Throwable problem,
                                    final @Nullable Text text, final Object... textArgs) {
            BasicLog.this.error(origin, problem, text, textArgs);
        }

        @Override public void error(final Origin origin, final Throwable problem) {
            BasicLog.this.error(origin, problem);
        }

        @Override public void error(final Origin origin, final Throwable problem, final String text,
                                    final Object... args) {
            if (BasicLog.this.logsAt(ERROR))
                log(new BasicStringProblemLogEntry(origin, ERROR, problem, text, args));
        }

        @Override public void error(final Origin origin, final String text, final Object... args) {
            if (BasicLog.this.logsAt(ERROR))
                log(new BasicStringLogEntry(origin, ERROR, text, args));
        }

        @Override public void info(final Origin origin, final String text, final Object... args) {
            if (BasicLog.this.logsAt(INFORMATION))
                log(new BasicStringLogEntry(origin, INFORMATION, text, args));
        }

        @Override
        public void debug(final Origin origin, final String text, final Object... textArgs) {
            BasicLog.this.debug(origin, text, textArgs);
        }

        @Override public LogWithStringExtensions discloseStringExtensions() {
            return this;
        }

    }

}
