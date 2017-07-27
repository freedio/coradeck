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

package com.coradec.corabus.server.main;

import com.coradec.corajet.cldr.CarClassLoader;
import com.coradec.corajet.cldr.Syslog;

import java.lang.reflect.InvocationTargetException;

/**
 * Car​​Launcher for the Server.
 */
public class CarLauncher {

    public static void main(String[] args) {
        CarClassLoader loader = new CarClassLoader();
        final String className = "com.coradec.corabus.server.main.Server";
        try {
            final Class<?> target = loader.findClass(className);
            target.getMethod("main", String[].class).invoke(null, (Object)args);
        } catch (ClassNotFoundException e) {
            Syslog.error("Class %s not found!", className);
        } catch (InvocationTargetException e) {
            Syslog.error(e.getTargetException());
        } catch (Exception e) {
            Syslog.error(e);
        }
    }

}
