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

import com.coradec.coracore.annotation.Inject;
import com.coradec.coracore.annotation.ToString;
import com.coradec.coracore.model.Factory;
import com.coradec.coracore.model.Origin;
import com.coradec.coracore.trouble.OperationInterruptedException;
import com.coradec.coracore.util.ClassUtil;
import com.coradec.coradoc.ctrl.XmlParser;
import com.coradec.coradoc.model.XmlAttributes;
import com.coradec.coradoc.model.impl.BasicXmlDocumentModel;
import com.coradec.coragui.trouble.ContainerRequiredException;
import com.coradec.coragui.trouble.ElementOutsideGUIException;
import com.coradec.coragui.trouble.EndElementMismatch;
import com.coradec.coragui.trouble.EndElementWithoutStartException;
import com.coradec.coragui.trouble.RecursiveGUIDefinition;

import java.net.URL;
import java.util.Stack;

/**
 * ​​Implementation of the Swing GUI model.
 */
public final class SwingGuiModel extends BasicXmlDocumentModel {

    @Inject private static Factory<XmlParser<SwingGuiModel>> PARSER;

    public static SwingGuiModel from(final URL guiDefURL) {
        return PARSER.create().from(guiDefURL).to(new SwingGuiModel()).parse().getModel();
    }

    private Origin document;
    private final Stack<String> names;
    private SwingGUI gui;
    private Object currentElement;

    private SwingGuiModel() {
        currentElement = null;
        names = new Stack<>();
        gui = null;
    }

    @ToString public Origin getDocument() {
        return document;
    }

    @Override public void onStartOfDocument(final Origin document) {
        this.document = document;
    }

    @Override public void onEndOfDocument() {

    }

    @Override
    public void onStartTag(final String name, final XmlAttributes attributes, final boolean empty) {
        if (!empty) names.push(name);
        if ("GUI".equals(name)) {
            if (currentElement != null) throw new RecursiveGUIDefinition();
            currentElement = gui = new SwingGUI(attributes);
        } else if ("model".equals(name) && currentElement == gui)
            gui.addModelPackage(attributes.get("package"));
        else if ("implementation".equals(name) && currentElement == gui)
            gui.addImplementationPackage(attributes.get("package"));
        else if (currentElement == null) throw new ElementOutsideGUIException(name);
        else {
            if (currentElement == gui) currentElement = gui.addElement(name, attributes);
            else if (currentElement instanceof SwingContainer) {
                final SwingGadget newElement = gui.createElement(name, attributes);
                try {
                    ((SwingContainer)currentElement).add(newElement).standby();
                } catch (InterruptedException e) {
                    throw new OperationInterruptedException();
                }
                currentElement = newElement;
            } else throw new ContainerRequiredException(String.valueOf(currentElement));
        }
    }

    @Override public void onEndTag(final String name) {
        if (names.isEmpty()) throw new EndElementWithoutStartException(name);
        if (!name.equals(names.peek())) throw new EndElementMismatch(names.peek(), name);
        names.pop();
        if ("GUI".equals(name)) currentElement = null;
        else if ("model".equals(name) || "implementation".equals(name)) currentElement = gui;
        else currentElement = ((SwingGadget<?>)currentElement).getParent().orElse(null);
    }

    @Override public String toString() {
        return ClassUtil.toString(this);
    }

    public SwingGUI getGUI() {
        return gui;
    }

}
