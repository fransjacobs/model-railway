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
package lan.wervel.jcs.entities;

import java.awt.Point;
import java.util.HashSet;
import java.util.Set;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author frans
 */
public class LayoutTileTest {

    public LayoutTileTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of getAdjacentPoints method, of class LayoutTile.
     */
    @Test
    public void testGetAdjacentPointsStraightEW() {
        System.out.println("getAdjacentPoints StraightTrack East/West");
        LayoutTile instanceE = new LayoutTile("StraightTrack", "East", "Center", 100, 100);
        LayoutTile instanceW = new LayoutTile("StraightTrack", "West", "Center", 100, 100);
        Set<Point> expResult = new HashSet<>();
        expResult.add(new Point(60, 100));
        expResult.add(new Point(140, 100));
        expResult.add(new Point(60, 80));
        expResult.add(new Point(60, 120));
        expResult.add(new Point(140, 80));
        expResult.add(new Point(140, 120));

        Set<Point> resultE = instanceE.getAdjacentPoints();
        assertEquals(expResult, resultE);
        Set<Point> resultW = instanceW.getAdjacentPoints();
        assertEquals(expResult, resultW);
    }

    @Test
    public void testGetAdjacentPointsStraightNS() {
        System.out.println("getAdjacentPoints StraightTrack North/South");
        LayoutTile instanceN = new LayoutTile("StraightTrack", "North", "Center", 100, 100);
        LayoutTile instanceS = new LayoutTile("StraightTrack", "South", "Center", 100, 100);
        Set<Point> expResult = new HashSet<>();
        expResult.add(new Point(100, 60));
        expResult.add(new Point(100, 140));
        expResult.add(new Point(80, 60));
        expResult.add(new Point(120, 60));
        expResult.add(new Point(80, 140));
        expResult.add(new Point(120, 140));

        Set<Point> resultN = instanceN.getAdjacentPoints();
        assertEquals(expResult, resultN);
        Set<Point> resultS = instanceS.getAdjacentPoints();
        assertEquals(expResult, resultS);
    }

    @Test
    public void testGetAdjacentPointsDiagonalEW() {
        System.out.println("getAdjacentPoints DiagonalTrack East/West");
        LayoutTile instanceE = new LayoutTile("DiagonalTrack", "East", "Center", 100, 100);
        LayoutTile instanceW = new LayoutTile("DiagonalTrack", "West", "Center", 100, 100);
        Set<Point> expResult = new HashSet<>();
        expResult.add(new Point(60, 60));
        expResult.add(new Point(60, 80));
        expResult.add(new Point(80, 60));
        expResult.add(new Point(100, 60));
        expResult.add(new Point(140, 140));
        expResult.add(new Point(120, 140));
        expResult.add(new Point(140, 120));
        expResult.add(new Point(100, 140));

        Set<Point> resultE = instanceE.getAdjacentPoints();
        assertEquals(expResult, resultE);
        Set<Point> resultW = instanceW.getAdjacentPoints();
        assertEquals(expResult, resultW);
    }

    @Test
    public void testGetAdjacentPointsDiagonalNS() {
        System.out.println("getAdjacentPoints DiagonalTrack North/South");
        LayoutTile instanceE = new LayoutTile("DiagonalTrack", "North", "Center", 100, 100);
        LayoutTile instanceW = new LayoutTile("DiagonalTrack", "South", "Center", 100, 100);
        Set<Point> expResult = new HashSet<>();
        expResult.add(new Point(140, 60));
        expResult.add(new Point(140, 80));
        expResult.add(new Point(120, 60));
        expResult.add(new Point(100, 60));
        expResult.add(new Point(60, 140));
        expResult.add(new Point(80, 140));
        expResult.add(new Point(60, 120));
        expResult.add(new Point(100, 140));

        Set<Point> resultE = instanceE.getAdjacentPoints();
        assertEquals(expResult, resultE);
        Set<Point> resultW = instanceW.getAdjacentPoints();
        assertEquals(expResult, resultW);
    }

