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

import static java.util.concurrent.TimeUnit.*;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;

import com.coradec.coracom.model.Message;
import com.coradec.coracom.model.Recipient;
import com.coradec.coracom.model.Request;
import com.coradec.coracom.trouble.RequestFailedException;
import com.coradec.coracore.model.State;
import com.coradec.coracore.util.StringUtil;
import com.coradec.coractrl.com.ExecuteStateTransitionRequest;
import com.coradec.coractrl.com.StartStateMachineRequest;
import com.coradec.coractrl.ctrl.Trajectory;
import com.coradec.coractrl.model.StateTransition;
import com.coradec.coractrl.model.impl.AbstractStateTransition;
import com.coradec.coractrl.trouble.StateMachineStalledException;
import com.coradec.corajet.cldr.Syslog;
import com.coradec.corajet.test.CoradeckJUnit4TestRunner;
import com.coradec.coralog.ctrl.impl.InternalLogger;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

@RunWith(CoradeckJUnit4TestRunner.class)
public class BasicStateMachineTest extends InternalLogger implements Recipient {

    private final BasicStateMachine testee = new BasicStateMachine(this);

    @Test public void directTrajectoryShouldYield1() {
        info("directTrajectoryShouldYield1");
        testee.initialize(TestState.A);
        testee.addTransitions(Collections.singletonList(new TAD()));
        testee.setTargetState(TestState.D);
        final Set<Trajectory> trajectories = testee.getTrajectories();
        assertThat(trajectories, is(not(nullValue())));
        assertThat(trajectories.size(), is(1));
    }

    @Test public void directTrajectoryShouldExecute() throws InterruptedException {
        info("directTrajectoryShouldExecute");
        testee.initialize(TestState.A);
        testee.addTransitions(Collections.singletonList(new TAD()));
        testee.setTargetState(TestState.D);
        testee.start().standby(1, SECONDS);
        assertThat(testee.getCurrentState(), is(equalTo(TestState.D)));
    }

    @Test public void simpleContinuousSeriesShouldYield1() {
        info("simpleContinuousSeriesShouldYield1");
        testee.initialize(TestState.A);
        testee.addTransitions(Arrays.asList(new TAB(), new TBC(), new TCD()));
        testee.setTargetState(TestState.D);
        final Set<Trajectory> trajectories = testee.getTrajectories();
        assertThat(trajectories, is(not(nullValue())));
        assertThat(trajectories.size(), is(1));
    }

    @Test public void simpleContinuousTrajectoryShouldExecute() throws InterruptedException {
        info("simpleContinuousTrajectoryShouldExecute");
        testee.initialize(TestState.A);
        testee.addTransitions(Arrays.asList(new TAB(), new TBC(), new TCD()));
        testee.setTargetState(TestState.D);
        testee.start().standby(1, SECONDS);
        assertThat(testee.getCurrentState(), is(equalTo(TestState.D)));
    }

    @Test public void simpleContinuousSeriesWithDuplicatesShouldYield1() {
        info("simpleContinuousSeriesWithDuplicatesShouldYield1");
        testee.initialize(TestState.A);
        testee.addTransitions(Arrays.asList(new TAB(), new TBC(), new TBC2(), new TCD()));
        testee.setTargetState(TestState.D);
        final Set<Trajectory> trajectories = testee.getTrajectories();
        assertThat(trajectories, is(not(nullValue())));
        assertThat(trajectories.size(), is(1));
    }

    @Test public void simpleContinuousTrajectoryWithDuplicatesShouldExecute()
            throws InterruptedException {
        info("simpleContinuousTrajectoryWithDuplicatesShouldExecute");
        testee.initialize(TestState.A);
        testee.addTransitions(Arrays.asList(new TAB(), new TBC(), new TBC2(), new TCD()));
        testee.setTargetState(TestState.D);
        testee.start().standby(1, SECONDS);
        assertThat(testee.getCurrentState(), is(equalTo(TestState.D)));
    }

    @Test public void simpleNoncontinousSeriesShouldYield0() {
        info("simpleNoncontinousSeriesShouldYield0");
        testee.initialize(TestState.A);
        testee.addTransitions(Arrays.asList(new TAB(), new TBC(), new TDE()));
        testee.setTargetState(TestState.E);
        final Set<Trajectory> trajectories = testee.getTrajectories();
        assertThat(trajectories, is(not(nullValue())));
        assertThat(trajectories.size(), is(0));
    }

    @Test public void simpleNonContinuousShouldStall() throws InterruptedException {
        info("simpleNonContinuousShouldStall");
        testee.initialize(TestState.A);
        testee.addTransitions(Arrays.asList(new TAB(), new TBC(), new TDE()));
        testee.setTargetState(TestState.D);
        try {
            testee.start().standby(1, SECONDS);
            Assert.fail("Expected RequestFailedException due to StateMachineStalledException");
        } catch (RequestFailedException e) {
            // expected that
            assertThat(e.getCause(), is(instanceOf(StateMachineStalledException.class)));
        }
    }

