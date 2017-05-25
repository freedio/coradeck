package com.coradec.corajet.cldr;

import com.coradec.coracore.annotation.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
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
@SuppressWarnings("ClassHasNoToStringMethod")
public class CarClassLoader extends ClassLoader {

    private static final String PROP_ENV_CLASSPATH = "java.class.path";
    public static final String PROP_MANIFEST_CLASSPATH = "Class-Path";
    public static final String PROP_COMPONENT_CLASSES = "Component-Classes";
    public static final String PROP_IMPLEMENTATION_CLASSES = "Implementation-Classes";
    private static final String PROP_ZIP_EXT = ".zip";
    private static final String PROP_JAR_EXT = ".jar";
    private static final String PROP_CAR_EXT = ".car";

    private final Object injector;
    private final Method embed;
    private final Map<String, List<URL>> resourceMap;
    private final Set<String> components = new HashSet<>();
    private final Set<String> implementations = new HashSet<>();
    private final Map<String, Class<?>> classes = new HashMap<>();

    CarClassLoader() {
        this(Thread.currentThread().getContextClassLoader());
    }

    CarClassLoader(final ClassLoader parent) {
        super(parent);
        resourceMap = createResourceMap();
        try {
            final Class<?> injectorClass = findClass("com.coradec.corajet.cldr.CarInjector");
            final Constructor<?> injectorConstructor =
                    injectorClass.getConstructor();
            embed = injectorClass.getMethod("embedClass", String.class, byte[].class, Integer.TYPE,
                    Integer.TYPE);
            injector = injectorConstructor.newInstance();
            implementations.forEach(implementation -> {
                try {
                    findClass(implementation);
                }
                catch (ClassNotFoundException e) {
                    Syslog.error(e);
                }
            });
        }
        catch (Exception e) {
            Syslog.error(e);
            throw new InternalError("Failed to load the injector!", e);
        }
    }

    private Map<String, List<URL>> createResourceMap() {
        final Map<String, List<URL>> result = new HashMap<>();
        final String classPath = File.pathSeparator + System.getProperty(PROP_ENV_CLASSPATH);
        try {
            collectResources(result, classPath.split(File.pathSeparator));
        }
        catch (final Exception e) {
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
        registerProtocols();
        for (final String path : classPath) {
//            Syslog.info("ClassPath entry: \"%s\"", path);
            final File file = new File(path);
            if (file.exists()) {
                if (file.isDirectory()) {
                    searchDirectory(resourceMap, file.toURI(), file.listFiles());
                } else if (path.endsWith(".jar")) {
                    searchJarFile(resourceMap, "car:" + file.toURI(),
                            new JarInputStream(new FileInputStream(file)), false);
                } else if (path.endsWith(PROP_ZIP_EXT)) {
                    searchZipFile(resourceMap, "car:" + file.toURI(),
                            new ZipInputStream(new FileInputStream(file)));
                }
            }
        }
        Syslog.debug("Collected components: %s", components);
        Syslog.debug("Collected implementations: %s", implementations);
//        Syslog.debug("Collected resources:");
//        resourceMap.forEach((key, value) -> System.out.printf("%s → %s%n", key, value));
    }

    private static void addResource(final Map<String, List<URL>> resourceMap, final String path,
                                    final URL url) {
        resourceMap.computeIfAbsent(path, k -> new ArrayList<>()).add(url);
    }

    private static void searchDirectory(final Map<String, List<URL>> resourceMap, final URI prefix,
                                        final File[] files) throws IOException {
        for (final File file : files) {
            addResource(resourceMap, file.getPath(), file.toURI().toURL());
        }
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
            if (list != null && !list.isEmpty()) {
                Collections.addAll(implementations, list.split("\\s+"));
            }
        }
    }

    private static void registerProtocols() {
        System.setProperty("java.protocol.handler.pkgs", "com.coradec.corajet.cldr.protocols");
    }

    @Override protected URL findResource(final String name) {
        // Validate preconditions: done by findResources.
        URL result = null;
        try {
            final Enumeration<URL> resources = findResources(name);
            if (resources.hasMoreElements()) {
                result = resources.nextElement();
            }
        }
        catch (final IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override protected Enumeration<URL> findResources(final String name) throws IOException {
        final List<URL> resourceList;
        try {
            resourceList = getResourceMap().get(name);
        }
        catch (Exception e) {
            throw new IOException("Failed to load the injector", e);
        }
        return Collections.enumeration(
                resourceList == null ? Collections.EMPTY_LIST : resourceList);
    }

    @Override protected Class<?> loadClass(final String name, final boolean resolve)
            throws ClassNotFoundException {
        Class<?> klass;
        try {
            klass = findClass(name);
        }
        catch (ClassNotFoundException e) {
            klass = super.loadClass(name, resolve);
        }
        return klass;
    }

    @Override public Class<?> findClass(final String name) throws ClassNotFoundException {
        try {
            return classes.computeIfAbsent(name, k -> {
                URL location = findResource(toResourceName(name));
    //        Syslog.debug("findClass(%s) -> location = %s%n", name, location);
                try {
                    if (location == null) return super.findClass(name);
                    else return loadClass(name, location);
                }
                catch (IOException e) {
                    throw new UnsupportedOperationException(new ClassNotFoundException(name, e));
                }
                catch (ClassNotFoundException e) {
                    throw new UnsupportedOperationException(e);
                }
            });
        }
        catch (UnsupportedOperationException e) {
            throw (ClassNotFoundException)e.getCause();
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
        byte[] data;
        if (injector != null && (components.contains(name) || implementations.contains(name))) {
            Syslog.debug(">> Injector.embed(%s)%n", name);
            try {
                data = (byte[])embed.invoke(injector, name, buffer, 0, buflen);
            }
            catch (Throwable e) {
                Syslog.error(e);
                ;
                data = Arrays.copyOfRange(buffer, 0, buflen);
            }
        } else data = Arrays.copyOfRange(buffer, 0, buflen);
        return defineClass(name, data, 0, data.length);
    }

    private static String toResourceName(final String name) {
        return name.replace('.', '/') + ".class";
    }

}
