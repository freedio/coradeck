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

import static com.coradec.corabus.state.HubState.*;
import static java.util.stream.Collectors.*;

import com.coradec.corabus.com.Invitation;
import com.coradec.corabus.model.BusHub;
import com.coradec.corabus.model.BusNode;
import com.coradec.corabus.view.BusContext;
import com.coradec.corabus.view.Member;
import com.coradec.corabus.view.impl.BasicBusContext;
import com.coradec.coracom.model.Message;
import com.coradec.coracom.model.ParallelMultiRequest;
import com.coradec.coracom.model.Recipient;
import com.coradec.coracom.model.Request;
import com.coradec.coracom.model.impl.AbstractSessionCommand;
import com.coradec.coracom.model.impl.BasicSessionRequest;
import com.coradec.coracore.annotation.Inject;
import com.coradec.coracore.annotation.Nullable;
import com.coradec.coracore.annotation.ToString;
import com.coradec.coracore.model.Factory;
import com.coradec.coracore.model.State;
import com.coradec.coracore.util.ClassUtil;
import com.coradec.coractrl.model.StateTransition;
import com.coradec.coradir.model.Path;
import com.coradec.corasession.model.Session;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Basic implementation of a bus hub.​​
 */
@SuppressWarnings("ClassHasNoToStringMethod")
public class BasicHub extends BasicNode implements BusHub {

    @Inject private static Factory<Invitation> INVITATION;
    @Inject private static Factory<ParallelMultiRequest> MULTIREQUEST;

    private final List<AddMemberRequest> candidates = new ArrayList<>();
    private final Map<String, Member> members = new LinkedHashMap<>();
    private final BusContext busContext = new InternalBusContext();

    @SuppressWarnings("WeakerAccess") public BasicHub() {
        addRoute(AddMemberRequest.class, this::addCandidate);
        approve(AddMemberCommand.class);
    }

    @SuppressWarnings("WeakerAccess") Map<String, Member> getMembers() {
        return members;
    }

    @Override protected Collection<StateTransition> getSetupTransitions(final Session session,
            final BusContext context, final Invitation invitation) {
        final Collection<StateTransition> result =
                super.getSetupTransitions(session, context, invitation);
        Collections.addAll(result, new Loading(session), new Loaded(session));
        return result;
    }

    @Override protected Collection<StateTransition> getShutdownTransitions(final Session session) {
        final Collection<StateTransition> result = super.getShutdownTransitions(session);
        Collections.addAll(result, new Unloading(session), new Unloaded(session),
                new Terminating(session));
        return result;
    }

    @Override protected State getReadyState() {
        return LOADED;
    }

    @Override protected @Nullable Request onInitialize(final Session session) {
        super.onInitialize(session);
//        addRoute(AddMemberRequest.class, this::addCandidate);
        return null;
    }

