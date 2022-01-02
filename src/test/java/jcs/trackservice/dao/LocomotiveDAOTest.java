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
import jcs.entities.enums.Direction;
import jcs.entities.Locomotive;
import jcs.entities.enums.DecoderType;
import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author frans
 */
public class LocomotiveDAOTest {

  private final List<Locomotive> locomotives;

  public LocomotiveDAOTest() {
    DAOTestHelper.setConnectProperties();
    DAOTestHelper.createNewDatabase();
    DAOTestHelper.insertLocoData();
    locomotives = new LinkedList<>();
  }

  @Before
  public void setUp() {

    Locomotive loco1 = new Locomotive(1, "V200 027", "V200", "3021", DecoderType.MM2, Direction.FORWARDS, 0, 14, 0, 0, 0, 5, "10000", null, Direction.FORWARDS, null, new BigDecimal(1));

    Locomotive loco2 = new Locomotive(2, "BR81 002", "BR 81 002", "30321", DecoderType.MM, Direction.FORWARDS, 0, 14, 0, 0, 0, 1, "1", null, Direction.FORWARDS, null, new BigDecimal(2));

    Locomotive loco3 = new Locomotive(3, "BR 1022", "BR1022", "3795.10", DecoderType.MM2, Direction.FORWARDS, 0, 14, 0, 0, 0, 5, "001-0", null, Direction.FORWARDS, null, new BigDecimal(3));

    Locomotive loco6 = new Locomotive(6, "BR 44 690", "BR 44 690", "3047", DecoderType.MM2, Direction.FORWARDS, 0, 14, 0, 0, 0, 5, "10000", null, Direction.FORWARDS, null, new BigDecimal(4));

    Locomotive loco8 = new Locomotive(8, "NS 6502", "DHG 700C NS 6502", "29159.1", DecoderType.MM, Direction.FORWARDS, 0, 14, 0, 0, 0, 1, "1", null, Direction.FORWARDS, null, new BigDecimal(5));

    Locomotive loco11 = new Locomotive(11, "NS 1205", "NS 1205", "3055", DecoderType.MM2, Direction.FORWARDS, 0, 14, 0, 0, 0, 5, "10000", null, Direction.FORWARDS, null, new BigDecimal(6));

    Locomotive loco12 = new Locomotive(12, "BR141 015", "E 141 015-8", "3034.10", DecoderType.MM2, Direction.FORWARDS, 0, 14, 0, 0, 0, 5, "10000", null, Direction.FORWARDS, null, new BigDecimal(7));

    Locomotive loco14 = new Locomotive(14, "V36", "V36/BR236", "3142", DecoderType.MM2, Direction.FORWARDS, 0, 14, 0, 0, 0, 5, "10000", null, Direction.FORWARDS, null, new BigDecimal(8));

    Locomotive loco17 = new Locomotive(17, "1855", "NS 1855", "37263", DecoderType.MM2, Direction.FORWARDS, 0, 14, 0, 0, 0, 5, "10000", null, Direction.FORWARDS, null, new BigDecimal(9));

    Locomotive loco24 = new Locomotive(6, "NS E186", "NS E186", "36629", DecoderType.MFX, Direction.FORWARDS, 0, 14, 0, 0, 0, 16, "1000000000000000", null, Direction.FORWARDS, null, new BigDecimal(10));

    Locomotive loco25 = new Locomotive(25, "ER 20", "Hercules Police", "36793", DecoderType.MM2, Direction.FORWARDS, 0, 14, 0, 0, 0, 5, "10000", null, Direction.FORWARDS, null, new BigDecimal(11));

    Locomotive loco52 = new Locomotive(5, "BR 152", "BR 152", "39850", DecoderType.MFX, Direction.FORWARDS, 0, 14, 0, 0, 0, 16, "1000000000000000", null, Direction.FORWARDS, null, new BigDecimal(12));

    locomotives.add(loco1);
    locomotives.add(loco2);
    locomotives.add(loco3);
    locomotives.add(loco52);
    locomotives.add(loco6);
    locomotives.add(loco24);
    locomotives.add(loco8);
    locomotives.add(loco11);
    locomotives.add(loco12);
    locomotives.add(loco14);
    locomotives.add(loco17);
    locomotives.add(loco25);
  }

  @After
  public void tearDown() {

  }

