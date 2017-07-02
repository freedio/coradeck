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

package com.coradec.coractrl.ctrl.impl;

import com.coradec.coracom.ctrl.MessageQueue;
import com.coradec.coracom.model.Command;
import com.coradec.coracom.model.Message;
import com.coradec.coracom.model.Recipient;
import com.coradec.coracom.model.Sender;
import com.coradec.coracom.model.impl.AbstractCommand;
import com.coradec.coracore.annotation.Implementation;
import com.coradec.coracore.annotation.Inject;
import com.coradec.coracore.annotation.Nullable;
import com.coradec.coracore.annotation.ToString;
import com.coradec.coracore.util.ClassUtil;
import com.coradec.coractrl.ctrl.Agent;
import com.coradec.coralog.ctrl.impl.Logger;
import com.coradec.coratext.model.LocalizedText;
import com.coradec.coratext.model.Text;

import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * ​​Basic implementation of an agent.
 */
@SuppressWarnings("WeakerAccess")
@Implementation
public class BasicAgent extends Logger implements Agent, Recipient, Sender {

    private static final Text TEXT_MESSAGE_BOUNCED = LocalizedText.define("MessageBounced");
    private static final Text TEXT_COMMAND_NOT_APPROVED =
            LocalizedText.define("CommandNotApproved");
    @SuppressWarnings("ProtectedField") @Inject
    private static MessageQueue MQ;

    private static final Map<Class<?>, AtomicInteger> IDS = new ConcurrentHashMap<>();
    private static final Text TEXT_MESSAGE_UNPROCESSED = LocalizedText.define("MessageUnprocessed");

    private final int id;
    private @Nullable Map<Class<?>, Consumer<?>> routes;
    private final Set<Class<?>> approvedCommands = new HashSet<>();

    protected BasicAgent() {
        id = IDS.computeIfAbsent(getClass(), klass -> new AtomicInteger(0)).incrementAndGet();
        approve(AddRouteCommand.class, RemoveRouteCommand.class);
    }

    /**
     * Approves commands of the specified types for execution.
     *
     * @param types a list of (additionally) approved commands.
     */
    @SafeVarargs protected final void approve(final Class<? extends Command>... types) {
        approvedCommands.addAll(Arrays.asList(types));
    }

    @ToString public int getId() {
        return id;
    }

    protected Map<Class<?>, Consumer<?>> getRoutes() {
        if (routes == null) routes = new HashMap<>();
        return routes;
    }

    protected <R extends Message> void addRoute(Class<? super R> selector, Consumer<R> processor) {
        inject(new AddRouteCommand<>(selector, processor));
    }

    protected <R extends Message> void removeRoute(Class<? super R> selector) {
        inject(new RemoveRouteCommand<>(selector));
    }

    /**
     * Injects the specified message on behalf of this sender.
     *
     * @param message the message to inject.
     */
    protected <M extends Message> M inject(final M message) {
        return MQ.inject(message);
    }

    @Override public String represent() {
        return String.format("%s#%d", getClass().getSimpleName(), id);
    }

    @SuppressWarnings("unchecked") @Override public void onMessage(final Message message) {
        boolean processed = false;
        if (routes != null) {
            processed = routes.entrySet()
                              .stream()
                              .filter(route -> route.getKey().isInstance(message))
                              .map(route -> {
                                  final Consumer<Message> processor =
                                          (Consumer<Message>)route.getValue();
                                  processor.accept(message);
                                  return processor;
                              })
                              .count() != 0;
        }
        if (!onMessage(processed, message)) warn(TEXT_MESSAGE_UNPROCESSED, this, message);
    }

    protected boolean onMessage(boolean processed, final Message message) {
        if (!processed && message instanceof Command) {
            final Command command = (Command)message;
            if (approved(command)) try {
                command.execute();
                command.succeed();
            } catch (Exception e) {
                command.fail(e);
            }
            else error(TEXT_COMMAND_NOT_APPROVED, command);
            processed = true;
        }
        return processed;
    }

    /**
     * Callback invoked to check if the specified command is approved for execution.
     * <p>
     * The default is to not approve commands for reasons of security except if there is a route for
     * them.  Classes that overwrite this method should return with {@code true} for all cases to
     * approve, and finally invoke the super method which will handle the superclass cases.
     *
     * @param command the command to check.
     * @return {@code true} if the command is approved for execution, {@code false} if not.
     */
    protected boolean approved(final Command command) {
        return approvedCommands.stream().anyMatch(cmd -> cmd.isInstance(command));
    }

    @Override public URI toURI() {
        return URI.create("agent:" + represent());
    }

    @Override public void bounce(final Message message) {
        error(TEXT_MESSAGE_BOUNCED, message);
    }

    @SuppressWarnings("ClassHasNoToStringMethod")
    private class AddRouteCommand<R extends Message> extends AbstractCommand {

        private final Class<? super R> selector;
        private final Consumer<? extends Message> processor;

        AddRouteCommand(final Class<? super R> selector, final Consumer<R> processor) {
            super(BasicAgent.this);
            this.selector = selector;
            this.processor = processor;
        }

        @ToString public Class<? super R> getSelector() {
            return this.selector;
        }

        @ToString public Consumer<? extends Message> getProcessor() {
            return this.processor;
        }

        @Override public void execute() {
            getRoutes().put(getSelector(), getProcessor());
        }
    }

    @SuppressWarnings({"ClassHasNoToStringMethod", "WeakerAccess"})
    private class RemoveRouteCommand<R extends Message> extends AbstractCommand {

        private final Class<? super R> selector;

        RemoveRouteCommand(final Class<? super R> selector) {
            super(BasicAgent.this);
            this.selector = selector;
        }

        @ToString public Class<? super R> getSelector() {
            return this.selector;
        }

        @Override public void execute() {
            getRoutes().remove(getSelector());
        }
    }

    @Override public String toString() {
        return ClassUtil.toString(this);
    }
}
