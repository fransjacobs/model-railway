/*
 * Copyright 2023 Frans Jacobs.
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
package jcs.persistence;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import jcs.entities.AccessoryBean;
import jcs.entities.AccessoryBean.AccessoryValue;
import jcs.entities.BlockBean;
import jcs.entities.CommandStationBean;
import jcs.entities.FunctionBean;
import jcs.entities.JCSPropertyBean;
import jcs.entities.LocomotiveBean;
import jcs.entities.LocomotiveBean.DecoderType;
import jcs.entities.RouteBean;
import jcs.entities.RouteElementBean;
import jcs.entities.SensorBean;
import jcs.entities.TileBean;
import jcs.entities.TileBean.Direction;
import jcs.entities.TileBean.Orientation;
import jcs.persistence.util.PersistenceTestHelper;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.jupiter.api.Order;
import org.tinylog.Logger;

public class PersistenceServiceTest {

  private final PersistenceTestHelper testHelper;

  private final List<JCSPropertyBean> jcsPropertyList;
  private final List<SensorBean> sensors;
  private final List<LocomotiveBean> locomotives;
  private final List<FunctionBean> functions;
  private final List<AccessoryBean> signals;
  private final List<AccessoryBean> turnouts;
  private final List<TileBean> tiles;
  private final List<RouteBean> routes;
  private final List<BlockBean> blocks;

  public PersistenceServiceTest() {
    System.setProperty("persistenceService", "jcs.persistence.TestH2PersistenceService");

    testHelper = PersistenceTestHelper.getInstance();

    jcsPropertyList = new LinkedList<>();
    sensors = new ArrayList<>();
    locomotives = new LinkedList<>();
    functions = new LinkedList<>();
    turnouts = new ArrayList<>();
    signals = new ArrayList<>();
    tiles = new ArrayList<>();
    routes = new ArrayList<>();
    blocks = new ArrayList<>();
  }

  @BeforeClass
  public static void setUpClass() throws Exception {
    System.setProperty("persistenceService", "jcs.persistence.TestH2PersistenceService");
    PersistenceTestHelper.createDatabaseUsers();
    PersistenceTestHelper.createDatabase();
    PersistenceTestHelper.getInstance().insertTestData();
    CommandStationBean csb = new CommandStationBean();
    csb.setId("marklin.cs");
    PersistenceFactory.getService().changeDefaultCommandStation(csb);
    Logger.info("####### PersistenceService Test Start....");
  }

  @AfterClass
  public static void tearDownClass() throws Exception {
    Logger.info("####### PersistenceService Test END....");
  }

  @Before
  public void setUp() {
    testHelper.insertTestData();

    JCSPropertyBean p0 = new JCSPropertyBean("CS3", "jcs.controller.cs3.MarklinCS3");
    JCSPropertyBean p10 = new JCSPropertyBean("k1", "v1");
    JCSPropertyBean p11 = new JCSPropertyBean("k2", "v2");

    jcsPropertyList.add(p0);
    jcsPropertyList.add(p10);
    jcsPropertyList.add(p11);

    SensorBean s1 = new SensorBean(1, "M1", 65, 1, null, 0, 0, 0,"marklin.cs", 1);

    sensors.add(s1);
    SensorBean s2 = new SensorBean(2, "M2", 65, 2, null, 1, 1, 0,"marklin.cs", 1);
    sensors.add(s2);

    LocomotiveBean loco2 = new LocomotiveBean(2L, "BR 81 002", 2L, 2, "DB BR 81 008", "mm_prg", 120, 1, 0, 0, false, true, true);
    loco2.setCommandStationId("marklin.cs");
    locomotives.add(loco2);

    LocomotiveBean loco8 = new LocomotiveBean(8L, "NS  6505", 8L, 8, "NS DHG 6505", "mm_prg", 120, 0, 0, 0, false, true, false);
    loco8.setCommandStationId("marklin.cs");
    locomotives.add(loco8);

    LocomotiveBean loco12 = new LocomotiveBean(12L, "BR 141 015-08", 12L, 12, "DB BR 141 136-2", "mm_prg", 120, 0, 0, 0, false, true, true);
    loco12.setCommandStationId("marklin.cs");
    locomotives.add(loco12);

    LocomotiveBean loco16389 = new LocomotiveBean(16389L, "193 304-3 DB AG", 16389L, 5, "DB BR 193 304-3", "mfx", 160, 5, 0, 0, false, true, true);

    loco16389.setCommandStationId("marklin.cs");
    locomotives.add(loco16389);

    LocomotiveBean loco49156 = new LocomotiveBean(49156L, "NS Plan Y", 49156L, 4, "NS Plan Y", "dcc", 120, 1, 0, 0, false, true, false);
    loco49156.setCommandStationId("marklin.cs");
    locomotives.add(loco49156);

    FunctionBean fb16389_5 = new FunctionBean(1L, 16389L, 5, 20, 0);
    functions.add(fb16389_5);
    FunctionBean fb16389_6 = new FunctionBean(2L, 16389L, 6, 41, 0);
    functions.add(fb16389_6);
    FunctionBean fb16389_7 = new FunctionBean(3L, 16389L, 7, 10, 0);
    functions.add(fb16389_7);
    FunctionBean fb16389_9 = new FunctionBean(4L, 16389L, 9, 171, 0);
    functions.add(fb16389_9);
    FunctionBean fb16389_10 = new FunctionBean(5L, 16389L, 10, 171, 0);
    functions.add(fb16389_10);
    FunctionBean fb16389_11 = new FunctionBean(6L, 16389L, 11, 29, 0);
    functions.add(fb16389_11);
    FunctionBean fb16389_12 = new FunctionBean(7L, 16389L, 12, 11, 0);
    functions.add(fb16389_12);
    FunctionBean fb16389_13 = new FunctionBean(8L, 16389L, 13, 116, 0);
    functions.add(fb16389_13);
    FunctionBean fb16389_14 = new FunctionBean(9L, 16389L, 14, 220, 0);
    functions.add(fb16389_14);
    FunctionBean fb12_0 = new FunctionBean(10L, 12L, 0, 1, 1);
    functions.add(fb12_0);
    FunctionBean fb12_3 = new FunctionBean(11L, 12L, 3, 8, 0);
    functions.add(fb12_3);
    FunctionBean fb12_4 = new FunctionBean(12L, 12L, 4, 18, 0);
    functions.add(fb12_4);
    FunctionBean fb2_0 = new FunctionBean(13L, 2L, 0, 1, 1);
    functions.add(fb2_0);
    FunctionBean fb2_4 = new FunctionBean(14L, 2L, 4, 18, 0);
    functions.add(fb2_4);
    FunctionBean fb8_0 = new FunctionBean(15L, 8L, 0, 1, 1);
    functions.add(fb8_0);
    FunctionBean fb8_1 = new FunctionBean(16L, 8L, 1, 1, 0);
    functions.add(fb8_1);
    FunctionBean fb49156_0 = new FunctionBean(17L, 49156L, 0, 1, 1);
    functions.add(fb49156_0);
    FunctionBean fb49156_1 = new FunctionBean(18L, 49156L, 1, 2, 1);
    functions.add(fb49156_1);
    FunctionBean fb49156_2 = new FunctionBean(20L, 49156L, 2, 2, 1);
    functions.add(fb49156_2);
    FunctionBean fb49156_3 = new FunctionBean(20L, 49156L, 3, 8, 0);
    functions.add(fb49156_3);
    FunctionBean fb49156_4 = new FunctionBean(21L, 49156L, 4, 18, 0);
    functions.add(fb49156_4);

    AccessoryBean w1 = new AccessoryBean("1", 1, "W 1R", "rechtsweiche", 1, 2, 200, "mm", "ein_alt", "weichen", "005", "magicon_a_005_01.svg", "marklin.cs");
    w1.setSynchronize(true);
    this.turnouts.add(w1);

    AccessoryBean w2 = new AccessoryBean("2", 2, "W 2L", "linksweiche", 1, 2, 200, "mm", "ein_alt", "weichen", "006", "magicon_a_006_01.svg", "marklin.cs");
    w2.setSynchronize(true);
    this.turnouts.add(w2);

    AccessoryBean w6 = new AccessoryBean("6", 6, "W 6R", "rechtsweiche", 1, 2, 200, "mm", "ein_alt", "weichen", "005", "magicon_a_005_01.svg", "marklin.cs");
    w6.setSynchronize(true);
    this.turnouts.add(w6);

    AccessoryBean w7 = new AccessoryBean("7", 7, "W 7L", "linksweiche", 1, 2, 200, "mm", "ein_alt", "weichen", "006", "magicon_a_006_01.svg", "marklin.cs");
    w7.setSynchronize(true);
    this.turnouts.add(w7);

    AccessoryBean s15 = new AccessoryBean("15", 15, "S 15", "lichtsignal_SH01", 0, 2, 200, "mm", "ein_alt", "lichtsignale", "019", "magicon_a_019_00.svg", "marklin.cs");
    s15.setSynchronize(true);
    this.signals.add(s15);

    AccessoryBean s19 = new AccessoryBean("19", 19, "S 19", "lichtsignal_HP01", 0, 2, 200, "mm", "ein_alt", "lichtsignale", "015", "magicon_a_015_00.svg", "marklin.cs");
    s19.setSynchronize(true);
    this.signals.add(s19);

    AccessoryBean s25 = new AccessoryBean("25", 25, "S 25/26", "urc_lichtsignal_HP012_SH01", 0, 4, 200, "mm", "ein_alt", "lichtsignale", "027", "magicon_a_027_00.svg", "marklin.cs");
    s25.setSynchronize(true);
    this.signals.add(s25);

    AccessoryBean s41 = new AccessoryBean("41", 41, "S 41", "urc_lichtsignal_HP012", 0, 3, 200, "mm", "ein_alt", "lichtsignale", "026", "magicon_a_026_00.svg", "marklin.cs");
    s41.setSynchronize(true);
    this.signals.add(s41);

    TileBean bk1 = new TileBean("bk-1", TileBean.TileType.BLOCK, Orientation.EAST, Direction.CENTER, 320, 140, null, null, null);
    tiles.add(bk1);
    TileBean bk2 = new TileBean("bk-2", TileBean.TileType.BLOCK, Orientation.EAST, Direction.CENTER, 420, 140, null, null, null);
    tiles.add(bk2);
    TileBean ct2 = new TileBean("ct-2", TileBean.TileType.CURVED, Orientation.EAST, Direction.CENTER, 260, 140, null, null, null);
    tiles.add(ct2);
    TileBean ct5 = new TileBean("ct-5", TileBean.TileType.CURVED, Orientation.SOUTH, Direction.CENTER, 180, 380, null, null, null);
    tiles.add(ct5);
    TileBean se5 = new TileBean("se-5", TileBean.TileType.SENSOR, Orientation.NORTH, Direction.CENTER, 340, 380, null, null, 2);
    tiles.add(se5);
    TileBean se6 = new TileBean("se-6", TileBean.TileType.SENSOR, Orientation.WEST, Direction.CENTER, 500, 380, null, null, 1);
    tiles.add(se6);
    TileBean si3 = new TileBean("si-3", TileBean.TileType.SIGNAL, Orientation.EAST, Direction.CENTER, 300, 140, null, "15", null);
    tiles.add(si3);
    TileBean st1 = new TileBean("st-1", TileBean.TileType.STRAIGHT, Orientation.EAST, Direction.CENTER, 300, 180, null, null, null);
    tiles.add(st1);
    TileBean sw1 = new TileBean("sw-1", TileBean.TileType.SWITCH, Orientation.WEST, Direction.LEFT, 260, 180, null, "2", null);
    tiles.add(sw1);
    TileBean sw2 = new TileBean("sw-2", TileBean.TileType.SWITCH, Orientation.EAST, Direction.RIGHT, 580, 180, null, null, null);
    tiles.add(sw2);

    RouteBean bk1pbk2m = new RouteBean("[bk-1+]->[bk-2-]", "bk-1", "+", "bk-2", "-", "red", false);

    List<RouteElementBean> rel = new LinkedList<>();

    RouteElementBean reb1 = new RouteElementBean(1L, "[bk-1+]->[bk-2-]", "bk-1+", "bk-1", null, 0);
    rel.add(reb1);
    RouteElementBean reb2 = new RouteElementBean(2L, "[bk-1+]->[bk-2-]", "ct-2", "ct-2", null, 1);
    rel.add(reb2);
    RouteElementBean reb3 = new RouteElementBean(3L, "[bk-1+]->[bk-2-]", "sw-1", "sw-1", "G", 2);
    rel.add(reb3);
    RouteElementBean reb4 = new RouteElementBean(4L, "[bk-1+]->[bk-2-]", "st-1", "st-1", null, 3);
    rel.add(reb4);
    RouteElementBean reb5 = new RouteElementBean(5L, "[bk-1+]->[bk-2-]", "bk2-", "bk-2", null, 4);
    rel.add(reb5);

    bk1pbk2m.setRouteElements(rel);
    this.routes.add(bk1pbk2m);

    RouteBean bk2mbk1p = new RouteBean("[bk-2-]->[bk-1+]", "bk-2", "-", "bk-1", "+", "yellow", false);
    this.routes.add(bk2mbk1p);

    BlockBean block1 = new BlockBean(bk1);
    block1.setDescription("Block 1");
    block1.setId("bk-1");
    this.blocks.add(block1);
    BlockBean block2 = new BlockBean(bk2);
    block2.setDescription("Block 2");
    block2.setId("bk-2");
    this.blocks.add(block2);
  }

  @After
  public void tearDown() {
  }

  /**
   * Test of getProperties method, of class PersistenceService.
   */
  @Test
  @Order(1)
  public void testGetProperties() {
    System.out.println("getProperties");
    PersistenceService instance = PersistenceFactory.getService();
    List<JCSPropertyBean> expResult = jcsPropertyList;
    List<JCSPropertyBean> result = instance.getProperties();
    assertEquals(expResult, result);
  }

  /**
   * Test of getProperty method, of class PersistenceService.
   */
  @Test
  @Order(2)
  public void testGetProperty() {
    System.out.println("getProperty");
    String key = "k2";
    PersistenceService instance = PersistenceFactory.getService();
    JCSPropertyBean expResult = jcsPropertyList.get(2);
    JCSPropertyBean result = instance.getProperty(key);
    assertEquals(expResult, result);
  }

  /**
   * Test of persist method, of class PersistenceService.
   */
  @Test
  @Order(3)
  public void testPersistJCSPropertyBean() {
    System.out.println("persistJCSPropertyBean");
    JCSPropertyBean propertyBean = new JCSPropertyBean("k3", "v3");
    PersistenceService instance = PersistenceFactory.getService();
    JCSPropertyBean expResult = propertyBean;
    JCSPropertyBean result = instance.persist(propertyBean);
    assertEquals(expResult, result);

    JCSPropertyBean chkP = instance.getProperty("k3");
    assertEquals(propertyBean, chkP);

    propertyBean.setValue("UPDATED");
    result = instance.persist(propertyBean);
    assertEquals(expResult, result);

    chkP = instance.getProperty("k3");
    assertEquals(propertyBean, chkP);
  }

  /**
   * Test of remove method, of class PersistenceService.
   */
  @Test
  @Order(4)
  public void testRemoveJCSPropertyBean() {
    System.out.println("removeJCSPropertyBean");
    JCSPropertyBean property = new JCSPropertyBean("k4", "v4");
    PersistenceService instance = PersistenceFactory.getService();

    instance.persist(property);
    JCSPropertyBean chkP = instance.getProperty("k4");
    assertEquals(property, chkP);

    instance.remove(property);
    chkP = instance.getProperty("k4");
    assertNull(chkP);
  }

  /**
   * Test of getSensors method, of class PersistenceService.
   */
  @Test
  @Order(5)
  public void testGetSensors() {
    System.out.println("getSensors");
    PersistenceService instance = PersistenceFactory.getService();
    List<SensorBean> expResult = this.sensors;
    List<SensorBean> result = instance.getSensors();
    assertEquals(expResult, result);
  }

  /**
   * Test of getSensor method, of class PersistenceService.
   */
  @Test
  @Order(6)
  public void testGetSensorString() {
    System.out.println("getSensorString");
    Integer id = 1;
    PersistenceService instance = PersistenceFactory.getService();
    SensorBean expResult = sensors.get(0);
    SensorBean result = instance.getSensor(id);
    assertEquals(expResult, result);
  }

  /**
   * Test of getSensor method, of class PersistenceService.
   */
  @Test
  @Order(7)
  public void testGetSensorIntegerInteger() {
    System.out.println("getSensorIntegerInteger");
    Integer deviceId = 65;
    Integer contactId = 2;
    PersistenceService instance = PersistenceFactory.getService();
    SensorBean expResult = sensors.get(1);
    SensorBean result = instance.getSensor(deviceId, contactId);
    assertEquals(expResult, result);
  }

  /**
   * Test of persist method, of class PersistenceService.
   */
  @Test
  @Order(8)
  public void testPersistSensorBean() {
    System.out.println("persistSensorBean");
    SensorBean sensor = new SensorBean(3, "M1P3", 0, 3, 65, 0, 1, 0, "marklin.cs", 2);

    PersistenceService instance = PersistenceFactory.getService();

    SensorBean result = instance.persist(sensor);

    assertEquals(sensor, result);

    SensorBean s3 = instance.getSensor(0, 3);

    assertEquals(sensor, s3);

    sensor.setStatus(1);
    result = instance.persist(sensor);
    assertEquals(sensor, result);

    s3 = instance.getSensor(3);
    assertEquals(sensor, s3);
  }

  /**
   * Test of remove method, of class PersistenceService.
   */
  @Test
  @Order(9)
  public void testRemoveSensorBean() {
    System.out.println("removeSensorBean");
    SensorBean sensor = new SensorBean(4, "M1P4", 1, 4, 65, 0, 1, 0,"marklin.cs", 2);

    PersistenceService instance = PersistenceFactory.getService();

    SensorBean result = instance.persist(sensor);

    assertEquals(sensor, result);

    SensorBean s3 = instance.getSensor(1, 4);
    assertEquals(sensor, s3);

    instance.remove(sensor);

    s3 = instance.getSensor(65, 4);
    assertNull(s3);
  }

  /**
   * Test of getLocomotives method, of class PersistenceService.
   */
  @Test
  @Order(10)
  public void testGetLocomotives() {
    System.out.println("getLocomotives");
    PersistenceService instance = PersistenceFactory.getService();
    List<LocomotiveBean> expResult = this.locomotives;
    List<LocomotiveBean> result = instance.getLocomotives();

    for (int i = 0; i < expResult.size(); i++) {
      LocomotiveBean lbr = result.get(i);
      LocomotiveBean lbe = expResult.get(i);
      Logger.trace(lbe.getName());

      assertEquals(lbe.getId(), lbr.getId());
      assertEquals(lbe.getName(), lbr.getName());
      assertEquals(lbe.getUid(), lbr.getUid());
      assertEquals(lbe.getAddress(), lbr.getAddress());
      assertEquals(lbe.getIcon(), lbr.getIcon());
      assertEquals(lbe.getTachoMax(), lbr.getTachoMax());
      assertEquals(lbe.getvMin(), lbr.getvMin());
      assertEquals(lbe.getDecoderType(), lbr.getDecoderType());
      assertEquals(lbe.getVelocity(), lbr.getVelocity());
      assertEquals(lbe.getRichtung(), lbr.getRichtung());
      assertEquals(lbe.isCommuter(), lbr.isCommuter());
      assertEquals(lbe.isShow(), lbr.isShow());
    }
    assertEquals(expResult, result);
  }

  /**
   * Test of getLocomotive method, of class PersistenceService.
   */
  @Test
  @Order(11)
  public void testGetLocomotiveIntegerDecoderType() {
    System.out.println("getLocomotiveIntegerDecoderType");
    Integer address = 8;
    DecoderType decoderType = DecoderType.MM_PRG;

    PersistenceService instance = PersistenceFactory.getService();
    LocomotiveBean expResult = this.locomotives.get(1);
    LocomotiveBean result = instance.getLocomotive(address, decoderType, "marklin.cs");
    assertEquals(expResult, result);

    assertEquals(expResult.getId(), result.getId());
    assertEquals(expResult.getName(), result.getName());
    assertEquals(expResult.getUid(), result.getUid());
    assertEquals(expResult.getAddress(), result.getAddress());
    assertEquals(expResult.getIcon(), result.getIcon());
    assertEquals(expResult.getTachoMax(), result.getTachoMax());
    assertEquals(expResult.getvMin(), result.getvMin());
    assertEquals(expResult.getDecoderType(), result.getDecoderType());
    assertEquals(expResult.getVelocity(), result.getVelocity());
    assertEquals(expResult.getRichtung(), result.getRichtung());
    assertEquals(expResult.isCommuter(), result.isCommuter());
    assertEquals(expResult.isShow(), result.isShow());

    assertEquals(expResult, result);

    // Check Functions
    List<FunctionBean> resFunctions = new LinkedList<>();
    resFunctions.addAll(result.getFunctions().values());

    assertEquals(2, resFunctions.size());
  }

  /**
   * Test of getLocomotive method, of class PersistenceService.
   */
  @Test
  @Order(12)
  public void testGetLocomotiveInteger() {
    System.out.println("getLocomotiveInteger");
    Long id = 2L;
    PersistenceService instance = PersistenceFactory.getService();
    LocomotiveBean expResult = this.locomotives.get(0);
    LocomotiveBean result = instance.getLocomotive(id);

    assertEquals(expResult.getId(), result.getId());
    assertEquals(expResult.getName(), result.getName());
    assertEquals(expResult.getUid(), result.getUid());
    assertEquals(expResult.getAddress(), result.getAddress());
    assertEquals(expResult.getIcon(), result.getIcon());
    assertEquals(expResult.getTachoMax(), result.getTachoMax());
    assertEquals(expResult.getvMin(), result.getvMin());
    assertEquals(expResult.getDecoderType(), result.getDecoderType());
    assertEquals(expResult.getVelocity(), result.getVelocity());
    assertEquals(expResult.getRichtung(), result.getRichtung());
    assertEquals(expResult.isCommuter(), result.isCommuter());
    assertEquals(expResult.isShow(), result.isShow());
    assertEquals(expResult.isSynchronize(), result.isSynchronize());
    assertEquals(expResult.getImported(), result.getImported());
    assertEquals(expResult.getCommandStationId(), result.getCommandStationId());

    assertEquals(expResult, result);
  }

  /**
   * Test of persist method, of class PersistenceService.
   */
  @Test
  @Order(13)
  public void testPersistLocomotiveBean() {
    System.out.println("persistLocomotiveBean");
    LocomotiveBean locomotive = new LocomotiveBean(80L, "DB BR 44 100", 16393L, 80, "DB BR 44 100", "mfx", 80, 5, 0, 0, false, true, false);
    locomotive.setCommandStationId("marklin.cs");

    locomotive.setImported("testcase");

    FunctionBean fb80_0 = new FunctionBean(80L, 0, 1, 0);

    locomotive.addFunction(fb80_0);

    PersistenceService instance = PersistenceFactory.getService();
    LocomotiveBean expResult = locomotive;
    LocomotiveBean result = instance.persist(locomotive);

    assertEquals(expResult, result);

    assertEquals("testcase", result.getImported());

    LocomotiveBean loco = instance.getLocomotive(80L);
    assertEquals(locomotive, loco);

    LocomotiveBean loco2 = instance.getLocomotive(80, DecoderType.MFX, "marklin.cs");
    assertEquals(locomotive, loco2);
    assertEquals(loco2, result);

    assertEquals(1, loco2.getFunctions().size());

    List<FunctionBean> locfunctions = new LinkedList();
    locfunctions.addAll(loco2.getFunctions().values());

    FunctionBean function = locfunctions.get(0);
    fb80_0.setId(23L);
    assertEquals(fb80_0, function);

    loco.setIcon("new Icon");
    instance.persist(loco);

    loco2 = instance.getLocomotive(80, DecoderType.MFX, "marklin.cs");
    assertEquals(loco, loco2);

    instance.remove(locomotive);
  }

  /**
   * Test of remove method, of class PersistenceService.
   */
  @Test
  @Order(14)
  public void testRemoveLocomotiveBean() {
    System.out.println("removeLocomotiveBean");
    LocomotiveBean locomotiveBean = new LocomotiveBean(70L, "To Be Removed", 16370L, 70, "To Be Removed", "mfx", 80, 5, 0, 0, false, true, false);
    locomotiveBean.setCommandStationId("marklin.cs");

    PersistenceService instance = PersistenceFactory.getService();

    LocomotiveBean expResult = locomotiveBean;
    LocomotiveBean result = instance.persist(locomotiveBean);
    assertEquals(expResult, result);

    LocomotiveBean loco = instance.getLocomotive(70L);
    assertEquals(locomotiveBean, loco);

    instance.remove(locomotiveBean);

    loco = instance.getLocomotive(70L);

    assertNull(loco);
  }

  /**
   * Test of getTurnouts method, of class PersistenceService.
   */
  @Test
  @Order(15)
  public void testGetTurnouts() {
    System.out.println("getTurnouts");
    PersistenceService instance = PersistenceFactory.getService();
    //Make sure the right commans station is default
    CommandStationBean csb = instance.getCommandStation("marklin.cs");
    instance.changeDefaultCommandStation(csb);

    List<AccessoryBean> expResult = this.turnouts;
    List<AccessoryBean> result = instance.getTurnouts();
    assertEquals(expResult, result);
  }

  /**
   * Test of getSignals method, of class PersistenceService.
   */
  @Test
  @Order(16)
  public void testGetSignals() {
    System.out.println("getSignals");
    PersistenceService instance = PersistenceFactory.getService();
    //Make sure the right commans station is default
    CommandStationBean csb = instance.getCommandStation("marklin.cs");
    instance.changeDefaultCommandStation(csb);

    List<AccessoryBean> expResult = this.signals;
    List<AccessoryBean> result = instance.getSignals();

    //expected: java.util.ArrayList<[S 15, S 19, S 25/26, S 41]>
    // but was: java.util.ArrayList<[S 15, S 19, S 25/26, S 41]>    
    assertEquals(expResult, result);
  }

  /**
   * Test of getAccessory method, of class PersistenceService.
   */
  @Test
  @Order(17)
  public void testGetAccessoryById() {
    System.out.println("getAccessoryById");
    String id = "25";
    PersistenceService instance = PersistenceFactory.getService();
    AccessoryBean expResult = this.signals.get(2);
    AccessoryBean result = instance.getAccessory(id);
    assertEquals(expResult, result);
  }

  /**
   * Test of getAccessoryByAddress method, of class PersistenceService.
   */
  @Test
  @Order(18)
  public void testGetAccessory() {
    System.out.println("getAccessory");
    Integer address = 7;
    PersistenceService instance = PersistenceFactory.getService();
    AccessoryBean expResult = this.turnouts.get(3);
    AccessoryBean result = instance.getAccessoryByAddress(address);
    assertEquals(expResult, result);
  }

  /**
   * Test of persist method, of class PersistenceService.
   */
  @Test
  @Order(19)
  public void testPersistAccessoryBean() {
    System.out.println("persistAccessoryBean");
    AccessoryBean accessory = new AccessoryBean("100", 100, "W 100", "rechtsweiche", 1, 2, 200, "mm", "ein_alt", "weichen", "005", "magicon_a_005_01.svg", "marklin.cs");

    PersistenceService instance = PersistenceFactory.getService();

    AccessoryBean result = instance.persist(accessory);

    AccessoryBean expResult = accessory;

    assertEquals(expResult, result);

    AccessoryBean ab = instance.getAccessory(expResult.getId());
    assertEquals(accessory, ab);

    accessory.setName("WWWWW");
    accessory.setSource("test");

    instance.persist(accessory);
    ab = instance.getAccessoryByAddressAndCommandStationId(expResult.getAddress(), "marklin.cs");
    assertEquals(accessory, ab);

    assertEquals("test", ab.getSource());
  }

  /**
   * Test of remove method, of class PersistenceService.
   */
  @Test
  @Order(20)
  public void testRemoveAccessoryBean() {
    System.out.println("removeAccessoryBean");
    AccessoryBean accessory = new AccessoryBean("101", 101, "W 101", "rechtsweiche", 1, 2, 200, "mm", "ein_alt", "weichen", "005", "magicon_a_005_01.svg", "marklin.cs");
    PersistenceService instance = PersistenceFactory.getService();

    AccessoryBean result = instance.persist(accessory);
    assertEquals(accessory, result);

    AccessoryBean ab = instance.getAccessoryByAddressAndCommandStationId(accessory.getAddress(), "marklin.cs");

    assertEquals(accessory, ab);

    instance.remove(accessory);

    ab = instance.getAccessory(accessory.getId());
    assertNull(ab);
  }

  /**
   * Test of getTileBeans method, of class PersistenceService.
   */
  @Test
  @Order(21)
  public void testGetTileBeans() {
    System.out.println("getTileBeans");
    PersistenceService instance = PersistenceFactory.getService();

    List<TileBean> result = instance.getTileBeans();
    List<TileBean> expResult = this.tiles;

    assertEquals(expResult, result);
  }

  /**
   * Test of getTileBean method, of class PersistenceService.
   */
  @Test
  @Order(22)
  public void testGetTile() {
    System.out.println("getTile");
    Integer x = 300;
    Integer y = 180;
    PersistenceService instance = PersistenceFactory.getService();
    TileBean expResult = tiles.get(7);
    TileBean result = instance.getTileBean(x, y);
    assertEquals(expResult, result);
  }

  /**
   * Test of persist method, of class PersistenceService.
   */
  @Test
  @Order(23)
  public void testPersistTileBean() {
    System.out.println("persistTileBean");
    TileBean sw12 = new TileBean("sw-12", TileBean.TileType.SWITCH, Orientation.EAST, Direction.RIGHT, 50, 50, null, null, null);

    PersistenceService instance = PersistenceFactory.getService();
    TileBean result = instance.persist(sw12);
    assertEquals(sw12, result);

    TileBean tb = instance.getTileBean("sw-12");
    assertEquals(sw12, tb);

    sw12.setDirection(Direction.RIGHT);
    result = instance.persist(sw12);
    assertEquals(sw12, result);

    TileBean tb1 = instance.getTileBean(50, 50);
    assertEquals(sw12, tb1);
  }

  @Test
  @Order(24)
  public void testRemoveTileBean() {
    System.out.println("removeTileBean");
    TileBean sw13 = new TileBean("sw-13", TileBean.TileType.CURVED, Orientation.EAST, Direction.CENTER, 80, 50, null, null, null);
    PersistenceService instance = PersistenceFactory.getService();

    TileBean result = instance.persist(sw13);
    assertEquals(sw13, result);

    TileBean tb = instance.getTileBean("sw-13");
    assertEquals(sw13, tb);

    instance.remove(tb);

    TileBean tb1 = instance.getTileBean(80, 50);
    assertNull(tb1);
  }

  /**
   * Test of persist method, of class PersistenceService.
   */
  @Test
  @Order(25)
  public void testPersistListTileBeans() {
    System.out.println("persistListTileBeans");
    PersistenceService instance = PersistenceFactory.getService();
    List<TileBean> tbl = this.tiles;
    TileBean sw22 = new TileBean("sw-22", TileBean.TileType.CROSS, Orientation.EAST, Direction.CENTER, 100, 100, null, null, null);
    List<TileBean> current = instance.getTileBeans();

    // There should be 10 tiles...
    assertEquals(10, current.size());
    // tbl.add(sw22);
    // instance.persist(tbl);
    List<TileBean> current2 = instance.getTileBeans();

    // There should now be 10 tiles...
    assertEquals(10, current2.size());

    // TileBean tb = instance.getTileBean(100, 100);
    // assertEquals(sw22, tb);
    // tbl.remove(sw22);
    // instance.persist(tbl);
    // current2 = instance.getTileBeans();
    // There should now be 11 tiles...
    // assertEquals(11, current2.size());
  }

  @Test
  @Order(26)
  public void testPersistLotsOfTileBeans() {
    System.out.println("persistLotsOfTileBeans");
    PersistenceService instance = PersistenceFactory.getService();

    List<TileBean> expected = new ArrayList<>();

    // Create a lot of test tiles
    int x = 20;
    int cy = 100;
    for (int i = 2; i < 1002; i++) {
      String id = "st-" + i;
      int cx = x + i * 40;

      TileBean stn = new TileBean(id, TileBean.TileType.STRAIGHT, Orientation.EAST, Direction.CENTER, cx, cy, null, null, null);
      expected.add(stn);
    }

    instance.persist(expected);

    List<TileBean> retrieved = instance.getTileBeans();
    assertEquals(expected.size(), retrieved.size());
    assertEquals(expected, retrieved);
  }

  @Test
  @Order(27)
  public void testGetRoutes() {
    System.out.println("getRoutes");
    PersistenceService instance = PersistenceFactory.getService();
    List<RouteBean> expResult = this.routes;
    List<RouteBean> result = instance.getRoutes();
    assertEquals(expResult, result);
  }

  @Test
  @Order(28)
  public void testGetRouteString() {
    System.out.println("getRouteString");
    PersistenceService instance = PersistenceFactory.getService();
    RouteBean expResult = this.routes.get(1);
    RouteBean result = instance.getRoute("[bk-2-]->[bk-1+]");
    assertEquals(expResult, result);
  }

  @Test
  @Order(29)
  public void testGetRoute_String_String_String_String() {
    System.out.println("getRoute_string_string_string_string");
    PersistenceService instance = PersistenceFactory.getService();
    RouteBean expResult = this.routes.get(0);
    List<RouteElementBean> expRel = expResult.getRouteElements();

    RouteBean result = instance.getRoute("bk-1", "+", "bk-2", "-");
    assertEquals(expResult, result);

    List<RouteElementBean> rerl = result.getRouteElements();
    assertEquals(expRel, rerl);
  }

  @Test
  @Order(30)
  public void testLockRoute() {
    System.out.println("LockRoute");
    PersistenceService instance = PersistenceFactory.getService();
    RouteBean expResult = this.routes.get(0);
    List<RouteElementBean> expRel = expResult.getRouteElements();

    RouteBean result = instance.getRoute("bk-1", "+", "bk-2", "-");
    assertEquals(expResult, result);

    List<RouteElementBean> rerl = result.getRouteElements();
    assertEquals(expRel, rerl);

    boolean locked = instance.isAccessoryLocked("2");
    assertFalse(locked);

    result.setLocked(true);
    instance.persist(result);

    locked = instance.isAccessoryLocked("2");
    assertTrue(locked);

    result.setLocked(false);
    instance.persist(result);

    locked = instance.isAccessoryLocked("2");
    assertFalse(locked);
  }

  @Test
  @Order(31)
  public void testPersistRouteBean() {
    System.out.println("persistRouteBean");
    RouteBean route = new RouteBean("[ct-2]->[ct-5]", "ct-2", "*", "ct-5", "*", "blue");

    List<RouteElementBean> rel = new LinkedList<>();
    RouteElementBean rect2 = new RouteElementBean("[ct-2]->[ct-5]", "ct-2", "ct-2", null, 0);
    rel.add(rect2);
    RouteElementBean resi3 = new RouteElementBean("[ct-2]->[ct-5]", "si-3", "si-3", AccessoryValue.GREEN, 1);
    rel.add(resi3);

    route.setRouteElements(rel);
    PersistenceService instance = PersistenceFactory.getService();

    RouteBean result = instance.persist(route);

    assertEquals(route, result);

    for (RouteElementBean re : rel) {
      if (re.getNodeId().equals("ct-2")) {
        re.setId(6L);
      } else {
        re.setId(7L);
      }
    }

    List<RouteElementBean> relr = result.getRouteElements();
    assertEquals(rel, relr);
    RouteBean rb = instance.getRoute("ct-2", "*", "ct-5", "*");
    assertEquals(route, rb);

    relr = rb.getRouteElements();
    assertEquals(rel, relr);
    RouteBean rb1 = instance.getRoute("[ct-2]->[ct-5]");
    assertEquals(route, rb1);
  }

  @Test
  @Order(32)
  public void testRemoveRouteBean() {
    System.out.println("removeRouteBean");
    RouteBean routeBean = new RouteBean("[ct-5]->[ct-2]", "ct-5", "*", "ct-2", "*", "orange");
    PersistenceService instance = PersistenceFactory.getService();

    RouteBean result = instance.persist(routeBean);

    assertEquals(routeBean, result);

    RouteBean rb = instance.getRoute("ct-5", "*", "ct-2", "*");
    assertEquals(routeBean, rb);

    instance.remove(routeBean);

    rb = instance.getRoute("ct-5", "*", "ct-2", "*");
    assertNull(rb);
  }

  @Test
  @Order(33)
  public void testGetBlocks() {
    System.out.println("getBlocks");
    PersistenceService instance = PersistenceFactory.getService();
    List<BlockBean> expResult = this.blocks;
    List<BlockBean> result = instance.getBlocks();
    assertEquals(expResult, result);
  }

  @Test
  @Order(34)
  public void testGetBlock() {
    System.out.println("getBlock");
    String id = "bk-1";
    PersistenceService instance = PersistenceFactory.getService();
    BlockBean expResult = this.blocks.get(0);
    BlockBean result = instance.getBlock(id);

    //expected:<BlockBean{id=bk-1, tileId=bk-1, description=Block 1, status=null, arrivalSuffix=null, plusSensorId=null, minSensorId=null, plusSignalId=null, minSignalId=null, locomotiveId=null}> 
    //but was:<null>
    assertEquals(expResult, result);
  }

  @Test
  @Order(35)
  public void testGetBlockByTileId() {
    System.out.println("getBlockByTileId");
    String tileId = "bk-2";
    PersistenceService instance = PersistenceFactory.getService();
    BlockBean expResult = this.blocks.get(1);
    BlockBean result = instance.getBlockByTileId(tileId);
    assertEquals(expResult, result);
  }

  @Test
  @Order(36)
  public void testPersistBlockBean() {
    System.out.println("persistBlockBean");
    BlockBean block = new BlockBean();
    block.setId("st-1");
    block.setTileId("st-1");
    block.setDescription("A Test Block");
    block.setMinWaitTime(10);

    PersistenceService instance = PersistenceFactory.getService();

    BlockBean expResult = block;

    BlockBean result = instance.persist(block);
    assertEquals(expResult, result);

    block.setLocomotiveId(2L);
    result = instance.persist(block);

    assertEquals(expResult, result);
  }

  @Test
  @Order(37)
  public void testRemoveBlockBean() {
    System.out.println("removeBlockBean");
    BlockBean block = new BlockBean();
    block.setId("si-3");
    block.setTileId("si-3");
    block.setDescription("A Test Block to remove");
    block.setMinWaitTime(10);

    PersistenceService instance = PersistenceFactory.getService();

    BlockBean result = instance.persist(block);
    assertEquals(block, result);

    result = instance.getBlockByTileId("si-3");

    assertEquals(block, result);

    instance.remove(block);
    result = instance.getBlockByTileId("si-3");

    assertNull(result);
  }

  @Test
  @Order(38)
  public void testRemoveAllBlocks() {
    System.out.println("removeAllBlocks");
    PersistenceService instance = PersistenceFactory.getService();

    List<BlockBean> expResult = this.blocks;
    List<BlockBean> result = instance.getBlocks();
    assertEquals(expResult, result);

    instance.removeAllBlocks();

    result = instance.getBlocks();

    assertEquals(0, result.size());
  }

  @Test
  @Order(39)
  public void testCommandStations() {
    System.out.println("commandStations");
    PersistenceService instance = PersistenceFactory.getService();

    List<CommandStationBean> commandStations = instance.getCommandStations();
    assertEquals(5, commandStations.size());

    for (CommandStationBean cs : commandStations) {
      Logger.trace("## -> " + cs + " default: " + cs.isDefault() + " id: " + cs.getId());
    }

    CommandStationBean defCS = instance.getDefaultCommandStation();

    assertEquals("marklin.cs", defCS.getId());

    defCS.setDefault(false);

    instance.persist(defCS);

    defCS = instance.getCommandStation("marklin.cs");

    assertFalse(defCS.isDefault());

    defCS.setDefault(true);

    instance.persist(defCS);

    defCS = instance.getCommandStation("marklin.cs");

    assertTrue(defCS.isDefault());

    defCS.setDefault(false);

    instance.persist(defCS);

    CommandStationBean defCS2 = instance.getDefaultCommandStation();

    assertNull(defCS2);

    defCS2 = instance.getCommandStation("dcc-ex");

    assertEquals("dcc-ex", defCS2.getId());
    assertFalse(defCS2.isDefault());

    defCS2.setDefault(true);
    instance.persist(defCS2);

    CommandStationBean defCS3 = instance.getDefaultCommandStation();
    assertEquals("dcc-ex", defCS3.getId());

  }

}
