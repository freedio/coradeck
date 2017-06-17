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

package com.coradec.coractrl.ctrl;

import com.coradec.coralog.ctrl.impl.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * ​​The static system control interface.
 */
@SuppressWarnings({"ClassHasNoToStringMethod", "WeakerAccess"})
public final class SysControl extends Logger {

    private static final SysControl ME = new SysControl();

    private final List<Runnable> shutdownHooks;
    private final Thread shutdown;

    private SysControl() {
        this.shutdownHooks = new ArrayList<>();
        Runtime.getRuntime().addShutdownHook(shutdown = new ShutdownHook());
    }

    List<Runnable> getShutdownHooks() {
        return this.shutdownHooks;
    }

    /**
     * Adds the specified shutdown hook.
     *
     * @param hook the hook.
     */
    public static void onShutdown(Runnable hook) {
        ME.addShutdownHook(hook);
    }

    /**
     * Performs the system termination.
     */
    public static void terminate() {
        ME.doTerminate();
    }

    private void doTerminate() {
        Runtime.getRuntime().removeShutdownHook(shutdown);
        try {
            shutdown.start();
            shutdown.join(2000);
        } catch (InterruptedException e) {
            warn(e);
        }
    }

    private void addShutdownHook(final Runnable hook) {
        shutdownHooks.add(hook);
    }

    private class ShutdownHook extends Thread {

        @Override public void run() {
            for (final Runnable shutdownHook : getShutdownHooks()) {
                try {
                    shutdownHook.run();
                } catch (Exception e) {
                    error(e);
                }
            }
        }

    }

}
