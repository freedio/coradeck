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

package com.coradec.corajet.test;

import com.coradec.coracore.trouble.ResourceFileNotFoundException;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.util.TraceClassVisitor;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * ​​Application that changes the constructors of a class.
 */
public class Trace {

    public static void main(String[] args) {
        try {
            new Trace().launch(args[0]);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void launch(final String inClassName) throws URISyntaxException, IOException {
        URL inClassURL = getClass().getClassLoader().getResource(inClassName);
        if (inClassURL == null) {
            final File file = new File(inClassName);
            if (file.isFile()) inClassURL = file.toURI().toURL();
            else throw new ResourceFileNotFoundException(inClassName);
        }
        final Path inPath = Paths.get(inClassURL.toURI());
        final byte[] inClassData = Files.readAllBytes(inPath);
        ClassReader reader = new ClassReader(inClassData);
        ClassVisitor x;
        TraceClassVisitor tracer = new TraceClassVisitor(new PrintWriter(System.out));
        reader.accept(tracer, 0);
    }

}
