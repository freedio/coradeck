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
import com.coradec.coracore.util.StringUtil;
import com.coradec.coratype.trouble.TypeConversionException;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

/**
 * ​​Implementation of an URL type converter.
 */
@Implementation(SINGLETON)
public class URLConverter extends BasicTypeConverter<URL> {

    public URLConverter() {
        super(URL.class);
    }

    @Override public URL convert(final Object value) throws TypeConversionException {
        if (value instanceof URI) {
            try {
                return ((URI)value).toURL();
            } catch (MalformedURLException e) {
                throw new TypeConversionException(value.getClass(), e);
            }
        } else return trivial(value);
    }

    @Override public URL decode(final String value) throws TypeConversionException {
        try {
            return new URL(value);
        } catch (MalformedURLException e) {
            throw new TypeConversionException(value, String.class, e);
        }
    }

    /**
     * Encodes the specified value into a string representation that can be decoded using {@link
     * #decode(String)}.
     *
     * @param value the value to encode.
     * @return the encoded object.
     */
    @Override public String encode(final URL value) {
        return value.toString();
    }

    @Override public URL unmarshal(final byte[] value) throws TypeConversionException {
        return decode(new String(value, StringUtil.UTF8));
    }

    @Override public byte[] marshal(final URL value) {
        return value.toExternalForm().getBytes(StringUtil.UTF8);
    }

}
