/*
 * Copyright 2023 fransjacobs.
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
package jcs.controller.cs;

import jcs.controller.cs.MarklinCS;
import java.awt.Image;
import java.util.List;
import jcs.entities.AccessoryBean;
import jcs.entities.LocomotiveBean;
import jcs.entities.enums.AccessoryValue;
import jcs.entities.enums.DecoderType;
import jcs.entities.enums.Direction;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.tinylog.Logger;

/**
 *
 * @author fransjacobs
 */
public class MarklinCS3Test {
    
    private static MarklinCS instance;
    private static boolean cs3Available = false;
    
    public MarklinCS3Test() {
        try {
            if (instance == null) {
                instance = new MarklinCS();
                pause(500);
                cs3Available = instance.connect();
                
                if (cs3Available) {
                    instance.disconnect();
                }
            }
        } catch (Exception e) {
            Logger.warn("CS 3 not available skipping tests");
        }
    }
    
    @Before
    public void setUp() {
        if (cs3Available) {
            pause(500);
        } else {
            Logger.warn("Skipping tests CS 3 not available");
        }
    }
    
    @After
    public void tearDown() {
        //instance.disconnect();
    }

    /**
     * Test of connect method, of class MarklinCS.
     */
    @Test
    public void testConnect() {
        if (cs3Available) {
            System.out.println("connect");
            boolean expResult = true;
            boolean result = instance.connect();
            assertEquals(expResult, result);
            result = instance.isConnected();
            assertEquals(expResult, result);

            // When really connected the UID of devices should not be 0
            assertTrue(instance.getCsUid() != 0);
            assertTrue(instance.getGfpUid() != 0);
            //Be sure it is connected and powered on
            assertTrue(instance.getLinkSxxUid() != 0);
        }
    }
    
    private void pause(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ex) {
            //ignore
        }
    }

    /**
     * Test of getSystemStatus method, of class MarklinCS.
     */
    @Test
    public void testPower() {
        if (cs3Available) {
            
            if (!instance.isConnected()) {
                return;
            }
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
    }

    /**
     * Test of getDeviceIp method, of class MarklinCS.
     */
    @Test
    public void testGetDeviceIp() {
        if (cs3Available) {
            if (!instance.isConnected()) {
                return;
            }
            System.out.println("getDeviceIp");
            String expResult = "192.168.1.180";
            String result = instance.getIp();
            assertEquals(expResult, result);
        }
    }

    /**
     * Test of disconnect method, of class MarklinCS.
     */
    //@Test
    public void testDisconnect() {
        if (cs3Available) {
            
            if (!instance.isConnected()) {
                return;
            }
            System.out.println("disconnect");
            MarklinCS instance = new MarklinCS();
            instance.disconnect();
            // TODO review the generated test code and remove the default call to fail.
            fail("The test case is a prototype.");
        }
    }

    /**
     * Test of getName method, of class MarklinCS.
     */
    // @Test
    public void testGetName() {
        if (!instance.isConnected()) {
            return;
        }
        System.out.println("getName");
        MarklinCS instance = new MarklinCS();
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
//    public void testToggleDirection() {
//        if(!instance.isConnected()) return;
//        System.out.println("toggleDirection");
//        int address = 0;
//        DecoderType decoderType = null;
//        MarklinCS instance = new MarklinCS();
//        instance.toggleDirection(address, decoderType);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
    /**
     * Test of changeDirection method, of class MarklinCS.
     */
    // @Test
    public void testSetDirection() {
        if (!instance.isConnected()) {
            return;
        }
        System.out.println("setDirection");
        int address = 0;
        DecoderType decoderType = null;
        Direction direction = null;
        MarklinCS instance = new MarklinCS();
        instance.changeDirection(address, decoderType, direction);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getDirection method, of class MarklinCS3.
     */
    //@Test
//    public void testGetDirection() {
//        if(!instance.isConnected()) return;
//        System.out.println("getDirection");
//        int address = 0;
//        DecoderType decoderType = null;
//        MarklinCS instance = new MarklinCS();
//        DirectionInfo expResult = null;
//        DirectionInfo result = instance.getDirection(address, decoderType);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
    /**
     * Test of changeVelocity method, of class MarklinCS.
     */
    //@Test
    public void testSetSpeed() {
        
        if (!instance.isConnected()) {
            return;
        }
        System.out.println("setSpeed");
        int address = 0;
        DecoderType decoderType = null;
        int speed = 0;
        MarklinCS instance = new MarklinCS();
        instance.changeVelocity(address, decoderType, speed);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of changeFunctionValue method, of class MarklinCS.
     */
    // @Test
    public void testSetFunction() {
        if (!instance.isConnected()) {
            return;
        }
        System.out.println("setFunction");
        int address = 0;
        DecoderType decoderType = null;
        int functionNumber = 0;
        boolean flag = false;
        MarklinCS instance = new MarklinCS();
        instance.changeFunctionValue(address, decoderType, functionNumber, flag);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of switchAccessory method, of class MarklinCS.
     */
    //@Test
    public void testSwitchAccessoiry() {
        if (!instance.isConnected()) {
            return;
        }
        System.out.println("switchAccessoiry");
        int address = 0;
        AccessoryValue value = null;
        MarklinCS instance = new MarklinCS();
        instance.switchAccessory(address, value);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getLocomotives method, of class MarklinCS.
     */
    //@Test
    public void testGetLocomotives() {
        if (!instance.isConnected()) {
            return;
        }
        System.out.println("getLocomotives");
        MarklinCS instance = new MarklinCS();
        List<LocomotiveBean> expResult = null;
        List<LocomotiveBean> result = instance.getLocomotives();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of cacheAllFunctionIcons method, of class MarklinCS.
     */
    //@Test
    public void testGetAllFunctionIcons() {
        if (!instance.isConnected()) {
            return;
        }
        System.out.println("getAllFunctionIcons");
        MarklinCS instance = new MarklinCS();
        instance.cacheAllFunctionIcons(null);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getAccessories method, of class MarklinCS.
     */
    //@Test
    public void testGetAccessories() {
        if (!instance.isConnected()) {
            return;
        }
        System.out.println("getAccessories");
        MarklinCS instance = new MarklinCS();
        List<AccessoryBean> expResult = null;
        List<AccessoryBean> result = instance.getAccessories();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getLocomotiveImage method, of class MarklinCS.
     */
    //@Test
    public void testGetLocomotiveImage() {
        System.out.println("getLocomotiveImage");
        String icon = "";
        MarklinCS instance = new MarklinCS();
        Image expResult = null;
        Image result = instance.getLocomotiveImage(icon);
        assertEquals(expResult, result);
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
//        MarklinCS instance = new MarklinCS();
//        SensorMessageEvent expResult = null;
//        SensorMessageEvent result = instance.querySensor(contactId);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
}