  @Test
  public void testFindAll() {
    System.out.println("findAll");
    LocomotiveDAO instance = new LocomotiveDAO();

    List<Locomotive> expResult = this.locomotives;
    List<Locomotive> result = instance.findAll();
    assertEquals(expResult, result);
  }

  @Test
  public void testFind() {
    System.out.println("find");
    Integer address = 17;
    LocomotiveDAO instance = new LocomotiveDAO();
    Locomotive expResult = this.locomotives.get(10);
    Locomotive result = instance.find(address,DecoderType.MM2);

    assertEquals(expResult.getId(), result.getId());
    assertEquals(expResult.getAddress(), result.getAddress());
    assertEquals(expResult.getName(), result.getName());
    assertEquals(expResult.getDescription(), result.getDescription());
    assertEquals(expResult.getCatalogNumber(), result.getCatalogNumber());
    assertEquals(expResult.getDecoderType(), result.getDecoderType());
    assertEquals(expResult.getDirection(), result.getDirection());
    assertEquals(expResult.getSpeed(), result.getSpeed());
    assertEquals(expResult.getSpeedSteps(), result.getSpeedSteps());
    assertEquals(expResult.getTachoMax(), result.getTachoMax());
    assertEquals(expResult.getvMax(), result.getvMax());
    assertEquals(expResult.getvMin(), result.getvMin());
    assertEquals(expResult.getDefaultDirection(), result.getDefaultDirection());
    assertEquals(expResult.getFunctionCount(), result.getFunctionCount());
    assertEquals(expResult.getFunctionTypes(), result.getFunctionTypes());

    assertArrayEquals(expResult.getFunctionValues(), result.getFunctionValues());

    assertEquals(expResult, result);
  }

  @Test
  public void testPersist() {
    System.out.println("persist");
    Locomotive newLoco = new Locomotive(80, "TEST Loco", "Test Loco to check Persist", "xxx", DecoderType.MM2, Direction.FORWARDS, 0, 14, 0, 0, 0, 5, "11000", null, Direction.FORWARDS, null, null);

    LocomotiveDAO instance = new LocomotiveDAO();
    BigDecimal result = instance.persist(newLoco);
    
    Locomotive locoId = instance.findById(result);
    
    assertEquals(newLoco, locoId);
    

    Locomotive loco = instance.find(80,DecoderType.MM2);
    assertEquals(newLoco, loco);
    assertEquals(loco.getId(), result);

    newLoco.setF0(true);
    instance.persist(newLoco);

    loco = instance.find(80,DecoderType.MM2);
    assertEquals(newLoco, loco);
    instance.remove(loco);
  }

    @Test
  public void testPersist2() {
    System.out.println("persist2");
    Locomotive newLoco = new Locomotive(66, "NS E186 2", "NS E186 2", "36629", DecoderType.MFX, Direction.FORWARDS, 0, 14, 0, 0, 0, 16, "1000000000000000", null, Direction.FORWARDS, null, null);

    LocomotiveDAO instance = new LocomotiveDAO();
    BigDecimal result = instance.persist(newLoco);
    
    Locomotive loco = instance.findById(result);
    
    assertEquals(newLoco, loco);
    Locomotive locoA = instance.find(66,DecoderType.MFX);
    assertEquals(newLoco, locoA);
    assertEquals(loco.getId(), result);

    newLoco.setF0(true);
    instance.persist(newLoco);

    loco = instance.findById(result);
    assertEquals(newLoco, loco);

    newLoco.setF1(true);
    instance.persist(newLoco);

    loco = instance.findById(result);
    assertEquals(newLoco, loco);



    instance.remove(loco);
  }

  
  
  @Test
  public void testRemove() {
    System.out.println("remove");
    Locomotive newLoco = new Locomotive(70, "TEST Loco remove", "Test Loco to check Persist", "xxx", DecoderType.MM, Direction.FORWARDS, 0, 14, 0, 0, 0, 1, "1", null, Direction.FORWARDS, null, null);

    LocomotiveDAO instance = new LocomotiveDAO();

    instance.persist(newLoco);
    Locomotive loco = instance.find(70,DecoderType.MM);
    assertEquals(newLoco, loco);
    BigDecimal id = loco.getId();
    
    Locomotive locoId = instance.findById(id);
    assertEquals(loco, locoId);
 
    instance.remove(newLoco);
    loco = instance.find(70,DecoderType.MM);
    assertNull(loco);
  }

}
