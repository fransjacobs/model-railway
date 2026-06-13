/*
 * Copyright 2026 Frans Jacobs.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jcs.ui.layout.tiles;

import java.awt.Point;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import jcs.entities.TileBean;
import jcs.entities.TileBean.Orientation;
import static jcs.entities.TileBean.Orientation.EAST;
import static jcs.entities.TileBean.Orientation.NORTH;
import static jcs.entities.TileBean.Orientation.SOUTH;
import static jcs.entities.TileBean.Orientation.WEST;
import static jcs.entities.TileBean.TileType.CROSS;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;

/**
 *
 */
public class CrossTest {

  public CrossTest() {
  }

  @BeforeAll
  public static void setUpClass() {
  }

  @AfterAll
  public static void tearDownClass() {
  }

  @BeforeEach
  public void setUp() {
    TileCache.flush();
  }

  @AfterEach
  public void tearDown() {
  }

  @Test
  public void testGetAltPointsEast() {
    System.out.println("getAltPointsEast");
    Tile instance = TileCache.createTile(CROSS, Orientation.EAST, 100, 100);

    Set<Point> expResult = new HashSet<>();
    Point cp2 = new Point(140, 100);
    expResult.add(cp2);

    Set<Point> result = instance.getAltPoints();
    assertEquals(expResult, result);

    TileBean tileBean = new TileBean("cx-1", CROSS, Orientation.EAST, 100, 100);
    instance = TileCache.createTile(tileBean, true);
    TileCache.addTile(instance);

    assertEquals(2, instance.getAllPoints().size());

    result = instance.getAltPoints();
    assertEquals(expResult, result);

    Set<Point> expResultAll = new HashSet<>();
    Point cp = new Point(100, 100);
    expResultAll.add(cp);
    expResultAll.add(cp2);

    result = instance.getAllPoints();
    assertEquals(expResultAll, result);

    Tile t = TileCache.findTile(cp);
    assertEquals(instance, t);
    t = TileCache.findTile(cp2);
    assertEquals(instance, t);
  }

  /**
   * Test of getAllPoints method, of class Cross.
   */
  @Test
  public void testGetAllPointsEast() {
    System.out.println("getAllPointsEast");
    Tile instance = TileCache.createTile(CROSS, Orientation.EAST, 100, 100);

    Set<Point> expResult = new HashSet<>();
    Point cp = new Point(100, 100);
    Point cp2 = new Point(140, 100);
    expResult.add(cp);
    expResult.add(cp2);

    Set<Point> result = instance.getAllPoints();
    assertEquals(2, result.size());
    assertEquals(expResult, result);
  }

  @Test
  public void testGetAllPointsWest() {
    System.out.println("getAllPointsWest");
    Tile instance = TileCache.createTile(CROSS, Orientation.WEST, 100, 100);

    Set<Point> expResult = new HashSet<>();
    Point cp = new Point(100, 100);
    Point cp2 = new Point(60, 100);
    expResult.add(cp);
    expResult.add(cp2);

    Set<Point> result = instance.getAllPoints();
    assertEquals(2, result.size());
    assertEquals(expResult, result);
  }

  @Test
  public void testGetAllPointsNorth() {
    System.out.println("getAllPointsNorth");
    Tile instance = TileCache.createTile(CROSS, Orientation.NORTH, 100, 100);

    Set<Point> expResult = new HashSet<>();
    Point cp = new Point(100, 100);
    Point cp2 = new Point(100, 60);
    expResult.add(cp);
    expResult.add(cp2);

    Set<Point> result = instance.getAllPoints();
    assertEquals(2, result.size());
    assertEquals(expResult, result);
  }

  @Test
  public void testGetAllPointsSouth() {
    System.out.println("getAllPointsSouth");
    Tile instance = TileCache.createTile(CROSS, Orientation.SOUTH, 100, 100);

    Set<Point> expResult = new HashSet<>();
    Point cp = new Point(100, 100);
    Point cp2 = new Point(100, 140);
    expResult.add(cp);
    expResult.add(cp2);

    Set<Point> result = instance.getAllPoints();
    assertEquals(2, result.size());
    assertEquals(expResult, result);
  }

  /**
   * Test of getNeighborPoints method, of class Cross.
   */
  @Test
  public void testGetNeighborPoints() {
    System.out.println("getNeighborPoints");
    Tile instance = TileCache.createTile(CROSS, Orientation.EAST, 100, 100);

    Map<TileBean.Orientation, Point> result = instance.getNeighborPoints();
    Map<TileBean.Orientation, Point> expResult = new HashMap<>();

    Point e = new Point(140, 60);
    Point n = new Point(100, 60);
    Point w = new Point(100, 140);
    Point s = new Point(140, 140);

    expResult.put(EAST, e);
    expResult.put(WEST, w);
    expResult.put(NORTH, n);
    expResult.put(SOUTH, s);

    assertEquals(expResult, result);
  }

  @Test
  public void testGetEdgePoints() {
    System.out.println("getEdgePoints");
    Tile instance = TileCache.createTile(CROSS, Orientation.EAST, 100, 100);

    Map<TileBean.Orientation, Point> result = instance.getEdgePoints();
    Map<TileBean.Orientation, Point> expResult = new HashMap<>();

    Point e = new Point(140, 80);
    Point n = new Point(100, 80);
    Point w = new Point(100, 120);
    Point s = new Point(140, 120);

    expResult.put(EAST, e);
    expResult.put(WEST, w);
    expResult.put(NORTH, n);
    expResult.put(SOUTH, s);

    assertEquals(expResult, result);
  }

  @Test
  public void testIsDiagonalOpposite() {
    System.out.println("isDiagonalOpposite");
    TileBean.Orientation from = EAST;
    TileBean.Orientation to = NORTH;
    Tile instance = TileCache.createTile(CROSS, Orientation.EAST, 100, 100);

    boolean expResult = false;
    boolean result = instance.isDiagonalOpposite(from, to);
    assertEquals(expResult, result);

    to = WEST;

    expResult = true;
    result = instance.isDiagonalOpposite(from, to);
    assertEquals(expResult, result);
  }

}
