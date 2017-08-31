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

package com.coradec.coradir.model;

import com.coradec.coracore.model.Factory;
import com.coradec.coracore.model.GenericFactory;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * ​A path that transcends the local machine and therefore has a system and machine part.
 */
public interface TranscendentPath extends Path {

    Factory<TranscendentPath> PATH = new GenericFactory<>(Path.class);

    /**
     * Returns a transcendent path from the specified URI.
     *
     * @param uri the URI.
     * @return a path.
     */
    static Path of(URI uri) {
        final String path = uri.getPath();
        if (path == null) return Path.of();
        final String host = uri.getHost();
        if (host == null) return Path.of(path);
        List<String> elements = new ArrayList<>(Arrays.asList(path.split("/")));
        String machine = elements.isEmpty() ? "" : elements.remove(0);
        return PATH.create(host, machine, elements);
    }

    /**
     * Returns the host name.
     *
     * @return the host name.
     */
    String getHostname();

}
