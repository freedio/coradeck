package com.coradec.coralog.ctrl.impl;

import static com.coradec.coralog.model.LogLevel.*;

import com.coradec.coracore.annotation.Component;
import com.coradec.coracore.annotation.Inject;
import com.coradec.coracore.annotation.Nullable;
import com.coradec.coracore.ctrl.AutoOrigin;
import com.coradec.coracore.ctrl.Factory;
import com.coradec.coracore.model.Origin;
import com.coradec.coracore.trouble.BasicException;
import com.coradec.coralog.ctrl.ClassLog;
import com.coradec.coralog.model.LogLevel;
import com.coradec.coralog.model.impl.BasicLogEntry;
import com.coradec.coralog.model.impl.BasicProblemLogEntry;
import com.coradec.coralog.model.impl.BasicStringLogEntry;
import com.coradec.coralog.model.impl.BasicStringProblemLogEntry;
import com.coradec.coralog.model.impl.BasicTextLogEntry;
import com.coradec.coratext.model.LocalizedText;
import com.coradec.coratext.model.Text;

/**
 * ​​A class template providing logging.
 */
@SuppressWarnings("ClassHasNoToStringMethod")
@Component
public class Logger extends AutoOrigin {

    @Inject private static Factory<ClassLog> CLASSLOG;

    private static Text ENTERING;
    private static Text LEAVING;
    private static Text FAILING;

    private final ClassLog log = CLASSLOG.create(getClass());

    /**
     * Logs a method entry at log level CHAT.
     */
    protected void enter() {
        if (ENTERING == null) ENTERING = LocalizedText.define("Entering");
        if (log.logsAt(CHAT)) {
            log.log(new BasicTextLogEntry(there(), CHAT, ENTERING));
        }
    }

    /**
     * Logs a method exit at log level CHAT.
     */
    protected void leave() {
        if (LEAVING == null) LEAVING = LocalizedText.define("Leaving");
        if (log.logsAt(CHAT)) {
            log.log(new BasicTextLogEntry(there(), CHAT, LEAVING));
        }
    }

    /**
     * Logs a method failure at log level CHAT, then rethrows the specified exception.
     *
     * @param problem the problem to log.
     * @throws BasicException always.
     */
    protected void fail(final BasicException problem) throws BasicException {
        if (FAILING == null) FAILING = LocalizedText.define("FailingWith");
        if (log.logsAt(CHAT)) {
            log.log(new BasicProblemLogEntry(there(), CHAT, problem, FAILING, problem));
        }
    }

    /**
     * Logs the specified text at log level DEBUG.
     *
     * @param text     the explanatory text.
     * @param textArgs optional arguments to fit into the template text.
     */
    protected void debug(final String text, final Object... textArgs) {
        if (log.logsAt(DEBUG)) {
            log.log(new BasicStringLogEntry(there(), DEBUG, text, textArgs));
        }
    }

    /**
     * Logs the specified text from the specified origin at log level DEBUG.
     *
     * @param origin   the origin of the message.
     * @param text     the explanatory text.
     * @param textArgs optional arguments to fit into the template text.
     */
    protected void debug(final Origin origin, final String text, final Object... textArgs) {
        if (log.logsAt(DEBUG)) {
            log.log(new BasicStringLogEntry(origin, DEBUG, text, textArgs));
        }
    }

    /**
     * Logs the specified text from the specified origin at log level INFORMATION.
     *
     * @param origin   the origin of the warning.
     * @param text     the explanatory text.
     * @param textArgs optional arguments to fit into the template text.
     */
    protected void info(final Origin origin, final Text text, final Object... textArgs) {
        if (log.logsAt(INFORMATION)) {
            log.log(new BasicTextLogEntry(origin, INFORMATION, text, textArgs));
        }
    }

    /**
     * Logs the specified text from the caller at log level INFORMATION.
     *
     * @param text     the explanatory text.
     * @param textArgs optional arguments to fit into the template text.
     */
    protected void info(final Text text, final Object... textArgs) {
        if (log.logsAt(INFORMATION)) {
            log.log(new BasicTextLogEntry(there(), INFORMATION, text, textArgs));
        }
    }

    /**
     * Logs the specified problem from the specified origin along with the specified explanatory
     * text at log level WARNING.
     *
     * @param origin   the origin.
     * @param problem  the problem to log.
     * @param text     the explanatory text (optional).
     * @param textArgs arguments for the explanatory text (optional).
     */
    protected void warn(final Origin origin, final @Nullable Throwable problem,
                        final @Nullable Text text, final @Nullable Object... textArgs) {
        if (log.logsAt(WARNING)) {
            log.log((problem != null) //
                         ? new BasicProblemLogEntry(origin, WARNING, problem, text, textArgs)
                         : text != null ? new BasicTextLogEntry(origin, WARNING, text, textArgs)
                                        : new BasicLogEntry(origin, WARNING));
        }
    }

    /**
     * Logs the specified problem along with the specified explanatory text at log level WARNING.
     *
     * @param problem  the problem to log.
     * @param text     the explanatory text.
     * @param textArgs arguments to fit into the template text.
     */
    protected void warn(final @Nullable Throwable problem, final @Nullable Text text,
                        final @Nullable Object... textArgs) {
        if (log.logsAt(WARNING)) {
            log.log((problem != null) //
                         ? new BasicProblemLogEntry(there(), WARNING, problem, text, textArgs)
                         : text != null ? new BasicTextLogEntry(there(), WARNING, text, textArgs)
                                        : new BasicLogEntry(there(), WARNING));
        }
    }

