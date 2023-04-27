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

import java.awt.Image;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import jcs.entities.AccessoryBean;
import jcs.entities.FunctionBean;
import jcs.entities.JCSPropertyBean;
import jcs.entities.LocomotiveBean;
import jcs.entities.RouteBean;
import jcs.entities.RouteElementBean;
import jcs.entities.SensorBean;
import jcs.entities.TileBean;
import jcs.entities.enums.DecoderType;
import jcs.entities.enums.Orientation;
import jcs.entities.enums.TileType;
import jcs.persistence.util.PersistenceTestHelper;
import jcs.ui.layout.tiles.enums.Direction;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
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

    public PersistenceServiceTest() {
        System.setProperty("persistenceService", "jcs.persistence.H2PersistenceService");

        testHelper = PersistenceTestHelper.getInstance();

        jcsPropertyList = new LinkedList<>();
        sensors = new LinkedList<>();
        locomotives = new LinkedList<>();
        functions = new LinkedList<>();
        turnouts = new LinkedList<>();
        signals = new LinkedList<>();
        tiles = new ArrayList<>();
        routes = new ArrayList<>();
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

        SensorBean s1 = new SensorBean(1L, "M1", 65, 1, 0, 0, 0, null);
        sensors.add(s1);
        SensorBean s2 = new SensorBean(2L, "M2", 65, 2, 1, 1, 0, null);
        sensors.add(s2);

        LocomotiveBean loco2 = new LocomotiveBean(2L, "BR 81 002", 2L, 0L, 2, "DB BR 81 008", "mm_prg", null, 120, 1, 0, 0, false, null, true);
        locomotives.add(loco2);
        LocomotiveBean loco8 = new LocomotiveBean(8L, "NS  6505", 8L, null, 8, "NS DHG 6505", "mm_prg", null, 120, 0, 0, 0, false, null, true);
        locomotives.add(loco8);
        LocomotiveBean loco12 = new LocomotiveBean(12L, "BR 141 015-08", 12L, null, 12, "DB BR 141 136-2", "mm_prg", null, 120, 0, 0, 0, false, null, true);
        locomotives.add(loco12);
        LocomotiveBean loco16389 = new LocomotiveBean(16389L, "193 304-3 DB AG", 16389L, 1945312555L, 5, "DB BR 193 304-3", "mfx", "0x5", 160, 5, 0, 0, false, null, true);
        locomotives.add(loco16389);
        LocomotiveBean loco49156 = new LocomotiveBean(49156L, "NS Plan Y", 49156L, null, 4, "NS Plan Y", "dcc", null, 120, 1, 0, 0, false, null, true);
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

        AccessoryBean w1 = new AccessoryBean(1L, 1, "W 1R", "rechtsweiche", 1, 2, 200, "mm", "ein_alt", "weichen", "005", "magicon_a_005_01.svg");
        this.turnouts.add(w1);
        AccessoryBean w2 = new AccessoryBean(2L, 2, "W 2L", "linksweiche", 1, 2, 200, "mm", "ein_alt", "weichen", "006", "magicon_a_006_01.svg");
        this.turnouts.add(w2);
        AccessoryBean w6 = new AccessoryBean(3L, 6, "W 6R", "rechtsweiche", 1, 2, 200, "mm", "ein_alt", "weichen", "005", "magicon_a_005_01.svg");
        this.turnouts.add(w6);
        AccessoryBean w7 = new AccessoryBean(4L, 7, "W 7L", "linksweiche", 1, 2, 200, "mm", "ein_alt", "weichen", "006", "magicon_a_006_01.svg");
        this.turnouts.add(w7);
        AccessoryBean s15 = new AccessoryBean(5L, 15, "S 15", "lichtsignal_SH01", 0, 2, 200, "mm", "ein_alt", "lichtsignale", "019", "magicon_a_019_00.svg");
        this.signals.add(s15);
        AccessoryBean s19 = new AccessoryBean(6L, 19, "S 19", "lichtsignal_HP01", 0, 2, 200, "mm", "ein_alt", "lichtsignale", "015", "magicon_a_015_00.svg");
        this.signals.add(s19);
        AccessoryBean s25 = new AccessoryBean(7L, 25, "S 25/26", "urc_lichtsignal_HP012_SH01", 0, 4, 200, "mm", "ein_alt", "lichtsignale", "027", "magicon_a_027_00.svg");
        this.signals.add(s25);
        AccessoryBean s41 = new AccessoryBean(8L, 41, "S 41", "urc_lichtsignal_HP012", 0, 3, 200, "mm", "ein_alt", "lichtsignale", "026", "magicon_a_026_00.svg");
        this.signals.add(s41);

        TileBean bk1 = new TileBean("bk-1", TileType.BLOCK, Orientation.EAST, Direction.CENTER, 320, 140, null, null, null);
        tiles.add(bk1);
        TileBean bk2 = new TileBean("bk-2", TileType.BLOCK, Orientation.EAST, Direction.CENTER, 420, 140, null, null, null);
        tiles.add(bk2);
        TileBean ct2 = new TileBean("ct-2", TileType.CURVED, Orientation.EAST, Direction.CENTER, 260, 140, null, null, null);
        tiles.add(ct2);
        TileBean ct5 = new TileBean("ct-5", TileType.CURVED, Orientation.SOUTH, Direction.CENTER, 180, 380, null, null, null);
        tiles.add(ct5);
        TileBean se5 = new TileBean("se-5", TileType.SENSOR, Orientation.NORTH, Direction.CENTER, 340, 380, null, null, 2L);
        tiles.add(se5);
        TileBean se6 = new TileBean("se-6", TileType.SENSOR, Orientation.WEST, Direction.CENTER, 500, 380, null, null, 1L);
        tiles.add(se6);
        TileBean si3 = new TileBean("si-3", TileType.SIGNAL, Orientation.EAST, Direction.CENTER, 300, 140, null, 5L, null);
        tiles.add(si3);
        TileBean st1 = new TileBean("st-1", TileType.STRAIGHT, Orientation.EAST, Direction.CENTER, 300, 180, null, null, null);
        tiles.add(st1);
        TileBean sw1 = new TileBean("sw-1", TileType.SWITCH, Orientation.WEST, Direction.LEFT, 260, 180, null, 2L, null);
        tiles.add(sw1);
        TileBean sw2 = new TileBean("sw-2", TileType.SWITCH, Orientation.EAST, Direction.RIGHT, 580, 180, null, null, null);
        tiles.add(sw2);

        RouteBean bk1pbk2m = new RouteBean(1L, "bk-1", "+", "bk-2", "-", "red");
        this.routes.add(bk1pbk2m);
        RouteBean bk2mbk1p = new RouteBean(2L, "bk-2", "-", "bk-1", "+", "yellow");

        List<RouteElementBean> rel = new LinkedList<>();

        RouteElementBean reb1 = new RouteElementBean(1L, 2L, "bk-2-", "bk-2", null, 0);
        rel.add(reb1);
        RouteElementBean reb2 = new RouteElementBean(2L, 2L, "ct-2", "ct-2", null, 1);
        rel.add(reb2);

        RouteElementBean reb3 = new RouteElementBean(3L, 2L, "st-1", "st-1", null, 2);
        rel.add(reb3);

        RouteElementBean reb4 = new RouteElementBean(4L, 2L, "bk1+", "bk-1", null, 3);
        rel.add(reb4);

        bk2mbk1p.setRouteElements(rel);

        this.routes.add(bk2mbk1p);
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of getProperties method, of class PersistenceService.
     */
    @Test
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
    public void testPersist_JCSPropertyBean() {
        System.out.println("persist");
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
    public void testRemove_JCSPropertyBean() {
        System.out.println("remove");
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
    public void testGetSensor_Long() {
        System.out.println("getSensor");
        Long id = 1L;
        PersistenceService instance = PersistenceFactory.getService();
        SensorBean expResult = sensors.get(0);
        SensorBean result = instance.getSensor(id);
        assertEquals(expResult, result);
    }

    /**
     * Test of getSensor method, of class PersistenceService.
     */
    @Test
    public void testGetSensor_Integer_Integer() {
        System.out.println("getSensor");
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
    public void testPersist_SensorBean() {
        System.out.println("persist");
        SensorBean sensor = new SensorBean("M1P3", 65, 3, 0, 1, 0, null);
        PersistenceService instance = PersistenceFactory.getService();

        SensorBean result = instance.persist(sensor);

        sensor.setId(3L);
        assertEquals(sensor, result);

        SensorBean s3 = instance.getSensor(65, 3);
        assertEquals(sensor, s3);

        sensor.setStatus(1);
        result = instance.persist(sensor);
        assertEquals(sensor, result);

        s3 = instance.getSensor(3L);
        assertEquals(sensor, s3);
    }

    /**
     * Test of remove method, of class PersistenceService.
     */
    @Test
    public void testRemove_SensorBean() {
        System.out.println("remove");
        SensorBean sensor = new SensorBean("M1P4", 65, 4, 0, 1, 0, null);
        PersistenceService instance = PersistenceFactory.getService();

        SensorBean result = instance.persist(sensor);

        sensor.setId(3L);
        assertEquals(sensor, result);

        SensorBean s3 = instance.getSensor(65, 4);
        assertEquals(sensor, s3);

        instance.remove(sensor);

        s3 = instance.getSensor(65, 4);
        assertNull(s3);
    }

    /**
     * Test of getLocomotives method, of class PersistenceService.
     */
    @Test
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
            assertEquals(lbe.getMfxUid(), lbr.getMfxUid());
            assertEquals(lbe.getAddress(), lbr.getAddress());
            assertEquals(lbe.getIcon(), lbr.getIcon());
            assertEquals(lbe.getMfxSid(), lbr.getMfxSid());
            assertEquals(lbe.getTachoMax(), lbr.getTachoMax());
            assertEquals(lbe.getvMin(), lbr.getvMin());
            assertEquals(lbe.getDecoderType(), lbr.getDecoderType());
            assertEquals(lbe.getVelocity(), lbr.getVelocity());
            assertEquals(lbe.getRichtung(), lbr.getRichtung());
            assertEquals(lbe.isCommuter(), lbr.isCommuter());
            assertEquals(lbe.getLength(), lbr.getLength());
            assertEquals(lbe.isShow(), lbr.isShow());
        }
        assertEquals(expResult, result);
    }

    /**
     * Test of getLocomotive method, of class PersistenceService.
     */
    @Test
    public void testGetLocomotive_Integer_DecoderType() {
        System.out.println("getLocomotive");
        Integer address = 8;
        DecoderType decoderType = DecoderType.MM;

        PersistenceService instance = PersistenceFactory.getService();
        LocomotiveBean expResult = this.locomotives.get(1);
        LocomotiveBean result = instance.getLocomotive(address, decoderType);
        assertEquals(expResult, result);

        assertEquals(expResult.getId(), result.getId());
        assertEquals(expResult.getName(), result.getName());
        assertEquals(expResult.getUid(), result.getUid());
        assertEquals(expResult.getMfxUid(), result.getMfxUid());
        assertEquals(expResult.getAddress(), result.getAddress());
        assertEquals(expResult.getIcon(), result.getIcon());
        assertEquals(expResult.getMfxSid(), result.getMfxSid());
        assertEquals(expResult.getTachoMax(), result.getTachoMax());
        assertEquals(expResult.getvMin(), result.getvMin());
        assertEquals(expResult.getDecoderType(), result.getDecoderType());
        assertEquals(expResult.getVelocity(), result.getVelocity());
        assertEquals(expResult.getRichtung(), result.getRichtung());
        assertEquals(expResult.isCommuter(), result.isCommuter());
        assertEquals(expResult.getLength(), result.getLength());
        assertEquals(expResult.isShow(), result.isShow());

        assertEquals(expResult, result);

        //Check Functions
        List<FunctionBean> resFunctions = new LinkedList<>();
        resFunctions.addAll(result.getFunctions().values());

        assertEquals(2, resFunctions.size());

//        List<LocomotiveFunction> locomotiveFunctions = new LinkedList<>();
//        assertEquals(functions, locomotiveFunctions);
    }

    /**
     * Test of getLocomotive method, of class PersistenceService.
     */
    @Test
    public void testGetLocomotive_Integer() {
        System.out.println("getLocomotive");
        Long id = 2L;
        PersistenceService instance = PersistenceFactory.getService();
        LocomotiveBean expResult = this.locomotives.get(0);
        LocomotiveBean result = instance.getLocomotive(id);
        assertEquals(expResult, result);

        assertEquals(expResult.getId(), result.getId());
        assertEquals(expResult.getName(), result.getName());
        assertEquals(expResult.getUid(), result.getUid());
        assertEquals(expResult.getMfxUid(), result.getMfxUid());
        assertEquals(expResult.getAddress(), result.getAddress());
        assertEquals(expResult.getIcon(), result.getIcon());
        assertEquals(expResult.getMfxSid(), result.getMfxSid());
        assertEquals(expResult.getTachoMax(), result.getTachoMax());
        assertEquals(expResult.getvMin(), result.getvMin());
        assertEquals(expResult.getDecoderType(), result.getDecoderType());
        assertEquals(expResult.getVelocity(), result.getVelocity());
        assertEquals(expResult.getRichtung(), result.getRichtung());
        assertEquals(expResult.isCommuter(), result.isCommuter());
        assertEquals(expResult.getLength(), result.getLength());
        assertEquals(expResult.isShow(), result.isShow());

        assertEquals(expResult, result);
    }

    /**
     * Test of persist method, of class PersistenceService.
     */
    @Test
    public void testPersist_LocomotiveBean() {
        System.out.println("persist");
        LocomotiveBean locomotive = new LocomotiveBean(80L, "DB BR 44 100", 16393L, 1945180593L, 80, "DB BR 44 100", "mfx", "0x81", 80, 5, 0, 0, false, null, true);

        FunctionBean fb80_0 = new FunctionBean(null, 80L, 0, 1, 0);

        locomotive.addFunction(fb80_0);

        PersistenceService instance = PersistenceFactory.getService();
        LocomotiveBean expResult = locomotive;
        LocomotiveBean result = instance.persist(locomotive);

        assertEquals(expResult, result);

        LocomotiveBean loco = instance.getLocomotive(80L);
        assertEquals(locomotive, loco);

        LocomotiveBean loco2 = instance.getLocomotive(80, DecoderType.MFX);
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

        loco2 = instance.getLocomotive(80, DecoderType.MFX);
        assertEquals(loco, loco2);

        instance.remove(locomotive);
    }

    /**
     * Test of remove method, of class PersistenceService.
     */
    @Test
    public void testRemove_LocomotiveBean() {
        System.out.println("remove");
        LocomotiveBean locomotiveBean = new LocomotiveBean(70L, "To Be Removed", 16370L, 1945180570L, 70, "To Be Removed", "mfx", "0x70", 80, 5, 0, 0, false, null, true);
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
     * Test of getFunctionImage method, of class PersistenceService.
     */
    //@Test
    public void testGetFunctionImage() {
        System.out.println("getFunctionImage");
        String imageName = "";
        PersistenceService instance = PersistenceFactory.getService();
        Image expResult = null;
        Image result = instance.getFunctionImage(imageName);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getTurnouts method, of class PersistenceService.
     */
    @Test
    public void testGetTurnouts() {
        System.out.println("getTurnouts");
        PersistenceService instance = PersistenceFactory.getService();

        List<AccessoryBean> expResult = this.turnouts;
        List<AccessoryBean> result = instance.getTurnouts();
        assertEquals(expResult, result);

    }

    /**
     * Test of getSignals method, of class PersistenceService.
     */
    @Test
    public void testGetSignals() {
        System.out.println("getSignals");
        PersistenceService instance = PersistenceFactory.getService();
        List<AccessoryBean> expResult = this.signals;
        List<AccessoryBean> result = instance.getSignals();
        assertEquals(expResult, result);
        assertEquals(expResult, result);
    }

    /**
     * Test of getAccessoryById method, of class PersistenceService.
     */
    @Test
    public void testGetAccessoryById() {
        System.out.println("getAccessoryById");
        Long id = 7L;
        PersistenceService instance = PersistenceFactory.getService();
        AccessoryBean expResult = this.signals.get(2);
        AccessoryBean result = instance.getAccessoryById(id);
        assertEquals(expResult, result);
    }

    /**
     * Test of getAccessory method, of class PersistenceService.
     */
    @Test
    public void testGetAccessory() {
        System.out.println("getAccessory");
        Integer address = 7;
        PersistenceService instance = PersistenceFactory.getService();
        AccessoryBean expResult = this.turnouts.get(3);
        AccessoryBean result = instance.getAccessory(address);
        assertEquals(expResult, result);
    }

    /**
     * Test of persist method, of class PersistenceService.
     */
    @Test
    public void testPersist_AccessoryBean() {
        System.out.println("persist");
        AccessoryBean accessory = new AccessoryBean(null, 100, "W 100", "rechtsweiche", 1, 2, 200, "mm", "ein_alt", "weichen", "005", "magicon_a_005_01.svg");
        PersistenceService instance = PersistenceFactory.getService();

        AccessoryBean result = instance.persist(accessory);

        AccessoryBean expResult = accessory;
        expResult.setId(9L);

        assertEquals(expResult, result);

        AccessoryBean ab = instance.getAccessoryById(expResult.getId());
        assertEquals(accessory, ab);

        accessory.setName("WWWWW");
        instance.persist(accessory);
        ab = instance.getAccessory(expResult.getAddress());
        assertEquals(accessory, ab);
    }

    /**
     * Test of remove method, of class PersistenceService.
     */
    @Test
    public void testRemove_AccessoryBean() {
        System.out.println("remove");
        AccessoryBean accessory = new AccessoryBean(null, 101, "W 101", "rechtsweiche", 1, 2, 200, "mm", "ein_alt", "weichen", "005", "magicon_a_005_01.svg");
        PersistenceService instance = PersistenceFactory.getService();

        AccessoryBean result = instance.persist(accessory);
        accessory.setId(9L);

        assertEquals(accessory, result);

        AccessoryBean ab = instance.getAccessory(accessory.getAddress());

        assertEquals(accessory, ab);

        instance.remove(accessory);

        ab = instance.getAccessoryById(accessory.getId());
        assertNull(ab);
    }

    /**
     * Test of getTiles method, of class PersistenceService.
     */
    @Test
    public void testGetTiles() {
        System.out.println("getTiles");
        PersistenceService instance = PersistenceFactory.getService();

        List<TileBean> result = instance.getTiles();
        List<TileBean> expResult = this.tiles;

        assertEquals(expResult, result);
    }

    /**
     * Test of getTile method, of class PersistenceService.
     */
    @Test
    public void testGetTile() {
        System.out.println("getTile");
        Integer x = 300;
        Integer y = 180;
        PersistenceService instance = PersistenceFactory.getService();
        TileBean expResult = tiles.get(7);
        TileBean result = instance.getTile(x, y);
        assertEquals(expResult, result);
    }

    /**
     * Test of persist method, of class PersistenceService.
     */
    @Test
    public void testPersist_TileBean() {
        System.out.println("persist");
        TileBean sw12 = new TileBean("sw-12", TileType.SWITCH, Orientation.EAST, Direction.RIGHT, 50, 50, null, null, null);

        PersistenceService instance = PersistenceFactory.getService();
        TileBean result = instance.persist(sw12);
        assertEquals(sw12, result);

        TileBean tb = instance.getTile("sw-12");
        assertEquals(sw12, tb);

        sw12.setDirection(Direction.RIGHT);
        result = instance.persist(sw12);
        assertEquals(sw12, result);

        TileBean tb1 = instance.getTile(50, 50);
        assertEquals(sw12, tb1);
    }

    @Test
    public void testRemove_TileBean() {
        System.out.println("remove");
        TileBean sw13 = new TileBean("sw-13", TileType.CURVED, Orientation.EAST, Direction.CENTER, 80, 50, null, null, null);
        PersistenceService instance = PersistenceFactory.getService();

        TileBean result = instance.persist(sw13);
        assertEquals(sw13, result);

        TileBean tb = instance.getTile("sw-13");
        assertEquals(sw13, tb);

        instance.remove(tb);

        TileBean tb1 = instance.getTile(80, 50);
        assertNull(tb1);
    }

    /**
     * Test of persist method, of class PersistenceService.
     */
    @Test
    public void testPersist_List_TileBeans() {
        System.out.println("persist list");
        PersistenceService instance = PersistenceFactory.getService();

        List<TileBean> tbl = this.tiles;

        TileBean sw22 = new TileBean("sw-22", TileType.CROSS, Orientation.EAST, Direction.CENTER, 100, 100, null, null, null);

        List<TileBean> current = instance.getTiles();

        //There should be 10 tiles...
        assertEquals(10, current.size());

        tbl.add(sw22);
        instance.persist(tbl);

        List<TileBean> current2 = instance.getTiles();

        //There should now be 11 tiles...
        assertEquals(11, current2.size());

        TileBean tb = instance.getTile(100, 100);
        assertEquals(sw22, tb);

        tbl.remove(sw22);

        instance.persist(tbl);
        current2 = instance.getTiles();

        //There should now be 10 tiles...
        assertEquals(10, current2.size());
    }

    @Test
    public void testGetRoutes() {
        System.out.println("getRoutes");
        PersistenceService instance = PersistenceFactory.getService();
        List<RouteBean> expResult = this.routes;
        List<RouteBean> result = instance.getRoutes();
        assertEquals(expResult, result);
    }

    @Test
    public void testGetRoute_Integer() {
        System.out.println("getRoute_integer");
        PersistenceService instance = PersistenceFactory.getService();
        RouteBean expResult = this.routes.get(0);
        RouteBean result = instance.getRoute(1);
        assertEquals(expResult, result);
    }

    @Test
    public void testGetRoute_String_String_String_String() {
        System.out.println("getRoute_string_string_string_string");
        PersistenceService instance = PersistenceFactory.getService();
        RouteBean expResult = this.routes.get(1);
        List<RouteElementBean> expRel = expResult.getRouteElements();

        RouteBean result = instance.getRoute("bk-2", "-", "bk-1", "+");
        assertEquals(expResult, result);

        List<RouteElementBean> rerl = result.getRouteElements();

        assertEquals(expRel, rerl);

    }

    @Test
    public void testPersist_RouteBean() {
        System.out.println("persist");
        RouteBean route = new RouteBean(null, "ct-2", "*", "ct-5", "*", "blue");

        List<RouteElementBean> rel = new LinkedList<>();
        RouteElementBean rect2 = new RouteElementBean(null, null, "ct-2", "ct-2", null, 0);
        rel.add(rect2);
        RouteElementBean resi3 = new RouteElementBean(null, null, "si-3", "si-3", null, 1);
        rel.add(resi3);

        route.setRouteElements(rel);

        PersistenceService instance = PersistenceFactory.getService();

        RouteBean result = instance.persist(route);

        route.setId(3L);
        assertEquals(route, result);

        for (RouteElementBean re : rel) {
            re.setRouteId(3L);
            if (re.getNodeId().equals("ct-2")) {
                re.setId(5L);
            } else {
                re.setId(6L);
            }
        }

        List<RouteElementBean> relr = result.getRouteElements();
        assertEquals(rel, relr);

        RouteBean rb = instance.getRoute("ct-2", "*", "ct-5", "*");
        assertEquals(route, rb);

        relr = rb.getRouteElements();
        assertEquals(rel, relr);

        RouteBean rb1 = instance.getRoute(3);
        assertEquals(route, rb1);
    }

    @Test
    public void testRemove_RouteBean() {
        System.out.println("remove");
        RouteBean routeBean = new RouteBean(null, "ct-5", "*", "ct-2", "*", "orange");
        PersistenceService instance = PersistenceFactory.getService();

        RouteBean result = instance.persist(routeBean);

        routeBean.setId(3L);
        assertEquals(routeBean, result);

        RouteBean rb = instance.getRoute("ct-5", "*", "ct-2", "*");
        assertEquals(routeBean, rb);

        instance.remove(routeBean);

        rb = instance.getRoute("ct-5", "*", "ct-2", "*");
        assertNull(rb);
    }

}
