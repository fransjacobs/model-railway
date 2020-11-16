/*
 * Copyright (C) 2020 fransjacobs.
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
package lan.wervel.jcs.ui.layout.tiles;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import lan.wervel.jcs.ui.layout.tiles.enums.Rotation;
import org.junit.After;
import org.junit.Before;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 *
 * @author fransjacobs
 */
public class StraightTrackTest {

    public StraightTrackTest() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of rotate method, of class StraightTrack.
     */
    //@Test
    public void testRotate() {
        System.out.println("rotate");
        StraightTrack instance = null;
        instance.rotate();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of renderTile method, of class StraightTrack.
     */
    //@Test
    public void testRenderTile() {
        System.out.println("renderTile");
        Graphics2D g2 = null;
        Color trackColor = null;
        StraightTrack instance = null;
        instance.renderTile(g2, trackColor);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    @Test
    public void testContains() {
        System.out.println("contains");
        StraightTrack tile = new StraightTrack(Rotation.R0, 100, 100);

        assertEquals(40, tile.getWidth());
        assertEquals(40, tile.getHeight());
        assertEquals(new Point(100, 100), tile.getCenter());

        System.out.println(tile);

        assertTrue(tile.contains(100, 100));

        assertTrue(tile.contains(80, 80));
        assertTrue(tile.contains(120, 120));
        assertTrue(tile.contains(80, 120));
        assertTrue(tile.contains(120, 80));

        assertFalse(tile.contains(79, 79));
        assertFalse(tile.contains(121, 121));
        assertFalse(tile.contains(79, 120));
        assertFalse(tile.contains(80, 121));

    }

}