    @Test public void simpleContinuousSeriesNotContainingTargetStateShouldYield0() {
        info("simpleContinuousSeriesNotContainingTargetStateShouldYield0");
        testee.initialize(TestState.A);
        testee.addTransitions(Arrays.asList(new TAB(), new TBC(), new TCD()));
        testee.setTargetState(TestState.E);
        final Set<Trajectory> trajectories = testee.getTrajectories();
        assertThat(trajectories, is(not(nullValue())));
        assertThat(trajectories.size(), is(0));
    }

    @Test public void simpleContinuousSeriesNotContainingTargetStateShouldStall()
            throws InterruptedException {
        info("simpleContinuousSeriesNotContainingTargetStateShouldStall");
        testee.initialize(TestState.A);
        testee.addTransitions(Arrays.asList(new TAB(), new TBC(), new TCD()));
        testee.setTargetState(TestState.E);
        try {
            testee.start().standby(1, SECONDS);
            Assert.fail("Expected RequestFailedException due to StateMachineStalledException");
        } catch (RequestFailedException e) {
            // expected that
            assertThat(e.getCause(), is(instanceOf(StateMachineStalledException.class)));
        }
    }

    @Test public void simpleContinuousSeriesNotContainingInitialStateShouldYield0() {
        info("simpleContinuousSeriesNotContainingInitialStateShouldYield0");
        testee.initialize(TestState.A);
        testee.addTransitions(Arrays.asList(new TBC(), new TDE()));
        testee.setTargetState(TestState.E);
        final Set<Trajectory> trajectories = testee.getTrajectories();
        assertThat(trajectories, is(not(nullValue())));
        assertThat(trajectories.size(), is(0));
    }

    @Test public void simpleContinuousSeriesNotContainingInitialStateShouldStall()
            throws InterruptedException {
        info("simpleContinuousSeriesNotContainingInitialStateShouldStall");
        testee.initialize(TestState.A);
        testee.addTransitions(Arrays.asList(new TBC(), new TDE()));
        testee.setTargetState(TestState.E);
        try {
            testee.start().standby(1, SECONDS);
            Assert.fail("Expected RequestFailedException due to StateMachineStalledException");
        } catch (RequestFailedException e) {
            // expected that
            assertThat(e.getCause(), is(instanceOf(StateMachineStalledException.class)));
        }
    }

    @Test public void simpleCyclicSeriesShouldYield1Straight() {
        info("simpleCyclicSeriesShouldYield1Straight");
        testee.initialize(TestState.A);
        testee.addTransitions(Arrays.asList(new TAB(), new TBC(), new TCA(), new TCD()));
        testee.setTargetState(TestState.D);
        final Set<Trajectory> trajectories = testee.getTrajectories();
        assertThat(trajectories, is(not(nullValue())));
        assertThat(trajectories.size(), is(1));
        assertThat(new ArrayList<>(trajectories).get(0).getTransitions().size(), is(3));
    }

    @Test public void simpleCyclicSeriesShouldExecute() throws InterruptedException {
        info("simpleCyclicSeriesShouldExecute");
        testee.initialize(TestState.A);
        testee.addTransitions(Arrays.asList(new TAB(), new TBC(), new TCA(), new TCD()));
        testee.setTargetState(TestState.D);
        testee.start().standby(1, SECONDS);
        assertThat(testee.getCurrentState(), is(equalTo(TestState.D)));
    }

    @Test public void branchedContinuousSeriesShouldYieldMultiple() {
        info("branchedContinuousSeriesShouldYieldMultiple");
        testee.initialize(TestState.A);
        testee.addTransitions(Arrays.asList(new TAB(), new TBC(), new TAC(), new TCD()));
        testee.setTargetState(TestState.D);
        final Set<Trajectory> trajectories = testee.getTrajectories();
        assertThat(trajectories, is(not(nullValue())));
        assertThat(trajectories.size(), is(2));
    }

    @Test public void branchedContinuousSeriesShouldTakeShortcut() throws InterruptedException {
        info("branchedContinuousSeriesShouldTakeShortcut");
        testee.initialize(TestState.A);
        testee.addTransitions(Arrays.asList(new TAB(), new TBC(), new TAC(), new TCD()));
        testee.setTargetState(TestState.D);
        final StartStateMachineRequest start = testee.start();
        start.standby(1, SECONDS);
        assertThat(testee.getCurrentState(), is(equalTo(TestState.D)));
        assertThat(start.getPassedStates().size(), is(3));
    }

    @Test public void multibranchedContinuousSeriesWithShortcutShouldYieldMultiple() {
        info("multibranchedContinuousSeriesWithShortcutShouldYieldMultiple");
        testee.initialize(TestState.A);
        testee.addTransitions(
                Arrays.asList(new TAB(), new TBC(), new TBD(), new TCD(), new TAD(), new TDE()));
        testee.setTargetState(TestState.E);
        final Set<Trajectory> trajectories = testee.getTrajectories();
        assertThat(trajectories, is(not(nullValue())));
        assertThat(trajectories.size(), is(3));
    }

