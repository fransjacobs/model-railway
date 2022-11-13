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
package jcs.trackservice.dao;

import jcs.trackservice.dao.util.DAOTestHelper;
import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;
import jcs.entities.LocomotiveBean;
import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.tinylog.Logger;

/**
 *
 * @author frans
 */
public class LocomotiveBeanDAOTest {

    private final List<LocomotiveBean> locomotives;

    public LocomotiveBeanDAOTest() {
        DAOTestHelper.setConnectProperties();
        DAOTestHelper.createNewDatabase();
        DAOTestHelper.insertLocoData();
        locomotives = new LinkedList<>();
    }

    @Before
    public void setUp() {
        LocomotiveBean loco2 = new LocomotiveBean(new BigDecimal(2), "BR 81 002", "BR  81 002", 2L, 0L, 2, "DB BR 81 008", "mm_prg", null, 120, 1, 1, 2, 64, null, 600, 0, null, null, true);
        locomotives.add(loco2);
        LocomotiveBean loco11 = new LocomotiveBean(new BigDecimal(11), "NS 1205", null, 11L, 0L, 11, "NS 1211", "mm_prg", null, 120, 0, 0, 0, 64, null, 0, 1, null, null, true);
        locomotives.add(loco11);
        LocomotiveBean loco12 = new LocomotiveBean(new BigDecimal(12), "BR 141 015-08", "BR 141 015-08", 12L, 0L, 12, "DB BR 141 136-2", "mm_prg", null, 120, 0, 0, 0, 64, null, 0, 0, null, null, true);
        locomotives.add(loco12);
        LocomotiveBean loco23 = new LocomotiveBean(new BigDecimal(23), "BR 101 003-2", "BR 101 003-2", 23L, 0L, 23, "DB BR 101 109-7", "mm_prg", null, 200, 0, 0, 0, 64, null, 0, 1, null, null, true);
        locomotives.add(loco23);
        LocomotiveBean loco37 = new LocomotiveBean(new BigDecimal(37), "NS 1720", "S. 1700", 37L, 0L, 37, "NS 1773", "mm_prg", null, 120, 0, 0, 0, 64, null, 0, 0, null, null, true);
        locomotives.add(loco37);
        LocomotiveBean loco63 = new LocomotiveBean(new BigDecimal(63), "NS 6513", "NS  6513", 63L, 0L, 63, "NS 6513", "mm_prg", null, 120, 0, 0, 0, 64, null, 0, 0, null, null, true);
        locomotives.add(loco63);
        LocomotiveBean loco16389 = new LocomotiveBean(new BigDecimal(16389), "193 304-3 DB AG", null, 16389L, 1945312555L, 5, "DB BR 193 304-3", "mfx", "0x5", 160, 5, 15, 15, 255, null, 0, 0, null, null, true);
        locomotives.add(loco16389);
        LocomotiveBean loco16390 = new LocomotiveBean(new BigDecimal(16390), "152 119-4 DBAG", null, 16390L, 2113628077L, 6, "DB BR 152 119-4", "mfx", "0x6", 140, 4, 28, 15, 255, "0", 0, 0, null, null, true);
        locomotives.add(loco16390);
        LocomotiveBean loco16391 = new LocomotiveBean(new BigDecimal(16391), "DB 640 017-9", "DB 640 017-9", 16391L, 2097006535L, 7, "DB BR 640 017-9", "mfx", "0x7", 100, 8, 15, 15, 64, null, 0, 1, null, null, true);
        locomotives.add(loco16391);
        LocomotiveBean loco16392 = new LocomotiveBean(new BigDecimal(16392), "BR 44 690", "BR 44 690", 16392L, 1945180592L, 8, "DB BR 44 100", "mfx", "0x8", 80, 5, 21, 12, 233, null, 0, 0, null, null, true);
        locomotives.add(loco16392);
        LocomotiveBean loco16393 = new LocomotiveBean(new BigDecimal(16393), "Rheingold 1", "Rheingold 1", 16393L, 1945195567L, 9, "DB BR 18 537", "mfx", "0x9", 81, 4, 12, 8, 255, null, 0, 0, null, null, true);
        locomotives.add(loco16393);
        LocomotiveBean loco16394 = new LocomotiveBean(new BigDecimal(16394), "561-05 RRF", "561-05 RRF", 16394L, 1945385732L, 10, "56-05 RRF", "mfx", "0xa", 120, 5, 31, 31, 220, "0", 0, 0, null, null, true);
        locomotives.add(loco16394);
        LocomotiveBean loco16395 = new LocomotiveBean(new BigDecimal(16395), "E 186 007-8 NS", null, 16395L, 1945441079L, 11, "NS 186 012-8", "mfx", "0xb", 140, 5, 16, 16, 255, null, 0, 1, null, null, true);
        locomotives.add(loco16395);
        LocomotiveBean loco16396 = new LocomotiveBean(new BigDecimal(16396), "BR 216 059-6", null, 16396L, 1945302187L, 12, "DB BR 216 059-6", "mfx", "0xc", 120, 5, 13, 13, 64, null, 0, 0, null, null, true);
        locomotives.add(loco16396);
        LocomotiveBean loco16397 = new LocomotiveBean(new BigDecimal(16397), "NS 1139", "NS 1139", 16397L, 4193976353L, 13, "NS 1136", "mfx", "0xd", 140, 6, 16, 4, 64, null, 0, 0, null, null, true);
        locomotives.add(loco16397);
        LocomotiveBean loco16398 = new LocomotiveBean(new BigDecimal(16398), "Rheingold 2", "Rheingold 2", 16398L, 1945186577L, 14, "DB BR 18 473", "mfx", "0xe", 81, 4, 12, 8, 255, null, 0, 0, null, null, false);
        locomotives.add(loco16398);
        LocomotiveBean loco49156 = new LocomotiveBean(new BigDecimal(49156), "NS Plan Y", "DCC Lok 4", 49156L, 0L, 4, "NS Plan Y", "dcc", null, 120, 0, 0, 0, 64, null, 0, 0, null, null, true);
        locomotives.add(loco49156);
    }

