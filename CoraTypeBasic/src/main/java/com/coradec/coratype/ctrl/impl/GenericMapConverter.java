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

import static java.util.stream.Collectors.*;

import com.coradec.coracore.annotation.Implementation;
import com.coradec.coracore.model.GenericType;
import com.coradec.coracore.util.StringUtil;
import com.coradec.coratype.trouble.TypeConversionException;

import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * ​​Generic implementation of a type converter for string/string maps.
 */
@Implementation
public class GenericMapConverter extends BasicTypeConverter<Map<String, String>> {

    public GenericMapConverter() {
        super(GenericType.of(Map.class));
    }

    @Override public Map<String, String> convert(final Object value)
            throws TypeConversionException {
        return trivial(value);
    }

    @Override public Map<String, String> decode(final String value) throws TypeConversionException {
        final Map<String, String> result = new HashMap<>();
        boolean escaped = false;
        char quoted = '\0';
        StringBuilder key = new StringBuilder(1024), val = new StringBuilder(1024), collector = key;
        for (int i = 0, is = value.length(); i < is; ++i) {
            char c = value.charAt(i);
            if (escaped) {
                collector.append(StringUtil.unescape(c));
                escaped = false;
            } else if (quoted != '\0') {
                if (c == quoted) quoted = '\0';
                else collector.append(c);
            } else if (c == '\\') escaped = true;
            else if (c == '\'' || c == '"') quoted = c;
            else if (c == '=') collector = val;
            else if (c == ',') {
                result.put(key.toString().trim(), val.toString().trim());
                key.setLength(0);
                val.setLength(0);
                collector = key;
            }
        }
        if (key.length() != 0) result.put(key.toString().trim(), val.toString().trim());
        return result;
    }

    @Override public String encode(final Map<String, String> value) {
        return value.entrySet()
                    .stream()
                    .map(e -> StringUtil.escape(e.getKey(), "=,") +
                              '=' +
                              StringUtil.escape(e.getValue(), "=,"))
                    .collect(joining(","));
    }

    @Override public Map<String, String> unmarshal(final byte[] value)
            throws TypeConversionException {
        final Map<String, String> result = new HashMap<>();
        try {
            Unmarshaller unmar = getUnmarshaller(value);
            for (String key = unmar.readUTF(); !key.isEmpty(); key = unmar.readUTF()) {
                final String val = unmar.readUTF();
                result.put(key, val);
            }
            return result;
        } catch (IOException e) {
            throw new TypeConversionException(GenericType.of(Map.class, String.class, String.class),
                    e);
        }
    }

    @Override public byte[] marshal(final Map<String, String> value) {
        Marshaller mar = getMarshaller();
        try {
            value.forEach((key, val) -> {
                try {
                    mar.writeUTF(key);
                    mar.writeUTF(val);
                } catch (IOException e) {
                    throw new TypeConversionException(
                            GenericType.of(Map.class, String.class, String.class), e);
                }
            });
            mar.writeUTF("");
            return mar.get();
        } catch (IOException e) {
            throw new TypeConversionException(LocalDate.class, e);
        }
    }

}
