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

package com.coradec.corabus.view.impl;

import com.coradec.corabus.trouble.MemberAlreadyPresentException;
import com.coradec.corabus.trouble.MemberMotPresentException;
import com.coradec.corabus.view.BusContext;
import com.coradec.corabus.view.BusService;
import com.coradec.corabus.view.Member;
import com.coradec.coracore.annotation.ToString;
import com.coradec.coracore.trouble.ServiceNotAvailableException;
import com.coradec.coracore.util.ClassUtil;
import com.coradec.coradir.model.Path;
import com.coradec.corasession.model.Session;
import com.coradec.corasession.view.impl.BasicView;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ​​Basic implementation of a bus context.
 */
public abstract class BasicBusContext extends BasicView implements BusContext {

    private final Map<String, Member> members = new ConcurrentHashMap<>();

    public BasicBusContext(final Session session) {
        super(session);
    }

    @ToString public Map<String, Member> getContextMembers() {
        return members;
    }

    @Override public void left(final String name) {
        if (members.remove(name) == null) throw new MemberMotPresentException();
    }

    @Override public void joined(final String name, final Member member) {
        if (members.containsKey(name)) throw new MemberAlreadyPresentException();
        members.put(name, member);
    }

    @Override public boolean contains(final Member member) {
        return members.containsValue(member);
    }

    @Override public Path getPath(final String name) {
        return Path.of(name);
    }

    @Override public String toString() {
        return ClassUtil.toString(this);
    }

    @Override
    public <S extends BusService> S getService(final Class<? super S> type, final Object... args)
            throws ServiceNotAvailableException {
        return (S)findService(type, args).orElseThrow(
                () -> new ServiceNotAvailableException(type, args));
    }

}
