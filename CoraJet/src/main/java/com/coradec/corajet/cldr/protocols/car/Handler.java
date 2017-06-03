/*
 * Copyright ⓒ 2017 by Coradec GmbH.
 *
 * This file is part of the Coradeck.
 *
 * Coradeck is free software: you can redistribute it under the the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or any later version.
 *
 * Coradeck is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR ANY PARTICULAR PURPOSE.  See the GNU General Public License for further details.
 *
 * The GNU General Public License is available from <http://www.gnu.org/licenses/>.
 *
 * @license GPL-3.0+ <http://spdx.org/licenses/GPL-3.0+>
 *
 * @author Dominik Wezel <dom@coradec.com>
 */

package com.coradec.corajet.cldr.protocols.car;

import com.coradec.coracore.annotation.Nullable;
import com.coradec.corajet.cldr.Syslog;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * An URL stream handler for the <code>car:</code> scheme.
 */
public class Handler extends URLStreamHandler {

    @SuppressWarnings("ClassHasNoToStringMethod")
    private class ZipUrlConnection extends URLConnection {

        /** The stream. */
        private final ZipInputStream stream;

        private ZipInputStream getStream() {
            return this.stream;
        }

        /**
         * Initializes a new instance of ZipUrlConnection.
         *
         * @param zip the input stream (required).
         * @param url the URL (required).
         */
        ZipUrlConnection(final URL url, final ZipInputStream zip) {
            super(url);
            this.stream = zip;
        }

        @Override public void connect() throws IOException {
            Syslog.info("Connecting to %s...", getURL());
        }

        @Override public InputStream getInputStream() throws IOException {
            return getStream();
        }

    }

    @SuppressWarnings("ClassHasNoToStringMethod")
    private class JarUrlConnection extends URLConnection {

        private final JarInputStream stream;

        private JarInputStream getStream() {
            return this.stream;
        }

        /**
         * Initializes a new instance of JarUrlConnection.
         *
         * @param url the URL to which the connection belongs (required).
         * @param jar the stream for which to create the connection.
         */
        JarUrlConnection(final URL url, final JarInputStream jar) {
            super(url);
            this.stream = jar;
        }

        @Override public void connect() throws IOException {
            // Validate preconditions:
            Syslog.info("Connecting to %s ...", getURL());
        }

        @Override public InputStream getInputStream() throws IOException {
            return this.stream;
        }

    }

    @Nullable @Override protected URLConnection openConnection(final URL url) throws IOException {
        URLConnection result = null;
        final String path = url.getPath();
        final String probe = url.getRef();
        try {
            final URL target = new URL(path);
            result = target.openConnection();
            if (probe != null && !probe.isEmpty()) {
                // must dive into jar or zip:
                if (path.endsWith(".jar")) {
                    result = openJarConnection(url, new JarInputStream(result.getInputStream()),
                            probe);
                } else if (path.endsWith(".zip")) {
                    result = openZipConnection(url, new ZipInputStream(result.getInputStream()),
                            probe);
                } else {
                    Syslog.error("Unknown container type: %s. No knowledge about how to get at %s",
                            path, probe);
                }
            }
        }
        catch (final MalformedURLException e) {
            Syslog.error(e);
        }

        return result;
    }

    /**
     * Opens an URLConnection for the specified probe in the specified zip file.
     *
     * @param url   the original URL (required).
     * @param zip   the zip file as stream (required).
     * @param probe the probe to open a stream for (required, not empty).
     * @return an URLConnection to the probe, or <code>null</code> if the probe is not existent or
     * inaccessible.
     * @throws IOException if an I/O error occurred.
     * @author dio (dominik.wezel@intellagent.ch)
     * @since Apr 18, 2012
     */
    @SuppressWarnings("AssignmentToNull") @Nullable private URLConnection openZipConnection(
            final URL url, final ZipInputStream zip, String probe) throws IOException {
        URLConnection result = null;
        String path = probe;
        final int endProbe = probe.indexOf('#');
        if (endProbe != -1) {
            path = probe.substring(0, endProbe);
            probe = probe.substring(endProbe + 1);
        } else {
            probe = null;
        }
        for (ZipEntry entry = zip.getNextEntry(); entry != null; entry = zip.getNextEntry()) {
            if (path.equals(entry.getName())) {
                if (probe != null && !probe.isEmpty()) {
                    // must dive into jar or zip:
                    if (path.endsWith(".jar")) { //$NON-NLS-1$
                        result = openJarConnection(url, new JarInputStream(zip), probe);
                    } else if (path.endsWith(".zip")) { //$NON-NLS-1$
                        result = openZipConnection(url, new ZipInputStream(zip), probe);
                    } else {
                        Syslog.error(
                                "Unknown container type: %s. No knowledge about how to get at %s",
                                path, probe);
                    }
                } else {
                    result = new ZipUrlConnection(url, zip);
                }
            }
            if (result != null) {
                break;
            }
        }
        return null;
    }

    /**
     * Opens an URLConnection for the specified probe in the specified jar file.
     *
     * @param url   the original URL (required).
     * @param jar   the jar file as stream (required).
     * @param probe the probe to open a stream for (required, not empty).
     * @return an URLConnection to the probe, or <code>null</code> if the probe is not existent or
     * inaccessible.
     * @throws IOException if an I/O error occurred.
     */
    @SuppressWarnings("AssignmentToNull") @Nullable private URLConnection openJarConnection(
            final URL url, final JarInputStream jar, String probe) throws IOException {
        URLConnection result = null;
        String path = probe;
        final int endProbe = probe.indexOf('#');
        if (endProbe != -1) {
            path = probe.substring(0, endProbe);
            probe = probe.substring(endProbe + 1);
        } else {
            probe = null;
        }
        for (JarEntry entry = jar.getNextJarEntry(); entry != null; entry = jar.getNextJarEntry()) {
//            Syslog.debug("Entry: {0}, path: {1}, probe: {2}", entry.getName(),
//                    path, probe);
            if (path.equals(entry.getName())) {
                if (probe != null && !probe.isEmpty()) {
                    // must dive into jar or zip:
                    if (path.endsWith(".jar")) { //$NON-NLS-1$
//                        Syslog.debug(
//                                "Opening JAR connection to {0} with probe {1}",
//                                url, probe);
                        result = openJarConnection(url, new JarInputStream(jar), probe);
                    } else if (path.endsWith(".zip")) { //$NON-NLS-1$
//                        Syslog.debug(
//                                "Opening ZIP connection to {0} with probe {1}",
//                                url, probe);
                        result = openZipConnection(url, new ZipInputStream(jar), probe);
                    } else {
                        Syslog.error(
                                "Unknown container type: %s. No knowledge about how to get at %s",
                                path, probe);
                    }
                } else {
//                    Syslog.debug("Opening URL connection to {0} from here", url);
                    result = new JarUrlConnection(url, jar);
                }
            }
            if (result != null) {
                break;
            }
        }
        return result;
    }

}
