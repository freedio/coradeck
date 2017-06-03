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

package com.coradec.coraconf.trouble;

import com.coradec.coracore.annotation.Nullable;
import com.coradec.coracore.annotation.ToString;

import java.net.URL;

/**
 * ​​Base class of all exceptions related to resources.
 */
@SuppressWarnings("WeakerAccess")
public class ResourceFailure extends ConfigurationException {

    private final @Nullable String resourceName;
    private final @Nullable URL resource;

    /**
     * Initializes a new instance of ResourceFailure for the resource with the specified name.
     *
     * @param resourceName the resource name.
     */
    public ResourceFailure(@Nullable final String resourceName) {
        this.resource = null;
        this.resourceName = resourceName;
    }

    /**
     * Initializes a new instance of ResourceFailure for the specified resource with the specified
     * underlying problem and explanation.
     *
     * @param resource    the resource.
     * @param explanation the explanation.
     * @param problem     the underlying problem.
     */
    public ResourceFailure(@Nullable final URL resource, final String explanation,
                           final Throwable problem) {
        super(explanation, problem);
        this.resource = resource;
        this.resourceName = resource != null ? resource.toExternalForm() : null;
    }

    @ToString public @Nullable String getResourceName() {
        String result = this.resourceName;
        if (resource != null) result = resource.toExternalForm();
        return result;
    }

}