    @After
    public void tearDown() {

    }

    @Test
    public void testFindAll() {
        System.out.println("findAll");
        LocomotiveBeanDAO instance = new LocomotiveBeanDAO();
        List<LocomotiveBean> expResult = this.locomotives;
        List<LocomotiveBean> result = instance.findAll();

        for (int i = 0; i < expResult.size(); i++) {
            LocomotiveBean lbr = result.get(i);
            LocomotiveBean lbe = expResult.get(i);
            Logger.trace(lbe.getName());

            assertEquals(lbe.getId(), lbr.getId());
            assertEquals(lbe.getName(), lbr.getName());
            assertEquals(lbe.getPreviousName(), lbr.getPreviousName());
            assertEquals(lbe.getUid(), lbr.getUid());
            assertEquals(lbe.getMfxUid(), lbr.getMfxUid());
            assertEquals(lbe.getAddress(), lbr.getAddress());
            assertEquals(lbe.getIcon(), lbr.getIcon());
            assertEquals(lbe.getDecoderTypeString(), lbr.getDecoderTypeString());
            assertEquals(lbe.getMfxSid(), lbr.getMfxSid());
            assertEquals(lbe.getTachoMax(), lbr.getTachoMax());
            assertEquals(lbe.getvMin(), lbr.getvMin());
            assertEquals(lbe.getAccelerationDelay(), lbr.getAccelerationDelay());
            assertEquals(lbe.getBrakeDelay(), lbr.getBrakeDelay());
            assertEquals(lbe.getVolume(), lbr.getVolume());
            assertEquals(lbe.getSpm(), lbr.getSpm());
            assertEquals(lbe.getVelocity(), lbr.getVelocity());
            assertEquals(lbe.getDirection(), lbr.getDirection());
            assertEquals(lbe.getMfxType(), lbr.getMfxType());
            assertEquals(lbe.getBlock(), lbr.getBlock());
        }
        assertEquals(expResult, result);
    }

    @Test
    public void testFind() {
        System.out.println("find");
        Integer address = 10;
        String decoderTpe = "mfx";
        LocomotiveBeanDAO instance = new LocomotiveBeanDAO();
        LocomotiveBean expResult = this.locomotives.get(11);
        LocomotiveBean result = instance.find(address, decoderTpe);

        assertEquals(expResult.getId(), result.getId());
        assertEquals(expResult.getName(), result.getName());
        assertEquals(expResult.getPreviousName(), result.getPreviousName());
        assertEquals(expResult.getUid(), result.getUid());
        assertEquals(expResult.getMfxUid(), result.getMfxUid());
        assertEquals(expResult.getAddress(), result.getAddress());
        assertEquals(expResult.getIcon(), result.getIcon());
        assertEquals(expResult.getDecoderTypeString(), result.getDecoderTypeString());
        assertEquals(expResult.getMfxSid(), result.getMfxSid());
        assertEquals(expResult.getTachoMax(), result.getTachoMax());
        assertEquals(expResult.getvMin(), result.getvMin());
        assertEquals(expResult.getAccelerationDelay(), result.getAccelerationDelay());
        assertEquals(expResult.getBrakeDelay(), result.getBrakeDelay());
        assertEquals(expResult.getVolume(), result.getVolume());
        assertEquals(expResult.getSpm(), result.getSpm());
        assertEquals(expResult.getVelocity(), result.getVelocity());
        assertEquals(expResult.getDirection(), result.getDirection());
        assertEquals(expResult.getMfxType(), result.getMfxType());
        assertEquals(expResult.getBlock(), result.getBlock());

        assertEquals(expResult, result);
    }

    @Test
    public void testPersist() {
        System.out.println("persist");
        LocomotiveBean newLoco = new LocomotiveBean(new BigDecimal(80L), "BR 81 002", "BR  81 002", 80L, 0L, 80, "DB BR 81 008", "mm_prg", null, 120, 1, 1, 2, 64, null, 600, 0, null, null, true);

        LocomotiveBeanDAO instance = new LocomotiveBeanDAO();
        BigDecimal result = instance.persist(newLoco);

        LocomotiveBean locoId = instance.findById(result);

        assertEquals(newLoco, locoId);

        LocomotiveBean loco = instance.find(80, "mm_prg");
        assertEquals(newLoco, loco);
        assertEquals(loco.getId(), result);

        newLoco.setIcon("new Icon");
        instance.persist(newLoco);

        loco = instance.find(80, "mm_prg");
        assertEquals(newLoco, loco);
        instance.remove(loco);
    }

    @Test
    public void testRemove() {
        System.out.println("remove");
        LocomotiveBean newLoco = new LocomotiveBean(new BigDecimal(70L), "TEST Loco remove", "TEST Loco remove", 70L, 0L, 70, "TEST Loco remove", "mm_prg", null, 120, 1, 1, 2, 64, null, 600, 0, null, null, true);

        LocomotiveBeanDAO instance = new LocomotiveBeanDAO();

        instance.persist(newLoco);
        LocomotiveBean loco = instance.find(70, "mm_prg");
        assertEquals(newLoco, loco);
        BigDecimal id = loco.getId();

        LocomotiveBean locoId = instance.findById(id);
        assertEquals(loco, locoId);

        instance.remove(newLoco);
        loco = instance.find(70, "mm_prg");
        assertNull(loco);
    }

}
