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

package com.coradec.coragui.swing.model.impl;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;

import com.coradec.coracom.ctrl.MessageQueue;
import com.coradec.coracore.annotation.Inject;
import com.coradec.coradoc.model.impl.BasicXmlAttributes;
import com.coradec.corajet.test.CoradeckJUnit4TestRunner;
import com.coradec.corasession.model.Session;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.swing.*;

@RunWith(CoradeckJUnit4TestRunner.class)
public class SwingFrameTest {

    @Inject private static MessageQueue MQ;
    private static final BasicXmlAttributes ATTRIBUTES = new BasicXmlAttributes();

    static {
        ATTRIBUTES.add("id", "main");
    }

    private final SwingFrame testee = new SwingFrame(ATTRIBUTES);
    @Inject private Session session;

    @Test public void testEmptyFrame() throws InterruptedException {
        MQ.inject(testee.setLeft(80)
                        .and(testee.setTop(50))
                        .and(testee.setWidth(200))
                        .and(testee.setHeight(100))
                        .andThen(testee.setVisible(true))).standby();
        JFrame p = testee.getPeer();
        assertThat(p.getX(), is(80));
        assertThat(p.getY(), is(50));
        assertThat(p.getWidth(), is(200));
        assertThat(p.getHeight(), is(102));
        assertThat(p.isVisible(), is(true));
        testee.setVisible(false).standby();
        assertThat(p.isVisible(), is(false));

        testee.discard();
    }

}
