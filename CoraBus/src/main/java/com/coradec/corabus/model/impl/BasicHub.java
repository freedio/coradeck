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
import com.coradec.corabus.model.ServiceProvider;
import com.coradec.corabus.trouble.MemberNotFoundException;
import com.coradec.corabus.trouble.MemberTypeInvalidException;
import com.coradec.corabus.trouble.MountPointUndefinedException;
import com.coradec.corabus.view.BusContext;
import com.coradec.corabus.view.BusService;
import com.coradec.corabus.view.Member;
import com.coradec.corabus.view.impl.BasicBusContext;
import com.coradec.coracom.model.Message;
import com.coradec.coracom.model.ParallelMultiRequest;
import com.coradec.coracom.model.Recipient;
import com.coradec.coracom.model.Request;
import com.coradec.coracom.model.SerialMultiRequest;
import com.coradec.coracom.model.impl.BasicSessionCommand;
import com.coradec.coracom.model.impl.BasicSessionRequest;
import com.coradec.coracore.annotation.Inject;
import com.coradec.coracore.annotation.Nullable;
import com.coradec.coracore.annotation.ToString;
import com.coradec.coracore.model.Factory;
import com.coradec.coracore.model.State;
import com.coradec.coracore.trouble.ServiceNotAvailableException;
import com.coradec.coracore.util.ClassUtil;
import com.coradec.coractrl.model.StateTransition;
import com.coradec.coradir.model.Path;
import com.coradec.coradir.trouble.PathAbsoluteException;
import com.coradec.coradir.trouble.PathEmptyException;
import com.coradec.corasession.model.Session;
import com.coradec.coratext.model.LocalizedText;
import com.coradec.coratext.model.Text;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Basic implementation of a bus hub.​​
 */
@SuppressWarnings("ClassHasNoToStringMethod")
public class BasicHub extends BasicNode implements BusHub {

    private static final Text TEXT_EMPTY_PATH = LocalizedText.define("EmptyPath");
    @Inject private static Factory<Invitation> INVITATION;
    @Inject private static Factory<SerialMultiRequest> SERIALMRQ;
    @Inject private static Factory<ParallelMultiRequest> PARALLELMRQ;

    private final List<AddMemberRequest> candidates = new ArrayList<>();
    private final Map<String, Member> members = new LinkedHashMap<>();

    public BasicHub() {
        addRoute(AddMemberRequest.class, this::addCandidate);
        approve(AddMemberCommand.class);
    }

    Map<String, Member> getMembers() {
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
     * soon as possible).
     *
     * @param session the session context.
     */
    protected @Nullable Request onLoading(final Session session) {
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
     * soon as possible).
     *
     * @param session the session context.
     */
    protected @Nullable Request onLoad(final Session session) {
        replaceRoute(AddMemberRequest.class, this::addMember);
        final @Nullable Request result =
                candidates.isEmpty() ? null : inject(SERIALMRQ.create(candidates, this));
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
     * late as possible).
     *
     * @param session the session context.
     */
    @Nullable Request onUnloading(final Session session) {
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
     * late as possible).
     *
     * @param session the session context.
     */
    @Nullable <R extends Message> Request onUnload(
            final Session session) {
        final Map<String, Member> members = getMembers();
        final int memberCount = members.size();
        debug("Unloading %d member%s.", memberCount, memberCount == 1 ? "" : "s");
        final Request result = memberCount == 0 ? null : inject(PARALLELMRQ.create(
                members.values().stream().map(Member::dismiss).collect(toList()), this,
                new Object[] {}));
        setState(UNLOADED);
        return result;
    }

    private void cantAdd(final AddMemberRequest r) {
        r.cancel();
    }

    @Override public Request add(final Session session, final String name, final BusNode node) {
        return inject(new AddMemberRequest(session, Path.of(name), node));
    }

    @Override public Request add(final Session session, final Path path, final BusNode node) {
        return inject(new AddMemberRequest(session, path, node));
    }

    @Override public Optional<BusNode> lookup(final Session session, final Path path) {
        if (path.isEmpty()) return Optional.of(this);
        if (path.isAbsolute()) throw new PathAbsoluteException();
        if (path.isName()) {
            final Member member = getMembers().get(path.represent());
            return Optional.ofNullable(member == null ? null : member.getNode());
        }
        final String head = path.head();
        final Member member = getMembers().get(head);
        if (member == null)
            throw new MemberNotFoundException(path.represent().replace(head, '→' + head + '←'));
        if (member instanceof BusHub)
            return ((BusHub)member.getNode()).lookup(session, path.tail());
        return Optional.empty();
    }

