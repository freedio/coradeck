/*
 * Copyright ⓒ 2017 by Coradec GmbH.
 *
 * This file is part of the Coradeck.
 *
 * Coradeck is free software: you can redistribute it under the the terms of the GNU General
 * Public License as published by the Free Software Foundation, either version 3 of the License,
 * or any later version.
 *
 * Coradeck is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR ANY PARTICULAR PURPOSE.  See the
 * GNU General Public License for further details.
 *
 * The GNU General Public License is available from <http://www.gnu.org/licenses/>.
 *
 * @license GPL-3.0+ <http://spdx.org/licenses/GPL-3.0+>
 * @author Dominik Wezel <dom@coradec.com>
 *
 */

package com.coradec.coralog.ctrl.impl;

import static com.coradec.coracore.model.Scope.*;

import com.coradec.coracore.annotation.Implementation;
import com.coradec.coracore.annotation.ToString;
import com.coradec.coracore.util.ClassUtil;
import com.coradec.coracore.util.StringUtil;
import com.coradec.coralog.ctrl.LogFacility;
import com.coradec.coralog.model.LogEntry;
import com.coradec.coralog.model.ProblemLogEntry;
import com.coradec.coratext.model.LocalizedText;
import com.coradec.coratext.model.Text;

import java.io.PrintStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.function.Function;

/**
 * ​​Basic implementation of a log facility.
 */
@SuppressWarnings({"UseOfSystemOutOrSystemErr", "ClassHasNoToStringMethod"})
@Implementation(SINGLETON)
public class ConsoleLogFacility implements LogFacility {

    private static final Text TEXT_DATE = LocalizedText.define("Date");
    private static final Text TEXT_LINE1 = LocalizedText.define("Line1");
    private static final Text TEXT_LINE2 = LocalizedText.define("Line2");

    private static final Extractor[] EXTRACTORS = {
            new Extractor("content", LogEntry::getContent),
            new Extractor("time", logEntry -> logEntry.getTimestamp().toLocalTime()),
            new Extractor("level", LogEntry::getLevel),
            new Extractor("origin", LogEntry::getOrigin),
            new Extractor("none", logEntry -> StringUtil.BLANK)
    };

    private LocalDate lastDate;

    @Override public void log(final LogEntry entry) {
        final PrintStream out = entry.getLevel().isSevere() ? System.err : System.out;
        final LocalDateTime timestamp = entry.getTimestamp();
        final LocalDate localDate = timestamp.toLocalDate();
        final LocalTime localTime = timestamp.toLocalTime();
        out.flush();
        if (!localDate.equals(lastDate)) {
            out.println(TEXT_DATE.resolve(localDate));
            lastDate = localDate;
        }
        out.println(format(TEXT_LINE1.get(), entry));
        out.println(format(TEXT_LINE2.get(), entry));
        if (entry instanceof ProblemLogEntry) {
            ((ProblemLogEntry)entry).getProblem().printStackTrace(out);
        }
        out.flush();
    }

    private CharSequence format(final String format, final LogEntry entry) {
        String form = format;
        for (final Extractor extractor : EXTRACTORS) {
            String tag = extractor.getTag() + '[';
            for (int i = form.indexOf(tag); i != -1; i = form.indexOf(tag)) {
                int k = i + tag.length();
                int j = form.indexOf(']', k + 1);
                String fmt = form.substring(k, j);
                final String formatted =
                        String.format(fmt, StringUtil.represent(extractor.apply(entry)));
                form = form.substring(0, i) + formatted + form.substring(j + 1);
            }
        }
        return form;
    }

    private static class Extractor {

        private final String tag;
        private final Function<LogEntry, Object> function;

        Extractor(final String tag, final Function<LogEntry, Object> f) {
            this.tag = tag;
            this.function = f;
        }

        @SuppressWarnings("WeakerAccess") @ToString public String getTag() {
            return this.tag;
        }

        Object apply(LogEntry entry) {
            return function.apply(entry);
        }

        @Override public String toString() {
            return ClassUtil.toString(this);
        }
    }

}
