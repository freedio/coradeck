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

package com.coradec.coracom.model.impl;

import static com.coradec.coracom.state.QueueState.*;

import com.coradec.coracom.ctrl.OriginResolver;
import com.coradec.coracom.model.Information;
import com.coradec.coracom.model.SessionInformation;
import com.coradec.coracom.state.QueueState;
import com.coradec.coracore.annotation.ToString;
import com.coradec.coracore.model.GenericType;
import com.coradec.coracore.model.Origin;
import com.coradec.coracore.model.State;
import com.coradec.coracore.trouble.PropertyNotFoundException;
import com.coradec.coracore.util.ClassUtil;
import com.coradec.coralog.ctrl.impl.Logger;
import com.coradec.corasession.model.Session;
import com.coradec.coratext.model.LocalizedText;
import com.coradec.coratext.model.Text;
import com.coradec.coratype.ctrl.TypeConverter;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * ​​Basic implementation of an information.
 */
public class BasicInformation extends Logger implements Information {

    private static final Text TEXT_MAYBE_MISSING_TYPE_CONVERTER =
            LocalizedText.define("MaybeMissingTypeConverter");
    private final Map<String, Object> properties = new LinkedHashMap<>();
    private final Origin origin;
    private final LocalDateTime createdAt;
    private final UUID id;
    private State state;

    /**
     * Initializes a new instance of BasicInformation from the specified origin.
     *
     * @param origin the origin.
     */
    public BasicInformation(Origin origin) {
        this.origin = origin;
        this.createdAt = LocalDateTime.now();
        this.id = UUID.randomUUID();
        this.state = NEW;
    }

    /**
     * Initializes a new instance of BasicInformation from the specified property map.
     *
     * @param properties the property map.
     */
    public BasicInformation(Map<String, Object> properties) {
        this.properties.putAll(properties);
        final Session session = Session.get(
                UUID.fromString((String)properties.get(SessionInformation.PROP_SESSION)));
        this.origin = OriginResolver.resolveOrigin(session, get(String.class, PROP_ORIGIN));
        this.createdAt = get(LocalDateTime.class, PROP_CREATED_AT);
        this.id = get(UUID.class, PROP_ID);
        this.state = lookup(QueueState.class, PROP_STATE).orElse(NEW);
    }

    /**
     * Checks if the property with the specified name has a value other than {@code null}.
     *
     * @param name the property name.
     * @return {@code true} if the specified property has a value other than {@code null}, otherwise
     * {@code false}.
     */
    protected boolean hasProperty(final String name) {
        return properties.containsKey(name);
    }

    /**
     * Returns the properties of the information as a map.
     *
     * @return the property map.
     */
    @Override public Map<String, Object> getProperties() {
        collect();
        return properties;
    }

    /**
     * Collects the properties into the built-in property map in preparation for {@link
     * #getProperties()}.
     * <p>
     * Every subclass defining its own attributes should override this method and add its own
     * properties by calling {@link #set(String, Object)} for each of them.
     */
    protected void collect() {
        set(PROP_ORIGIN, getOrigin());
        set(PROP_CREATED_AT, getCreatedAt());
        set(PROP_ID, getId());
        set(PROP_STATE, getState());
    }

    /**
     * Sets the property with the specified name and type to the specified value.
     * <p>
     * The type field is used to select an appropriate type converter — choose wisely: its's rarely
     * just {@code value.getClass()} …
     *
     * @param name  the property name.
     * @param value the property value.
     */
    protected <T> void set(final String name, final T value) {
        //noinspection unchecked
        properties.put(name, TypeConverter.to((Class<T>)value.getClass()).encode(value));
    }

    /**
     * Sets the property with the specified name and generic type to the specified value.
     * <p>
     * The type field is used to select an appropriate type converter — choose wisely: its's rarely
     * just {@code value.getClass()} …
     *
     * @param type  the property type.
     * @param name  the property name.
     * @param value the property value.
     */
    protected <T> void set(GenericType<T> type, final String name, final T value) {
        properties.put(name, TypeConverter.to(type).encode(value));
    }

    @Override public <T> Optional<T> lookup(final GenericType<T> type, final String name) {
        try {
            return Optional.ofNullable(properties.get(name))
                           .map(o -> type.isInstance(o) ? type.cast(o) : type.cast(
                                   TypeConverter.to(type).convert(o)));
        } catch (ClassCastException e) {
            error(e, TEXT_MAYBE_MISSING_TYPE_CONVERTER, type);
            throw e;
        }
    }

    @Override public <T> Optional<T> lookup(final Class<T> type, final String name) {
        try {
            return Optional.ofNullable(properties.get(name))
                           .map(o -> type.isInstance(o) ? type.cast(o) : type.cast(
                                   TypeConverter.to(type).convert(o)));
        } catch (ClassCastException e) {
            error(e, TEXT_MAYBE_MISSING_TYPE_CONVERTER, type);
            throw e;
        }
    }

    @Override public <T> T get(final Class<T> type, final String name)
            throws PropertyNotFoundException {
        return lookup(type, name).orElseThrow(() -> new PropertyNotFoundException(type, name));
    }

    @Override public <T> T get(final GenericType<T> type, final String name)
            throws PropertyNotFoundException {
        return lookup(type, name).orElseThrow(() -> new PropertyNotFoundException(type, name));
    }

    @Override public <T, D extends T> T get(final Class<T> type, final String name, D dflt) {
        return lookup(type, name).orElse(dflt);
    }

    @Override @ToString public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }

    @Override @ToString public State getState() {
        return this.state;
    }

    /**
     * Sets the state of the message.
     *
     * @param state the new state.
     */
    protected void setState(final State state) {
        this.state = state;
    }

    @Override @ToString public UUID getId() {
        return id;
    }

    @Override @ToString public Origin getOrigin() {
        return origin;
    }

    @Override public void onEnqueue() throws IllegalStateException {
        if (getState() != NEW) throw new IllegalStateException(
                String.format("Information %s has illegal state %s (should be NEW)", this,
                        getState().name()));
        setState(ENQUEUED);
    }

    @Override public void onDispatch() throws IllegalStateException {
        if (getState() != ENQUEUED) throw new IllegalStateException(
                String.format("Information %s has illegal state %s (should be ENQUEUED)", this,
                        getState().name()));
        setState(DISPATCHED);
    }

    @Override public void onDeliver() throws IllegalStateException {

    }

    @Override public Information renew() {
        setState(NEW);
        return this;
    }

    @Override public String toString() {
        return ClassUtil.toString(this);
    }
}
