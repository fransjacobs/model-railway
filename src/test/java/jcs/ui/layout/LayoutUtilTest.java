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
package jcs.ui.layout;

import java.awt.Point;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import jcs.entities.enums.AccessoryValue;
import jcs.entities.enums.Orientation;
import jcs.entities.enums.TileType;
import jcs.ui.layout.tiles.TileFactory;
import jcs.ui.layout.tiles.enums.Direction;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author fransjacobs
 */
public class LayoutUtilTest {

    public LayoutUtilTest() {
        System.setProperty("trackServiceSkipControllerInit", "true");

      
    }

    /**
     * Test of snapToGrid method, of class LayoutUtil.
     */
    @Test
    public void testSnapToGrid_Point() {
        System.out.println("snapToGrid");
        Point p = new Point(205, 205);
        Point expResult = new Point(220, 220);
        Point result = LayoutUtil.snapToGrid(p);
        assertEquals(expResult, result);
    }

    /**
     * Test of snapToGrid method, of class LayoutUtil.
     */
    @Test
    public void testSnapToGrid_int_int() {
        System.out.println("snapToGrid");
        int x = 90;
        int y = 90;
        Point expResult = new Point(100, 100);
        Point result = LayoutUtil.snapToGrid(x, y);
        assertEquals(expResult, result);
    }

    /**
     * Test of loadLayout method, of class LayoutUtil.
     */
    //@Test
    public void testLoadLayout_boolean_boolean() {
        System.out.println("loadLayout");
        boolean drawGridLines = false;
        boolean showValues = false;
        //Map<Point, Tile> expResult = null;
        Map<Point, Tile> result = LayoutUtil.loadLayout(drawGridLines, showValues);
        //assertEquals(expResult, result);
        assertEquals(37, result.size());

    }

    /**
     * Test of findTile method, of class LayoutUtil.
     */
    //@Test
    public void testFindTile_Point() {
        System.out.println("findTile");
        Point cp = new Point(500, 380);

        Tile expResult = LayoutUtil.getTiles().get(cp);

        Tile result = LayoutUtil.findTile(cp);
        assertEquals(expResult, result);
    }

    /**
     * Test of isTile method, of class LayoutUtil.
     */
    //@Test
    public void testIsTile_Point() {
        System.out.println("isTile");
        Point cp = new Point(660, 380);
        boolean expResult = true;
        boolean result = LayoutUtil.isTile(cp);
        assertEquals(expResult, result);
    }

    /**
     * Test of isBlock method, of class LayoutUtil.
     */
    //@Test
    public void testIsBlock_Point() {
        System.out.println("isBlock");
        Point cp = new Point(420, 140);
        boolean expResult = true;
        boolean result = LayoutUtil.isBlock(cp);
        assertEquals(expResult, result);
    }

    /**
     * Test of isTrack method, of class LayoutUtil.
     */
    //@Test
    public void testIsTrack_Point() {
        System.out.println("isTrack");
        Point cp = new Point(180, 260);
        boolean expResult = true;
        boolean result = LayoutUtil.isTrack(cp);
        assertEquals(expResult, result);
    }

    /**
     * Test of getTiles method, of class LayoutUtil.
     */
    //@Test
    public void testGetTiles() {
        System.out.println("getTiles");
        //Map<Point, Tile> expResult = null;
        Map<Point, Tile> result = LayoutUtil.getTiles();
        //assertEquals(expResult, result);
        assertEquals(37, result.size());
    }

    /**
     * Test of euclideanDistance method, of class LayoutUtil.
     */
    @Test
    public void testEuclideanDistance() {
        System.out.println("euclideanDistance");
        Point p1 = new Point(100, 100);
        Point p2 = new Point(300, 300);
        double expResult = 282.842712474619;
        double result = LayoutUtil.euclideanDistance(p1, p2);
        assertEquals(expResult, result, 0);
    }

    @Test
    public void testAdjacentPointsFor_Switch_West_Left_Common() {
        System.out.println("adjacentPointsFor Switch West Left Common");
        //     R
        // C _/_ G
        // sw cp (260,180)
        // C  (220,180)
        // G  (300,180)
        // R  (260,140)

        Tile tile = TileFactory.createTile(TileType.SWITCH, Orientation.WEST, Direction.LEFT, new Point(260, 180), true);

        Set<Point> expResult = new HashSet<>();
        Point pc = new Point(220, 180);
        expResult.add(pc);

        //No direction so the common
        Set<Point> result = LayoutUtil.adjacentPointsFor(tile);
        assertEquals(expResult, result);
    }

    @Test
    public void testAdjacentPointsFor_Switch_West_Left_Green() {
        System.out.println("adjacentPointsFor Switch West Left Green");
        //     R
        // C _/_ G
        // sw cp (260,180)
        // C  (220,180)
        // G  (300,180)
        // R  (260,140)

        Tile tile = TileFactory.createTile(TileType.SWITCH, Orientation.WEST, Direction.LEFT, new Point(260, 180), true);

        Set<Point> expResult = new HashSet<>();
        Point pg = new Point(300, 180);
        expResult.add(pg);

        Set<Point> result = LayoutUtil.adjacentPointsFor(tile, AccessoryValue.GREEN);
        assertEquals(expResult, result);
    }

