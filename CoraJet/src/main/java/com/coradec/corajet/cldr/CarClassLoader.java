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

package com.coradec.corajet.cldr;

import static java.nio.file.StandardOpenOption.*;

import com.coradec.coracore.annotation.Nullable;
import com.coradec.coracore.trouble.ObjectInstantiationFailure;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * ​​Implementation of the Comprehensive ARchive class loader.
 */
@SuppressWarnings({"ClassHasNoToStringMethod", "WeakerAccess"})
public class CarClassLoader extends ClassLoader {

    private static final String PROP_ENV_CLASSPATH = "java.class.path";
    public static final String PROP_MANIFEST_CLASSPATH = "Class-Path";
    public static final String PROP_COMPONENT_CLASSES = "Component-Classes";
    public static final String PROP_IMPLEMENTATION_CLASSES = "Implementation-Classes";
    private static final String PROP_ZIP_EXT = ".zip";
    private static final String PROP_JAR_EXT = ".jar";
    private static final String PROP_CAR_EXT = ".car";
    private static final String NAME_INJECTOR = "com.coradec.corajet.cldr.CarInjector";

    private final Object injector;
    private final Method embed, analyze, implement;
    private final Map<String, List<URL>> resourceMap;
    private final Set<String> components = new HashSet<>();
    private final Set<String> implementations = new HashSet<>();
    private final Map<String, Class<?>> classes = new HashMap<>();
    private final List<String> fileList = new LinkedList<>();
    private final boolean outputInjectedFiles;

    public CarClassLoader() {
        this(Thread.currentThread().getContextClassLoader());
    }

    CarClassLoader(final ClassLoader parent) {
        super(parent);
        final String propOutput = System.getProperty("output.injected.files");
        outputInjectedFiles = propOutput != null && propOutput.equals("true");
        resourceMap = createResourceMap();
        try {
            Syslog.info("Loading the injector ...");
            final Class<?> injectorClass = findClass(NAME_INJECTOR);
            final Constructor<?> injectorConstructor = injectorClass.getConstructor();
            embed = injectorClass.getMethod("embedClass", String.class, byte[].class, Integer.TYPE,
                    Integer.TYPE);
            analyze = injectorClass.getMethod("analyzeClass", String.class, byte[].class,
                    Integer.TYPE, Integer.TYPE);
            implement = injectorClass.getMethod("implementationFor", Class.class, List.class,
                    Object[].class);
            injector = injectorConstructor.newInstance();
            Syslog.info("Analyzing naked resources ...");
            for (final String file : fileList) {
                final URL resource = findResource(file);
                if (resource != null) parseAnnotations(file, resource);
            }
            implementations.forEach(implementation -> {
                try {
                    findClass(implementation);
                } catch (ClassNotFoundException e) {
                    Syslog.error(e);
                }
            });
            Syslog.debug("Collected components: %s", components);
            Syslog.debug("Collected implementations: %s", implementations);
        } catch (Exception e) {
            Syslog.error(e);
            throw new InternalError("Failed to load the injector!", e);
        }
    }

