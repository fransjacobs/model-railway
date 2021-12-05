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
package lan.wervel.jcs.trackservice.dao;

import lan.wervel.jcs.trackservice.dao.util.DAOTestHelper;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lan.wervel.jcs.entities.LayoutTile;
import lan.wervel.jcs.entities.enums.Orientation;
import lan.wervel.jcs.entities.enums.TileType;
import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author frans
 */
public class LayoutTileDAOTest {

    private final List<LayoutTile> layoutTiles;

    public LayoutTileDAOTest() {
        DAOTestHelper.setConnectProperties();
        DAOTestHelper.createNewDatabase();
        DAOTestHelper.insertLayoutTileData();

        layoutTiles = new ArrayList<>();
    }

    @Before
    public void setUp() {
        LayoutTile lt1 = new LayoutTile(new BigDecimal(1), TileType.SWITCH, Orientation.EAST, "Left", 30, 30);
        layoutTiles.add(lt1);

        LayoutTile lt2 = new LayoutTile(new BigDecimal(2), TileType.CURVED, Orientation.SOUTH, "Center", 40, 50);
        layoutTiles.add(lt2);

        LayoutTile lt3 = new LayoutTile(new BigDecimal(3), TileType.STRAIGHT, Orientation.WEST, "Center", 50, 60);
        layoutTiles.add(lt3);
    }

    @After
    public void tearDown() {
    }

    //@Test
    public void testFindAll() {
        System.out.println("findAll");
        LayoutTileDAO instance = new LayoutTileDAO();
        List<LayoutTile> expResult = layoutTiles;
        List<LayoutTile> result = instance.findAll();

        assertEquals(expResult, result);
        result = instance.findAll();
        assertEquals(layoutTiles, result);
    }

    //@Test
    public void testFind() {
        System.out.println("find");

        Integer address = 2;
        LayoutTileDAO instance = new LayoutTileDAO();
        LayoutTile expResult = layoutTiles.get(1);
        LayoutTile result = instance.find(address);
        assertEquals(expResult, result);

        address = 1;
        expResult = layoutTiles.get(0);
        result = instance.find(address);
        assertEquals(expResult, result);
    }

    //@Test
    public void testFindById() {
        System.out.println("findById");
        BigDecimal id = new BigDecimal(3);
        LayoutTileDAO instance = new LayoutTileDAO();
        LayoutTile expResult = layoutTiles.get(2);
        LayoutTile result = instance.findById(id);
        assertEquals(expResult, result);

        id = new BigDecimal(1);
        expResult = layoutTiles.get(0);
        result = instance.findById(id);
        assertEquals(expResult, result);
    }

    //@Test
    public void testFindByXY() {
        System.out.println("findByXY");
        Integer x = 50;
        Integer y = 60;
        LayoutTileDAO instance = new LayoutTileDAO();
        LayoutTile expResult = layoutTiles.get(2);
        LayoutTile result = instance.findByXY(x, y);
        assertEquals(expResult, result);

        x = 40;
        y = 50;
        expResult = layoutTiles.get(1);
        result = instance.findByXY(x, y);
        assertEquals(expResult, result);
    }

    //@Test
//    public void testfindByTileType() {
//        System.out.println("findByTileType");
//        LayoutTileDAO instance = new LayoutTileDAO();
//        BigDecimal ltgrId = new BigDecimal(1);
//
//        List<LayoutTile> expResult = instance.findAll();
//        //Check
//        List<LayoutTile> result = instance.findByLtgrId(ltgrId);
//        assertEquals(layoutTiles, expResult);
//        assertEquals(0, result.size());
//
//        assertEquals(layoutTiles, expResult);
//        expResult.get(0).setLtgrId(ltgrId);
//        BigDecimal id = instance.persist(expResult.get(0));
//
//        LayoutTile lt = instance.findById(id);
//        assertEquals(expResult.get(0), lt);
//        assertEquals(ltgrId, lt.getLtgrId());
//
//        result = instance.findByLtgrId(ltgrId);
//        assertEquals(1, result.size());
//
//        List<LayoutTile> expRes = new ArrayList<>();
//        expRes.add(expResult.get(0));
//        assertEquals(expRes, result);
//
//        expResult.get(1).setLtgrId(ltgrId);
//        id = instance.persist(expResult.get(1));
//        lt = instance.findById(id);
//        assertEquals(expResult.get(1), lt);
//
//        result = instance.findByLtgrId(ltgrId);
//        assertEquals(2, result.size());
//        expRes.add(lt);
//        assertEquals(expRes, result);
//    }
    //@Test
    public void testPersist() {
        System.out.println("persist");
        LayoutTile layoutTile = new LayoutTile(TileType.SWITCH, Orientation.NORTH, "Right", 80, 90);
        LayoutTileDAO instance = new LayoutTileDAO();
        BigDecimal expResult = new BigDecimal(4);
        BigDecimal result = instance.persist(layoutTile);
        assertEquals(expResult, result);

        LayoutTile lt = instance.findById(expResult);
        assertNotNull(lt);
    }

    //@Test
    public void testPersist2() {
        System.out.println("persist2");
        LayoutTile layoutTile = new LayoutTile(TileType.SWITCH, Orientation.SOUTH, "Right", 80, 90);
        BigDecimal ltgrId = new BigDecimal(2);
        LayoutTileDAO instance = new LayoutTileDAO();
        BigDecimal expResult = new BigDecimal(4);
        BigDecimal result = instance.persist(layoutTile);
        assertEquals(expResult, result);

        LayoutTile lt = instance.findById(expResult);
        assertNotNull(lt);
    }

    //@Test
    public void testRemove() {
        System.out.println("remove");
        LayoutTile layoutTile = new LayoutTile(TileType.CURVED, Orientation.NORTH, "Center", 110, 240);
        LayoutTileDAO instance = new LayoutTileDAO();
        instance.persist(layoutTile);
        LayoutTile lt = instance.findByXY(110, 240);
        assertNotNull(lt);

        instance.remove(lt);
        lt = instance.findByXY(110, 240);
        assertNull(lt);
    }
}
