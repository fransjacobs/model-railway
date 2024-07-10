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
package jcs.commandStation.autopilot;

import java.util.ArrayList;
import java.util.List;
import jcs.commandStation.autopilot.state.Dispatcher;
import jcs.entities.BlockBean;
import jcs.entities.LocomotiveBean;
import jcs.persistence.PersistenceFactory;
import jcs.persistence.PersistenceService;
import jcs.persistence.util.PersistenceTestHelper;
import jcs.ui.layout.tiles.Tile;
import jcs.ui.layout.tiles.TileFactory;
import static org.junit.Assert.assertEquals;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.tinylog.Logger;

/**
 *
 * @author frans
 */
public class AutoPilotTest {

  private static final long NS_DHG_6505 = 7L;
  private static final long BR_101_003_2 = 23L;
  private static final long NS_1631 = 39L;

  private final PersistenceTestHelper testHelper;
  private PersistenceService db;

  public AutoPilotTest() {
    System.setProperty("persistenceService", "jcs.persistence.H2PersistenceService");
    testHelper = PersistenceTestHelper.getInstance();
  }

  @BeforeEach
  public void setUp() {
    testHelper.runTestDataInsertScript("autopilot_test_layout.sql");
    db = PersistenceFactory.getService();
  }

  @AfterEach
  public void tearDown() {
  }

  @Test
  public void testStartAutoMode() {
    System.out.println("startAutoMode");

    AutoPilot instance = AutoPilot.getInstance();
    assertFalse(instance.isAutoModeActive());

    instance.startAutoMode();
    Logger.debug("Started automode");

    assertTrue(instance.isAutoModeActive());
  }

  @Test
  public void testStopAutoMode() {
    System.out.println("stopAutoMode");
    AutoPilot instance = AutoPilot.getInstance();
    assertFalse(instance.isAutoModeActive());

    instance.startAutoMode();
    Logger.debug("Started automode");
    assertTrue(instance.isAutoModeActive());

    instance.stopAutoMode();
    assertFalse(instance.isAutoModeActive());
  }

  @Test
  public void testAreDispatchersRunning() {
    System.out.println("areDispatchersRunning");
    AutoPilot instance = AutoPilot.getInstance();
    assertFalse(instance.isAutoModeActive());
    instance.startAutoMode();
    assertTrue(instance.isAutoModeActive());

    assertFalse(instance.areDispatchersRunning());
  }

  @Test
  public void testGetOnTrackLocomotives() {
    System.out.println("getOnTrackLocomotives");
    //Check is the test scrip has run well
    List<Tile> tiles = TileFactory.toTiles(db.getTileBeans(), false, false);
    assertEquals(29, tiles.size());
    List<BlockBean> blocks = db.getBlocks();
    assertEquals(4, blocks.size());

    AutoPilot instance = AutoPilot.getInstance();
    List<LocomotiveBean> onTraclLocos = instance.getOnTrackLocomotives();
    assertTrue(onTraclLocos.isEmpty());

    //get a loc and put it in a block
    LocomotiveBean ns1631 = PersistenceFactory.getService().getLocomotive(NS_1631);
    BlockBean block1 = PersistenceFactory.getService().getBlockByTileId("bk-1");
    block1.setLocomotive(ns1631);
    db.persist(block1);

    List<LocomotiveBean> expected = new ArrayList<>();
    expected.add(ns1631);

    onTraclLocos = instance.getOnTrackLocomotives();
    assertEquals(expected, onTraclLocos);
  }

  //@Test
  public void testGetLocomotiveDispatchers() {
    System.out.println("getLocomotiveDispatchers");
    AutoPilot instance = AutoPilot.getInstance();

    List<LocomotiveBean> onTraclLocos = instance.getOnTrackLocomotives();
    assertTrue(onTraclLocos.isEmpty());
    //get a loc and put it in a block
    LocomotiveBean ns1631 = PersistenceFactory.getService().getLocomotive(NS_1631);
    BlockBean block1 = PersistenceFactory.getService().getBlockByTileId("bk-1");
    block1.setLocomotive(ns1631);
    db.persist(block1);

    List<LocomotiveBean> expected = new ArrayList<>();
    expected.add(ns1631);
    onTraclLocos = instance.getOnTrackLocomotives();
    assertEquals(expected, onTraclLocos);
    instance.startAutoMode();

    instance.prepareDispatchers();
    List<Dispatcher> dispList = instance.getLocomotiveDispatchers();
    assertEquals(1, dispList.size());

    assertTrue(instance.isAutoModeActive());
    assertTrue(instance.areDispatchersRunning());

    //This part is not yet working as the tread does not yet stop in the desired way...
//    instance.stopAutoMode();
//    assertFalse(instance.isAutoModeActive());
//    
//    assertFalse(instance.areDispatchersRunning());
  }

  //@Test
  public void testGetLocomotiveDispatcher() {
    System.out.println("getLocomotiveDispatcher");
    AutoPilot instance = AutoPilot.getInstance();

    List<LocomotiveBean> onTraclLocos = instance.getOnTrackLocomotives();
    assertTrue(onTraclLocos.isEmpty());
    //get a loc and put it in a block
    LocomotiveBean ns1631 = PersistenceFactory.getService().getLocomotive(NS_1631);
    BlockBean block1 = PersistenceFactory.getService().getBlockByTileId("bk-1");
    block1.setLocomotive(ns1631);
    db.persist(block1);

    instance.startAutoMode();
    instance.prepareDispatchers();

    Dispatcher disp = instance.getLocomotiveDispatcher(ns1631);
    assertNotNull(disp);
    assertEquals("NS 1631", disp.getName());
  }

  //@Test
  public void testResetDispatcher() {
    System.out.println("resetDispatcher");
    LocomotiveBean locomotiveBean = null;
    AutoPilot instance = null;
    instance.resetDispatcher(locomotiveBean);
    fail("The test case is a prototype.");
  }

  //@Test
  public void testStartAllLocomotives() {
    System.out.println("startAllLocomotives");
    AutoPilot instance = null;
    instance.startAllLocomotives();
    fail("The test case is a prototype.");
  }

  //@Test
  public void testStopAllLocomotives() {
    System.out.println("stopAllLocomotives");
    AutoPilot instance = null;
    instance.stopAllLocomotives();
    fail("The test case is a prototype.");
  }

  //@Test
  public void testResetStates() {
    System.out.println("resetStates");
    AutoPilot instance = null;
    instance.resetStates();
    fail("The test case is a prototype.");
  }

  //@Test
  public void testIsSensorRegistered() {
    System.out.println("isSensorRegistered");
    String sensorId = "";
    AutoPilot instance = null;
    boolean expResult = false;
    boolean result = instance.isSensorRegistered(sensorId);
    assertEquals(expResult, result);
    fail("The test case is a prototype.");
  }

  //@Test
  public void testIsRunning_0args() {
    System.out.println("isRunning");
    AutoPilot instance = null;
    boolean expResult = false;
    boolean result = instance.isAutoModeActive();
    assertEquals(expResult, result);
    fail("The test case is a prototype.");
  }

  //@Test
  public void testIsRunning_LocomotiveBean() {
    System.out.println("isRunning");
    LocomotiveBean locomotive = null;
    AutoPilot instance = null;
    boolean expResult = false;
    boolean result = instance.isRunning(locomotive);
    assertEquals(expResult, result);
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
