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

import com.coradec.coracore.annotation.Inject;
import com.coradec.coragui.model.Frame;
import com.coradec.corajet.test.CoradeckJUnit4TestRunner;
import com.coradec.corasession.model.Session;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.net.URL;

@RunWith(CoradeckJUnit4TestRunner.class)
public class SwingGUITest {

    @Inject private Session session;
    private SwingGUI testee;

    @Test public void testSwingGUI() throws IOException {
        final URL gui = getClass().getClassLoader().getResource("SwingTestGUI.gui");
        if (gui == null) throw new NullPointerException("GUI definition file not found!");
        SwingGuiModel model = SwingGuiModel.from(gui);
        testee = model.getGUI();

        Frame frame = testee.getComponent(session, Frame.class, "main-frame");

        frame.setVisible(true);
        assertThat(frame.getTop(), is(50));
        assertThat(frame.getLeft(), is(100));
        assertThat(frame.getGauge(), is(new SwingGauge(600, 800)));
        assertThat(frame.isVisible(), is(true));

        frame.setVisible(false);
        assertThat(frame.isVisible(), is(false));
    }

}
