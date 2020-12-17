/*
 * Copyright (C) 2020 fransjacobs.
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
package lan.wervel.jcs.controller.cs2;

import lan.wervel.jcs.controller.cs2.can.CanMessage;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author fransjacobs
 */
public class FeedbackEventStatusTest {
    
    private CanMessage message;

    public FeedbackEventStatusTest() {
    }
    
    
    @Before
    public void setUp() {
        message = new CanMessage(new int[] { 0x00, 0x23, 0xcb, 0x12, 0x08, 0x00, 0x00, 0x00, 0x30, 0x00, 0x01, 0x0f,0x59 } );
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of isNewValue method, of class FeedbackEventStatus.
     */
    @Test
    public void testIsNewValue() {
        System.out.println("isNewValue");
        SensorEvent instance = new SensorEvent(message);
        boolean expResult = true;
        boolean result = instance.isNewValue();
        assertEquals(expResult, result);
    }

    /**
     * Test of isOldValue method, of class FeedbackEventStatus.
     */
    @Test
    public void testIsOldValue() {
        System.out.println("isOldValue");
        SensorEvent instance = new SensorEvent(message);
        boolean expResult = false;
        boolean result = instance.isOldValue();
        assertEquals(expResult, result);
    }

    /**
     * Test of getContactId method, of class FeedbackEventStatus.
     */
    @Test
    public void testGetContactId() {
        System.out.println("getContactId");
        SensorEvent instance = new SensorEvent(message);
        int expResult = 48;
        int result = instance.getContactId();
        assertEquals(expResult, result);
    }

    /**
     * Test of getDeviceId method, of class FeedbackEventStatus.
     */
    @Test
    public void testGetDeviceId() {
        System.out.println("getDeviceId");
        SensorEvent instance = new SensorEvent(message);
        int expResult = 0;
        int result = instance.getDeviceId();
        assertEquals(expResult, result);
    }

    /**
     * Test of getMillis method, of class FeedbackEventStatus.
     */
    @Test
    public void testGetMillis() {
        System.out.println("getMillis");
        SensorEvent instance = new SensorEvent(message);
        int expResult = 39290;
        int result = instance.getMillis();
        assertEquals(expResult, result);
    }

    /**
     * Test of toString method, of class FeedbackEventStatus.
     */
    @Test
    public void testToString() {
        System.out.println("toString");
        SensorEvent instance = new SensorEvent(message);
        String expResult = "FeedbackEventStatus{newValue=true, oldValue=false, contactId=48, deviceId=0, millis=39290}";
        String result = instance.toString();
        assertEquals(expResult, result);
    }
    
}