    @Test
    public void testAdjacentPointsFor_Switch_West_Left_Red() {
        System.out.println("adjacentPointsFor Switch West Left Red");
        //     R
        // C _/_ G
        // sw cp (260,180)
        // C  (220,180)
        // G  (300,180)
        // R  (260,140)

        Tile tile = TileFactory.createTile(TileType.SWITCH, Orientation.WEST, Direction.LEFT, new Point(260, 180), true);

        Set<Point> expResult = new HashSet<>();
        Point pr = new Point(260, 140);
        expResult.add(pr);

        Set<Point> result = LayoutUtil.adjacentPointsFor(tile, AccessoryValue.RED);
        assertEquals(expResult, result);
    }

    @Test
    public void testAdjacentPointsFor_Switch_East_Right_Common() {
        System.out.println("adjacentPointsFor Switch East Right Common");
        //   R
        // G _\_ C
        // sw cp (580,180)
        // C  (620,180)
        // G  (540,180)
        // R  (580,140)

        Tile tile = TileFactory.createTile(TileType.SWITCH, Orientation.EAST, Direction.RIGHT, new Point(580, 180), true);

        Set<Point> expResult = new HashSet<>();
        Point pc = new Point(620, 180);
        //Point pg = new Point(540, 180);
        //Point pr = new Point(580, 140);

        expResult.add(pc);
        //expResult.add(pg);
        //expResult.add(pr);

        //expected:<[java.awt.Point[x=540,y=180], java.awt.Point[x=620,y=180], java.awt.Point[x=580,y=140]]
        //>but was:<[java.awt.Point[x=620,y=180]]>
        //No direction so the common        
        Set<Point> result = LayoutUtil.adjacentPointsFor(tile);
        assertEquals(expResult, result);
    }

    @Test
    public void testAdjacentPointsFor_Switch_East_Right_Green() {
        System.out.println("adjacentPointsFor Switch East Right Green");
        //   R
        // G _\_ C
        // sw cp (580,180)
        // C  (620,180)
        // G  (540,180)
        // R  (580,140)

        Tile tile = TileFactory.createTile(TileType.SWITCH, Orientation.EAST, Direction.RIGHT, new Point(580, 180), true);

        Set<Point> expResult = new HashSet<>();
        Point pg = new Point(540, 180);
        expResult.add(pg);

        Set<Point> result = LayoutUtil.adjacentPointsFor(tile, AccessoryValue.GREEN);
        assertEquals(expResult, result);
    }

    @Test
    public void testAdjacentPointsFor_Switch_East_Right_Red() {
        System.out.println("adjacentPointsFor Switch East Right Red");
        //   R
        // G _\_ C
        // sw cp (580,180)
        // C  (620,180)
        // G  (540,180)
        // R  (580,140)

        Tile tile = TileFactory.createTile(TileType.SWITCH, Orientation.EAST, Direction.RIGHT, new Point(580, 180), true);

        Set<Point> expResult = new HashSet<>();
        Point pr = new Point(580, 140);
        expResult.add(pr);

        Set<Point> result = LayoutUtil.adjacentPointsFor(tile, AccessoryValue.RED);
        assertEquals(expResult, result);
    }

    /**
     * Test of isPlusAdjacent method, of class LayoutUtil.
     */
    @Test
    public void testIsPlusAdjacent() {
        System.out.println("isPlusAdjacent");
        // (340,380) ----|+  (420,380)  -|---- (500,380)        
        Tile block = TileFactory.createTile(TileType.BLOCK, Orientation.WEST, Direction.CENTER, new Point(420, 380), true);

        Point pointP = new Point(340, 380);
        Point pointM = new Point(500, 380);
        boolean expResult = true;
        boolean result = LayoutUtil.isPlusAdjacent(block, pointP);
        assertEquals(expResult, result);
        result = LayoutUtil.isPlusAdjacent(block, pointM);
        assertEquals(!expResult, result);
    }

    /**
     * Test of getPlusAdjacent method, of class LayoutUtil.
     */
    @Test
    public void testGetPlusAdjacent() {
        System.out.println("getPlusAdjacent");
        // (340,380) ----|+  (420,380)  -|---- (500,380)        
        Tile block = TileFactory.createTile(TileType.BLOCK, Orientation.WEST, Direction.CENTER, new Point(420, 380), true);

        Point expResult = new Point(340, 380);
        Point result = LayoutUtil.getPlusAdjacent(block);
        assertEquals(expResult, result);
    }