    /**
     * Interceptor invoked during the state transition from INITIALIZED to LOADING.
     * <p>
     * Can be used to do preliminary loading checks.
     * <p>
     * The base method currently does nothing, but might do so in an advanced version.
     * <p>
     * This method usually returns {@code null}; if however some asynchronous work needs to be done
     * in order for it to succeed, the corresponding request can be returned here.  The state
     * machine will wait for the request to complete before moving on.
     * <p>
     * Subclasses can wrap this method early (i.e. override it and invoke the superclass method as
     * soon as possible.
     *
     * @param session the session context.
     */
    @SuppressWarnings("WeakerAccess") protected @Nullable Request onLoading(final Session session) {
        setState(LOADING);
        return null;
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
    @SuppressWarnings("WeakerAccess") protected @Nullable Request onLoad(final Session session) {
        replaceRoute(AddMemberRequest.class, this::addMember);
        final Request result = inject(MULTIREQUEST.create(candidates, this));
        setState(LOADED);
        return result;
    }

    /**
     * Interceptor invoked during the state transition from LOADED to UNLOADING.
     * <p>
     * Can be used to do pre-unload checks.
     * <p>
     * The base method turns off further additions to the hub.
     * <p>
     * This method usually returns {@code null}; if however some asynchronous work needs to be done
     * in order for it to succeed, the corresponding request can be returned here.  The state
     * machine will wait for the request to complete before moving on.
     * <p>
     * Subclasses can wrap this method late (i.e. override it and invoke the superclass method as
     * late as possible.
     *
     * @param session the session context.
     */
    @SuppressWarnings("WeakerAccess") @Nullable Request onUnloading(final Session session) {
        setState(UNLOADING);
        return null;
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
    @SuppressWarnings("WeakerAccess") @Nullable <R extends Message> Request onUnload(
            final Session session) {
        final Map<String, Member> members = getMembers();
        final int memberCount = members.size();
//        debug("Unloading %d member%s.", memberCount, memberCount == 1 ? "" : "s");
        final ParallelMultiRequest result = inject(MULTIREQUEST.create(
                members.values().stream().map(Member::dismiss).collect(toList()), this,
                new Object[] {}));
        setState(UNLOADED);
        return result;
    }

    private void cantAdd(final AddMemberRequest r) {
        r.cancel();
    }

    @Override public Request add(final Session session, final String name, final BusNode node) {
        return inject(new AddMemberRequest(session, name, node));
    }

    private void addCandidate(final AddMemberRequest request) {
        candidates.add((AddMemberRequest)request.renew());
//        debug("%s: added candidate %s as \"%s\"", this, request.getNode(), request.getName());
    }

    @SuppressWarnings("WeakerAccess") void addMember(final AddMemberRequest request) {
//        debug("%s: received addMemberRequest for %s <<%s>>", this, request.getName(),
//                request.getNode());
        final Session session = request.getSession();
        final Invitation invitation =
                inject(INVITATION.create(session, request.getName(), getBusContext(), this,
                        new Recipient[] {
                                request.getNode()
                        }));
        invitation.andThen(() -> inject(new AddMemberCommand(session, request.getName(),
                invitation.getMember())).reportCompletionTo(request)).orElse(request::fail);
    }

    private BusContext getBusContext() {
        return busContext;
    }

    private class InternalBusContext extends BasicBusContext {

        @Override public Path getPath(final String name) {
            return BasicHub.this.getPath().add(name);
        }

    }

    @SuppressWarnings("ClassHasNoToStringMethod")
    private class Loading extends NodeStateTransition {

        Loading(final Session session) {
            super(session, INITIALIZED, LOADING);
        }

        @Override protected @Nullable Request onExecute() {
            replaceRoute(AddMemberRequest.class, BasicHub.this::addMember);
            return onLoading(getSession());
        }

    }

    private class Loaded extends NodeStateTransition {

        Loaded(final Session session) {
            super(session, LOADING, LOADED);
        }

        @Override protected @Nullable Request onExecute() {
            return onLoad(getSession());
        }
    }

    private class Unloading extends NodeStateTransition {

        Unloading(final Session session) {
            super(session, LOADED, UNLOADING);
        }

        @Override protected @Nullable Request onExecute() {
            return onUnloading(getSession());
        }
    }

    private class Unloaded extends NodeStateTransition {

        Unloaded(final Session session) {
            super(session, UNLOADING, UNLOADED);
        }

        @Override protected @Nullable Request onExecute() {
            return onUnload(getSession());
        }
    }

    private class Terminating extends NodeStateTransition {

        Terminating(final Session session) {
            super(session, UNLOADED, TERMINATING);
        }

        @Override protected @Nullable Request onExecute() {
            return onTerminating(getSession());
        }
    }

    private class AddMemberRequest extends BasicSessionRequest {

        private final String name;
        private final BusNode node;

        /**
         * Initializes a new instance of AddMemberRequest for the specified member with the
         * specified requested name in the context of the specified session.
         *
         * @param session the session context.
         * @param name    the member name.
         * @param node    the member node.
         */
        AddMemberRequest(final Session session, final String name, final BusNode node) {
            super(session, BasicHub.this);
            this.name = name;
            this.node = node;
        }

        @ToString public String getName() {
            return name;
        }

        @ToString public BusNode getNode() {
            return node;
        }

        @Override public String toString() {
            return ClassUtil.toString(this);
        }
    }

    private class AddMemberCommand extends AbstractSessionCommand {

        private final String name;
        private final Member member;

        AddMemberCommand(final Session session, final String name, final Member member) {
            super(session, BasicHub.this);
            this.name = name;
            this.member = member;
        }

        @ToString public String getName() {
            return name;
        }

        @ToString public Member getMember() {
            return member;
        }

        @Override public void execute() {
            getMembers().put(name, member);
        }

        @Override public String toString() {
            return ClassUtil.toString(this);
        }
    }

}
