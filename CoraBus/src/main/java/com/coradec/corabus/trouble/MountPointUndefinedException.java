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

package com.coradec.corabus.trouble;

import com.coradec.corabus.model.BusNode;
import com.coradec.coracore.annotation.ToString;
import com.coradec.coradir.model.Path;

/**
 * ​​Indicates an attempt to attach a node without a predefined mount mount.
 */
public class MountPointUndefinedException extends BusException {

    private final Path path;
    private final BusNode node;

    public MountPointUndefinedException(final String name, final BusNode node) {
        this(Path.of(name), node);
    }

    public MountPointUndefinedException(final Path path, final BusNode node) {
        this.path = path;
        this.node = node;
    }

    @ToString public Path getPath() {
        return path;
    }

    @ToString public BusNode getNode() {
        return node;
    }
}
