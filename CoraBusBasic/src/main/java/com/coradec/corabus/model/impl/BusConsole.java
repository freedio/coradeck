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

import static java.awt.BorderLayout.*;

import com.coradec.corabus.model.Bus;
import com.coradec.corabus.model.MachineService;
import com.coradec.coracom.ctrl.MessageQueue;
import com.coradec.coracom.model.Request;
import com.coradec.coracore.annotation.Inject;
import com.coradec.coracore.annotation.Nullable;
import com.coradec.corasession.model.Session;
import com.coradec.coratext.model.LocalizedText;
import com.coradec.coratext.model.Text;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * ​​The server console as a bus application.
 */
@SuppressWarnings({"ClassHasNoToStringMethod", "FieldCanBeLocal"})
public class BusConsole extends BasicBusProcess implements MachineService {

    private static final Text TEXT_SERVER_CONSOLE_TITLE =
            LocalizedText.define("ServerConsoleTitle");
    static final Text TEXT_SHUTDOWN_ACTION = LocalizedText.define("ShutdownActionName");
    static final Text TEXT_CLEAR_LOCKCOUNT_ACTION = LocalizedText.define("ClearShutdownLock");

    JFrame frame;
    Session initSession;
    @Inject Bus bus;
    @Inject MessageQueue mq;
    private JTextField lockCountField;
    private Action shutdownAction;
    private Action clearLockAction;

    @Override protected @Nullable Request onInitialize(final Session session) {
        initSession = session;
        debug("Initializing %s ...", TEXT_SERVER_CONSOLE_TITLE.resolve());
        final Request request = super.onInitialize(session);

        final JLabel titleLabel = new JLabel(TEXT_SERVER_CONSOLE_TITLE.resolve(), JLabel.CENTER);
        final JPanel title = new JPanel(new FlowLayout(FlowLayout.CENTER));
        title.add(titleLabel);

        final TreeNode treeRoot = buildTree();
        final JTree tree = new JTree(treeRoot);

        final JPanel info = new JPanel();

        JLabel lockCountLabel = new JLabel("Lock Count: ");
        lockCountField = new JTextField("0");
        lockCountField.setEnabled(false);

        final JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEADING));
        statusPanel.add(lockCountLabel);
        statusPanel.add(lockCountField);

        shutdownAction = new ShutdownAction();
        final JButton shutdownButton = new JButton(shutdownAction);
        clearLockAction = new ClearLockAction();
        final JButton clearLockButton = new JButton(clearLockAction);

        final JPanel commandPanel = new JPanel(new FlowLayout(FlowLayout.TRAILING));
        commandPanel.add(clearLockButton);
        commandPanel.add(shutdownButton);

        final Box control = new Box(BoxLayout.LINE_AXIS);
        control.add(statusPanel);
        control.add(Box.createGlue());
        control.add(commandPanel);

        final JPanel content = new JPanel(new BorderLayout(4, 4));

        frame = new JFrame(TEXT_SERVER_CONSOLE_TITLE.resolve());
        final Container contentPane = frame.getContentPane();
        contentPane.setLayout(new BorderLayout(4, 4));
        contentPane.add(title, PAGE_START);
        contentPane.add(tree, LINE_START);
        contentPane.add(info, LINE_END);
        contentPane.add(control, PAGE_END);
        contentPane.add(content, CENTER);
        frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
        return request;
    }

    @Override protected @Nullable Request onTerminate(final Session session) {
//        try {
        if (frame != null) /*SwingUtilities.invokeAndWait(() -> */ frame.dispose()/*)*/;
//        } catch (InterruptedException | InvocationTargetException e) {
//            e.printStackTrace();
//        }
        return super.onTerminate(session);
    }

    private TreeNode buildTree() {
        return new DefaultMutableTreeNode("", true);
    }

    @Override public void run() {
        while (!Thread.interrupted()) {
            try {
//                debug("Console: poll");
                Thread.sleep(1000);
                checkSuspend();
                final int count = mq.getShutdownLockCount();
                SwingUtilities.invokeLater(() -> {
                    lockCountField.setText(String.valueOf(count));
                    clearLockAction.setEnabled(count != 0);
                    shutdownAction.setEnabled(count == 0);
                });
            } catch (InterruptedException e) {
                debug("Console interrupted.");
                break;
            }
        }
        debug("Console terminated.");
    }

    private class ShutdownAction extends AbstractAction {

        ShutdownAction() {
            super(TEXT_SHUTDOWN_ACTION.resolve(), null);
        }

        @Override public void actionPerformed(final ActionEvent e) {
//            SwingUtilities.invokeLater(() -> {
                frame.dispose();
            frame = null;
            bus.shutdown(initSession);
//            });
        }
    }

    private class ClearLockAction extends AbstractAction {

        /**
         * Creates an {@code Action}.
         */
        ClearLockAction() {
            super(TEXT_CLEAR_LOCKCOUNT_ACTION.resolve(), null);
        }

        @Override public void actionPerformed(final ActionEvent e) {
            mq.clearShutdownLock();
        }
    }
}
