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

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

/**
 * ​​Implementation of a File type converter.
 */
@Implementation(SINGLETON)
public class FileConverter extends BasicTypeConverter<File> {

    public FileConverter() {
        super(File.class);
    }

    @Override public File convert(final Object value) throws TypeConversionException {
        if (value instanceof URL && "file".equals(((URL)value).getProtocol()))
            return new File(((URL)value).getPath());
        if (value instanceof URI) {
            try {
                return new File(((URI)value).toURL().getPath());
            } catch (MalformedURLException e) {
                throw new TypeConversionException(value.getClass(), e);
            }
        } else return trivial(value);
    }

    @Override public File decode(final String value) throws TypeConversionException {
        return new File(value);
    }

    @Override public File unmarshal(final byte[] value) throws TypeConversionException {
        return decode(new String(value, StringUtil.CHARSET));
    }

    @Override public byte[] marshal(final File value) {
        return value.getPath().getBytes(StringUtil.CHARSET);
    }

}
