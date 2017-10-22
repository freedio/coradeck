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

import com.coradec.coracore.annotation.NonNull;
import com.coradec.coracore.annotation.Nullable;
import com.coradec.coracore.annotation.ToString;
import com.coradec.coracore.model.GenericType;
import com.coradec.coracore.model.Unknown;
import com.coradec.coracore.util.StringUtil;
import com.coradec.coratype.ctrl.TypeConverter;
import com.coradec.coratype.trouble.TypeConversionException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * ​​Basic implementation of a type converter.
 */
public abstract class BasicTypeConverter<T> implements TypeConverter<T> {

    private final GenericType<T> type;

    /**
     * Initializes a new instance of BasicTypeConverter with the specified target type.
     *
     * @param type the target type (required).
     */
    public BasicTypeConverter(final GenericType<T> type) {
        this.type = type;
    }

    /**
     * Initializes a new instance of BasicTypeConverter with the specified target type.
     *
     * @param type the target type (required).
     */
    public BasicTypeConverter(final Class<T> type) {
        this(GenericType.of(type));
    }

    @ToString public GenericType<T> getType() {
        return type;
    }

    /**
     * Handles the trivial cases of type conversion: object is of same type or of type String.
     *
     * @param value the value to convert (required).
     * @return the converted value.
     * @throws TypeConversionException if the value cannot be converted.
     */
    @SuppressWarnings("unchecked") protected T trivial(final @Nullable Object value)
            throws TypeConversionException {
        if (type.isInstance(value)) {
            return (T)value;
        }
        if (value instanceof String) {
            return decode((String)value);
        }
        if (value instanceof byte[]) {
            return unmarshal((byte[])value);
        }
        throw new TypeConversionException(value != null ? value.getClass() : Unknown.class,
                String.format("Failed to convert object ‹%s› to type ‹%s›", value, type));
    }

    protected Unmarshaller getUnmarshaller(final byte[] value) {
        return new Unmarshaller(value);
    }

    protected Marshaller getMarshaller() {
        return new Marshaller();
    }

    protected byte[] standardMarshal(final T value) {
        return encode(value).getBytes(StringUtil.UTF8);
    }

    protected T standardUnmarshal(final byte[] value) {
        return decode(new String(value, StringUtil.UTF8));
    }

    @SuppressWarnings("ClassHasNoToStringMethod")
    protected class Unmarshaller implements DataInput {

        private final DataInputStream extractor;

        public Unmarshaller(final byte[] value) {
            extractor = new DataInputStream(new ByteArrayInputStream(value));
        }

        @Override public void readFully(final @NonNull byte[] b) throws IOException {
            extractor.readFully(b);
        }

        @Override public void readFully(final @NonNull byte[] b, final int off, final int len)
                throws IOException {
            extractor.readFully(b, off, len);
        }

        @Override public int skipBytes(final int n) throws IOException {
            return extractor.skipBytes(n);
        }

        @Override public boolean readBoolean() throws IOException {
            return extractor.readBoolean();
        }

        @Override public byte readByte() throws IOException {
            return extractor.readByte();
        }

        @Override public int readUnsignedByte() throws IOException {
            return extractor.readUnsignedByte();
        }

        @Override public short readShort() throws IOException {
            return extractor.readShort();
        }

        @Override public int readUnsignedShort() throws IOException {
            return extractor.readUnsignedShort();
        }

        @Override public char readChar() throws IOException {
            return extractor.readChar();
        }

        @Override public int readInt() throws IOException {
            return extractor.readInt();
        }

        @Override public long readLong() throws IOException {
            return extractor.readLong();
        }

        @Override public float readFloat() throws IOException {
            return extractor.readFloat();
        }

        @Override public double readDouble() throws IOException {
            return extractor.readDouble();
        }

        @SuppressWarnings("deprecation") @Override public String readLine() throws IOException {
            return extractor.readLine();
        }

        @Override public @NonNull String readUTF() throws IOException {
            return extractor.readUTF();
        }
    }

    @SuppressWarnings("ClassHasNoToStringMethod")
    protected class Marshaller implements DataOutput {

        private final DataOutputStream inserter;
        private final ByteArrayOutputStream buffer;

        Marshaller() {
            inserter = new DataOutputStream(buffer = new ByteArrayOutputStream());
        }

        @Override public void write(final int b) throws IOException {
            inserter.write(b);
        }

        @Override public void write(final @NonNull byte[] b) throws IOException {
            inserter.write(b);
        }

        @Override public void write(final @NonNull byte[] b, final int off, final int len)
                throws IOException {
            inserter.write(b, off, len);
        }

        @Override public void writeBoolean(final boolean v) throws IOException {
            inserter.writeBoolean(v);
        }

        @Override public void writeByte(final int v) throws IOException {
            inserter.writeByte(v);
        }

        @Override public void writeShort(final int v) throws IOException {
            inserter.writeShort(v);
        }

        @Override public void writeChar(final int v) throws IOException {
            inserter.writeChar(v);
        }

        @Override public void writeInt(final int v) throws IOException {
            inserter.writeInt(v);
        }

        @Override public void writeLong(final long v) throws IOException {
            inserter.writeLong(v);
        }

        @Override public void writeFloat(final float v) throws IOException {
            inserter.writeFloat(v);
        }

        @Override public void writeDouble(final double v) throws IOException {
            inserter.writeDouble(v);
        }

        @Override public void writeBytes(final @NonNull String s) throws IOException {
            inserter.writeBytes(s);
        }

        @Override public void writeChars(final @NonNull String s) throws IOException {
            inserter.writeChars(s);
        }

        @Override public void writeUTF(final @NonNull String s) throws IOException {
            inserter.writeUTF(s);
        }

        public byte[] get() throws IOException {
            inserter.flush();
            return buffer.toByteArray();
        }
    }

}
