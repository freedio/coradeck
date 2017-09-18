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

import java.util.Optional;

/**
 * ​A basic graphical component, not necessarily visible.
 *
 * @param <P> the peer type.
 */
public interface Gadget<P> {

    /**
     * Returns the system representative ("peer") of the gadget
     *
     * @return the peer.
     */
    P getPeer();

    /**
     * Returns the name of the gadget.
     *
     * @return the name.
     */
    String getName();

    /**
     * Returns the parent of this gadget, if the gadget is bound into a GUI structure.
     *
     * @return the parent, or {@link Optional#empty()}
     */
    Optional<Container> getParent();

    /**
     * Sets the parent of the gadget.  This is usually done by {@link Container#add(Gadget)}.
     *
     * @param parent the parent to set.
     */
    void setParent(Container parent);

}
