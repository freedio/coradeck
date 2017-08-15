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

package com.coradec.coracore.util;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.List;

/**
 * ​​Static library of network utilities.
 */
public final class NetworkUtil {

    private NetworkUtil() {
    }

    /**
     * Returns the local machine ID.  This is the so-called "MAC address" if it is available,
     * otherwise the String "localhost".
     *
     * @return the local machine ID.
     */
    public static String getLocalMachineId() {
        String sysId = "localhost";
        try {
            for (final Enumeration<NetworkInterface> interfaces =
                 NetworkInterface.getNetworkInterfaces(); interfaces.hasMoreElements(); ) {
                final NetworkInterface network = interfaces.nextElement();
                if (network.isUp()) {
                    final List<InterfaceAddress> addresses = network.getInterfaceAddresses();
                    final byte[] mac = network.getHardwareAddress();
                    final String displayName = network.getDisplayName();
                    final String name = network.getName();
                    final String mac$ = mac == null ? "" : StringUtil.format(mac, "-");
//                    System.out.printf("Network %s (\"%s\") [%s]: %s%n", name, displayName, mac$,
//                            addresses);
                    if (mac != null) sysId = mac$;
                }
            }
        } catch (SocketException e) {
            // ignore
        }
        return sysId;
    }

    static boolean isOnline() {
        boolean online = false;
        try {
            for (final Enumeration<NetworkInterface> interfaces =
                 NetworkInterface.getNetworkInterfaces(); interfaces.hasMoreElements(); ) {
                final NetworkInterface network = interfaces.nextElement();
                if (network.isUp() && !network.isLoopback()) {
                    //noinspection UseOfSystemOutOrSystemErr
                    System.out.printf("Network %s is UP%n", network);
                    online = true;
                    break;
                }
            }
        } catch (SocketException e) {
            // ignore
        }
        return online;
    }

    /**
     * Returns the canonical host name of the most promising interface (the one that is connected to
     * the internet), or the string representation of the IP-address of it.
     *
     * @return the canonical host name.
     */
    public static String getCanonicalHostName() {
        String hostname = "localhost";
        InetAddress bestBet = null;
        try {
            for (final Enumeration<NetworkInterface> interfaces =
                 NetworkInterface.getNetworkInterfaces(); interfaces.hasMoreElements(); ) {
                final NetworkInterface network = interfaces.nextElement();
                if (network.isUp()) {
                    for (Enumeration<InetAddress> inetAddresses = network.getInetAddresses();
                         inetAddresses.hasMoreElements(); ) {
                        final InetAddress inetAddress = inetAddresses.nextElement();
                        if (inetAddress == null ||
                            inetAddress.isLoopbackAddress() ||
                            inetAddress.isLinkLocalAddress() ||
                            inetAddress.isSiteLocalAddress()) continue;
                        final String canonicalHostName = inetAddress.getCanonicalHostName();
//                        System.out.printf("On interface %s: %s (%s)%n", network.getName(),
//                                canonicalHostName, inetAddress.toString());
                        if (canonicalHostName.matches(".+\\.[a-zA-Z][a-zA-Z0-9_-]$")) {
                            hostname = canonicalHostName;
                            bestBet = inetAddress;
                        } else if (canonicalHostName.equals(inetAddress.getHostAddress())) {
                            bestBet = inetAddress;
                        }
                    }
                }
            }
            if (hostname.equals("localhost") && bestBet != null) {
                hostname = bestBet.getHostAddress();
            }
        } catch (SocketException e) {
            // ignore
        }
        return hostname;
    }

    /**
     * Returns the local host loopback address.
     *
     * @return the local host loopback address.
     */
    public static InetAddress getLocalAddress() {
        return InetAddress.getLoopbackAddress();
    }

    /**
     * Returns a socket on the loopback address with the specified port.
     *
     * @param port the port.
     * @return a socket.
     */
    public static SocketAddress getLocalAddress(final int port) {
        return new InetSocketAddress(getLocalAddress(), port);
    }
}
