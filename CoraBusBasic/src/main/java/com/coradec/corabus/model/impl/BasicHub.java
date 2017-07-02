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

package com.coradec.corabus.model.impl;

import static com.coradec.corabus.model.HubState.*;

import com.coradec.corabus.model.BusHub;
import com.coradec.corabus.view.BusContext;
import com.coradec.coracom.model.Request;
import com.coradec.coracore.annotation.Inject;
import com.coradec.coracore.model.State;
import com.coradec.coractrl.model.StateTransition;
import com.coradec.corasession.model.Session;

import java.util.Collection;
import java.util.Collections;

/**
 * Basic implementation of a bus hub.​​
 */
public class BasicHub extends BasicNode implements BusHub {

    protected BasicHub() {
    }

    @Override protected Collection<StateTransition> getSetupTransitions(final Session session,
                                                                        final BusContext context) {
        final Collection<StateTransition> result = super.getSetupTransitions(session, context);
        Collections.addAll(result, new Loading(session), new Loaded(session));
        return result;
    }

    @Override protected Collection<StateTransition> getShutdownTransitions(final Session session) {
        final Collection<StateTransition> result = super.getShutdownTransitions(session);
        Collections.addAll(result, new Unloading(session), new Unloaded(session));
        return result;
    }

    @Override protected State getReadyState() {
        return super.getReadyState();
    }

    /**
     * Interceptor invoked during the state transition from INITIALIZED to LOADING.
     * <p>
     * Can be used to do preliminary loading checks.
     * <p>
     * The base method currently does nothing, but might do so in an advanced version.
     * <p>
     * Subclasses can wrap this method early (i.e. override it and invoke the superclass method as
     * soon as possible.
     *
     * @param session the session context.
     */
    @SuppressWarnings("WeakerAccess") protected void onLoading(final Session session) {
    }

    /**
     * Interceptor invoked during the state transition from LOADING to LOADED.
     * <p>
     * Can be used to do after-load setup.
     * <p>
     * The base method loads the candidate members and switches from candidate to member mode.  This
     * process is inherently asynchronous if any candidates are present, because the candidates will
     * be asked to join in their candidate order.
     * <p>
     * Subclasses can wrap this method early (i.e. override it and invoke the superclass method as
     * soon as possible.
     *
     * @param session the session context.
     */
    @SuppressWarnings("WeakerAccess") protected void onLoad(final Session session) {
    }

    /**
     * Interceptor invoked during the state transition from LOADED to UNLOADING.
     * <p>
     * Can be used to do pre-unload checks.
     * <p>
     * The base method turns off further additions to the hub.
     * <p>
     * Subclasses can wrap this method late (i.e. override it and invoke the superclass method as
     * late as possible.
     *
     * @param session the session context.
     */
    @SuppressWarnings("WeakerAccess") void onUnloading(final Session session) {
    }

    /**
     * Interceptor invoked during the state transition from UNLOADING to UNLOADED.
     * <p>
     * Can be used to do post-unload work.
     * <p>
     * The base method invokes the shutdown procedure on all members.
     * <p>
     * Subclasses can wrap this method late (i.e. override it and invoke the superclass method as
     * late as possible.
     *
     * @param session the session context.
     */
    @SuppressWarnings("WeakerAccess") void onUnload(final Session session) {
    }

    @SuppressWarnings("ClassHasNoToStringMethod")
    private class Loading extends NodeStateTransition {

        @Inject private Request Request;

        Loading(final Session session) {
            super(session, INITIALIZED, LOADING);
        }

        @Override protected void onExecute() {
            onLoading(getSession());
        }

    }

    @SuppressWarnings("ClassHasNoToStringMethod")
    private class Loaded extends NodeStateTransition {

        @Inject private Request Request;

        Loaded(final Session session) {
            super(session, LOADING, LOADED);
        }

        @Override protected void onExecute() {
            onLoad(getSession());
        }
    }

    @SuppressWarnings("ClassHasNoToStringMethod")
    private class Unloading extends NodeStateTransition {

        @Inject private Request Request;

        Unloading(final Session session) {
            super(session, LOADED, UNLOADING);
        }

        @Override protected void onExecute() {
            onUnloading(getSession());
        }
    }

    @SuppressWarnings("ClassHasNoToStringMethod")
    private class Unloaded extends NodeStateTransition {

        @Inject private Request Request;

        Unloaded(final Session session) {
            super(session, UNLOADING, UNLOADED);
        }

        @Override protected void onExecute() {
            onUnload(getSession());
        }
    }

}
