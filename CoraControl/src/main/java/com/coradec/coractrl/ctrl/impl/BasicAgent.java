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
import com.coradec.coracom.ctrl.Observer;
import com.coradec.coracom.model.Command;
import com.coradec.coracom.model.Event;
import com.coradec.coracom.model.Information;
import com.coradec.coracom.model.Message;
import com.coradec.coracom.model.MultiRequest;
import com.coradec.coracom.model.Recipient;
import com.coradec.coracom.model.Request;
import com.coradec.coracom.model.impl.BasicCommand;
import com.coradec.coracore.annotation.Implementation;
import com.coradec.coracore.annotation.Inject;
import com.coradec.coracore.annotation.Internal;
import com.coradec.coracore.annotation.Nullable;
import com.coradec.coracore.annotation.ToString;
import com.coradec.coracore.model.Origin;
import com.coradec.coracore.util.ClassUtil;
import com.coradec.coractrl.ctrl.Agent;
import com.coradec.coralog.ctrl.impl.Logger;
import com.coradec.coratext.model.LocalizedText;
import com.coradec.coratext.model.Text;

import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * ​​Basic implementation of an agent.
 */
@SuppressWarnings("PublicField")
@Implementation
public class BasicAgent extends Logger implements Agent, Recipient, Origin {

    private static final Map<Class<?>, AtomicInteger> IDS = new ConcurrentHashMap<>();
    private static final Text TEXT_MESSAGE_BOUNCED = LocalizedText.define("MessageBounced");
    private static final Text TEXT_COMMAND_NOT_APPROVED =
            LocalizedText.define("CommandNotApproved");
    private static final Text TEXT_MESSAGE_UNPROCESSED = LocalizedText.define("MessageUnprocessed");
    static final Text TEXT_ROUTE_ALREADY_SET = LocalizedText.define("RouteAlreadySet");
    private final int id;

    @Inject private MessageQueue MQ;
    private @Nullable Map<Class<?>, Consumer<?>> routes;
    private final Set<Class<?>> approvedCommands = new CopyOnWriteArraySet<>();

    protected BasicAgent() {
        id = IDS.computeIfAbsent(getClass(), klass -> new AtomicInteger(0)).incrementAndGet();
        approve(AddRouteCommand.class, RemoveRouteCommand.class, ReplaceRouteCommand.class,
                InternalCommandWrapper.class);
    }

    /**
     * Approves commands of the specified types for execution.
     *
     * @param types a list of (additionally) approved commands.
     */
    @SafeVarargs protected final void approve(final Class<? extends Command>... types) {
        approvedCommands.addAll(Arrays.asList(types));
    }

    /**
     * Returns the agent ID.
     * <p>
     * Each agent gets a new individual number from a counter during the execution of the JVM.
     *
     * @return the agent ID.
     */
    @ToString public int getId() {
        return id;
    }

    /**
     * Returns the established message routes.
     *
     * @return the message routes.
     */
    protected Map<Class<?>, Consumer<?>> getRoutes() {
        if (routes == null) routes = new HashMap<>();
        return routes;
    }

    /**
     * Adds a new message route.  Messages of the specified type injected into the agent will be
     * handled by the specified message consumer.
     *
     * @param <R>       the message type.
     * @param selector  the message type selector.
     * @param processor the message processor.
     */
    protected <R extends Message> void addRoute(Class<? super R> selector, Consumer<R> processor) {
        inject(new AddRouteCommand<>(selector, processor));
    }

    /**
     * Redirects an existing route to a new message processor.  Messages of the specified type
     * injected into the agent will be handled by the specified message consumer.
     *
     * @param <R>       the message type.
     * @param selector  the message type selector.
     * @param processor the message processor.
     */
    protected <R extends Message> void replaceRoute(Class<? super R> selector,
            Consumer<R> processor) {
        inject(new ReplaceRouteCommand<>(selector, processor));
    }

    /**
     * Removes an existing route.  Disables the established message processor for messages of the
     * specified type.
     *
     * @param <R>      the message type.
     * @param selector the message type selector.
     */
    protected <R extends Message> void removeRoute(Class<? super R> selector) {
        inject(new RemoveRouteCommand<>(selector));
    }

    /**
     * Injects the specified information into the central message queue on behalf of its sender.
     *
     * @param info the information to inject.
     * @return the message, for further processing.
     */
    protected <I extends Information> I inject(final I info) {
        return MQ.inject(info);
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
        if (!onMessage(processed, message)) {
            warn(TEXT_MESSAGE_UNPROCESSED, message, this);
            if (message instanceof Request) ((Request)message).cancel();
        }
    }

    /**
     * Returns the recipient ID.
     *
     * @return the recipient ID.
     */
    @Override public String getRecipientId() {
        return String.format("%s#%d", getClass().getName(), getId());
    }

