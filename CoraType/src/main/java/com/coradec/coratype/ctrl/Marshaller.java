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

package com.coradec.coratype.ctrl;

/**
 * ​Serializes and deserializes "canned" objects of a particular type.
 * <p>
 * Object serializable through Marshaller must have a constructor taking a map.  The constructor
 *
 * @param <V> the object type
 */
public interface Marshaller<V> {

    /**
     * Serialzies an object into a byte array.
     *
     * @param object the object to serialize.
     * @return the serialized object.
     */
    byte[] marshal(V object);

    /**
     * Deserailizes a byte array into an object.
     *
     * @param data the canned object representation.
     * @return an object instance.
     */
    V unmarshal(byte[] data);

}
