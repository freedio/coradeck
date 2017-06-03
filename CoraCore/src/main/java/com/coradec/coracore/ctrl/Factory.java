/*
 * Copyright ⓒ 2017 by Coradec GmbH.
 *
 * This file is part of the Coradeck.
 *
 * Coradeck is free software: you can redistribute it under the the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or any later version.
 *
 * Coradeck is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR ANY PARTICULAR PURPOSE.  See the GNU General Public License for further details.
 *
 * The GNU General Public License is available from <http://www.gnu.org/licenses/>.
 *
 * @license GPL-3.0+ <http://spdx.org/licenses/GPL-3.0+>
 *
 * @author Dominik Wezel <dom@coradec.com>
 */

package com.coradec.coracore.ctrl;

/**
 * API of an object factory.
 *
 * @param <G> the generated object type.
 */
public interface Factory<G> {

    /**
     * Returns an instance of the requested type, supplying the specified arguments for
     * construction or selection.
     *
     * If CoreJet picks a singleton to satisfy the request, an already existing instance will be
     * returned, otherwise a new object will be created.
     *
     * @param args the initialization arguments.
     * @return an instance of G.
     */
    G get(Object... args);

    /**
     * Returns a new instance of the requested type, supplying the specified arguments for
     * construction or selection.
     *
     * This method is the same as {@link #get(Object...)}, but it will always create a new instance,
     * i.e. it will never pick a singleton instance.
     *
     * @param args the initialization arguments.
     * @return an instance of G.
     */
    G create(Object... args);

}
