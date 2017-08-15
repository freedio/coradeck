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

package com.coradec.coracom.model;

/**
 * A request launching other requests and collecting/synchronizing their results.​
 * <p>
 * In order for the multi-request to succeed, all sub-requests must succeed.  As soon as one of the
 * sub-requests fails or is cancelled, the entire multi-request fails or is cancelled,
 * respectively.
 */
public interface MultiRequest extends Request {

    /**
     * Processes the multi-request.
     *
     * @return this multi-request.
     */
    MultiRequest process();

}
