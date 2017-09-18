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

package com.coradec.coragui.model;

import com.coradec.corasession.model.Session;

import java.util.Optional;

/**
 * ​API of the GUI façade and collection.
 */
public interface GUI {

    /**
     * Returns the top level component with the specified name, cast to the specified type, in the
     * context of the specified session.
     *
     * @param <C>     the component type.
     * @param session the session context.
     * @param type    the component type selector.
     * @param name    the name of the top-level component in the GUI definition..
     * @return the component.
     */
    <C extends Gadget> C getComponent(Session session, Class<C> type, String name);

    /**
     * Returns the top level component with the specified name, cast to the specified type, in the
     * context of the specified session, if such a component exists.
     *
     * @param <C>     the component type.
     * @param session the session context.
     * @param type    the component type selector.
     * @param name    the name of the top-level component in the GUI definition..
     * @return the component, or {@link Optional#empty()}.
     */
    <C extends Gadget> Optional<C> findComponent(Session session, Class<C> type, String name);

}
