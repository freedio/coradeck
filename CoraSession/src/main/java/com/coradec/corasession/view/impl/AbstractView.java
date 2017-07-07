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

package com.coradec.corasession.view.impl;

import com.coradec.coracore.annotation.ToString;
import com.coradec.coracore.util.ClassUtil;
import com.coradec.corasession.model.Session;
import com.coradec.corasession.view.View;

/**
 * ​​Basic implementation of a view.
 */
public class AbstractView implements View {

    private final Session session;

    /**
     * Initializes a new instance of AbstractView with the specified session context.
     *
     * @param session the session context.
     */
    protected AbstractView(final Session session) {
        this.session = session;
    }

    @ToString @Override public Session getSession() {
        return session;
    }

    @Override public String toString() {
        return ClassUtil.toString(this);
    }

}
