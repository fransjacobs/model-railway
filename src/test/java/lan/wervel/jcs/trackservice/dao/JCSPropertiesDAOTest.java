/*
 * Copyright (C) 2020 Frans Jacobs.
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
package lan.wervel.jcs.trackservice.dao;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lan.wervel.jcs.entities.JCSProperty;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Frans Jacobs
 */
public class JCSPropertiesDAOTest {

    private final List<JCSProperty> jcsPropertyList;

    public JCSPropertiesDAOTest() {
        DAOTestHelper.setConnectProperties();
        DAOTestHelper.createNewDatabase();
        DAOTestHelper.insertJCSPropertiesData();
        jcsPropertyList = new ArrayList<>();
    }

    @Before
    public void setUp() {

        JCSProperty p0 = new JCSProperty("S88-demo", "lan.wervel.jcs.feedback.DemoFeedbackService");
        JCSProperty p1 = new JCSProperty("S88-remote", "FeedbackService");
        JCSProperty p2 = new JCSProperty("S88-CS2", "lan.wervel.jcs.feedback.cs2.CS2FeedbackService");
        JCSProperty p3 = new JCSProperty("activeFeedbackService", "CS2FeedbackService");

        JCSProperty p4 = new JCSProperty("M6050-remote", "ControllerService");
        JCSProperty p5 = new JCSProperty("CS2", "lan.wervel.jcs.controller.cs2.CS2Controller");
        JCSProperty p6 = new JCSProperty("M6050-demo", "lan.wervel.jcs.controller.m6050.M6050DemoController");
        JCSProperty p7 = new JCSProperty("M6050-local", "lan.wervel.jcs.controller.m6050.M6050Controller");
        JCSProperty p8 = new JCSProperty("activeControllerService", "CS2");

        JCSProperty p9 = new JCSProperty("k1", "v1");
        JCSProperty p10 = new JCSProperty("k2", "v2");

        jcsPropertyList.add(p0);
        jcsPropertyList.add(p1);
        jcsPropertyList.add(p2);
        jcsPropertyList.add(p3);
        jcsPropertyList.add(p4);
        jcsPropertyList.add(p5);
        jcsPropertyList.add(p6);
        jcsPropertyList.add(p7);
        jcsPropertyList.add(p8);
        jcsPropertyList.add(p9);
        jcsPropertyList.add(p10);
    }

    @Test
    public void testFindAll() {
        System.out.println("findAll");
        JCSPropertiesDAO instance = new JCSPropertiesDAO();
        List<JCSProperty> expResult = jcsPropertyList;
        List<JCSProperty> result = instance.findAll();
        assertEquals(expResult, result);
    }

    @Test
    public void testFind_String() {
        System.out.println("find");
        String key = "k2";
        JCSPropertiesDAO instance = new JCSPropertiesDAO();
        JCSProperty expResult = jcsPropertyList.get(10);
        JCSProperty result = instance.find(key);
        assertEquals(expResult, result);
    }

    @Test
    public void testPersist() {
        System.out.println("persist");
        JCSProperty property = new JCSProperty("k3", "v3");
        JCSPropertiesDAO instance = new JCSPropertiesDAO();
        BigDecimal expResult = new BigDecimal(12);
        BigDecimal result = instance.persist(property);
        assertEquals(expResult, result);

        //really check
        JCSProperty chkP = instance.find("k3");
        assertEquals(property, chkP);

        property.setValue("UPDATED");
        result = instance.persist(property);
        assertEquals(expResult, result);

        chkP = instance.find("k3");
        assertEquals(property, chkP);

    }

    @Test
    public void testRemove() {
        System.out.println("remove");
        JCSProperty property = new JCSProperty("k4", "v4");
        JCSPropertiesDAO instance = new JCSPropertiesDAO();
        instance.persist(property);
        JCSProperty chkP = instance.find("k4");
        assertEquals(property, chkP);

        instance.remove(property);
        chkP = instance.find("k4");
        assertNull(chkP);
    }

}