    /**
     * Logs the specified problem at log level WARNING.
     *
     * @param problem the problem to log.
     */
    protected void warn(final Throwable problem) {
        if (log.logsAt(WARNING)) {
            log.log(new BasicProblemLogEntry(there(), WARNING, problem, null));
        }
    }

    /**
     * Logs the specified problem from the specified origin at log level WARNING.
     *
     * @param origin  the origin.
     * @param problem the problem to log.
     */
    protected void warn(final Origin origin, final Throwable problem) {
        if (log.logsAt(WARNING)) {
            log.log(new BasicProblemLogEntry(origin, WARNING, problem, null));
        }
    }

    /**
     * Logs the specified text from the specified origin at log level WARNING.
     *
     * @param origin   the origin of the warning.
     * @param text     the explanatory text.
     * @param textArgs optional arguments to fit into the template text.
     */
    protected void warn(final Origin origin, final Text text, final Object... textArgs) {
        if (log.logsAt(WARNING)) {
            log.log(new BasicTextLogEntry(origin, WARNING, text, textArgs));
        }
    }

    /**
     * Logs the specified text at log level WARNING.
     *
     * @param text     the explanatory text.
     * @param textArgs optional arguments to fit into the template text.
     */
    protected void warn(final Text text, final Object... textArgs) {
        if (log.logsAt(WARNING)) {
            log.log(new BasicTextLogEntry(there(), WARNING, text, textArgs));
        }
    }

    /**
     * Logs the specified problem from the specified origin along with the specified explanatory
     * text at log level ERROR.
     *
     * @param origin   the origin.
     * @param problem  the problem to log.
     * @param text     the explanatory text.
     * @param textArgs arguments to fit into the template text.
     */
    protected void error(final Origin origin, final @Nullable Throwable problem,
                         final @Nullable Text text, final Object... textArgs) {
        if (log.logsAt(LogLevel.ERROR)) {
            log.log((problem != null) ? new BasicProblemLogEntry(origin, ERROR, problem, text)
                                           : text != null ? new BasicTextLogEntry(origin, ERROR,
                                                   text, textArgs)
                                                          : new BasicLogEntry(origin, ERROR));
        }
    }

    /**
     * Logs the specified problem along with the specified explanatory text at log level ERROR.
     *
     * @param problem  the problem to log.
     * @param text     the explanatory text.
     * @param textArgs arguments to fit into the template text.
     */
    protected void error(final @Nullable Throwable problem, final @Nullable Text text,
                         final Object... textArgs) {
        if (log.logsAt(LogLevel.ERROR)) {
            log.log((problem != null) //
                         ? new BasicProblemLogEntry(there(), ERROR, problem, text, textArgs)
                         : text != null ? new BasicTextLogEntry(there(), ERROR, text, textArgs)
                                        : new BasicLogEntry(there(), ERROR));
        }
    }

    /**
     * Logs the specified problem from the specified origin at log level ERROR.
     *
     * @param origin  the origin.
     * @param problem the problem to log.
     */
    protected void error(final Origin origin, final Throwable problem) {
        if (log.logsAt(LogLevel.ERROR)) {
            log.log(new BasicProblemLogEntry(origin, ERROR, problem, null));
        }
    }

    /**
     * Logs the specified problem at log level ERROR.
     *
     * @param problem the problem to log.
     */
    protected void error(final Throwable problem) {
        if (log.logsAt(LogLevel.ERROR)) {
            log.log(new BasicProblemLogEntry(there(), ERROR, problem, null));
        }
    }

    /**
     * Logs the specified text from the specified origin at log level ERROR.
     *
     * @param origin   the origin of the warning.
     * @param text     the explanatory text.
     * @param textArgs optional arguments to fit into the template text.
     */
    protected void error(final Origin origin, final Text text, final Object... textArgs) {
        if (log.logsAt(ERROR)) {
            log.log(new BasicTextLogEntry(origin, ERROR, text, textArgs));
        }
    }

    /**
     * Logs the specified text at log level ERROR.
     *
     * @param text     the explanatory text.
     * @param textArgs optional arguments to fit into the template text.
     */
    protected void error(final Text text, final Object... textArgs) {
        if (log.logsAt(LogLevel.ERROR)) {
            log.log(new BasicTextLogEntry(there(), ERROR, text, textArgs));
        }
    }

    protected ExtendedLogger discloseStringExtensions() {
        return new ExtendedLogger();
    }

    protected class ExtendedLogger {

        public void warn(final Origin origin, final Throwable problem, final String text,
                         final Object... textArgs) {
            if (log.logsAt(WARNING))
                log.log(new BasicStringProblemLogEntry(origin, WARNING, problem, text, textArgs));
        }

        public void warn(final String text, final Object... textArgs) {
            if (log.logsAt(WARNING))
                log.log(new BasicStringLogEntry(tthere(), WARNING, text, textArgs));
        }

        public void error(final String text, final Object... textArgs) {
            if (log.logsAt(ERROR))
                log.log(new BasicStringLogEntry(tthere(), ERROR, text, textArgs));
        }
    }
}
