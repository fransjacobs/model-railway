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
package jcs.trackservice.dao;

import jcs.trackservice.dao.util.DAOTestHelper;
import java.util.LinkedList;
import java.util.List;
import jcs.entities.JCSProperty;
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
        jcsPropertyList = new LinkedList<>();
    }

    @Before
    public void setUp() {

        JCSProperty p0 = new JCSProperty("CS3", "jcs.controller.cs3.MarklinCS3");
        JCSProperty p10 = new JCSProperty("k1", "v1");
        JCSProperty p11 = new JCSProperty("k2", "v2");

        jcsPropertyList.add(p0);
        jcsPropertyList.add(p10);
        jcsPropertyList.add(p11);
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
        JCSProperty expResult = jcsPropertyList.get(2);
        JCSProperty result = instance.find(key);
        assertEquals(expResult, result);
    }

    @Test
    public void testPersist() {
        System.out.println("persist");
        JCSProperty property = new JCSProperty("k3", "v3");
        JCSPropertiesDAO instance = new JCSPropertiesDAO();
        String expResult = "k3";
        String result = instance.persist(property);
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
