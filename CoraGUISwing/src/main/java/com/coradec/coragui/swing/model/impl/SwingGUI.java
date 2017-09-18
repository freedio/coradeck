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

package com.coradec.coragui.swing.model.impl;

import static com.coradec.coracore.model.Scope.*;

import com.coradec.coracore.annotation.Implementation;
import com.coradec.coracore.annotation.Nullable;
import com.coradec.coracore.model.Registry;
import com.coradec.coracore.trouble.ClassInstantiationFailure;
import com.coradec.coractrl.ctrl.impl.BasicAgent;
import com.coradec.coradoc.model.XmlAttributes;
import com.coradec.coragui.model.GUI;
import com.coradec.coragui.model.Gadget;
import com.coradec.coragui.trouble.ComponentNotFoundException;
import com.coradec.corajet.trouble.CarClassLoaderRequiredException;
import com.coradec.corajet.trouble.NoImplementationFoundException;
import com.coradec.corasession.model.Session;
import com.coradec.coratext.model.LocalizedText;
import com.coradec.coratext.model.Text;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * ​​Swing implementation of the GUI façade.
 */
@SuppressWarnings("ClassHasNoToStringMethod")
@Implementation(SINGLETON)
public class SwingGUI extends BasicAgent implements GUI, Registry {

    private static final Text TEXT_FROM_INACCESSIBLE = LocalizedText.define("FromInaccessible");
    private static final Text TEXT_TARGET_EXCEPTION = LocalizedText.define("TargetException");
    private static final Text TEXT_FROM_NOT_FOUND = LocalizedText.define("FromNotFound");
    private static final Text TEXT_CLASS_NOT_FOUND = LocalizedText.define("ClassNotFound");

    private static final Set<Class<? extends SwingGadget>> COMPONENTS = new HashSet<>();

    public static void registerComponent(Class<?> componentClass) {
        if (SwingGadget.class.isAssignableFrom(componentClass)) {
            //noinspection unchecked
            COMPONENTS.add((Class<? extends SwingGadget>)componentClass);
        }
    }

    private final Map<String, Gadget> components = new HashMap<>();
    private final Set<String> modelPackages = new HashSet<>();
    private final Set<String> implementationPackages = new HashSet<>();

    @Override public <C extends Gadget> C getComponent(final Session session, final Class<C> type,
            final String name) {
        return findComponent(session, type, name).orElseThrow(
                () -> new ComponentNotFoundException(name));
    }

    @Override
    public <C extends Gadget> Optional<C> findComponent(final Session session, final Class<C> type,
            final String name) {
        return Optional.ofNullable(components.get(name)).map(type::cast);
    }

    /**
     * Adds the specified model package for component resolution to the GUI.
     *
     * @param modelPackage the model package.
     */
    public void addModelPackage(final @Nullable String modelPackage) {
        if (modelPackage != null) modelPackages.add(modelPackage);
    }

    /**
     * Adds the specified implementation package for implementation selection to the GUI.
     *
     * @param implementationPackage the implementation package.
     */
    public void addImplementationPackage(final @Nullable String implementationPackage) {
        if (implementationPackage != null) implementationPackages.add(implementationPackage);
    }

    /**
     * Adds a suitable implementation of the specified gadget to the top level elements of the GUI.
     * <p>
     * The unqualified gadget interface name is resolved against the set of already defined model
     * packages.  An implementation is chosen among all implementation classes of the gadget
     * interface in the CarClassLoader, restricting candidates to classes whose package name is one
     * of the predefined implementation packages.
     *
     * @param name       the gadget name.
     * @param attributes attributes of the gadget.
     * @return the gadget implementation.
     */
    public SwingGadget addElement(final String name, final XmlAttributes attributes) {
        final SwingGadget element = createElement(name, attributes);
        String id = element.getName();
        components.put(id, element);
        return element;
    }

    private SwingGadget getInstanceForName(final String className) {
        try {
            return (SwingGadget)getClassForName(className).newInstance();
        } catch (InstantiationException | IllegalAccessException | ClassCastException e) {
            throw new ClassInstantiationFailure(className, e);
        }
    }

    private Class<?> getClassForName(final String className) throws ClassInstantiationFailure {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new ClassInstantiationFailure(className, e);
        }
    }

    /**
     * Composes a class name from the specified package and unqualified class name.
     *
     * @param pkg  the package.
     * @param name the unqualified class name.
     * @return the qualified class name.
     */
    private String compose(final String pkg, final String name) {
        return pkg.endsWith(".") ? pkg + name : pkg + '.' + name;
    }

    /**
     * Creates a suitable implementation of the specified gadget to the top level elements of the
     * GUI.
     * <p>
     * The unqualified gadget interface name is resolved against the set of already defined model
     * packages.  An implementation is chosen among all implementation classes of the gadget
     * interface in the CarClassLoader, restricting candidates to classes whose package name is one
     * of the predefined implementation packages.
     *
     * @param name       the gadget name.
     * @param attributes attributes of the gadget.
     * @return the gadget implementation.
     */
    public SwingGadget createElement(final String name, final XmlAttributes attributes) {
        final String id = attributes.getValue("id");
        try {
            return modelPackages.stream().map(pkg -> compose(pkg, name)).filter(cn -> {
                try {
                    Class.forName(cn);
                    return true;
                } catch (ClassNotFoundException e) {
                    return false;
                }
            }).findAny().map(gadget -> {
                final Class<?> gadgetInterface = getClassForName(gadget);
                return COMPONENTS.stream()
                                 .filter(gadgetInterface::isAssignableFrom)
                                 .findAny()
                                 .map(cc -> instantiate(cc, id))
                                 .orElseThrow(() -> new NoImplementationFoundException(name));
            }).orElseThrow(() -> new NoImplementationFoundException(name));
        } catch (ClassCastException e) {
            throw new CarClassLoaderRequiredException();
        }
    }

    private SwingGadget instantiate(final Class<? extends SwingGadget> component, final String id) {
        Constructor<? extends SwingGadget> constructor;
        try {
            //noinspection JavaReflectionMemberAccess
            return component.getConstructor(String.class).newInstance(id);
        } catch (InvocationTargetException e) {
            throw new ClassInstantiationFailure(component.getName(), e.getTargetException());
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException e) {
            throw new ClassInstantiationFailure(component.getName(), e);
        }
    }

}
