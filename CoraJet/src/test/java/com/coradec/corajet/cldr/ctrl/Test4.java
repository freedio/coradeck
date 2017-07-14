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

package com.coradec.corajet.cldr.ctrl;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;

import com.coradec.coracore.annotation.Inject;
import com.coradec.coracore.model.Factory;
import com.coradec.corajet.cldr.data.GenericInterface;
import com.coradec.corajet.cldr.data.MultiGenericInterface;
import com.coradec.corajet.cldr.data.SingletonInterface;
import com.coradec.corajet.cldr.data.TemplateInterface;

/**
 * ​​CarClassLoader and injector test with factory.
 */
@SuppressWarnings("ClassHasNoToStringMethod")
public class Test4 {

    @Inject
    private static Factory<SingletonInterface> SINGLETON_FACTORY;
    @Inject
    private static Factory<GenericInterface<Integer>> GENERIC_FACTORY;

    @Inject
    private Factory<TemplateInterface> templateFactory;
    @Inject
    private Factory<MultiGenericInterface<Integer, String>> multiGenericFactory;

    public static void main(String... args) {
        new Test4().launch();
    }

    private void launch() {
        // Phase 1:
        final SingletonInterface singleton = SINGLETON_FACTORY.get();
        final TemplateInterface template = templateFactory.get();
        final SingletonInterface anotherSingleton = SINGLETON_FACTORY.get();
        final TemplateInterface anotherTemplate = templateFactory.get();
        assertThat(singleton, is(not(nullValue())));
        assertThat(anotherSingleton, is(sameInstance(singleton)));
        assertThat(template, is(not(nullValue())));
        assertThat(anotherTemplate, is(not(nullValue())));
        assertThat(anotherTemplate, is(not(sameInstance(template))));

        // Phase 2:
        GenericInterface<Integer> random = GENERIC_FACTORY.get(Integer.class);
        final Integer value1 = random.value();
        final Integer value2 = random.value();
        assertThat(value1, is(not(equalTo(value2))));
    }

}