    @Test public void multibranchedContinuousSeriesWithShortcutShouldTakeShortcut()
            throws InterruptedException {
        info("multibranchedContinuousSeriesWithShortcutShouldTakeShortcut");
        testee.initialize(TestState.A);
        testee.addTransitions(
                Arrays.asList(new TAB(), new TBC(), new TBD(), new TCD(), new TAD(), new TDE()));
        testee.setTargetState(TestState.E);
        final StartStateMachineRequest start = testee.start();
        start.standby(1, SECONDS);
        assertThat(testee.getCurrentState(), is(equalTo(TestState.E)));
        assertThat(start.getPassedStates().size(), is(3));
    }

    @Test public void multibranchedContinuousSeries2WithShortcutShouldYieldMultiple() {
        info("multibranchedContinuousSeries2WithShortcutShouldYieldMultiple");
        testee.initialize(TestState.A);
        testee.addTransitions(
                Arrays.asList(new TAB(), new TBC(), new TBE(), new TCD(), new TAE(), new TDE()));
        testee.setTargetState(TestState.E);
        final Set<Trajectory> trajectories = testee.getTrajectories();
        assertThat(trajectories, is(not(nullValue())));
        assertThat(trajectories.size(), is(3));
    }

    @Test public void multibranchedContinuousSeries2WithShortcutShouldTakeShortcut()
            throws InterruptedException {
        info("multibranchedContinuousSeries2WithShortcutShouldTakeShortcut");
        testee.initialize(TestState.A);
        testee.addTransitions(
                Arrays.asList(new TAB(), new TBC(), new TBE(), new TCD(), new TAE(), new TDE()));
        testee.setTargetState(TestState.E);
        final StartStateMachineRequest start = testee.start();
        start.standby(1, SECONDS);
        assertThat(testee.getCurrentState(), is(equalTo(TestState.E)));
        assertThat(start.getPassedStates().size(), is(2));
    }

    @Test public void interruptionShouldSwitchGracefullyIfContinuous() throws InterruptedException {
        info("interruptionShouldSwitchGracefullyIfContinuous");
        testee.initialize(TestState.A);
        testee.addTransitions(Arrays.asList(new TAB(), new TBC(), new TCD(), new TDE(), new TCA()));
        testee.setTargetState(TestState.E);
        final StartStateMachineRequest start = testee.start();
        testee.onState(TestState.B, () -> {
            info("Triggered!");
            testee.setTargetState(TestState.A);
        });
        start.standby();
        assertThat(testee.getCurrentState(), is(equalTo(TestState.A)));
        assertThat(start.getPassedStates().size(), is(4));
    }

    @Override public void onMessage(final Message message) {
        if (message instanceof ExecuteStateTransitionRequest) {
            ExecuteStateTransitionRequest estr = (ExecuteStateTransitionRequest)message;
            StateTransition transition = estr.getTransition();
            if (transition instanceof TestTransition) try {
                transition.execute();
                estr.succeed();
            } catch (Exception e) {
                estr.fail(e);
            }
            else
                Syslog.error("Received request to execute foreign state transition %s", transition);
        } else Syslog.error("Received message %s", StringUtil.toString(message));
    }

    enum TestState implements State { // @formatter:off
        A, B, C, D, E, F, G
    } // @formatter:on

    private class TestTransition extends AbstractStateTransition {

        TestTransition(final TestState from, final TestState to) {
            super(from, to);
        }

        @Override public Optional<Request> execute() {
            Syslog.info("Transition from %s to %s", getInitialState(), getTerminalState());
            return Optional.empty();
        }

        @Override public int getOrder() {
            // Prefer shortcuts:
            return getInitialState().ordinal() - getTerminalState().ordinal();
        }
    }

    private class TAB extends TestTransition {

        protected TAB() {
            super(TestState.A, TestState.B);
        }

    }

    private class TBC extends TestTransition {

        protected TBC() {
            super(TestState.B, TestState.C);
        }

    }

    private class TBC2 extends TestTransition {

        protected TBC2() {
            super(TestState.B, TestState.C);
        }

    }

    private class TCD extends TestTransition {

        protected TCD() {
            super(TestState.C, TestState.D);
        }

    }

    private class TDE extends TestTransition {

        protected TDE() {
            super(TestState.D, TestState.E);
        }

    }

    private class TCA extends TestTransition {

        protected TCA() {
            super(TestState.C, TestState.A);
        }

    }

    private class TDC extends TestTransition {

        protected TDC() {
            super(TestState.D, TestState.C);
        }

    }

    private class TAC extends TestTransition {

        protected TAC() {
            super(TestState.A, TestState.C);
        }

    }

    private class TAD extends TestTransition {

        protected TAD() {
            super(TestState.A, TestState.D);
        }

    }

    private class TBD extends TestTransition {

        protected TBD() {
            super(TestState.B, TestState.D);
        }

    }

    private class TBE extends TestTransition {

        protected TBE() {
            super(TestState.B, TestState.E);
        }

    }

    private class TAE extends TestTransition {

        protected TAE() {
            super(TestState.A, TestState.E);
        }

    }

}
