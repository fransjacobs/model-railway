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
package jcs.controller.cs3.net;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author fransjacobs
 */
public class CS3ConnectionFactoryTest {
    
    public CS3ConnectionFactoryTest() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of getInstance method, of class CS3ConnectionFactory.
     */
    @Test
    public void testGetInstance() {
        System.out.println("getInstance");
        CS3ConnectionFactory expResult = null;
        CS3ConnectionFactory result = CS3ConnectionFactory.getInstance();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getConnectionImpl method, of class CS3ConnectionFactory.
     */
    @Test
    public void testGetConnectionImpl() {
        System.out.println("getConnectionImpl");
        CS3ConnectionFactory instance = null;
        CS3Connection expResult = null;
        CS3Connection result = instance.getConnectionImpl();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getConnection method, of class CS3ConnectionFactory.
     */
    @Test
    public void testGetConnection() {
        System.out.println("getConnection");
        CS3Connection expResult = null;
        CS3Connection result = CS3ConnectionFactory.getConnection();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of disconnectAll method, of class CS3ConnectionFactory.
     */
    @Test
    public void testDisconnectAll() {
        System.out.println("disconnectAll");
        CS3ConnectionFactory.disconnectAll();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getHTTPConnectionImpl method, of class CS3ConnectionFactory.
     */
    @Test
    public void testGetHTTPConnectionImpl() {
        System.out.println("getHTTPConnectionImpl");
        CS3ConnectionFactory instance = null;
        HTTPConnection expResult = null;
        HTTPConnection result = instance.getHTTPConnectionImpl();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getHTTPConnection method, of class CS3ConnectionFactory.
     */
    @Test
    public void testGetHTTPConnection() {
        System.out.println("getHTTPConnection");
        HTTPConnection expResult = null;
        HTTPConnection result = CS3ConnectionFactory.getHTTPConnection();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of sendMobileAppPing method, of class CS3ConnectionFactory.
     */
    @Test
    public void testSendMobileAppPing() {
        System.out.println("sendMobileAppPing");
        CS3ConnectionFactory instance = null;
        instance.sendMobileAppPing();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getControllerIpImpl method, of class CS3ConnectionFactory.
     */
    @Test
    public void testGetControllerIpImpl() {
        System.out.println("getControllerIpImpl");
        CS3ConnectionFactory instance = null;
        String expResult = "";
        String result = instance.getControllerIpImpl();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getControllerIp method, of class CS3ConnectionFactory.
     */
    @Test
    public void testGetControllerIp() {
        System.out.println("getControllerIp");
        String expResult = "";
        String result = CS3ConnectionFactory.getControllerIp();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    
}