    /**
     * Test of isNeighbour method, of class LayoutTile.
     */
    @Test
    public void testIsNeighbourStraightEastWest() {
        System.out.println("isNeighbourStraightEastWest");
        LayoutTile instanceE = new LayoutTile("StraightTrack", "East", "Center", 100, 100);
        LayoutTile instanceW = new LayoutTile("StraightTrack", "West", "Center", 100, 100);

        LayoutTile adjacentE = new LayoutTile("StraightTrack", "West", "Center", 140, 100);
        LayoutTile adjacentW = new LayoutTile("StraightTrack", "East", "Center", 60, 100);
        boolean resultE = instanceE.isNeighbour(adjacentE);
        assertTrue(resultE);
        resultE = instanceW.isNeighbour(adjacentW);
        assertTrue(resultE);

        boolean resultW = instanceE.isNeighbour(adjacentW);
        assertTrue(resultW);
        resultW = instanceW.isNeighbour(adjacentE);
        assertTrue(resultW);

        adjacentE.setX(120);
        resultE = instanceE.isNeighbour(adjacentE);
        assertFalse(resultE);
        //Check adjacent diagonal
        LayoutTile adjacentNE = new LayoutTile("DiagonalTrack", "South", "Center", 140, 120);
        LayoutTile adjacentSE = new LayoutTile("DiagonalTrack", "East", "Center", 140, 80);
        LayoutTile adjacentSW = new LayoutTile("DiagonalTrack", "West", "Center", 60, 80);
        LayoutTile adjacentNW = new LayoutTile("DiagonalTrack", "North", "Center", 60, 120);

        boolean resultNE = instanceE.isNeighbour(adjacentNE);
        boolean resultSE = instanceE.isNeighbour(adjacentSE);
        boolean resultSW = instanceE.isNeighbour(adjacentSW);
        boolean resultNW = instanceE.isNeighbour(adjacentNW);
        assertTrue(resultNE);
        assertTrue(resultSE);
        assertTrue(resultSW);
        assertTrue(resultNW);
    }

    @Test
    public void testIsNeighbourStraightNorthSouth() {
        System.out.println("isNeighbourStraightNorthSouth");
        LayoutTile instanceN = new LayoutTile("StraightTrack", "North", "Center", 100, 100);
        LayoutTile instanceS = new LayoutTile("StraightTrack", "South", "Center", 100, 100);

        LayoutTile adjacentN = new LayoutTile("StraightTrack", "North", "Center", 100, 60);
        LayoutTile adjacentS = new LayoutTile("StraightTrack", "South", "Center", 100, 140);
        boolean resultN = instanceN.isNeighbour(adjacentN);
        assertTrue(resultN);
        resultN = instanceN.isNeighbour(adjacentS);
        assertTrue(resultN);

        boolean resultS = instanceS.isNeighbour(adjacentN);
        assertTrue(resultS);
        resultS = instanceS.isNeighbour(adjacentS);
        assertTrue(resultS);

        adjacentN.setY(120);
        resultN = instanceN.isNeighbour(adjacentN);
        assertFalse(resultN);
        //Check adjacent diagonal
        LayoutTile adjacentNE = new LayoutTile("DiagonalTrack", "South", "Center", 120, 60);
        LayoutTile adjacentSE = new LayoutTile("DiagonalTrack", "East", "Center", 80, 60);
        LayoutTile adjacentSW = new LayoutTile("DiagonalTrack", "West", "Center", 120, 140);
        LayoutTile adjacentNW = new LayoutTile("DiagonalTrack", "North", "Center", 80, 140);

        boolean resultNE = instanceS.isNeighbour(adjacentNE);
        boolean resultSE = instanceS.isNeighbour(adjacentSE);
        boolean resultSW = instanceS.isNeighbour(adjacentSW);
        boolean resultNW = instanceS.isNeighbour(adjacentNW);
        assertTrue(resultNE);
        assertTrue(resultSE);
        assertTrue(resultSW);
        assertTrue(resultNW);
        resultNE = instanceN.isNeighbour(adjacentNE);
        resultSE = instanceN.isNeighbour(adjacentSE);
        resultSW = instanceN.isNeighbour(adjacentSW);
        resultNW = instanceN.isNeighbour(adjacentNW);
        assertTrue(resultNE);
        assertTrue(resultSE);
        assertTrue(resultSW);
        assertTrue(resultNW);
    }
    
      
    @Test
    public void testIsNeighbourDiagonalEastWest() {
        System.out.println("isNeighbourDiagonalEastWest");
        LayoutTile instanceE = new LayoutTile("DiagonalTrack", "East", "Center", 100, 100);
        LayoutTile instanceW = new LayoutTile("DiagonalTrack", "West", "Center", 100, 100);

        LayoutTile adjacentN = new LayoutTile("StraightTrack", "North", "Center", 80, 60);
        LayoutTile adjacentE = new LayoutTile("StraightTrack", "East", "Center", 140, 120);
        LayoutTile adjacentS = new LayoutTile("StraightTrack", "South", "Center", 120, 140);
        LayoutTile adjacentW = new LayoutTile("StraightTrack", "West", "Center", 60, 80);
        
        LayoutTile instanceNE = new LayoutTile("DiagonalTrack", "East", "Center", 60, 60);
        LayoutTile instanceNW = new LayoutTile("DiagonalTrack", "West", "Center", 140, 140);
        LayoutTile instanceSE = new LayoutTile("DiagonalTrack", "North", "Center", 100, 140);
        LayoutTile instanceSW = new LayoutTile("DiagonalTrack", "South", "Center", 100, 60);

        // _\|/
        //   \_
        //  /|\
        //
        //Check the straights
        boolean resultN = instanceE.isNeighbour(adjacentN);
        boolean resultE = instanceE.isNeighbour(adjacentE);
        boolean resultS = instanceE.isNeighbour(adjacentS);
        boolean resultW = instanceE.isNeighbour(adjacentW);
        assertTrue(resultN);
        assertTrue(resultE);
        assertTrue(resultS);
        assertTrue(resultW);
        
        //Check opposite direction
        resultN = instanceW.isNeighbour(adjacentN);
        resultE = instanceW.isNeighbour(adjacentE);
        resultS = instanceW.isNeighbour(adjacentS);
        resultW = instanceW.isNeighbour(adjacentW);
        assertTrue(resultN);
        assertTrue(resultE);
        assertTrue(resultS);
        assertTrue(resultW);
        
        //Check the Diagonals     
        boolean resultNE = instanceE.isNeighbour(instanceNE);
        boolean resultNW = instanceE.isNeighbour(instanceNW);
        boolean resultSE = instanceE.isNeighbour(instanceSE);
        boolean resultSW = instanceE.isNeighbour(instanceSW);
        assertTrue(resultNE);
        assertTrue(resultNW);
        assertTrue(resultSE);
        assertTrue(resultSW);
        //Check opposite direction
        resultNE = instanceW.isNeighbour(instanceNE);
        resultNW = instanceW.isNeighbour(instanceNW);
        resultSE = instanceW.isNeighbour(instanceSE);
        resultSW = instanceW.isNeighbour(instanceSW);
        assertTrue(resultNE);
        assertTrue(resultNW);
        assertTrue(resultSE);
        assertTrue(resultSW);
    }

