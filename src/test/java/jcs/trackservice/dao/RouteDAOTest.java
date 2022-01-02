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

import jcs.trackservice.dao.util.DAOTestHelper;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import jcs.entities.Route;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.BeforeClass;

/**
 *
 * @author frans
 */
public class RouteDAOTest {

    private List<Route> routes;

    public RouteDAOTest() {
        DAOTestHelper.setConnectProperties();
        DAOTestHelper.createNewDatabase();
        DAOTestHelper.insertLocoData();
        DAOTestHelper.insertLayoutTileData();
        DAOTestHelper.insertDriveWayData();
        DAOTestHelper.insertRouteData();
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
        routes = new ArrayList<>();
        BigDecimal id = new BigDecimal(1);
        Integer address = 1;
        String name = "Rt 1";
        String description = "Route 1";
        BigDecimal drwaId = new BigDecimal(1);
        BigDecimal latiId = new BigDecimal(1);

        Route r = new Route(id, address, name, description, drwaId, latiId);
        routes.add(r);
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of findAll method, of class RouteDAO.
     */
    //@Test
    public void testFindAll() {
        System.out.println("findAll");
        RouteDAO instance = new RouteDAO();
        List<Route> expResult = routes;
        List<Route> result = instance.findAll();
        assertEquals(expResult, result);
    }

    /**
     * Test of find method, of class RouteDAO.
     */
    //@Test
    public void testFind() {
        System.out.println("find");
        Integer address = 1;
        RouteDAO instance = new RouteDAO();
        Route expResult = routes.get(0);
        Route result = instance.find(address);
        assertEquals(expResult, result);
    }

    /**
     * Test of findById method, of class RouteDAO.
     */
    //@Test
    public void testFindById() {
        System.out.println("findById");
        BigDecimal id = new BigDecimal(1);
        RouteDAO instance = new RouteDAO();
        Route expResult = routes.get(0);
        Route result = instance.findById(id);
        assertEquals(expResult, result);
    }

    /**
     * Test of findByDrwaId method, of class RouteDAO.
     */
    //@Test
    public void testFindByDrwaId() {
        System.out.println("findByDrwaId");
        BigDecimal drwaId = new BigDecimal(1);
        RouteDAO instance = new RouteDAO();
        List<Route> expResult = routes;
        List<Route> result = instance.findByDrwaId(drwaId);
        assertEquals(expResult, result);
    }

    /**
     * Test of findByLatiId method, of class RouteDAO.
     */
    //@Test
    public void testFindByLatiId() {
        System.out.println("findByLatiId");
        BigDecimal latiId = new BigDecimal(1);
        RouteDAO instance = new RouteDAO();
        List<Route> expResult = routes;
        List<Route> result = instance.findByLatiId(latiId);
        assertEquals(expResult, result);
    }

    /**
     * Test of persist method, of class RouteDAO.
     */
    //@Test
    public void testPersist() {
        System.out.println("persist");
        Route route = new Route(2, new BigDecimal(1), new BigDecimal(2));
        RouteDAO instance = new RouteDAO();
        BigDecimal expResult = new BigDecimal(2);
        BigDecimal result = instance.persist(route);
        assertEquals(expResult, result);

        Route r = instance.findById(result);

        route.setId(result);

        assertEquals(route, r);

        route.setLatiId(new BigDecimal(3));

        instance.persist(route);

        r = instance.findById(result);

        assertEquals(route, r);
    }

    /**
     * Test of remove method, of class RouteDAO.
     */
    //@Test
    public void testRemove() {
        System.out.println("remove");
        Route route = new Route(2, new BigDecimal(1), new BigDecimal(2));
        RouteDAO instance = new RouteDAO();

        Route r = instance.find(2);
        assertNull(r);

        instance.persist(route);
        r = instance.find(2);
        assertNotNull(r);

        route.setId(new BigDecimal(2));
        assertEquals(route, r);

        instance.remove(route);
        r = instance.find(2);
        assertNull(r);
    }

}
