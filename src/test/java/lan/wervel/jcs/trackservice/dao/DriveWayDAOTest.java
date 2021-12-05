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
import lan.wervel.jcs.entities.DriveWay;
import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author frans
 */
public class DriveWayDAOTest {

    private final List<DriveWay> driveWays;

    public DriveWayDAOTest() {
        DAOTestHelper.setConnectProperties();
        DAOTestHelper.createNewDatabase();
        DAOTestHelper.insertLocoData();
        DAOTestHelper.insertDriveWayData();

        driveWays = new ArrayList<>();
    }

    @Before
    public void setUp() {
        BigDecimal id = new BigDecimal(1);
        Integer address = 1;
        BigDecimal femoId = new BigDecimal(1);
        String name = "Blk 1";
        String description = "Block 1";
        DriveWay dw = new DriveWay(address, name, description);
        dw.setId(id);
        dw.setActive(true);
        this.driveWays.add(dw);
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testFindAll() {
        System.out.println("findAll");
        DriveWayDAO instance = new DriveWayDAO();
        List<DriveWay> expResult = driveWays;
        List<DriveWay> result = instance.findAll();
        assertEquals(expResult, result);
    }

    @Test
    public void testFind() {
        System.out.println("find");
        Integer address = 1;
        DriveWayDAO instance = new DriveWayDAO();
        DriveWay expResult = driveWays.get(0);
        DriveWay result = instance.find(address);

        assertEquals(expResult.getId(), result.getId());
        assertEquals(expResult.getAddress(), result.getAddress());
        assertEquals(expResult.getName(), result.getName());
        assertEquals(expResult.getDescription(), result.getDescription());
        assertEquals(expResult.getFromLatiId(), result.getFromLatiId());
        assertEquals(expResult.getToLatiId(), result.getToLatiId());
        assertEquals(expResult.getLocoId(), result.getLocoId());
        assertEquals(expResult.isActive(), result.isActive());
        assertEquals(expResult.isReserved(), result.isReserved());
        assertEquals(expResult.isOccupied(), result.isOccupied());

        assertEquals(expResult, result);
    }

    @Test
    public void testPersist() {
        System.out.println("persist");

        BigDecimal id = new BigDecimal(2);
        Integer address = 2;
        String name = "Blk 2";
        String description = "Block 2";
        BigDecimal fromLatiId = null;
        BigDecimal toLatiId = null;
        BigDecimal locoId = null;
        boolean active = true;
        boolean reserved = false;
        boolean occupied = false;

        DriveWay dw = new DriveWay(null, address, name, description, fromLatiId, toLatiId, locoId, active, reserved, occupied);

        DriveWay expected = new DriveWay(id, address, name, description, fromLatiId, toLatiId, locoId, active, reserved, occupied);

        DriveWayDAO instance = new DriveWayDAO();
        BigDecimal result = instance.persist(dw);
        BigDecimal expResult = id;
        assertNotNull(result);
        assertEquals(expResult, result);

        DriveWay pdw = instance.find(2);
        assertNotNull(dw);
        assertEquals(expected, pdw);

        expected = new DriveWay(id, address, name, description, fromLatiId, toLatiId, new BigDecimal(1), active, true, true);
        dw.setId(id);
        dw.setOccupied(true);
        dw.setReserved(true);
        dw.setLocoId(new BigDecimal(1));
        instance.persist(dw);

        pdw = instance.find(2);
        assertEquals(expected, pdw);
    }

    @Test
    public void testRemove() {
        System.out.println("remove");
        BigDecimal id = new BigDecimal(2);
        Integer address = 2;
        String name = "Blk 2";
        String description = "Block 2";
        BigDecimal fromLatiId = null;
        BigDecimal toLatiId = null;
        BigDecimal locoId = null;
        boolean active = true;
        boolean reserved = false;
        boolean occupied = false;

        DriveWay dw = new DriveWay(null, address, name, description, fromLatiId, toLatiId, locoId, active, reserved, occupied);

        DriveWayDAO instance = new DriveWayDAO();

        BigDecimal result = instance.persist(dw);
        assertNotNull(result);

        DriveWay pdw = instance.find(address);
        assertNotNull(pdw);

        instance.remove(pdw);
        pdw = instance.find(address);
        assertNull(pdw);
    }

}