    private Map<String, List<URL>> createResourceMap() {
        final Map<String, List<URL>> result = new HashMap<>();
        final String classPath = File.pathSeparator + System.getProperty(PROP_ENV_CLASSPATH);
        Syslog.debug("ClassPath: %s", classPath);
        try {
            collectResources(result, classPath.split(File.pathSeparator));
        } catch (final Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    private Map<String, List<URL>> getResourceMap() throws Exception {
        return resourceMap;
    }

    /**
     * Recursively collect resources from the specified class path into the
     * specified resource map.
     *
     * @param resourceMap the resource map.
     * @param classPath   the class path.
     * @throws IOException if a file was not found or a read error occurred.
     */
    private void collectResources(final Map<String, List<URL>> resourceMap,
                                  final String[] classPath) throws IOException {
        Syslog.info("Collecting resources ...");
        registerProtocols();
        for (final String path : classPath) {
            Syslog.trace("ClassPath entry: \"%s\"", path);
            final File file = new File(path);
            if (file.exists()) {
                if (file.isDirectory()) {
                    searchDirectory(resourceMap, file.toURI(), file.listFiles(),
                            file.toString().length() + 1);
                } else if (path.endsWith(PROP_JAR_EXT)) {
                    searchJarFile(resourceMap, "car:" + file.toURI(),
                            new JarInputStream(new FileInputStream(file)), false);
                } else if (path.endsWith(PROP_ZIP_EXT)) {
                    searchZipFile(resourceMap, "car:" + file.toURI(),
                            new ZipInputStream(new FileInputStream(file)));
                }
            }
        }
//        Syslog.debug("Collected resources:");
//        resourceMap.forEach((key, value) -> System.out.printf("%s → %s%n", key, value));
    }

    private static void addResource(final Map<String, List<URL>> resourceMap, final String path,
                                    final URL url) {
        Syslog.trace("Adding %s → %s", path, url);
        resourceMap.computeIfAbsent(path, k -> new ArrayList<>()).add(url);
    }

    private void searchDirectory(final Map<String, List<URL>> resourceMap, final URI prefix,
                                 final @Nullable File[] files, final int fprefix)
            throws IOException {
        if (files != null) {
            for (final File file : files) {
                InputStream in = null;
                if (file != null) {
                    final String path = file.getPath();
                    if (file.isDirectory()) {
                        searchDirectory(resourceMap, file.toURI(), file.listFiles(), fprefix);
                    } else if (path.endsWith(PROP_JAR_EXT)) {
                        searchJarFile(resourceMap, "car:" + file.toURI(), (JarInputStream)(in =
                                new JarInputStream(new FileInputStream(file))), false);
                    } else if (path.endsWith(PROP_ZIP_EXT)) {
                        searchZipFile(resourceMap, "car:" + file.toURI(), (ZipInputStream)(in =
                                new ZipInputStream(new FileInputStream(file))));
                    } else if (path.endsWith(".class")) {
                        final String name = path.substring(fprefix);
                        final URL location = file.toURI().toURL();
                        fileList.add(name);
                        addResource(resourceMap, name, location);
                    }
                }
                if (in != null) in.close();
            }
        }
    }

    /**
     * Parses the class in the specified input stream for annotations to find out whether it is a
     * component or implementation.
     *
     * @param name     the name of the class.
     * @param location the location of the class.
     * @throws IOException if the class failed to be read.
     */
    private void parseAnnotations(final String name, final URL location) throws IOException {
        readClass(name.replaceFirst("\\.class$", "").replace('/', '.'), location, false, true);
    }

    private void searchZipFile(final Map<String, List<URL>> resourceMap, final String prefix,
                               final ZipInputStream zipFile) throws IOException {
        for (ZipEntry entry = zipFile.getNextEntry();
             entry != null;
             entry = zipFile.getNextEntry()) {
            final String resourceName = entry.getName();
            final URL resourceLocation = new URL(prefix + '#' + resourceName);
            if (entry.isDirectory()) {
                Syslog.debug("collectZipResources: Skipping directory entry %s%n", resourceName);
            } else {
                addResource(resourceMap, resourceName, resourceLocation);
            }
        }
    }

    private void searchJarFile(final Map<String, List<URL>> resourceMap, final String prefix,
                               final JarInputStream jarFile, final boolean flat)
            throws IOException {
        final Manifest manifest = jarFile.getManifest();
        Set<String> classPath = null;
        if (manifest != null) {
            final Attributes mainAttributes = manifest.getMainAttributes();
            final String manifestClassPath = mainAttributes.getValue(PROP_MANIFEST_CLASSPATH);
            if (manifestClassPath != null && !manifestClassPath.isEmpty()) {
                classPath = new HashSet<>(Arrays.asList((" " + manifestClassPath).split(" ")));
            }
            registerComponents(prefix, mainAttributes.getValue(PROP_COMPONENT_CLASSES));
            registerImplementations(prefix, mainAttributes.getValue(PROP_IMPLEMENTATION_CLASSES));
        }
        for (JarEntry entry = jarFile.getNextJarEntry();
             entry != null;
             entry = jarFile.getNextJarEntry()) {
            if (!entry.isDirectory()) {
                final String resourceName = entry.getName();
                final String url = prefix + '#' + resourceName;
                final URL resourceLocation = new URL(url);
                if (classPath != null) {
                    for (final String path : classPath) {
                        if (path.equals(resourceName)) {
                            if (path.endsWith(PROP_ZIP_EXT)) {
                                final String zipPrefix = prefix + '#' + resourceName;
                                searchZipFile(resourceMap, zipPrefix, new ZipInputStream(jarFile));
                            } else if (path.endsWith(PROP_JAR_EXT)) {
                                final String jarPrefix = prefix + '#' + resourceName;
                                searchJarFile(resourceMap, jarPrefix, new JarInputStream(jarFile),
                                        true);
                            } else {
                                Syslog.debug("-> Lost entry \"%s\" in path \"%s\"", resourceName,
                                        path);
                            }
                        } else if (resourceName.startsWith(path)) {
//                            Syslog.debug("-> Adding resource \"%s\" as \"%s\"", resourceName,
//                                    resourceLocation);
                            final String rest = resourceName.substring(path.length());
                            if (!rest.isEmpty()) {
                                addResource(resourceMap, rest, resourceLocation);
                            }
//                        } else {
//                            Syslog.debug("-> Skipping entry \"%s\" in path \"%s\"", resourceName,
//                                    path);
                        }
                    }
                } else {
                    addResource(resourceMap, resourceName, resourceLocation);
                }
            }
        }
    }

    /**
     * Parses the specified list (if present) for names of component classes contained in the
     * specified JAR file.
     *
     * @param jarURL the URL of the JAR file.
     * @param list   the list of classes (optional).
     */
    private void registerComponents(final String jarURL, final @Nullable String list) {
//        Syslog.debug("Registering components from list \"%s\"", list);
        if (list != null && !list.isEmpty()) {
            Collections.addAll(components, list.split("\\s+"));
        }
    }

    /**
     * Parses the specified list (if present) for names of implementation classes contained in the
     * specified JAR file.
     *
     * @param jarURL the URL of the JAR file.
     * @param list   the list of classes (optional).
     */
    private void registerImplementations(final String jarURL, final @Nullable String list) {
//        Syslog.debug("Registering implementations from list \"%s\"", list);
        if (list != null && !list.isEmpty()) {
            Collections.addAll(implementations, list.split("\\s+"));
        }
    }

    private static void registerProtocols() {
        System.setProperty("java.protocol.handler.pkgs", "com.coradec.corajet.cldr.protocols");
    }

    @Override @Nullable protected URL findResource(final String name) {
        URL result = null;
        try {
            final Enumeration<URL> resources = findResources(name);
            if (resources.hasMoreElements()) {
                result = resources.nextElement();
            }
        } catch (final IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override protected Enumeration<URL> findResources(final String name) throws IOException {
        final List<URL> resourceList;
        try {
            resourceList = getResourceMap().get(name);
        } catch (Exception e) {
            throw new IOException("Failed to load the injector", e);
        }
        //noinspection unchecked
        return Collections.enumeration(
                resourceList == null ? Collections.EMPTY_LIST : resourceList);
    }

    @Override protected Class<?> loadClass(final String name, final boolean resolve)
            throws ClassNotFoundException {
        Class<?> klass;
        try {
            klass = findClass(name);
        } catch (ClassNotFoundException e) {
            klass = super.loadClass(name, resolve);
        }
        return klass;
    }

    @Override public Class<?> findClass(final String name) throws ClassNotFoundException {
        if (name.startsWith("java.") || name.startsWith("sun.")) return super.findClass(name);
        try {
            return classes.computeIfAbsent(name, k -> {
                URL location = findResource(toResourceName(name));
                //        Syslog.debug("findClass(%s) -> location = %s%n", name, location);
                try {
                    if (location == null) return super.findClass(name);
                    else return loadClass(name, location);
                } catch (IOException e) {
                    throw new UnsupportedOperationException(new ClassNotFoundException(name, e));
                } catch (ClassNotFoundException e) {
                    throw new UnsupportedOperationException(e);
                }
            });
        } catch (UnsupportedOperationException e) {
            throw (ClassNotFoundException)e.getCause();
        }
    }

    /**
     * Returns an implementation of the specified interface with the specified type and construction
     * arguments.
     *
     * @param <T>   the base type.
     * @param type  the base type selector.
     * @param types type arguments to match with type parameters of a suitable implementation
     *              class.
     * @param args  constructor arguments.
     * @return a suitable instance of an implementation class..
     */
    @SuppressWarnings("unchecked") public <T> T implement(final Class<? super T> type,
                                                          final List<Type> types, Object... args) {
        if (injector == null) throw new IllegalStateException("No injector!");
        try {
            return (T)implement.invoke(injector, type, types, args);
        } catch (final Exception e) {
            throw new ObjectInstantiationFailure(type, e);
        }
    }

    /**
     * Loads the closs with the specified name from the specified location.
     *
     * @param name     the name of the class.
     * @param location the location.
     * @return the loaded, emdedded and defined class.
     * @throws IOException if the class failed to be read from its source.
     */
    private Class<?> loadClass(final String name, final URL location) throws IOException {
        Syslog.debug("Loading class %s from %s", name, location);
        byte[] data = readClass(name, location,
                components.contains(name) || implementations.contains(name), false);
        return defineClass(name, data, 0, data.length);
    }

    private byte[] readClass(final String name, final URL location, final boolean doEmbed,
                             final boolean doAnalyze) throws IOException {
        final InputStream in = location.openStream();
        final int blocksize = 65536;
        byte[] buffer = new byte[blocksize];  // should covert 99% of all classes.
        int buflen = 0;
        for (int len = in.read(buffer, buflen, buffer.length - buflen);
             len != -1;
             len = in.read(buffer, buflen, buffer.length - buflen)) {
            buflen += len;
            if (buflen == buffer.length) buffer = Arrays.copyOf(buffer, buflen + blocksize);
        }
        byte[] data = Arrays.copyOfRange(buffer, 0, buflen);
        if (injector != null) {
            if (doEmbed) {
                Syslog.debug(">> Injector.embed(%s)", name);
                try {
                    data = (byte[])embed.invoke(injector, name, buffer, 0, buflen);
                } catch (final Exception e) {
                    Syslog.error(e);
                }
            } else if (doAnalyze) {
                Syslog.debug(">> Injector.analyze(%s)", name);
                try {
                    int state = (int)analyze.invoke(injector, name, buffer, 0, buflen);
                    if ((state & 1) != 0) {
                        Syslog.debug("-> Adding as component");
                        components.add(name);
                    }
                    if ((state & 2) != 0) {
                        Syslog.debug("-> Adding as implementation");
                        implementations.add(name);
                    }
                } catch (final Exception e) {
                    Syslog.error(e);
                }
            }
        }
        if (outputInjectedFiles)
            Files.write(Paths.get("/tmp/", name + ".class"), data, CREATE, TRUNCATE_EXISTING);
        return data;
    }

    private static String toResourceName(final String name) {
        return name.replace('.', '/') + ".class";
    }

    public Class<?> load(final Class<?> klass) {
        Class<?> result;
        try {
            result = findClass(klass.getName());
        } catch (ClassNotFoundException e) {
            Syslog.error("Class not found: %s", klass.getName());
            result = klass;
        }
        return result;
    }

    public void showImpplementations() {
        Syslog.info("Collected implementations: %s", implementations);
    }
}
