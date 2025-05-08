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
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author fransjacobs
 */
public class LayoutUtilTest {

  public LayoutUtilTest() {
    System.setProperty("trackServiceSkipControllerInit", "true");
    System.setProperty("persistenceService", "jcs.persistence.TestH2PersistenceService");
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

}
