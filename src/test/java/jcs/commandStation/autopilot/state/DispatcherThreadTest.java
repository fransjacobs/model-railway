/*
 * Copyright 2024 Frans Jacobs.
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import jcs.JCS;
import jcs.commandStation.FeedbackController;
import jcs.commandStation.autopilot.AutoPilot;
import jcs.commandStation.events.SensorEvent;
import jcs.entities.BlockBean;
import jcs.entities.LocomotiveBean;
import jcs.entities.SensorBean;
import jcs.persistence.PersistenceFactory;
import jcs.persistence.PersistenceService;
import jcs.persistence.util.PersistenceTestHelper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.tinylog.Logger;

public class DispatcherThreadTest {

  private static final long NS_DHG_6505 = 7L;
  private static final long BR_101_003_2 = 23L;
  private static final long NS_1631 = 39L;

  private final PersistenceTestHelper testHelper;
  private AutoPilot autoPilot;
  private PersistenceService db;
  private LocomotiveBean dhg;
  private Dispatcher dispatcher;

  private final ExecutorService executor;

  public DispatcherThreadTest() {
    System.setProperty("persistenceService", "jcs.persistence.H2PersistenceService");
    testHelper = PersistenceTestHelper.getInstance();
    this.executor = Executors.newSingleThreadExecutor();
  }

  @BeforeEach
  public void setUp() {
    testHelper.runTestDataInsertScript("autopilot_test_layout.sql");
    db = PersistenceFactory.getService();
    dhg = PersistenceFactory.getService().getLocomotive(NS_DHG_6505);

    BlockBean block1 = PersistenceFactory.getService().getBlockByTileId("bk-1");
    block1.setLocomotive(dhg);
    db.persist(block1);

    autoPilot = AutoPilot.getInstance();
    autoPilot.startAutoMode();
    autoPilot.prepareDispatchers();
    dispatcher = autoPilot.getLocomotiveDispatcher(dhg);
  }

  @AfterEach
  public void tearDown() {
  }

  @Test
  public void testStateMachineHappyFlow() {
    System.out.println("stateMachineHappyFlow");
    DispatcherThread instance = dispatcher.getDispatcherThread();
    //force a route
    BlockBean block4 = db.getBlockByTileId("bk-4");
    block4.setBlockState(BlockBean.BlockState.OUT_OF_ORDER);
    db.persist(block4);

    assertFalse(instance.isThreadRunning());
    assertFalse(instance.isLocomotiveAutomodeOn());
    assertFalse(instance.isAlive());

    assertEquals("IdleState", instance.getDispatcherStateName());

    instance.handleStates(false);

    assertEquals("IdleState", instance.getDispatcherStateName());

    instance.setLocomotiveAutomode(true);

    assertTrue(instance.isLocomotiveAutomodeOn());

    instance.handleStates(false);

    assertEquals("PrepareRouteState", instance.getDispatcherStateName());

    instance.handleStates(false);

    assertEquals("StartState", instance.getDispatcherStateName());

    instance.handleStates(false);

    assertEquals("StartState", instance.getDispatcherStateName());

    SensorBean s11 = db.getSensor("0-0011");
    toggleSensorBackground(s11);
    pause(200);

    instance.handleStates(false);

    assertEquals("EnterBlockState", instance.getDispatcherStateName());

    instance.handleStates(false);
    assertEquals("EnterBlockState", instance.getDispatcherStateName());

    SensorBean s10 = db.getSensor("0-0010");
    toggleSensorBackground(s10);
    pause(100);

    instance.handleStates(false);

    assertEquals("InBlockState", instance.getDispatcherStateName());

    instance.handleStates(false);
    assertEquals("WaitState", instance.getDispatcherStateName());
  }

  @Test
  public void testStartStopThreadRunning() {
    System.out.println("startStopThreadRunning");
    DispatcherThread instance = dispatcher.getDispatcherThread();

    assertFalse(instance.isThreadRunning());
    assertFalse(instance.isAlive());

    assertEquals("DT->NS DHG 6505", instance.getName());
    assertEquals("IdleState", instance.getDispatcherStateName());

    instance.start();
    pause(10);

    Logger.debug("Dispatcher Thread Started");
    assertTrue(instance.isThreadRunning());
    assertTrue(instance.isAlive());
    assertFalse(instance.isLocomotiveAutomodeOn());

    instance.stopRunningThread();
    Logger.debug("Dispatcher Thread Stopped");
    assertFalse(instance.isThreadRunning());
    assertTrue(instance.isAlive());
  }

  @Test
  public void testStartStopLocomotiveAutomode() {
    System.out.println("startStopLocomotiveAutomode");
    DispatcherThread instance = dispatcher.getDispatcherThread();

    //force a route
    BlockBean block3 = db.getBlockByTileId("bk-3");
    block3.setBlockState(BlockBean.BlockState.OUT_OF_ORDER);
    db.persist(block3);

    assertFalse(instance.isThreadRunning());
    assertFalse(instance.isLocomotiveAutomodeOn());
    assertEquals("IdleState", instance.getDispatcherStateName());
    instance.handleStates(false);
    assertEquals("IdleState", instance.getDispatcherStateName());

    instance.setLocomotiveAutomode(true);
    assertTrue(instance.isLocomotiveAutomodeOn());
    instance.handleStates(false);
    assertEquals("PrepareRouteState", instance.getDispatcherStateName());
    instance.handleStates(false);
    assertEquals("StartState", instance.getDispatcherStateName());
    instance.handleStates(false);
    assertEquals("StartState", instance.getDispatcherStateName());

    SensorBean s13 = db.getSensor("0-0013");
    toggleSensorDirect(s13);
    instance.handleStates(false);
    assertEquals("EnterBlockState", instance.getDispatcherStateName());
    instance.handleStates(false);
    assertEquals("EnterBlockState", instance.getDispatcherStateName());

    SensorBean s12 = db.getSensor("0-0012");
    toggleSensorDirect(s12);
    instance.handleStates(false);
    assertEquals("InBlockState", instance.getDispatcherStateName());
    instance.handleStates(false);
    assertEquals("WaitState", instance.getDispatcherStateName());

    instance.setLocomotiveAutomode(false);
    assertFalse(instance.isLocomotiveAutomodeOn());

    instance.handleStates(false);
    assertEquals("IdleState", instance.getDispatcherStateName());
  }

  @Test
  public void testStartStopLocomotiveAutomode2() {
    System.out.println("startStopLocomotiveAutomode2");
    DispatcherThread instance = dispatcher.getDispatcherThread();

    //force  routes
    BlockBean block2 = db.getBlockByTileId("bk-2");
    block2.setBlockState(BlockBean.BlockState.OUT_OF_ORDER);
    db.persist(block2);
    
    BlockBean block3 = db.getBlockByTileId("bk-3");
    block3.setBlockState(BlockBean.BlockState.OUT_OF_ORDER);
    db.persist(block3);
    
    assertFalse(instance.isThreadRunning());
    assertFalse(instance.isLocomotiveAutomodeOn());
    assertEquals("IdleState", instance.getDispatcherStateName());
    instance.handleStates(false);
    assertEquals("IdleState", instance.getDispatcherStateName());

    instance.setLocomotiveAutomode(true);
    assertTrue(instance.isLocomotiveAutomodeOn());
    instance.handleStates(false);
    assertEquals("PrepareRouteState", instance.getDispatcherStateName());
    instance.handleStates(false);
    assertEquals("StartState", instance.getDispatcherStateName());
    instance.handleStates(false);
    assertEquals("StartState", instance.getDispatcherStateName());

    SensorBean s13 = db.getSensor("0-0013");
    toggleSensorDirect(s13);
    instance.handleStates(false);
    assertEquals("EnterBlockState", instance.getDispatcherStateName());
    instance.handleStates(false);
    assertEquals("EnterBlockState", instance.getDispatcherStateName());

    SensorBean s12 = db.getSensor("0-0012");
    toggleSensorDirect(s12);
    instance.handleStates(false);
    assertEquals("InBlockState", instance.getDispatcherStateName());
    instance.handleStates(false);
    assertEquals("WaitState", instance.getDispatcherStateName());

    instance.handleStates(false);
    
    assertEquals("PrepareRouteState", instance.getDispatcherStateName());
    instance.handleStates(false);
    assertEquals("StartState", instance.getDispatcherStateName());
    instance.handleStates(false);
    assertEquals("StartState", instance.getDispatcherStateName());
    
    SensorBean s2 = db.getSensor("0-0002");
    toggleSensorDirect(s2);
    instance.handleStates(false);
    assertEquals("EnterBlockState", instance.getDispatcherStateName());
    instance.handleStates(false);
    assertEquals("EnterBlockState", instance.getDispatcherStateName());

    SensorBean s1 = db.getSensor("0-0001");
    toggleSensorDirect(s1);
    instance.handleStates(false);
    assertEquals("InBlockState", instance.getDispatcherStateName());
    instance.handleStates(false);
    assertEquals("WaitState", instance.getDispatcherStateName());

    instance.setLocomotiveAutomode(false);
    assertFalse(instance.isLocomotiveAutomodeOn());

    instance.handleStates(false);
    assertEquals("IdleState", instance.getDispatcherStateName());
  }

  private void toggleSensorDirect(SensorBean sensorBean) {
    sensorBean.toggle();
    sensorBean.setActive((sensorBean.getStatus() == 1));
    SensorEvent sensorEvent = new SensorEvent(sensorBean);
    fireFeedbackEvent(sensorEvent);
  }

  private void toggleSensorBackground(SensorBean sensorBean) {
    sensorBean.toggle();
    sensorBean.setActive((sensorBean.getStatus() == 1));
    SensorEvent sensorEvent = new SensorEvent(sensorBean);
    this.executor.execute(() -> fireFeedbackEvent(sensorEvent));
  }

  private void fireFeedbackEvent(SensorEvent sensorEvent) {
    List<FeedbackController> acl = JCS.getJcsCommandStation().getFeedbackControllers();
    for (FeedbackController fbc : acl) {
      fbc.fireSensorEventListeners(sensorEvent);
    }
  }

  private void pause(int millis) {
    try {
      Thread.sleep(millis);
    } catch (InterruptedException e) {
      Logger.error(e);
    }
  }
}
