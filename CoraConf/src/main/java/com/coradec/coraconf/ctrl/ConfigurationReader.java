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

package com.coradec.coraconf.ctrl;

import com.coradec.coraconf.model.AnnotatedProperty;

import java.io.IOException;
import java.util.Set;

/**
 * ​An object that reads a configuration file.
 */
public interface ConfigurationReader {

    /**
     * Returns the parsed properties.
     *
     * @return the parsed properties.
     * @throws IOException if parsing failed.
     */
    Set<AnnotatedProperty> getProperties() throws IOException;
}
