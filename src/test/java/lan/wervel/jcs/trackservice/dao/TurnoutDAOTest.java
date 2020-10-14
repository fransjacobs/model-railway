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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lan.wervel.jcs.entities.Turnout;
import lan.wervel.jcs.entities.enums.AccessoryValue;
import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author frans
 */
public class TurnoutDAOTest {

  private final List<Turnout> turnouts;

  public TurnoutDAOTest() {
    DAOTestHelper.setConnectProperties();
    DAOTestHelper.createNewDatabase();
    DAOTestHelper.insertTurnoutData();

    turnouts = new ArrayList<>();
  }

  @Before
  public void setUp() {
    Turnout t1 = new Turnout(1, "T 1", "5117 R");

    t1.setValue(AccessoryValue.GREEN);
    t1.setName("T 1");
    t1.setId(new BigDecimal(1));
    turnouts.add(t1);

    Turnout t2 = new Turnout(2, "T 2", "5117 L");
    t2.setValue(AccessoryValue.RED);
    t2.setName("T 2");
    t2.setId(new BigDecimal(2));
    turnouts.add(t2);
  }

  @After
  public void tearDown() {
  }

  @Test
  public void testFindAll() {
    System.out.println("findAll");
    TurnoutDAO instance = new TurnoutDAO();
    List<Turnout> expResult = turnouts;
    List<Turnout> result = instance.findAll();

    assertEquals(expResult, result);
    result = instance.findAll();
    assertEquals(turnouts, result);
  }

  @Test
  public void testFind() {
    System.out.println("find");

    Integer address = 2;
    TurnoutDAO instance = new TurnoutDAO();
    Turnout expResult = turnouts.get(1);
    Turnout result = instance.find(address);
    assertEquals(expResult, result);

    address = 1;
    expResult = turnouts.get(0);
    result = instance.find(address);
    assertEquals(expResult, result);
  }

  @Test
  public void testPersist() {
    System.out.println("persist");

    Turnout t = new Turnout(3, "T 3", "5117 R");
    t.setValue(AccessoryValue.GREEN);
    t.setName("T 3");

    TurnoutDAO instance = new TurnoutDAO();
    BigDecimal result = instance.persist(t);
    Turnout tr = instance.find(3);

    BigDecimal expResult = new BigDecimal(3);
    assertEquals(expResult, result);
    assertNotNull(tr);
  }
  
  @Test
  public void testPersistUpdate() {
    System.out.println("persist");
    TurnoutDAO instance = new TurnoutDAO();

    Turnout t = instance.find(2);
    assertNotNull(t);
    assertEquals(AccessoryValue.RED,t.getValue());
    BigDecimal expResult = t.getId();
    t.setValue(AccessoryValue.GREEN);
    
    BigDecimal result = instance.persist(t);
    assertEquals(expResult, result);

    Turnout tr = instance.find(2);
    assertNotNull(tr);
    assertEquals(AccessoryValue.GREEN,tr.getValue());
  }

  @Test
  public void testPersistNullValue() {
    System.out.println("persistNullValue");

    Turnout t = new Turnout(4, "T 4", "5117 R");
    t.setName("T 4");

    TurnoutDAO instance = new TurnoutDAO();
    BigDecimal result = instance.persist(t);
    Turnout tr = instance.find(4);

    BigDecimal expResult = new BigDecimal(3);
    assertEquals(expResult, result);
    assertNotNull(tr);

    assertEquals(AccessoryValue.OFF, tr.getValue());
  }

  @Test
  public void testPersist2() {
    System.out.println("persist");
    Turnout t = new Turnout(79, "T 79", "5117 L");

    t.setValue(AccessoryValue.OFF);
    t.setName("W 79");

    TurnoutDAO instance = new TurnoutDAO();
    BigDecimal result = instance.persist(t);

    Turnout tr = instance.find(79);

    BigDecimal expResult = new BigDecimal(3);
    assertEquals(expResult, result);

    assertNotNull(tr);

    int sas = instance.findAll().size();
    assertEquals(3, sas);
  }

  @Test
  public void testRemove() {
    System.out.println("remove");
    Turnout t = new Turnout(12, "T 12", "5117 L");
    t.setValue(AccessoryValue.GREEN);
    t.setName("T 12");
    t.setValue(AccessoryValue.OFF);
    t.setName("W 12");

    TurnoutDAO instance = new TurnoutDAO();

    instance.persist(t);
    Turnout tr = instance.find(12);

    assertEquals(t, tr);
    instance.remove(t);

    tr = instance.find(12);
    assertNull(tr);
  }
}
