/*
 * Copyright (C) 2021 fransjacobs.
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
import java.util.ArrayList;
import java.util.List;
import lan.wervel.jcs.entities.TileBean;
import lan.wervel.jcs.entities.enums.Orientation;
import lan.wervel.jcs.entities.enums.TileType;
import lan.wervel.jcs.ui.layout.tiles.enums.Direction;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author fransjacobs
 */
public class TileBeanDAOTest {

    private final List<TileBean> tiles;

    public TileBeanDAOTest() {
        DAOTestHelper.setConnectProperties();
        DAOTestHelper.createNewDatabase();
        DAOTestHelper.insertTileData();

        tiles = new ArrayList<>();
    }

    @Before
    public void setUp() {
        TileBean tb1 = new TileBean(TileType.SWITCH, Orientation.EAST, Direction.LEFT, 30, 30, "sw-1", null);
        tiles.add(tb1);

        TileBean tb2 = new TileBean(TileType.CURVED, Orientation.SOUTH, Direction.CENTER, 40, 50, "ct-5", null);
        tiles.add(tb2);

        TileBean tb3 = new TileBean(TileType.STRAIGHT, Orientation.WEST, Direction.CENTER, 50, 60, "st-7", null);
        tiles.add(tb3);
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testFindAll() {
        System.out.println("findAll");
        TileBeanDAO instance = new TileBeanDAO();
        List<TileBean> expResult = tiles;
        List<TileBean> result = instance.findAll();

        assertEquals(expResult, result);
        result = instance.findAll();
        assertEquals(tiles, result);
    }

    @Test
    public void testFindById() {
        System.out.println("findById");

        String id = "ct-5";
        TileBeanDAO instance = new TileBeanDAO();
        TileBean expResult = tiles.get(1);
        TileBean result = instance.findById(id);
        assertEquals(expResult, result);

        id = "sw-1";
        expResult = tiles.get(0);
        result = instance.findById(id);
        assertEquals(expResult, result);
    }

    @Test
    public void testFindByTileType() {
        System.out.println("findByTileType");
        String tileType = TileType.STRAIGHT.getTileType();
        TileBeanDAO instance = new TileBeanDAO();
        List<TileBean> expResult = new ArrayList<>();
        expResult.add(tiles.get(2));
        List<TileBean> result = instance.findByTileType(tileType);
        assertEquals(expResult, result);
    }

    @Test
    public void testFindByXY() {
        System.out.println("findByXY");
        Integer x = 40;
        Integer y = 50;
        TileBeanDAO instance = new TileBeanDAO();
        TileBean expResult = tiles.get(1);
        TileBean result = instance.findByXY(x, y);
        assertEquals(expResult, result);
    }

    @Test
    public void testPersist() {
        System.out.println("persist");
        TileBean tile = new TileBean(TileType.SENSOR, Orientation.NORTH, Direction.CENTER, 80, 90, "se-3", null);
        TileBeanDAO instance = new TileBeanDAO();
        String expResult = "se-3";
        Object result = instance.persist(tile);
        assertEquals(expResult, result);

        TileBean tb = instance.findById(expResult);
        assertNotNull(tb);
    }

    @Test
    public void testPersist2() {
        System.out.println("persist2");
        TileBean tile = new TileBean(TileType.SWITCH, Orientation.EAST, Direction.LEFT, 80, 90, "sw-2");
        TileBeanDAO instance = new TileBeanDAO();
        String expResult = "sw-2";
        Object result = instance.persist(tile);
        assertEquals(expResult, result);

        TileBean tb = instance.findById(expResult);
        assertNotNull(tb);
        assertEquals(tile, tb);

        tile.setDirection(Direction.RIGHT);
        result = instance.persist(tile);
        assertEquals(expResult, result);

        TileBean tb1 = instance.findById(expResult);
        assertNotNull(tb1);
        assertEquals(tile, tb1);
    }

    @Test
    public void testRemove() {
        System.out.println("remove");
        TileBean tile = new TileBean(TileType.CURVED, Orientation.NORTH, Direction.CENTER, 110, 240, "ct-9", null);
        TileBeanDAO instance = new TileBeanDAO();
        instance.persist(tile);
        TileBean tb = instance.findByXY(110, 240);
        assertNotNull(tb);
        assertEquals(tile, tb);

        instance.remove(tile);
        tb = instance.findByXY(110, 240);
        assertNull(tb);
    }

}
