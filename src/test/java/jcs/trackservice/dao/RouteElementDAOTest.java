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
import jcs.entities.Route;
import jcs.entities.RouteElement;
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

    private List<RouteElement> routeElements;

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

        Route r1 = new Route("bk-1+|bk-3-", "bk-1+", "bk-3-", "red");
        Route r2 = new Route("bk-2+|bk-3-", "bk-2+", "bk-3-", "green");

        RouteElement re0 = new RouteElement("bk-1+|bk-3-", "bk-1+", "bk-1", null, 0, new BigDecimal(1));
        RouteElement re1 = new RouteElement("bk-1+|bk-3-", "se-2", "se-2", null, 1, new BigDecimal(2));
        RouteElement re2 = new RouteElement("bk-1+|bk-3-", "st-2", "st-2", null, 2, new BigDecimal(3));
        RouteElement re3 = new RouteElement("bk-1+|bk-3-", "sw-2-G", "sw-2", AccessoryValue.GREEN, 3, new BigDecimal(4));
        RouteElement re4 = new RouteElement("bk-1+|bk-3-", "sw-2", "sw-2", AccessoryValue.GREEN, 4, new BigDecimal(5));
        RouteElement re5 = new RouteElement("bk-1+|bk-3-", "st-5", "st-5", null, 5, new BigDecimal(6));
        RouteElement re6 = new RouteElement("bk-1+|bk-3-", "ct-4", "ct-4", null, 6, new BigDecimal(7));
        RouteElement re7 = new RouteElement("bk-1+|bk-3-", "st-11", "st-11", null, 7, new BigDecimal(8));
        RouteElement re8 = new RouteElement("bk-1+|bk-3-", "st-12", "st-12", null, 8, new BigDecimal(9));
        RouteElement re9 = new RouteElement("bk-1+|bk-3-", "st-13", "st-13", null, 9, new BigDecimal(10));
        RouteElement re10 = new RouteElement("bk-1+|bk-3-", "st-14", "st-14", null, 10, new BigDecimal(11));
        RouteElement re11 = new RouteElement("bk-1+|bk-3-", "ct-6", "ct-6", null, 11, new BigDecimal(12));
        RouteElement re12 = new RouteElement("bk-1+|bk-3-", "st-20", "st-20", null, 12, new BigDecimal(13));
        RouteElement re13 = new RouteElement("bk-1+|bk-3-", "st-19", "st-19", null, 13, new BigDecimal(14));
        RouteElement re14 = new RouteElement("bk-1+|bk-3-", "st-18", "st-18", null, 14, new BigDecimal(15));
        RouteElement re15 = new RouteElement("bk-1+|bk-3-", "se-6", "se-6", null, 15, new BigDecimal(16));
        RouteElement re16 = new RouteElement("bk-1+|bk-3-", "bk-3-", "bk-3", null, 16, new BigDecimal(17));

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
        List<RouteElement> expResult = this.routeElements;
        List<RouteElement> result = instance.findAll();
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
        RouteElement expResult = this.routeElements.get(15);
        RouteElement result = instance.findById(id);
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
        List<RouteElement> expResult = this.routeElements;
        List<RouteElement> result = instance.findByRouteId(key);
        assertEquals(expResult, result);
    }

    /**
     * Test of persist method, of class RouteElementDAO.
     */
    @Test
    public void testPersist() {
        System.out.println("persist");
        RouteElement routeElement = new RouteElement("bk-2+|bk-3-", "bk-2+", "bk-2", null, 0);

        RouteElementDAO instance = new RouteElementDAO();
        BigDecimal expResult = new BigDecimal(18);
        BigDecimal result = instance.persist(routeElement);
        assertEquals(expResult, result);

        routeElement.setId(expResult);

        RouteElement re = instance.findById(expResult);
        assertEquals(routeElement, re);
    }

    
    @Test
    public void testPersist2() {
        System.out.println("persist2");
        RouteElement routeElement = new RouteElement("bk-2+|bk-3-", "sw-2-R", "sw-2", AccessoryValue.RED, 3);


        RouteElementDAO instance = new RouteElementDAO();
        BigDecimal expResult = new BigDecimal(18);
        BigDecimal result = instance.persist(routeElement);
        assertEquals(expResult, result);

        routeElement.setId(expResult);

        RouteElement re = instance.findById(expResult);
        assertEquals(routeElement, re);
    }
    
    
    
    /**
     * Test of remove method, of class RouteElementDAO.
     */
    @Test
    public void testRemove() {
        System.out.println("remove");
        RouteElement routeElement = new RouteElement("bk-2+|bk-3-", "se-3", "se-3", null, 0);
        RouteElementDAO instance = new RouteElementDAO();

        BigDecimal result = instance.persist(routeElement);
        assertNotNull(result);
        RouteElement re = instance.findById(result);
        routeElement.setId(result);
        assertEquals(routeElement, re);

        instance.remove(routeElement);

        re = instance.findById(result);

        assertNull(re);
    }

}
