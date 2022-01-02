/*
 * Copyright (C) 2019 frans.
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
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import jcs.entities.SwitchBean;
import jcs.entities.enums.AccessoryValue;
import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author frans
 */
public class SwitchDAOTest {

    private final List<SwitchBean> turnouts;

    public SwitchDAOTest() {
        DAOTestHelper.setConnectProperties();
        DAOTestHelper.createNewDatabase();
        DAOTestHelper.insertTurnoutData();

        turnouts = new ArrayList<>();
    }

    @Before
    public void setUp() {
        SwitchBean t1 = new SwitchBean(1, "T 1", "5117 R");

        t1.setValue(AccessoryValue.GREEN);
        t1.setName("T 1");
        t1.setId(new BigDecimal(1));
        t1.setSwitchTime(200);
        turnouts.add(t1);

        SwitchBean t2 = new SwitchBean(2, "T 2", "5117 L");
        t2.setValue(AccessoryValue.RED);
        t2.setName("T 2");
        t2.setId(new BigDecimal(2));
        t2.setSwitchTime(250);
        turnouts.add(t2);
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testFindAll() {
        System.out.println("findAll");
        SwitchDAO instance = new SwitchDAO();
        List<SwitchBean> expResult = turnouts;
        List<SwitchBean> result = instance.findAll();

        assertEquals(expResult, result);
        result = instance.findAll();
        assertEquals(turnouts, result);
    }

    @Test
    public void testFind() {
        System.out.println("find");

        Integer address = 2;
        SwitchDAO instance = new SwitchDAO();
        SwitchBean expResult = turnouts.get(1);
        SwitchBean result = instance.find(address);
        assertEquals(expResult, result);

        address = 1;
        expResult = turnouts.get(0);
        result = instance.find(address);
        assertEquals(expResult, result);
    }

    //@Test
    public void testPersist() {
        System.out.println("persist");

        SwitchBean t = new SwitchBean(3, "T 3", "5117 R");
        t.setValue(AccessoryValue.GREEN);
        t.setName("T 3");

        SwitchDAO instance = new SwitchDAO();
        BigDecimal result = instance.persist(t);
        SwitchBean tr = instance.find(3);

        BigDecimal expResult = new BigDecimal(3);
        assertEquals(expResult, result);
        assertNotNull(tr);
    }

    //@Test
    public void testPersistUpdate() {
        System.out.println("persist");
        SwitchDAO instance = new SwitchDAO();

        SwitchBean t = instance.find(2);
        assertNotNull(t);
        assertEquals(AccessoryValue.RED, t.getValue());
        BigDecimal expResult = t.getId();
        t.setValue(AccessoryValue.GREEN);

        BigDecimal result = instance.persist(t);
        assertEquals(expResult, result);

        SwitchBean tr = instance.find(2);
        assertNotNull(tr);
        assertEquals(AccessoryValue.GREEN, tr.getValue());
    }

    //@Test
    public void testPersistNullValue() {
        System.out.println("persistNullValue");

        SwitchBean t = new SwitchBean(4, "T 4", "5117 R");
        t.setName("T 4");

        SwitchDAO instance = new SwitchDAO();
        BigDecimal result = instance.persist(t);
        SwitchBean tr = instance.find(4);

        BigDecimal expResult = new BigDecimal(3);
        assertEquals(expResult, result);
        assertNotNull(tr);

        assertEquals(AccessoryValue.OFF, tr.getValue());
    }

    //@Test
    public void testPersist2() {
        System.out.println("persist");
        SwitchBean t = new SwitchBean(79, "T 79", "5117 L");

        t.setValue(AccessoryValue.OFF);
        t.setName("W 79");

        SwitchDAO instance = new SwitchDAO();
        BigDecimal result = instance.persist(t);

        SwitchBean tr = instance.find(79);

        BigDecimal expResult = new BigDecimal(3);
        assertEquals(expResult, result);

        assertNotNull(tr);

        int sas = instance.findAll().size();
        assertEquals(3, sas);
    }

    //@Test
    public void testRemove() {
        System.out.println("remove");
        SwitchBean t = new SwitchBean(12, "T 12", "5117 L");
        t.setValue(AccessoryValue.GREEN);
        t.setName("T 12");
        t.setValue(AccessoryValue.OFF);
        t.setName("W 12");
        t.setSwitchTime(0);

        SwitchDAO instance = new SwitchDAO();

        instance.persist(t);
        SwitchBean tr = instance.find(12);

        assertEquals(t, tr);
        instance.remove(t);

        tr = instance.find(12);
        assertNull(tr);
    }
}