    /**
     * Test of getMinusAdjacent method, of class LayoutUtil.
     */
    @Test
    public void testGetMinusAdjacent() {
        System.out.println("getMinusAdjacent");
        // (340,380) ----|+  (420,380)  -|---- (500,380)        
        Tile block = TileFactory.createTile(TileType.BLOCK, Orientation.WEST, Direction.CENTER, new Point(420, 380), true);
        Point expResult = new Point(500, 380);
        Point result = LayoutUtil.getMinusAdjacent(block);
        assertEquals(expResult, result);
    }

    /**
     * Test of isMinusAdjacent method, of class LayoutUtil.
     */
    @Test
    public void testIsMinusAdjacent() {
        System.out.println("isMinusAdjacent");
        // (340,380) ----|+  (420,380)  -|---- (500,380)  
        Tile block = TileFactory.createTile(TileType.BLOCK, Orientation.WEST, Direction.CENTER, new Point(420, 380), true);

        Point pointP = new Point(340, 380);
        Point pointM = new Point(500, 380);
        boolean expResult = true;
        boolean result = LayoutUtil.isMinusAdjacent(block, pointM);
        assertEquals(expResult, result);
        result = LayoutUtil.isMinusAdjacent(block, pointP);
        assertEquals(!expResult, result);
    }

    /**
     * Test of getPlusCenter method, of class LayoutUtil.
     */
    @Test
    public void testGetPlusCenter() {
        System.out.println("getPlusCenter");
        // (340,380) ----|+  (420,380)  -|---- (500,380)  
        Tile block = TileFactory.createTile(TileType.BLOCK, Orientation.WEST, Direction.CENTER, new Point(420, 380), true);
        Point expResult = new Point(380, 380);
        Point result = LayoutUtil.getPlusCenter(block);
        assertEquals(expResult, result);
    }

    /**
     * Test of getMinusCenter method, of class LayoutUtil.
     */
    @Test
    public void testGetMinusCenter() {
        System.out.println("getMinusCenter");
        // (340,380) ----|+  (420,380)  -|---- (500,380)  
        Tile block = TileFactory.createTile(TileType.BLOCK, Orientation.WEST, Direction.CENTER, new Point(420, 380), true);
        Point expResult = new Point(460, 380);
        Point result = LayoutUtil.getMinusCenter(block);
        assertEquals(expResult, result);
    }

    /**
     * Test of getNodeIdsForAdjacentSwitch method, of class LayoutUtil.
     */
    @Test
    public void testgGetNodeIdsForAdjacentSwitch_Common() {
        System.out.println("getNodeIdsForAdjacentSwitch Common");
        //     R
        // C _/_ G
        // sw cp (260,180)
        // C  (220,180)
        // G  (300,180)
        // R  (260,140)

        Tile tile = TileFactory.createTile(TileType.STRAIGHT, Orientation.EAST, Direction.CENTER, new Point(220, 180), true);

        Tile switchTile = TileFactory.createTile(TileType.SWITCH, Orientation.WEST, Direction.LEFT, new Point(260, 180), true);
        String id = switchTile.getId();

        List<String> expResult = new LinkedList<>();
        expResult.add(id);

        List<String> result = LayoutUtil.getNodeIdsForAdjacentSwitch(tile, switchTile);
        assertEquals(expResult, result);
    }

    @Test
    public void testgGetNodeIdsForAdjacentSwitch_Green() {
        System.out.println("getNodeIdsForAdjacentSwitch Green");
        //     R
        // C _/_ G
        // sw cp (260,180)
        // C  (220,180)
        // G  (300,180)
        // R  (260,140)

        Tile tile = TileFactory.createTile(TileType.STRAIGHT, Orientation.EAST, Direction.CENTER, new Point(300, 180), true);

        Tile switchTile = TileFactory.createTile(TileType.SWITCH, Orientation.WEST, Direction.LEFT, new Point(260, 180), true);
        String id = switchTile.getId();

        List<String> expResult = new LinkedList<>();
        expResult.add(id + "-G");

        List<String> result = LayoutUtil.getNodeIdsForAdjacentSwitch(tile, switchTile);
        assertEquals(expResult, result);
    }

    @Test
    public void testgGetNodeIdsForAdjacentSwitch_Red() {
        System.out.println("getNodeIdsForAdjacentSwitch Red");
        //     R
        // C _/_ G
        // sw cp (260,180)
        // C  (220,180)
        // G  (300,180)
        // R  (260,140)

        Tile tile = TileFactory.createTile(TileType.CURVED, Orientation.EAST, Direction.CENTER, new Point(260, 140), true);

        Tile switchTile = TileFactory.createTile(TileType.SWITCH, Orientation.WEST, Direction.LEFT, new Point(260, 180), true);
        String id = switchTile.getId();

        List<String> expResult = new LinkedList<>();
        expResult.add(id + "-R");

        List<String> result = LayoutUtil.getNodeIdsForAdjacentSwitch(tile, switchTile);
        assertEquals(expResult, result);
    }

}
