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
import jcs.entities.RouteBean;
import jcs.entities.RouteElementBean;
import jcs.entities.enums.AccessoryValue;
import jcs.trackservice.dao.util.DAOTestHelper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author fransjacobs
 */
public class RouteElementDAOTest {

    private List<RouteElementBean> routeElements;

    public RouteElementDAOTest() {
        DAOTestHelper.setConnectProperties();
        DAOTestHelper.createNewDatabase();
        DAOTestHelper.insertTileLayoutData();
        DAOTestHelper.insertRouteData();
        DAOTestHelper.insertRouteElementData();
    }

    @Before
    public void setUp() {

        routeElements = new LinkedList<>();

        RouteBean r1 = new RouteBean("bk-1+|bk-3-", "bk-1+", "bk-3-", "red");
        RouteBean r2 = new RouteBean("bk-2+|bk-3-", "bk-2+", "bk-3-", "green");

        RouteElementBean re0 = new RouteElementBean("bk-1+|bk-3-", "bk-1+", "bk-1", null, 0, new BigDecimal(1));
        RouteElementBean re1 = new RouteElementBean("bk-1+|bk-3-", "se-2", "se-2", null, 1, new BigDecimal(2));
        RouteElementBean re2 = new RouteElementBean("bk-1+|bk-3-", "st-2", "st-2", null, 2, new BigDecimal(3));
        RouteElementBean re3 = new RouteElementBean("bk-1+|bk-3-", "sw-2-G", "sw-2", AccessoryValue.GREEN, 3, new BigDecimal(4));
        RouteElementBean re4 = new RouteElementBean("bk-1+|bk-3-", "sw-2", "sw-2", AccessoryValue.GREEN, 4, new BigDecimal(5));
        RouteElementBean re5 = new RouteElementBean("bk-1+|bk-3-", "st-5", "st-5", null, 5, new BigDecimal(6));
        RouteElementBean re6 = new RouteElementBean("bk-1+|bk-3-", "ct-4", "ct-4", null, 6, new BigDecimal(7));
        RouteElementBean re7 = new RouteElementBean("bk-1+|bk-3-", "st-11", "st-11", null, 7, new BigDecimal(8));
        RouteElementBean re8 = new RouteElementBean("bk-1+|bk-3-", "st-12", "st-12", null, 8, new BigDecimal(9));
        RouteElementBean re9 = new RouteElementBean("bk-1+|bk-3-", "st-13", "st-13", null, 9, new BigDecimal(10));
        RouteElementBean re10 = new RouteElementBean("bk-1+|bk-3-", "st-14", "st-14", null, 10, new BigDecimal(11));
        RouteElementBean re11 = new RouteElementBean("bk-1+|bk-3-", "ct-6", "ct-6", null, 11, new BigDecimal(12));
        RouteElementBean re12 = new RouteElementBean("bk-1+|bk-3-", "st-20", "st-20", null, 12, new BigDecimal(13));
        RouteElementBean re13 = new RouteElementBean("bk-1+|bk-3-", "st-19", "st-19", null, 13, new BigDecimal(14));
        RouteElementBean re14 = new RouteElementBean("bk-1+|bk-3-", "st-18", "st-18", null, 14, new BigDecimal(15));
        RouteElementBean re15 = new RouteElementBean("bk-1+|bk-3-", "se-6", "se-6", null, 15, new BigDecimal(16));
        RouteElementBean re16 = new RouteElementBean("bk-1+|bk-3-", "bk-3-", "bk-3", null, 16, new BigDecimal(17));

        //path: bk-1+ -> se-2 -> st-2 -> sw-2-G -> sw-2 -> st-5 -> ct-4 -> st-11 -> st-12 -> st-13 -> st-14 -> ct-6 -> st-20 -> st-19 -> st-18 -> se-6 -> bk-3-
        routeElements.add(re0);
        routeElements.add(re1);
        routeElements.add(re2);
        routeElements.add(re3);
        routeElements.add(re4);
        routeElements.add(re5);
        routeElements.add(re6);
        routeElements.add(re7);
        routeElements.add(re8);
        routeElements.add(re9);
        routeElements.add(re10);
        routeElements.add(re11);
        routeElements.add(re12);
        routeElements.add(re13);
        routeElements.add(re14);
        routeElements.add(re15);
        routeElements.add(re16);
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of findAll method, of class RouteElementDAO.
     */
    @Test
    public void testFindAll() {
        System.out.println("findAll");
        RouteElementDAO instance = new RouteElementDAO();
        List<RouteElementBean> expResult = this.routeElements;
        List<RouteElementBean> result = instance.findAll();
        assertEquals(expResult, result);
    }

    /**
     * Test of findById method, of class RouteElementDAO.
     */
    @Test
    public void testFindById() {
        System.out.println("findById");
        BigDecimal id = new BigDecimal(16);
        RouteElementDAO instance = new RouteElementDAO();
        RouteElementBean expResult = this.routeElements.get(15);
        RouteElementBean result = instance.findById(id);
        assertEquals(expResult, result);
    }

    /**
     * Test of findByRouteId method, of class RouteElementDAO.
     */
    @Test
    public void testFindByRouteId() {
        System.out.println("findByRouteId");
        String key = "bk-1+|bk-3-";
        RouteElementDAO instance = new RouteElementDAO();
        List<RouteElementBean> expResult = this.routeElements;
        List<RouteElementBean> result = instance.findByRouteId(key);
        assertEquals(expResult, result);
    }

    /**
     * Test of persist method, of class RouteElementDAO.
     */
    @Test
    public void testPersist() {
        System.out.println("persist");
        RouteElementBean routeElement = new RouteElementBean("bk-2+|bk-3-", "bk-2+", "bk-2", null, 0);

        RouteElementDAO instance = new RouteElementDAO();
        BigDecimal expResult = new BigDecimal(18);
        BigDecimal result = instance.persist(routeElement);
        assertEquals(expResult, result);

        routeElement.setId(expResult);

        RouteElementBean re = instance.findById(expResult);
        assertEquals(routeElement, re);
    }

    
    @Test
    public void testPersist2() {
        System.out.println("persist2");
        RouteElementBean routeElement = new RouteElementBean("bk-2+|bk-3-", "sw-2-R", "sw-2", AccessoryValue.RED, 3);


        RouteElementDAO instance = new RouteElementDAO();
        BigDecimal expResult = new BigDecimal(18);
        BigDecimal result = instance.persist(routeElement);
        assertEquals(expResult, result);

        routeElement.setId(expResult);

        RouteElementBean re = instance.findById(expResult);
        assertEquals(routeElement, re);
    }
    
    
    
    /**
     * Test of remove method, of class RouteElementDAO.
     */
    @Test
    public void testRemove() {
        System.out.println("remove");
        RouteElementBean routeElement = new RouteElementBean("bk-2+|bk-3-", "se-3", "se-3", null, 0);
        RouteElementDAO instance = new RouteElementDAO();

        BigDecimal result = instance.persist(routeElement);
        assertNotNull(result);
        RouteElementBean re = instance.findById(result);
        routeElement.setId(result);
        assertEquals(routeElement, re);

        instance.remove(routeElement);

        re = instance.findById(result);

        assertNull(re);
    }

}