    @Test
    public void testIsNeighbourDiagonalNorthSouth() {
        System.out.println("isNeighbourDiagonalNorthSouth");
        LayoutTile instanceN = new LayoutTile("DiagonalTrack", "North", "Center", 100, 100);
        LayoutTile instanceS = new LayoutTile("DiagonalTrack", "South", "Center", 100, 100);

        LayoutTile adjacentN = new LayoutTile("StraightTrack", "North", "Center", 120, 60);
        LayoutTile adjacentE = new LayoutTile("StraightTrack", "East", "Center", 140, 80);
        LayoutTile adjacentS = new LayoutTile("StraightTrack", "South", "Center", 80, 140);
        LayoutTile adjacentW = new LayoutTile("StraightTrack", "West", "Center", 60, 120);
        
        LayoutTile instanceNE = new LayoutTile("DiagonalTrack", "East", "Center", 100, 140);
        LayoutTile instanceNW = new LayoutTile("DiagonalTrack", "West", "Center", 100, 60);
        LayoutTile instanceSE = new LayoutTile("DiagonalTrack", "North", "Center", 140, 60);
        LayoutTile instanceSW = new LayoutTile("DiagonalTrack", "South", "Center", 60, 140);

        //   \|/_
        //   _/
        //   /|\
        //
        //Check the straights
        boolean resultN = instanceN.isNeighbour(adjacentN);
        boolean resultE = instanceN.isNeighbour(adjacentE);
        boolean resultS = instanceN.isNeighbour(adjacentS);
        boolean resultW = instanceN.isNeighbour(adjacentW);
        assertTrue(resultN);
        assertTrue(resultE);
        assertTrue(resultS);
        assertTrue(resultW);
        
        //Check opposite direction
        resultN = instanceS.isNeighbour(adjacentN);
        resultE = instanceS.isNeighbour(adjacentE);
        resultS = instanceS.isNeighbour(adjacentS);
        resultW = instanceS.isNeighbour(adjacentW);
        assertTrue(resultN);
        assertTrue(resultE);
        assertTrue(resultS);
        assertTrue(resultW);
        
        //Check the Diagonals     
        boolean resultNE = instanceN.isNeighbour(instanceNE);
        boolean resultNW = instanceN.isNeighbour(instanceNW);
        boolean resultSE = instanceN.isNeighbour(instanceSE);
        boolean resultSW = instanceN.isNeighbour(instanceSW);
        assertTrue(resultNE);
        assertTrue(resultNW);
        assertTrue(resultSE);
        assertTrue(resultSW);
        //Check opposite direction
        resultNE = instanceS.isNeighbour(instanceNE);
        resultNW = instanceS.isNeighbour(instanceNW);
        resultSE = instanceS.isNeighbour(instanceSE);
        resultSW = instanceS.isNeighbour(instanceSW);
        assertTrue(resultNE);
        assertTrue(resultNW);
        assertTrue(resultSE);
        assertTrue(resultSW);
    }
    
    
}