    @Override public BusNode get(final Session session, final Path path)
            throws MemberNotFoundException {
        return lookup(session, path).orElseThrow(() -> new MemberNotFoundException(path));
    }

    @Override public boolean has(final Session session, final Path path) {
        if (path.isAbsolute()) throw new PathAbsoluteException();
        final Map<String, Member> members = getMembers();
        final Member member;
        final BusNode node;
        return path.isEmpty() ||
               path.isName() && members.containsKey(path.represent()) ||
               (member = members.get(path.head())) != null &&
               (node = member.getNode()) instanceof BusHub &&
               ((BusHub)node).has(session, path.tail());
    }

    /**
     * Returns the member (which must be a hub) with the specified name.
     *
     * @param name the member name.
     * @return a hub.
     */
    private BusHub getMemberHub(final String name) {
        final Member member = getMembers().get(name);
        final BusNode node = member.getNode();
        if (node instanceof BusHub) return (BusHub)node;
        throw new MemberTypeInvalidException(BusHub.class, node.getClass());
    }

    private void addCandidate(final AddMemberRequest request) {
        candidates.add((AddMemberRequest)request.renew());
//        debug("%s: added candidate %s as \"%s\"", this, request.getNode(), request.getName());
    }

    void addMember(final AddMemberRequest request) {
        final Session session = request.getSession();
        final Path path = request.getPath();
        final BusNode node = request.getNode();
        try {
            if (path.isEmpty()) throw new PathEmptyException();
            if (path.isAbsolute()) throw new PathAbsoluteException();
            if (path.isName()) {
                final String name = path.represent();
                final Invitation invitation =
                        inject(INVITATION.create(session, name, new InternalBusContext(session),
                                this, new Recipient[] {
                                        request.getNode()
                                }));
                invitation.andThen(() -> inject(new AddMemberCommand(session, name,
                        invitation.getMember())).reportCompletionTo(request)).orElse(request::fail);
            } else {
                final String head = path.head();
                final Member member = getMembers().get(head);
                if (member == null) throw new MemberNotFoundException(
                        path.represent().replace(head, '→' + head + '←'));
                BusNode hub = member.getNode();
                if (node instanceof BusHub)
                    ((BusHub)hub).add(session, path.tail(), node).reportCompletionTo(request);
                else throw new MountPointUndefinedException(path, node);
            }
        } catch (RuntimeException e) {
            request.fail(e);
            throw e;
        }
    }

    private class InternalBusContext extends BasicBusContext {

        InternalBusContext(Session session) {
            super(session);
        }

        @Override public Path getPath(final String name) {
            return BasicHub.this.getPath().add(name);
        }

        @Override public <S extends BusService> boolean provides(final Class<? super S> type,
                final Object... args) {
            return BasicHub.this.getMembers()
                                .values()
                                .stream()
                                .anyMatch(member -> member instanceof ServiceProvider &&
                                                    ((ServiceProvider)member).provides(getSession(),
                                                            type, args));
        }

        @Override public <S extends BusService> Optional<S> findService(final Class<? super S> type,
                final Object... args) {
            return getMembers().values()
                               .stream()
                               .filter(member -> member instanceof ServiceProvider &&
                                                 ((ServiceProvider)member).provides(getSession(),
                                                         type, args))
                               .findFirst()
                               .map(member -> {
                                   try {
                                       return ((ServiceProvider)member).getService(getSession(),
                                               type, args);
                                   } catch (ServiceNotAvailableException e) {
                                       error(e);
                                       return null;
                                   }
                               });
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

        private final Path path;
        private final BusNode node;

        /**
         * Initializes a new instance of AddMemberRequest for the specified member with the
         * specified requested relative path in the context of the specified session.
         *
         * @param session the session context.
         * @param path    the member path.
         * @param node    the member node.
         */
        AddMemberRequest(final Session session, final Path path, final BusNode node) {
            super(session, BasicHub.this);
            this.path = path;
            this.node = node;
        }

        @ToString public Path getPath() {
            return path;
        }

        @ToString public BusNode getNode() {
            return node;
        }

    }

    private class AddMemberCommand extends BasicSessionCommand {

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
