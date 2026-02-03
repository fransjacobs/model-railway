/*
 * Copyright 2025 Frans Jacobs.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jcs.commandStation.autopilot.state;

import java.util.List;
import jcs.JCS;
import jcs.commandStation.FeedbackController;
import jcs.commandStation.autopilot.AutoPilot;
import jcs.commandStation.events.SensorEvent;
import jcs.entities.BlockBean;
import jcs.entities.LocomotiveBean;
import jcs.entities.RouteBean;
import jcs.entities.SensorBean;
import jcs.persistence.PersistenceFactory;
import jcs.persistence.PersistenceService;
import jcs.persistence.util.PersistenceTestHelper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.TestMethodOrder;
import org.tinylog.Logger;

@TestMethodOrder(OrderAnnotation.class)
public abstract class AbstractStateMachineStepByStepTest {

  protected static final Long NS_DHG_6505 = 7L;
  protected static final Long BR_101_003_2 = 23L;
  protected static final Long NS_1631 = 39L;

  protected final PersistenceTestHelper testHelper;
  protected final PersistenceService ps;
  protected LocomotiveBean dhg;
  protected LocomotiveBean ns1631;
  protected Dispatcher dispatcher;

  public AbstractStateMachineStepByStepTest() {
    System.setProperty("persistenceService", "jcs.persistence.TestH2PersistenceService");
    //Switch the Virtual Simulator OFF as it will interfeare with this step test
    System.setProperty("do.not.simulate.virtual.drive", "true");
    System.setProperty("state.machine.stepTest", "true");

    testHelper = PersistenceTestHelper.getInstance();
    testHelper.runTestDataInsertScript("autopilot_test_layout.sql");

    ps = PersistenceFactory.getService();
  }

  @BeforeEach
  public void setUp() {
    //Reset the layout...
    for (BlockBean block : ps.getBlocks()) {
      block.setLocomotive(null);
      block.setBlockState(BlockBean.BlockState.FREE);
      block.setArrivalSuffix(null);
      ps.persist(block);
    }

    for (RouteBean route : ps.getRoutes()) {
      route.setLocked(false);
      ps.persist(route);
    }
    if (JCS.getJcsCommandStation().connect()) {

      JCS.getJcsCommandStation().switchPower(true);
      AutoPilot.runAutoPilot(true);
      Logger.info("=========================== setUp done..............");
    } else {
      Logger.error("###### Can't connect to command station! ########");
    }
  }

  @AfterEach
  public void tearDown() {
    Logger.info("=========================== Teardown..............");
    AutoPilot.runAutoPilot(false);
    long now = System.currentTimeMillis();
    long start = now;
    long timeout = now + 10000;
    boolean autoPilotRunning = AutoPilot.isAutoModeActive();
    while (autoPilotRunning && timeout > now) {
      pause(1);
      autoPilotRunning = AutoPilot.isAutoModeActive();
      now = System.currentTimeMillis();
    }

    assertTrue(timeout > now);
    assertFalse(AutoPilot.isAutoModeActive());

    Logger.debug("Autopilot Automode stopped in " + (now - start) + " ms.");
    AutoPilot.clearDispatchers();
  }

  protected void toggleSensorDirect(SensorBean sensorBean) {
    sensorBean.toggle();
    sensorBean.setActive((sensorBean.getStatus() == 1));
    SensorEvent sensorEvent = new SensorEvent(sensorBean);
    fireFeedbackEvent(sensorEvent);
  }

  protected void fireFeedbackEvent(SensorEvent sensorEvent) {
    List<FeedbackController> acl = JCS.getJcsCommandStation().getFeedbackControllers();
    for (FeedbackController fbc : acl) {
      fbc.fireAllSensorEventsListeners(sensorEvent);
    }
  }

  protected void pause(int millis) {
    try {
      Thread.sleep(millis);
    } catch (InterruptedException e) {
      Logger.error(e);
    }
  }

}
