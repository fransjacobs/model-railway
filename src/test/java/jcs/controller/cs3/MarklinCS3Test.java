/*
 * Copyright (C) 2022 fransjacobs.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package jcs.controller.cs3;

import jcs.controller.cs3.can.parser.DirectionInfo;
import java.awt.Image;
import java.util.List;
import jcs.controller.ControllerEventListener;
import jcs.controller.HeartbeatListener;
import jcs.controller.cs3.events.CanMessageListener;
import jcs.controller.cs3.events.SensorMessageEvent;
import jcs.controller.cs3.events.SensorMessageListener;
import jcs.entities.AccessoryBean;
import jcs.entities.LocomotiveBean;
import jcs.entities.enums.AccessoryValue;
import jcs.entities.enums.DecoderType;
import jcs.entities.enums.Direction;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author fransjacobs
 */
public class MarklinCS3Test {

    private static MarklinCS3 instance;

    public MarklinCS3Test() {
        if (instance == null) {
            instance = new MarklinCS3();
        }
    }

    @Before
    public void setUp() {
        pause(500);
    }

    @After
    public void tearDown() {
        //instance.disconnect();
    }

    /**
     * Test of connect method, of class MarklinCS3.
     */
    @Test
    public void testConnect() {
        System.out.println("connect");
        boolean expResult = true;
        boolean result = instance.connect();
        assertEquals(expResult, result);
        result = instance.isConnected();
        assertEquals(expResult, result);

        // When really connected the UID of devices should not be 0
        assertTrue(instance.getCs3Uid() != 0);
        assertTrue(instance.getGfpUid() != 0);
        //Be sure it is connected and powered on
        assertTrue(instance.getLinkSxxUid() != 0);
    }

    private void pause(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ex) {
            //ignore
        }
    }

    /**
     * Test of getSystemStatus method, of class MarklinCS3.
     */
    @Test
    public void testPower() {
        if(!instance.isConnected()) return;
        System.out.println("isPower");
        //The gfpUid should not be 0 when connected to a real CS..
        int gfpuid = instance.getGfpUid();
        assertTrue(gfpuid != 0);

        boolean expResult = instance.power(true);
        pause(500);
        boolean result = instance.isPower();
        assertEquals(expResult, result);

        pause(500);
        expResult = instance.power(false);
        pause(500);
        result = instance.isPower();
        assertEquals(expResult, result);

    }

    /**
     * Test of getControllerInfo method, of class MarklinCS3.
     */
    //@Test
