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
package jcs.commandStation.marklin.cs.net;

import org.junit.After;
import org.junit.Before;
import static org.junit.Assert.*;

/**
 *
 * @author fransjacobs
 */
public class CSConnectionFactoryTest {
    
    public CSConnectionFactoryTest() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of getInstance method, of class CSConnectionFactory.
     */
    //@Test
    public void testGetInstance() {
        System.out.println("getInstance");
        CSConnectionFactory expResult = null;
        CSConnectionFactory result = CSConnectionFactory.getInstance();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getConnectionImpl method, of class CSConnectionFactory.
     */
   //@Test
    public void testGetConnectionImpl() {
        System.out.println("getConnectionImpl");
        CSConnectionFactory instance = null;
        CSConnection expResult = null;
        CSConnection result = instance.getConnectionImpl();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getConnection method, of class CSConnectionFactory.
     */
    //@Test
    public void testGetConnection() {
        System.out.println("getConnection");
        CSConnection expResult = null;
        CSConnection result = CSConnectionFactory.getConnection();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of disconnectAll method, of class CSConnectionFactory.
     */
    //@Test
    public void testDisconnectAll() {
        System.out.println("disconnectAll");
        CSConnectionFactory.disconnectAll();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getHTTPConnectionImpl method, of class CSConnectionFactory.
     */
    //@Test
    public void testGetHTTPConnectionImpl() {
        System.out.println("getHTTPConnectionImpl");
        CSConnectionFactory instance = null;
        HTTPConnection expResult = null;
        HTTPConnection result = instance.getHTTPConnectionImpl(true);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getHTTPConnection method, of class CSConnectionFactory.
     */
    //@Test
    public void testGetHTTPConnection() {
        System.out.println("getHTTPConnection");
        HTTPConnection expResult = null;
        HTTPConnection result = CSConnectionFactory.getHTTPConnection(true);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of sendMobileAppPing method, of class CSConnectionFactory.
     */
    //@Test
    public void testSendMobileAppPing() {
        System.out.println("sendMobileAppPing");
        CSConnectionFactory instance = null;
        instance.sendMobileAppPing();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getControllerIpImpl method, of class CSConnectionFactory.
     */
    //@Test
    public void testGetControllerIpImpl() {
        System.out.println("getControllerIpImpl");
        CSConnectionFactory instance = null;
        String expResult = "";
        String result = instance.getControllerIpImpl();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getControllerIp method, of class CSConnectionFactory.
     */
    //@Test
    public void testGetControllerIp() {
        System.out.println("getControllerIp");
        String expResult = "";
        String result = CSConnectionFactory.getControllerIp();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    
}
