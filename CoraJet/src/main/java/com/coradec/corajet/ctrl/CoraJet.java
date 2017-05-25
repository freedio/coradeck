package com.coradec.corajet.ctrl;

import com.coradec.coracore.util.StringUtil;
import org.objectweb.asm.ClassReader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

/**
 * Injector core.
 */
@SuppressWarnings("ClassHasNoToStringMethod")
public class CoraJet {

    private int total, analyzed;
    private ClassParser classParser = new ClassParser();

    private List<URL> getRootLocations() {
        List<URL> result = new ArrayList<>();
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        while (loader != null) {
            if (loader instanceof URLClassLoader) {
                final URL[] urls = ((URLClassLoader)loader).getURLs();
                result.addAll(Arrays.asList(urls));
            }
            loader = loader.getParent();
        }
        return result;
    }

    void analyze() throws IOException {
        LocalTime start = LocalTime.now();
        final List<URL> classPath = getRootLocations();
        System.out.printf("Class Path: %s%n", StringUtil.toString(classPath));
        for (URL url : classPath) {
            File f = new File(url.getPath());
            if (f.isDirectory()) visitFile(f);
            else visitJar(url);
        }
        LocalTime finish = LocalTime.now();
        System.out.printf("Analysis took %sms.  %d of %d classes analyzed.%n",
                start.until(finish, ChronoUnit.MILLIS), analyzed, total);
        classParser.list();
    }

    private void visitFile(final File f) {
        try {
            if (f.isDirectory()) {
                final File[] files = f.listFiles();
                if (files != null) {
                    for (File file : files) {
                        visitFile(file);
                    }
                }
            } else if (f.getName().endsWith(".class")) {
                final FileInputStream in = new FileInputStream(f);
                analyze(in, f.getPath());
                in.close();
            }
        }
        catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private void visitJar(final URL url) {
        try (JarInputStream in = new JarInputStream(url.openStream())) {
            for (JarEntry entry = in.getNextJarEntry();
                 entry != null;
                 entry = in.getNextJarEntry()) {
                if (entry.getName().endsWith(".class")) {
                    analyze(in, entry.getName());
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private void analyze(final InputStream in, final String name) throws IOException {
        ++total;
//        System.out.printf("Analyzing file \"%s\"", name);
        if (!name.matches(
                "^(java/|javax/|sun/|com/sun/|javafx/|jdk/nashorn/).*")) {
//            System.out.println(": skipped");
            ++analyzed;
            ClassReader reader = new ClassReader(in);
            reader.accept(classParser, 0);
//        System.out.println();
        }
    }

    public static void main(String[] args) {
        final CoraJet coraJet = new CoraJet();
        try {
            coraJet.analyze();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

}