    /**
     * Processes the specified message after applying message routing, which may or may not have
     * found a processor for the message, according to the specified processed flag ({@code true}:
     * at least one message processor was activated by the message, {@code false}: no message
     * processor has seen this message yet).
     *
     * @param processed indicates whether at least one of the message processors has already seen
     *                  this message.
     * @param message   the message.
     * @return {@code true} if after-processing processed this message, {@code false} if not (which
     * means that a message was injected which the system cannot understand (has no processors for
     * it).
     */
    protected boolean onMessage(boolean processed, final Message message) {
        if (!processed) {
            if (message instanceof Command) {
                final Command command = (Command)message;
                if (approved(command)) try {
                    command.execute();
                    command.succeed();
                } catch (Exception e) {
                    command.fail(e);
                }
                else error(TEXT_COMMAND_NOT_APPROVED, command);
                processed = true;
            } else if (message instanceof MultiRequest) {
                MultiRequest multi = (MultiRequest)message;
                multi.process();
                processed = true;
            }
        }
        return processed;
    }

    /**
     * Performs the specified task in concurrency separation by wrapping it in a command and
     * injecting it into the message queue.
     *
     * @param task the task to perform.
     */
    protected Request execute(final Runnable task) {
        return inject(new InternalCommandWrapper(task));
    }

    /**
     * Pulls the specified trigger in concurrency separation when the specified event occurs.
     *
     * @param once    {@code true} if the trigger shall fire only once, {@code false} if it shall be
     *                pulled every time the event occurs.
     * @param event   the type of event.
     * @param trigger the trigger to pull.
     */
    @SuppressWarnings("unchecked") protected <E extends Event> void on(boolean once,
            Class<? super E> event, Consumer<E> trigger) {
        MQ.subscribe(new ReactiveTrigger(event, trigger, once));
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

    /**
     * Allows the message queue to shut down as an important asynchronous process is running.
     */
    protected void allowMessageQueueShutdown() {
        MQ.allowShutdown();
    }

    /**
     * Prevents the message queue from shutting down after an important asynchronous process has
     * finished.
     */
    protected void preventMessageQueueShutdown() {
        MQ.preventShutdown();
    }

    @Override public String toString() {
        return ClassUtil.toString(this);
    }

    /**
     * Re-injects the specified info into the message queue after renewing it.
     *
     * @param <I>  the information type.
     * @param info the information to re-inject.
     */
    protected <I extends Information> void again(final I info) {
        inject(info.renew());
    }

    @SuppressWarnings("ClassHasNoToStringMethod")
    @Internal
    private class AddRouteCommand<R extends Message> extends BasicCommand {

        private final Class<? super R> selector;
        private final Consumer<? extends Message> processor;

        AddRouteCommand(final Class<? super R> selector, final Consumer<R> processor) {
            super(BasicAgent.this, BasicAgent.this);
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
            if (getRoutes().putIfAbsent(getSelector(), getProcessor()) != null) {
                warn(TEXT_ROUTE_ALREADY_SET, selector);
            }
        }

        @Override public String getRecipientId() {
            return BasicAgent.this.getRecipientId();
        }

    }

    @SuppressWarnings("ClassHasNoToStringMethod")
    @Internal
    private class ReplaceRouteCommand<R extends Message> extends BasicCommand {

        private final Class<? super R> selector;
        private final Consumer<? extends Message> processor;

        public ReplaceRouteCommand(final Class<? super R> selector, final Consumer<R> processor) {
            super(BasicAgent.this, BasicAgent.this);
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

        @Override public String getRecipientId() {
            return BasicAgent.this.getRecipientId();
        }

    }

    @SuppressWarnings("ClassHasNoToStringMethod")
    @Internal
    private class RemoveRouteCommand<R extends Message> extends BasicCommand {

        private final Class<? super R> selector;

        RemoveRouteCommand(final Class<? super R> selector) {
            super(BasicAgent.this, BasicAgent.this);
            this.selector = selector;
        }

        @ToString public Class<? super R> getSelector() {
            return this.selector;
        }

        @Override public void execute() {
            getRoutes().remove(getSelector());
        }

        @Override public String getRecipientId() {
            return BasicAgent.this.getRecipientId();
        }

    }

    @SuppressWarnings("ClassHasNoToStringMethod")
    @Internal
    private class InternalCommandWrapper extends BasicCommand {

        private final Runnable task;

        InternalCommandWrapper(final Runnable task) {
            super(BasicAgent.this, BasicAgent.this);
            this.task = task;
        }

        @Override public void execute() {
            try {
                task.run();
                succeed();
            } catch (Exception e) {
                fail(e);
            }
        }

        @Override public String getRecipientId() {
            return BasicAgent.this.getRecipientId();
        }

    }

    @SuppressWarnings("ClassHasNoToStringMethod")
    private class ReactiveTrigger<E extends Event> implements Observer {

        private final Class<E> event;
        private final Consumer<E> trigger;
        private final boolean once;

        public ReactiveTrigger(final Class<E> event, final Consumer<E> trigger, boolean once) {
            this.event = event;
            this.trigger = trigger;
            this.once = once;
        }

        @SuppressWarnings("unchecked") @Override public boolean notify(final Information info) {
            if (wants(info)) try {
                trigger.accept((E)info);
            } catch (Exception e) {
                error(e);
            }
            return once;
        }

        @Override public boolean wants(final Information info) {
            return event.isInstance(info);
        }

    }

}