//    public void testGetControllerInfo() {
//        System.out.println("getControllerInfo");
//        StatusDataConfigParser expResult = null;
//        StatusDataConfigParser result = instance.getControllerInfo();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }

    /**
     * Test of getDeviceIp method, of class MarklinCS3.
     */
    @Test
    public void testGetDeviceIp() {
        if(!instance.isConnected()) return;
        System.out.println("getDeviceIp");
        String expResult = "192.168.1.180";
        String result = instance.getIp();
        assertEquals(expResult, result);
    }

    /**
     * Test of disconnect method, of class MarklinCS3.
     */
    //@Test
    public void testDisconnect() {
        if(!instance.isConnected()) return;
        System.out.println("disconnect");
        MarklinCS3 instance = new MarklinCS3();
        instance.disconnect();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getName method, of class MarklinCS3.
     */
    // @Test
    public void testGetName() {
        if(!instance.isConnected()) return;
        System.out.println("getName");
        MarklinCS3 instance = new MarklinCS3();
        String expResult = "";
        String result = instance.getName();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of toggleDirection method, of class MarklinCS3.
     */
    //@Test
    public void testToggleDirection() {
        if(!instance.isConnected()) return;
        System.out.println("toggleDirection");
        int address = 0;
        DecoderType decoderType = null;
        MarklinCS3 instance = new MarklinCS3();
        instance.toggleDirection(address, decoderType);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of changeDirection method, of class MarklinCS3.
     */
    // @Test
    public void testSetDirection() {
        if(!instance.isConnected()) return;
        System.out.println("setDirection");
        int address = 0;
        DecoderType decoderType = null;
        Direction direction = null;
        MarklinCS3 instance = new MarklinCS3();
        instance.changeDirection(address, decoderType, direction);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getDirection method, of class MarklinCS3.
     */
    //@Test
    public void testGetDirection() {
        if(!instance.isConnected()) return;
        System.out.println("getDirection");
        int address = 0;
        DecoderType decoderType = null;
        MarklinCS3 instance = new MarklinCS3();
        DirectionInfo expResult = null;
        DirectionInfo result = instance.getDirection(address, decoderType);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setSpeed method, of class MarklinCS3.
     */
    //@Test
    public void testSetSpeed() {
        if(!instance.isConnected()) return;
        System.out.println("setSpeed");
        int address = 0;
        DecoderType decoderType = null;
        int speed = 0;
        MarklinCS3 instance = new MarklinCS3();
        instance.setSpeed(address, decoderType, speed);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setFunction method, of class MarklinCS3.
     */
    // @Test
    public void testSetFunction() {
         if(!instance.isConnected()) return;
       System.out.println("setFunction");
        int address = 0;
        DecoderType decoderType = null;
        int functionNumber = 0;
        boolean flag = false;
        MarklinCS3 instance = new MarklinCS3();
        instance.setFunction(address, decoderType, functionNumber, flag);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of switchAccessory method, of class MarklinCS3.
     */
    //@Test
    public void testSwitchAccessoiry() {
         if(!instance.isConnected()) return;
       System.out.println("switchAccessoiry");
        int address = 0;
        AccessoryValue value = null;
        MarklinCS3 instance = new MarklinCS3();
        instance.switchAccessory(address, value);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getLocomotives method, of class MarklinCS3.
     */
    //@Test
    public void testGetLocomotives() {
        if(!instance.isConnected()) return;
        System.out.println("getLocomotives");
        MarklinCS3 instance = new MarklinCS3();
        List<LocomotiveBean> expResult = null;
        List<LocomotiveBean> result = instance.getLocomotives();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of cacheAllFunctionIcons method, of class MarklinCS3.
     */
    //@Test
    public void testGetAllFunctionIcons() {
        if(!instance.isConnected()) return;
        System.out.println("getAllFunctionIcons");
        MarklinCS3 instance = new MarklinCS3();
        instance.cacheAllFunctionIcons();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getAccessories method, of class MarklinCS3.
     */
    //@Test
    public void testGetAccessories() {
        if(!instance.isConnected()) return;
        System.out.println("getAccessories");
        MarklinCS3 instance = new MarklinCS3();
        List<AccessoryBean> expResult = null;
        List<AccessoryBean> result = instance.getAccessories();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getLocomotiveImage method, of class MarklinCS3.
     */
    //@Test
    public void testGetLocomotiveImage() {
        System.out.println("getLocomotiveImage");
        String icon = "";
        MarklinCS3 instance = new MarklinCS3();
        Image expResult = null;
        Image result = instance.getLocomotiveImage(icon);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getDeviceInfo method, of class MarklinCS3.
     */
    //@Test
//    public void testGetDeviceInfo() {
//        System.out.println("getDeviceInfo");
//        MarklinCS3 instance = new MarklinCS3();
//        StatusDataConfigParser expResult = null;
//        StatusDataConfigParser result = instance.getCs3Device();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
    /**
     * Test of addControllerEventListener method, of class MarklinCS3.
     */
    //@Test
    public void testAddControllerEventListener() {
        System.out.println("addControllerEventListener");
        ControllerEventListener listener = null;
        MarklinCS3 instance = new MarklinCS3();
        instance.addControllerEventListener(listener);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of removeControllerEventListener method, of class MarklinCS3.
     */
    //@Test
    public void testRemoveControllerEventListener() {
        System.out.println("removeControllerEventListener");
        ControllerEventListener listener = null;
        MarklinCS3 instance = new MarklinCS3();
        instance.removeControllerEventListener(listener);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of notifyAllControllerEventListeners method, of class MarklinCS3.
     */
    //@Test
    public void testNotifyAllControllerEventListeners() {
        System.out.println("notifyAllControllerEventListeners");
        MarklinCS3 instance = new MarklinCS3();
        instance.notifyAllControllerEventListeners();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of addHeartbeatListener method, of class MarklinCS3.
     */
    //@Test
//    public void testAddHeartbeatListener() {
//        System.out.println("addHeartbeatListener");
//        HeartbeatListener listener = null;
//        MarklinCS3 instance = new MarklinCS3();
//        instance.addHeartbeatListener(listener);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }

    /**
     * Test of removeHeartbeatListener method, of class MarklinCS3.
     */
    //@Test
//    public void testRemoveHeartbeatListener() {
//        System.out.println("removeHeartbeatListener");
//        HeartbeatListener listener = null;
//        MarklinCS3 instance = new MarklinCS3();
//        instance.removeHeartbeatListener(listener);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }

    /**
     * Test of removeAllHeartbeatListeners method, of class MarklinCS3.
     */
    //@Test
//    public void testRemoveAllHeartbeatListeners() {
//        System.out.println("removeAllHeartbeatListeners");
//        MarklinCS3 instance = new MarklinCS3();
//        instance.removeAllHeartbeatListeners();
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }

    /**
     * Test of addCanMessageListener method, of class MarklinCS3.
     */
    //@Test
//    public void testAddCanMessageListener() {
//        System.out.println("addCanMessageListener");
//        CanMessageListener listener = null;
//        MarklinCS3 instance = new MarklinCS3();
//        instance.addCanMessageListener(listener);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }

    /**
     * Test of removeCanMessageListener method, of class MarklinCS3.
     */
    //@Test
//    public void testRemoveCanMessageListener() {
//        System.out.println("removeCanMessageListener");
//        CanMessageListener listener = null;
//        MarklinCS3 instance = new MarklinCS3();
//        instance.removeCanMessageListener(listener);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }

    /**
     * Test of addSensorMessageListener method, of class MarklinCS3.
     */
    //@Test
    public void testAddSensorMessageListener() {
        System.out.println("addSensorMessageListener");
        SensorMessageListener listener = null;
        MarklinCS3 instance = new MarklinCS3();
        instance.addSensorMessageListener(listener);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of removeSensorMessageListener method, of class MarklinCS3.
     */
    //@Test
    public void testRemoveSensorMessageListener() {
        System.out.println("removeSensorMessageListener");
        SensorMessageListener listener = null;
        MarklinCS3 instance = new MarklinCS3();
        instance.removeSensorMessageListener(listener);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of querySensor method, of class MarklinCS3.
     */
    //@Test
//    public void testQuerySensor() {
//        System.out.println("querySensor");
//        int contactId = 0;
//        MarklinCS3 instance = new MarklinCS3();
//        SensorMessageEvent expResult = null;
//        SensorMessageEvent result = instance.querySensor(contactId);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }

    /**
     * Test of querySensors method, of class MarklinCS3.
     */
    //@Test
    public void testQuerySensors() {
        System.out.println("querySensors");
        int sensorCount = 0;
        MarklinCS3 instance = new MarklinCS3();
        List<SensorMessageEvent> expResult = null;
        List<SensorMessageEvent> result = instance.querySensors(sensorCount);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

}
