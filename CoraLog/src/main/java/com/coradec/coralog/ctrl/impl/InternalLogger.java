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

import static com.coradec.coralog.model.LogLevel.*;

import com.coradec.coracore.model.Origin;
import com.coradec.coralog.model.impl.BasicStringLogEntry;
import com.coradec.coralog.model.impl.BasicStringProblemLogEntry;

/**
 * ​​A logger for internal purposes.
 */
public class InternalLogger extends Logger {

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
        if (log.logsAt(ERROR)) log.log(new BasicStringLogEntry(tthere(), ERROR, text, textArgs));
    }

    public void info(final String text, final Object... textArgs) {
        if (log.logsAt(INFORMATION))
            log.log(new BasicStringLogEntry(tthere(), INFORMATION, text, textArgs));
    }

}
