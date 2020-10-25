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
package lan.wervel.jcs.controller.cs2;

import lan.wervel.jcs.controller.cs2.can.CanMessage;
import lan.wervel.jcs.entities.enums.Direction;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author fransjacobs
 */
public class DirectionInfoTest {

    private CanMessage message;

    public DirectionInfoTest() {
    }

    @Before
    public void setUp() {
        message = new CanMessage(new int[]{0x00, 0x0a, 0xcb, 0x13, 0x04, 0x00, 0x00, 0x00, 0x0c, 0x00, 0x00, 0x00, 0x00});
        CanMessage response = new CanMessage(new int[]{0x00, 0x0b, 0xcb, 0x13, 0x08, 0x00, 0x00, 0x00, 0x0c, 0x02, 0x00, 0x00, 0x00});
        message.addResponse(response);
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of toString method, of class DirectionInfo.
     */
    @Test
    public void testToString() {
        System.out.println("toString");
        DirectionInfo instance = new DirectionInfo(message);
        String expResult = "DirectionInfo{direction=BACKWARDS}";
        String result = instance.toString();
        assertEquals(expResult, result);
    }

    /**
     * Test of getDirection method, of class DirectionInfo.
     */
    @Test
    public void testGetDirection() {
        System.out.println("getDirection");
        DirectionInfo instance = new DirectionInfo(message);
        Direction expResult = Direction.BACKWARDS;
        Direction result = instance.getDirection();
        assertEquals(expResult, result);
    }

}
