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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
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

  @Test
  public void testGetNeighborPointsE() {
    System.out.println("getNeighborPointsE");
    Tile instance = TileCache.createTile(CROSS, EAST, 200, 200);

    Map<TileBean.Orientation, Point> result = instance.getNeighborPoints();
    Map<TileBean.Orientation, Point> expResult = new HashMap<>();

    expResult.put(NORTH, new Point(200, 160));
    expResult.put(EAST, new Point(240, 160));
    expResult.put(SOUTH, new Point(240, 240));
    expResult.put(WEST, new Point(200, 240));

    assertEquals(expResult, result);
  }

  @Test
  public void testGetNeighborPointsS() {
    System.out.println("getNeighborPointsS");
    Tile instance = TileCache.createTile(CROSS, SOUTH, 200, 200);

    Map<TileBean.Orientation, Point> result = instance.getNeighborPoints();
    Map<TileBean.Orientation, Point> expResult = new HashMap<>();

    expResult.put(NORTH, new Point(240, 200));
    expResult.put(EAST, new Point(240, 240));
    expResult.put(SOUTH, new Point(160, 240));
    expResult.put(WEST, new Point(160, 200));

    assertEquals(expResult, result);
  }

  @Test
  public void testGetNeighborPointsN() {
    System.out.println("getNeighborPointsN");
    Tile instance = TileCache.createTile(CROSS, NORTH, 200, 200);

    Map<TileBean.Orientation, Point> result = instance.getNeighborPoints();
    Map<TileBean.Orientation, Point> expResult = new HashMap<>();

    expResult.put(NORTH, new Point(240, 160));
    expResult.put(EAST, new Point(240, 200));
    expResult.put(SOUTH, new Point(160, 200));
    expResult.put(WEST, new Point(160, 160));

    assertEquals(expResult, result);
  }

  @Test
  public void testGetNeighborPointsW() {
    System.out.println("getNeighborPointsW");
    Tile instance = TileCache.createTile(CROSS, WEST, 200, 200);

    Map<TileBean.Orientation, Point> result = instance.getNeighborPoints();
    Map<TileBean.Orientation, Point> expResult = new HashMap<>();

    expResult.put(NORTH, new Point(160, 160));
    expResult.put(EAST, new Point(200, 160));
    expResult.put(SOUTH, new Point(200, 240));
    expResult.put(WEST, new Point(160, 240));

    assertEquals(expResult, result);
  }

  @Test
  public void testGetEdgePointsE() {
    System.out.println("getEdgePointsE");
    Tile instance = TileCache.createTile(CROSS, Orientation.EAST, 200, 200);

    Map<TileBean.Orientation, Point> result = instance.getEdgePoints();
    Map<TileBean.Orientation, Point> expResult = new HashMap<>();

    expResult.put(NORTH, new Point(200, 180));
    expResult.put(EAST, new Point(240, 180));
    expResult.put(SOUTH, new Point(240, 220));
    expResult.put(WEST, new Point(200, 220));

    assertEquals(expResult, result);
  }

  @Test
  public void testGetEdgePointsS() {
    System.out.println("getEdgePointsS");
    Tile instance = TileCache.createTile(CROSS, Orientation.SOUTH, 200, 200);

    Map<TileBean.Orientation, Point> result = instance.getEdgePoints();
    Map<TileBean.Orientation, Point> expResult = new HashMap<>();

    expResult.put(NORTH, new Point(220, 200));
    expResult.put(EAST, new Point(220, 240));
    expResult.put(SOUTH, new Point(180, 240));
    expResult.put(WEST, new Point(180, 200));

    assertEquals(expResult, result);
  }

  @Test
  public void testGetEdgePointsW() {
    System.out.println("getEdgePointsW");
    Tile instance = TileCache.createTile(CROSS, Orientation.WEST, 200, 200);

    Map<TileBean.Orientation, Point> result = instance.getEdgePoints();
    Map<TileBean.Orientation, Point> expResult = new HashMap<>();

    expResult.put(NORTH, new Point(160, 180));
    expResult.put(EAST, new Point(200, 180));
    expResult.put(SOUTH, new Point(200, 220));
    expResult.put(WEST, new Point(160, 220));

    assertEquals(expResult, result);
  }

  @Test
  public void testGetEdgePointsN() {
    System.out.println("getEdgePointsN");
    Tile instance = TileCache.createTile(CROSS, Orientation.NORTH, 200, 200);

    Map<TileBean.Orientation, Point> result = instance.getEdgePoints();
    Map<TileBean.Orientation, Point> expResult = new HashMap<>();

    expResult.put(NORTH, new Point(220, 160));
    expResult.put(EAST, new Point(220, 200));
    expResult.put(SOUTH, new Point(180, 200));
    expResult.put(WEST, new Point(180, 160));

    assertEquals(expResult, result);
  }

  @Test
  public void testIsDiagonalOppositeE() {
    System.out.println("isDiagonalOppositeE");
    Tile instance = TileCache.createTile(CROSS, Orientation.EAST, 100, 100);

    assertTrue(instance.isDiagonalOpposite(EAST, WEST));
    assertTrue(instance.isDiagonalOpposite(WEST, EAST));

    assertTrue(instance.isDiagonalOpposite(NORTH, SOUTH));
    assertTrue(instance.isDiagonalOpposite(SOUTH, NORTH));

    assertFalse(instance.isDiagonalOpposite(NORTH, EAST));
    assertFalse(instance.isDiagonalOpposite(EAST, NORTH));

    assertFalse(instance.isDiagonalOpposite(WEST, SOUTH));
    assertFalse(instance.isDiagonalOpposite(SOUTH, WEST));
  }

  @Test
  public void testIsDiagonalOppositeS() {
    System.out.println("isDiagonalOppositeS");
    Tile instance = TileCache.createTile(CROSS, Orientation.SOUTH, 100, 100);

    assertTrue(instance.isDiagonalOpposite(EAST, WEST));
    assertTrue(instance.isDiagonalOpposite(WEST, EAST));

    assertTrue(instance.isDiagonalOpposite(NORTH, SOUTH));
    assertTrue(instance.isDiagonalOpposite(SOUTH, NORTH));

    assertFalse(instance.isDiagonalOpposite(NORTH, EAST));
    assertFalse(instance.isDiagonalOpposite(EAST, NORTH));

    assertFalse(instance.isDiagonalOpposite(WEST, SOUTH));
    assertFalse(instance.isDiagonalOpposite(SOUTH, WEST));
  }

  @Test
  public void testIsDiagonalOppositeN() {
    System.out.println("isDiagonalOppositeN");
    Tile instance = TileCache.createTile(CROSS, Orientation.NORTH, 100, 100);

    assertTrue(instance.isDiagonalOpposite(EAST, WEST));
    assertTrue(instance.isDiagonalOpposite(WEST, EAST));

    assertTrue(instance.isDiagonalOpposite(NORTH, SOUTH));
    assertTrue(instance.isDiagonalOpposite(SOUTH, NORTH));

    assertFalse(instance.isDiagonalOpposite(NORTH, EAST));
    assertFalse(instance.isDiagonalOpposite(EAST, NORTH));

    assertFalse(instance.isDiagonalOpposite(WEST, SOUTH));
    assertFalse(instance.isDiagonalOpposite(SOUTH, WEST));
  }

  @Test
  public void testIsDiagonalOppositeW() {
    System.out.println("isDiagonalOppositeW");
    Tile instance = TileCache.createTile(CROSS, Orientation.WEST, 100, 100);

    assertTrue(instance.isDiagonalOpposite(EAST, WEST));
    assertTrue(instance.isDiagonalOpposite(WEST, EAST));

    assertTrue(instance.isDiagonalOpposite(NORTH, SOUTH));
    assertTrue(instance.isDiagonalOpposite(SOUTH, NORTH));

    assertFalse(instance.isDiagonalOpposite(NORTH, EAST));
    assertFalse(instance.isDiagonalOpposite(EAST, NORTH));

    assertFalse(instance.isDiagonalOpposite(WEST, SOUTH));
    assertFalse(instance.isDiagonalOpposite(SOUTH, WEST));
  }

}
