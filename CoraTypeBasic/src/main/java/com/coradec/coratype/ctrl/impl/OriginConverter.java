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

package com.coradec.coratype.ctrl.impl;

import static com.coradec.coracore.model.Scope.*;

import com.coradec.coracore.annotation.Implementation;
import com.coradec.coracore.model.Origin;
import com.coradec.coracore.model.impl.URIgin;
import com.coradec.coracore.util.StringUtil;
import com.coradec.coratype.trouble.TypeConversionException;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * ​​Implementation of an origin type converter.
 */
@Implementation(SINGLETON)
public class OriginConverter extends BasicTypeConverter<Origin> {

    /**
     * Initializes a new instance of OriginConverter with the specified target type.
     */
    public OriginConverter() {
        super(Origin.class);
    }

    /**
     * Converts the specified object into an object of the target type, if possible.
     *
     * @param value the object to convert.
     * @return the converted object.
     * @throws TypeConversionException if the type conversion failed.
     */
    @Override public Origin convert(final Object value) throws TypeConversionException {
        return trivial(value);
    }

    /**
     * Decodes the specified string into an object of the target type, if possible.
     *
     * @param value the value to decode.
     * @return the decoded object.
     * @throws TypeConversionException if the type conversion failed.
     */
    @Override public Origin decode(final String value) throws TypeConversionException {
        try {
            URL url = new URL(value);
            return new URIgin(url.toURI());
        } catch (URISyntaxException | MalformedURLException e) {
            // continue
        }
        try {
            URI uri = new URI(value);
            return new URIgin(uri);
        } catch (URISyntaxException e) {
            // continue
        }
        throw new TypeConversionException(value, getType());
    }

    /**
     * Encodes the specified value into a string representation that can be decoded using {@link
     * #decode(String)}.
     *
     * @param value the value to encode.
     * @return the encoded object.
     */
    @Override public String encode(final Origin value) {
        return value.represent();
    }

    /**
     * Unmarshals the specified byte array into an object of the target type, if possible.
     *
     * @param value the value to unmarshal.
     * @return the decoded object.
     * @throws TypeConversionException if the type conversion failed.
     */
    @Override public Origin unmarshal(final byte[] value) throws TypeConversionException {
        return decode(new String(value, StringUtil.CHARSET));
    }

    /**
     * Marshals the specified value into a byte array that can be unmarshalled using {@link
     * #unmarshal(byte[])}.
     *
     * @param value the value to marshal.
     * @return the encoded object.
     */
    @Override public byte[] marshal(final Origin value) {
        return encode(value).getBytes(StringUtil.CHARSET);
    }

}
