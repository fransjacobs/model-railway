/*
 * Copyright (C) 2020 Frans Jacobs <frans.jacobs@gmail.com>.
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

import lan.wervel.jcs.entities.enums.Direction;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Frans Jacobs <frans.jacobs@gmail.com>
 */
public class LocomotiveTest {

  private Locomotive loco1;
  private Locomotive loco2;

  public LocomotiveTest() {

  }

  @Before
  public void setUp() {
    loco1 = new Locomotive(10, "Loco1", "Loco 1", "1234", 0, true, false, false, false, false);
    loco1.setDirection(Direction.FORWARDS);
    loco1.setSpeed(50);

    loco2 = new Locomotive(11, "Loco2", "Loco 2", "4321", 0, true, true, false, false, false);
    loco2.setDirection(Direction.FORWARDS);
    loco2.setSpeed(50);
  }

  @After
  public void tearDown() {
  }

  /**
   * Test of changeDirection method, of class Locomotive.
   */
  //@Test
  public void testChangeDirection() {
    System.out.println("changeDirection");
    Locomotive instance = new Locomotive();
    instance.changeDirection();
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of getDefaultDirection method, of class Locomotive.
   */
  //@Test
  public void testGetDefaultDirection() {
    System.out.println("getDefaultDirection");
    Locomotive instance = new Locomotive();
    Direction expResult = null;
    Direction result = instance.getDefaultDirection();
    assertEquals(expResult, result);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of isChanged method, of class Locomotive.
   */
  @Test
  public void testIsChanged1() {
    System.out.println("isChanged1");
    Locomotive other = new Locomotive(10, "Loco1", "Loco 1", "1234", 0, true, false, false, false, false);
    other.setDirection(Direction.FORWARDS);
    other.setSpeed(50);

    Locomotive instance = loco1;
    boolean expResult = false;
    boolean result = instance.isChanged(other);

    assertEquals(expResult, result);
  }

  @Test
  public void testIsChanged2() {
    System.out.println("isChanged2");
    Locomotive other = new Locomotive(10, "Loco1", "Loco 1", "1234", 0, true, true, false, false, false);

    other.setDirection(Direction.FORWARDS);
    other.setSpeed(50);
    
    
    System.out.println("f0: "+other.isF0()+", f1: "+other.isF1()+", f2: "+other.isF2()+", f3: "+other.isF3()+", f4: "+other.isF4() );

    Locomotive instance = loco1;
    boolean expResult = true;
    boolean result = instance.isChanged(other);

    assertEquals(expResult, result);
  }

  @Test
  public void testIsChanged3() {
    System.out.println("isChanged3");
    Locomotive other = new Locomotive(10, "Loco1", "Loco 1", "1234", 0, true, false, false, false, false);
    other.setDirection(Direction.BACKWARDS);
    other.setSpeed(50);

    Locomotive instance = loco1;
    boolean expResult = true;
    boolean result = instance.isChanged(other);

    assertEquals(expResult, result);
  }

  @Test
  public void testIsChanged4() {
    System.out.println("isChanged3");
    Locomotive other = new Locomotive(10, "Loco1", "Loco 1", "1234", 0, true, false, false, false, false);
    other.setDirection(Direction.FORWARDS);
    other.setSpeed(40);

    Locomotive instance = loco1;
    boolean expResult = true;
    boolean result = instance.isChanged(other);

    assertEquals(expResult, result);
  }

  /**
   * Test of equals method, of class Locomotive.
   */
  @Test
  public void testEquals() {
    System.out.println("equals");

    Locomotive loco = new Locomotive(11, "Loco2", "Loco 2", "4321", 0, true, true, false, false, false);
    loco.setDirection(Direction.FORWARDS);
    loco.setSpeed(50);

    Locomotive instance = loco2;
    boolean expResult = true;
    boolean result = instance.equals(loco);
    assertEquals(expResult, result);

    assertFalse(loco.equals(loco1));
  }

  
  
}
