package com.coradec.corajet.test;

import com.coradec.coracore.trouble.ResourceFileNotFoundException;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.util.TraceClassVisitor;

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
        if (inClassURL == null) throw new ResourceFileNotFoundException(inClassName);
        final Path inPath = Paths.get(inClassURL.toURI());
        final byte[] inClassData = Files.readAllBytes(inPath);
        ClassReader reader = new ClassReader(inClassData);
        ClassVisitor x;
        TraceClassVisitor tracer = new TraceClassVisitor(new PrintWriter(System.out));
        reader.accept(tracer, 0);
    }

}
