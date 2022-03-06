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
package jcs.trackservice.dao;

import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;
import jcs.entities.AccessoryBean;
import jcs.trackservice.dao.util.DAOTestHelper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author fransjacobs
 */
public class AccessoryBeanDAOTest {

    private final List<AccessoryBean> turnouts;

    public AccessoryBeanDAOTest() {
        DAOTestHelper.setConnectProperties();
        DAOTestHelper.createNewDatabase();
        DAOTestHelper.insertTurnoutData();

        turnouts = new LinkedList<>();
    }

    @Before
    public void setUp() {
        AccessoryBean ab1 = new AccessoryBean(new BigDecimal(1), "W 1R", "rechtsweiche", 1, 200, "mm2", "ein_alt");
        turnouts.add(ab1);
        AccessoryBean ab2 = new AccessoryBean(new BigDecimal(2), "W 2L", "linksweiche", 1, 200, "mm2", "ein_alt");
        turnouts.add(ab2);
        AccessoryBean ab3 = new AccessoryBean(new BigDecimal(3), "W 3R", "rechtsweiche", 0, 200, "mm2", "ein_alt");
        turnouts.add(ab3);
        AccessoryBean ab4 = new AccessoryBean(new BigDecimal(4), "W 4R", "rechtsweiche", 1, 200, "mm2", "ein_alt");
        turnouts.add(ab4);
        AccessoryBean ab5 = new AccessoryBean(new BigDecimal(5), "W 5R", "rechtsweiche", 1, 200, "mm2", "ein_alt");
        turnouts.add(ab5);
        AccessoryBean ab6 = new AccessoryBean(new BigDecimal(6), "W 6R", "rechtsweiche", 0, 200, "mm2", "ein_alt");
        turnouts.add(ab6);
        AccessoryBean ab7 = new AccessoryBean(new BigDecimal(7), "W 7L", "linksweiche", 1, 200, "mm2", "ein_alt");
        turnouts.add(ab7);
        AccessoryBean ab8 = new AccessoryBean(new BigDecimal(8), "W 8L", "linksweiche", 0, 200, "mm2", "ein_alt");
        turnouts.add(ab8);
        AccessoryBean ab9 = new AccessoryBean(new BigDecimal(9), "W 9R", "rechtsweiche", 0, 200, "mm2", "ein_alt");
        turnouts.add(ab9);
        AccessoryBean ab10 = new AccessoryBean(new BigDecimal(10), "W 10R", "rechtsweiche", 0, 200, "mm2", "ein_alt");
        turnouts.add(ab10);
        AccessoryBean ab11 = new AccessoryBean(new BigDecimal(11), "W 11L", "linksweiche", 1, 200, "mm2", "ein_alt");
        turnouts.add(ab11);
        AccessoryBean ab12 = new AccessoryBean(new BigDecimal(12), "W 12L", "linksweiche", 0, 200, "mm2", "ein_alt");
        turnouts.add(ab12);
        AccessoryBean ab13 = new AccessoryBean(new BigDecimal(13), "W 13L", "linksweiche", 0, 200, "mm2", "ein_alt");
        turnouts.add(ab13);
        AccessoryBean ab14 = new AccessoryBean(new BigDecimal(14), "W 14R", "rechtsweiche", 1, 200, "mm2", "ein_alt");
        turnouts.add(ab14);
        AccessoryBean ab17 = new AccessoryBean(new BigDecimal(15), "W 17R", "rechtsweiche", 1, 200, "mm2", "ein_alt");
        turnouts.add(ab17);
        AccessoryBean ab18 = new AccessoryBean(new BigDecimal(16), "W 18R", "rechtsweiche", 1, 200, "mm2", "ein_alt");
        turnouts.add(ab18);
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of findAll method, of class AccessoryBeanDAO.
     */
    @Test
    public void testFindAll() {
        System.out.println("findAll");
        AccessoryBeanDAO instance = new AccessoryBeanDAO();
        List<AccessoryBean> expResult = this.turnouts;
        List<AccessoryBean> result = instance.findAll();
        assertEquals(expResult, result);
    }

    /**
     * Test of findBy method, of class AccessoryBeanDAO.
     */
    @Test
    public void testFindBy() {
        System.out.println("findBy");
        String key = "rechtsweiche";
        AccessoryBeanDAO instance = new AccessoryBeanDAO();
        List<AccessoryBean> expResult = new LinkedList<>();
        for (AccessoryBean ab : this.turnouts) {
            if ("rechtsweiche".equals(ab.getType())) {
                expResult.add(ab);
            }
        }

        List<AccessoryBean> result = instance.findBy(key);
        assertEquals(expResult, result);
    }

    /**
     * Test of findById method, of class AccessoryBeanDAO.
     */
    @Test
    public void testFindById() {
        System.out.println("findById");
        BigDecimal id = new BigDecimal(7);
        AccessoryBeanDAO instance = new AccessoryBeanDAO();
        AccessoryBean expResult = this.turnouts.get(6);
        AccessoryBean result = instance.findById(id);
        assertEquals(expResult, result);
    }

    /**
     * Test of persist method, of class AccessoryBeanDAO.
     */
    @Test
    public void testPersist() {
        System.out.println("persist");
        AccessoryBean accessory = new AccessoryBean("W 100", "rechtsweiche", 0, 200, "mm2", "ein_alt");

        AccessoryBeanDAO instance = new AccessoryBeanDAO();
        BigDecimal expResult = new BigDecimal(17);
        BigDecimal result = instance.persist(accessory);
        assertEquals(expResult, result);

        AccessoryBean ab = instance.findById(expResult);

        assertEquals(accessory, ab);

        accessory.setName("WWWWW");
        instance.persist(accessory);
        ab = instance.findById(expResult);
        assertEquals(accessory, ab);
    }

    /**
     * Test of remove method, of class AccessoryBeanDAO.
     */
    @Test
    public void testRemove() {
        System.out.println("remove");
        AccessoryBean accessoiry = new AccessoryBean("W 101", "rechtsweiche", 0, 200, "mm2", "ein_alt");
        AccessoryBeanDAO instance = new AccessoryBeanDAO();

        BigDecimal expResult = new BigDecimal(17);
        BigDecimal result = instance.persist(accessoiry);
        assertEquals(expResult, result);

        AccessoryBean ab = instance.findById(expResult);

        assertEquals(accessoiry, ab);

        instance.remove(accessoiry);

        ab = instance.findById(expResult);

        assertNull(ab);
    }

}
