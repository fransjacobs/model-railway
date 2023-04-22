/*
 * Copyright (C) 2021 frans.
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

import java.awt.Color;
import jcs.trackservice.dao.util.DAOTestHelper;
import java.util.LinkedList;
import java.util.List;
import jcs.entities.RouteBean;
import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author frans
 */
public class RouteDAOTest {

    private List<RouteBean> routes;

    public RouteDAOTest() {
        DAOTestHelper.setConnectProperties();
        DAOTestHelper.createNewDatabase();
        DAOTestHelper.insertTileLayoutData();
        DAOTestHelper.insertRouteData();
    }

    @Before
    public void setUp() {
        routes = new LinkedList<>();

        RouteBean r1 = new RouteBean("bk-1+|bk-3-", "bk-1+", "bk-3-", "red");
        RouteBean r2 = new RouteBean("bk-2+|bk-3-", "bk-2+", "bk-3-", "green");
        routes.add(r1);
        routes.add(r2);

    }

    @After
    public void tearDown() {
    }

    /**
     * Test of findAll method, of class RouteDAO.
     */
    @Test
    public void testFindAll() {
        System.out.println("findAll");
        RouteDAO instance = new RouteDAO();
        List<RouteBean> expResult = routes;
        List<RouteBean> result = instance.findAll();
        assertEquals(expResult, result);
    }

    /**
     * Test of findById method, of class RouteDAO.
     */
    @Test
    public void testFindById() {
        System.out.println("findById");
        String id = "bk-2+|bk-3-";
        RouteDAO instance = new RouteDAO();
        RouteBean expResult = routes.get(1);
        RouteBean result = instance.findById(id);
        assertEquals(expResult, result);
    }

    /**
     * Test of persist method, of class RouteDAO.
     */
    @Test
    public void testPersist() {
        System.out.println("persist");
        RouteBean route = new RouteBean("bk-2-|bk-3+", "bk-2-", "bk-3+", "yellow");
        RouteDAO instance = new RouteDAO();
        String expResult = "bk-2-|bk-3+";
        String result = instance.persist(route);
        assertEquals(expResult, result);

        RouteBean r = instance.findById(result);
        assertEquals(route, r);

        route.setColor(Color.blue);
        instance.persist(route);

        r = instance.findById(result);
        assertEquals(route, r);
    }

    /**
     * Test of remove method, of class RouteDAO.
     */
    @Test
    public void testRemove() {
        System.out.println("remove");
        RouteBean route = new RouteBean("bk-1-|bk-3+", "bk-1-", "bk-3+", "orange");
        RouteDAO instance = new RouteDAO();

        RouteBean r = instance.findById("bk-1-|bk-3+");
        assertNull(r);
        instance.persist(route);
        r = instance.findById("bk-1-|bk-3+");
        assertNotNull(r);

        instance.remove(route);
        r = instance.findById("bk-1-|bk-3+");
        assertNull(r);
    }

}
