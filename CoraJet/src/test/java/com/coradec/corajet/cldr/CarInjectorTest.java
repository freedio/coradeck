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

package com.coradec.corajet.cldr;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;

import com.coradec.coracore.annotation.Inject;
import com.coradec.corajet.cldr.model.ClassArgObject;
import com.coradec.corajet.cldr.model.ImplArgObject;
import com.coradec.corajet.cldr.model.InterfaceArgFactoryMethodObject;
import com.coradec.corajet.cldr.model.NoArgFactoryMethodObject;
import com.coradec.corajet.cldr.model.NoArgObject;
import com.coradec.corajet.cldr.model.impl.BasicClassArgObject;
import com.coradec.corajet.cldr.model.impl.BasicImplArgObject;
import com.coradec.corajet.cldr.model.impl.BasicNoArgObject;
import com.coradec.corajet.cldr.model.impl.SpecialInterfaceArgFactoryMethodObject;
import com.coradec.corajet.cldr.model.impl.SpecialNoArgFactoryMethodObject;
import com.coradec.corajet.test.CoradeckJUnit4TestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * ​​Test suite for the CarInjector.
 */
@RunWith(CoradeckJUnit4TestRunner.class)
public class CarInjectorTest {

    @Inject private NoArgObject noArgObject;
    @Inject private ImplArgObject implArgObject;
    @Inject private ClassArgObject classArgObject;
    @Inject private NoArgFactoryMethodObject noArgFactoryMethodObject;
    @Inject private InterfaceArgFactoryMethodObject interfaceArgFactoryMethodObject;

    @Test public void testConstructorInjection() {
        assertThat(noArgObject, is(instanceOf(BasicNoArgObject.class)));
        assertThat(implArgObject, is(instanceOf(BasicImplArgObject.class)));
        assertThat(classArgObject, is(instanceOf(BasicClassArgObject.class)));
        assertThat(noArgFactoryMethodObject, is(instanceOf(SpecialNoArgFactoryMethodObject.class)));
        assertThat(interfaceArgFactoryMethodObject,
                is(instanceOf(SpecialInterfaceArgFactoryMethodObject.class)));
    }

}
