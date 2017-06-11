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

package com.coradec.coraconf.ctrl.impl;

import com.coradec.coraconf.model.AnnotatedProperty;
import com.coradec.coracore.trouble.UnimplementedOperationException;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;

/**
 * ​​A configuration file reader for JSON-style configurations.
 */
public class JsonConfigurationReader extends BasicConfigurationReader {

    public JsonConfigurationReader(final String context, final URL resource) {
        super(context, resource);
    }

    @Override protected void open() throws IOException {
        throw new UnimplementedOperationException();
    }

    @Override protected void close() throws IOException {
        throw new UnimplementedOperationException();
    }

    @Override protected Optional<AnnotatedProperty> getNextProperty() {
        throw new UnimplementedOperationException();
    }

}
