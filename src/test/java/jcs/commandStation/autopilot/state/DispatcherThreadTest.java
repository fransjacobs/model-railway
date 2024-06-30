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

import jcs.commandStation.autopilot.AutoPilot;
import jcs.entities.BlockBean;
import jcs.entities.LocomotiveBean;
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

  public DispatcherThreadTest() {
    System.setProperty("persistenceService", "jcs.persistence.H2PersistenceService");
    testHelper = PersistenceTestHelper.getInstance();
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
  public void testStartStateMachine() {
    System.out.println("startStateMachine");
    DispatcherThread instance = dispatcher.getDispatcherThread();
    
    assertTrue(instance.isThreadRunning());
    assertFalse(instance.isLocomotiveAutomodeOn());
    assertTrue(instance.isAlive());
    
    assertEquals("DT->NS DHG 6505", instance.getName());
    assertEquals("IdleState", instance.getDispatcherStateName());
    
    instance.startStateMachine();
    Logger.debug("Statemachine Started");
    assertTrue(instance.isThreadRunning());
    assertTrue(instance.isLocomotiveAutomodeOn());
    assertTrue(instance.isAlive());
  }

  @Test
  public void testStopStateMachine() {
    System.out.println("stopStateMachine");
    DispatcherThread instance = dispatcher.getDispatcherThread();
    
    assertTrue(instance.isThreadRunning());
    assertFalse(instance.isLocomotiveAutomodeOn());
    assertTrue(instance.isAlive());
    
    assertEquals("DT->NS DHG 6505", instance.getName());
    assertEquals("IdleState", instance.getDispatcherStateName());

    instance.startStateMachine();
    Logger.debug("Statemachine Started");
    assertTrue(instance.isThreadRunning());
    assertTrue(instance.isLocomotiveAutomodeOn());
    assertTrue(instance.isAlive());
    assertEquals("IdleState", instance.getDispatcherStateName());

    instance.stopStateMachine();
    Logger.debug("Statemachine Stopped");
    assertFalse(instance.isLocomotiveAutomodeOn());

    assertFalse(instance.isThreadRunning());
    assertTrue(instance.isAlive());
  }
  
  @Test
  public void testStopLocomotiveAutomode() {
    System.out.println("stopLocomotiveAutomode");
    DispatcherThread instance = dispatcher.getDispatcherThread();
    
    assertTrue(instance.isThreadRunning());
    assertFalse(instance.isLocomotiveAutomodeOn());
    assertTrue(instance.isAlive());
    
    assertEquals("DT->NS DHG 6505", instance.getName());
    assertEquals("IdleState", instance.getDispatcherStateName());

    instance.startStateMachine();
    Logger.debug("Statemachine Started");
    assertTrue(instance.isThreadRunning());
    assertTrue(instance.isLocomotiveAutomodeOn());
    assertTrue(instance.isAlive());
    assertEquals("IdleState", instance.getDispatcherStateName());

    pause(250);
    assertEquals("StartState", instance.getDispatcherStateName());
    
    instance.stopLocomotiveAutomode();
    Logger.debug("Requested to stop Automode");
    assertFalse(instance.isLocomotiveAutomodeOn());
    assertTrue(instance.isThreadRunning());
    
    assertEquals("StartState", instance.getDispatcherStateName());
    pause(250);
    assertEquals("StartState", instance.getDispatcherStateName());
    //State still stays in startstate, so need to iterate through the states
    //this is part of other test
    //for now stop all
    instance.stopStateMachine();
    
    assertFalse(instance.isLocomotiveAutomodeOn());
    assertFalse(instance.isThreadRunning());
    
    
  }
  
  
  
  
  
  //@Test
  public void testIsThreadRunning() {
    System.out.println("isThreadRunning");
    DispatcherThread instance = null;
    boolean expResult = false;
    boolean result = instance.isThreadRunning();
    assertEquals(expResult, result);
    fail("The test case is a prototype.");
  }

  //@Test
  public void testIsStopRequested() {
    System.out.println("isStopRequested");
    DispatcherThread instance = null;
    boolean expResult = false;
    boolean result = instance.isLocomotiveAutomodeOn();
    assertEquals(expResult, result);
    fail("The test case is a prototype.");
  }

  //@Test
  public void testRequestStart() {
    System.out.println("requestStart");
    DispatcherThread instance = null;
    //instance.requestStart();
    fail("The test case is a prototype.");
  }

  //@Test
  public void testRequestStop() {
    System.out.println("requestStop");
    DispatcherThread instance = null;
    //instance.requestStop();
    fail("The test case is a prototype.");
  }

  //@Test
  public void testStopThread() {
    System.out.println("stopThread");
    DispatcherThread instance = null;
    //instance.stopThread();
    fail("The test case is a prototype.");
  }

  //@Test
  public void testForceStop() {
    System.out.println("forceStop");
    DispatcherThread instance = null;
    //instance.forceStop();
    fail("The test case is a prototype.");
  }

  //@Test
  public void testGetDispatcherState() {
    System.out.println("getDispatcherState");
    DispatcherThread instance = null;
    DispatcherState expResult = null;
    DispatcherState result = instance.getDispatcherState();
    assertEquals(expResult, result);
    fail("The test case is a prototype.");
  }

  //@Test
  public void testRun() {
    System.out.println("run");
    DispatcherThread instance = null;
    instance.run();
    fail("The test case is a prototype.");
  }

  
  
  private void pause(int millis) {
    try {
      Thread.sleep(millis);
    } catch (InterruptedException e) {
      Logger.error(e);
    }
  }
}
