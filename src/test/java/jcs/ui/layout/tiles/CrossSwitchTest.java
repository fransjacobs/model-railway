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

import static jcs.entities.TileBean.Orientation.EAST;
import static jcs.entities.TileBean.Orientation.NORTH;
import static jcs.entities.TileBean.TileType.CROSS_SWITCH;

import static org.junit.Assert.assertEquals;

import java.awt.Point;
import java.util.Map;
import java.util.HashMap;

import java.util.Set;
import java.util.HashSet;
import static jcs.entities.AccessoryBean.AccessoryValue.GREEN;
import static jcs.entities.AccessoryBean.AccessoryValue.OFF;
import static jcs.entities.AccessoryBean.AccessoryValue.RED;
import static jcs.entities.TileBean.Direction.LEFT;
import static jcs.entities.TileBean.Direction.RIGHT;
import jcs.entities.TileBean.Orientation;
import static jcs.entities.TileBean.Orientation.SOUTH;
import static jcs.entities.TileBean.Orientation.WEST;

import org.junit.Test;

public class CrossSwitchTest {

  public CrossSwitchTest() {
  }

  @Test
  public void testGetAltPointsNR() {
    System.out.println("getAltPointsNR");
    Tile instance = TileCache.createTile(CROSS_SWITCH, NORTH, RIGHT, 200, 200);
    Set<Point> expResult = new HashSet<>();
    expResult.add(new Point(200, 160));
    Set<Point> result = instance.getAltPoints();
    assertEquals(expResult, result);
  }

  @Test
  public void testGetAltPointsSR() {
    System.out.println("getAltPointsSR");
    Tile instance = TileCache.createTile(CROSS_SWITCH, SOUTH, RIGHT, 200, 200);
    Set<Point> expResult = new HashSet<>();
    expResult.add(new Point(200, 240));
    Set<Point> result = instance.getAltPoints();
    assertEquals(expResult, result);
  }

  @Test
  public void testGetAltPointsWR() {
    System.out.println("getAltPointsWR");
    Tile instance = TileCache.createTile(CROSS_SWITCH, WEST, RIGHT, 200, 200);
    Set<Point> expResult = new HashSet<>();
    expResult.add(new Point(160, 200));
    Set<Point> result = instance.getAltPoints();
    assertEquals(expResult, result);
  }

  @Test
  public void testGetAltPointsER() {
    System.out.println("getAltPointsER");
    Tile instance = TileCache.createTile(CROSS_SWITCH, EAST, RIGHT, 200, 200);
    Set<Point> expResult = new HashSet<>();
    expResult.add(new Point(240, 200));
    Set<Point> result = instance.getAltPoints();
    assertEquals(expResult, result);
  }

  @Test
  public void testGetAllPointsNR() {
    System.out.println("getAllPointsNR");
    Tile instance = TileCache.createTile(CROSS_SWITCH, NORTH, RIGHT, 200, 200);
    Set<Point> expResult = new HashSet<>();
    Set<Point> result = instance.getAllPoints();
    expResult.add(new Point(200, 200));
    expResult.add(new Point(200, 160));

    assertEquals(expResult, result);
  }

  @Test
  public void testGetAllPointsNRPoint() {
    System.out.println("getAllPointsNRPoint");
    Tile instance = TileCache.createTile(CROSS_SWITCH, NORTH, RIGHT, 200, 200);
    Set<Point> expResult = new HashSet<>();
    expResult.add(new Point(200, 200));
    expResult.add(new Point(200, 160));

    Point center = new Point(200, 160);
    Set<Point> result = instance.getAllPoints(center);
    assertEquals(expResult, result);
  }

  @Test
  public void testGetAltPoints_Point() {
    System.out.println("getAltPoints");
    Tile instance = TileCache.createTile(CROSS_SWITCH, NORTH, RIGHT, 200, 200);
    Set<Point> expResult = new HashSet<>();
    expResult.add(new Point(200, 160));

    Point center = new Point(200, 160);
    Set<Point> result = instance.getAltPoints(center);
    assertEquals(expResult, result);
  }

  @Test
  public void testGetNeighborPointsNR() {
    System.out.println("getNeighborPointsNR");
    Tile instance = TileCache.createTile(CROSS_SWITCH, NORTH, RIGHT, 200, 200);

    Map<Orientation, Point> expResult = new HashMap<>();

    expResult.put(NORTH, new Point(200, 120));
    expResult.put(WEST, new Point(160, 200));
    expResult.put(SOUTH, new Point(200, 240));
    expResult.put(EAST, new Point(240, 160));

    Map<Orientation, Point> result = instance.getNeighborPoints();
    assertEquals(expResult, result);
  }

  @Test
  public void testGetNeighborPointsSR() {
    System.out.println("getNeighborPointsSR");
    Tile instance = TileCache.createTile(CROSS_SWITCH, SOUTH, RIGHT, 200, 200);

    Map<Orientation, Point> expResult = new HashMap<>();

    expResult.put(NORTH, new Point(200, 160));
    expResult.put(WEST, new Point(160, 240));
    expResult.put(SOUTH, new Point(200, 280));
    expResult.put(EAST, new Point(240, 200));

    Map<Orientation, Point> result = instance.getNeighborPoints();
    assertEquals(expResult, result);
  }

  @Test
  public void testGetNeighborPointsWR() {
    System.out.println("getNeighborPointsWR");
    Tile instance = TileCache.createTile(CROSS_SWITCH, WEST, RIGHT, 200, 200);

    Map<Orientation, Point> expResult = new HashMap<>();

    expResult.put(NORTH, new Point(160, 160));
    expResult.put(WEST, new Point(120, 200));
    expResult.put(SOUTH, new Point(200, 240));
    expResult.put(EAST, new Point(240, 200));

    Map<Orientation, Point> result = instance.getNeighborPoints();
    assertEquals(expResult, result);
  }

  @Test
  public void testGetNeighborPointsER() {
    System.out.println("getNeighborPointsER");
    Tile instance = TileCache.createTile(CROSS_SWITCH, EAST, RIGHT, 200, 200);

    Map<Orientation, Point> expResult = new HashMap<>();

    expResult.put(NORTH, new Point(200, 160));
    expResult.put(WEST, new Point(160, 200));
    expResult.put(SOUTH, new Point(240, 240));
    expResult.put(EAST, new Point(280, 200));

    Map<Orientation, Point> result = instance.getNeighborPoints();
    assertEquals(expResult, result);
  }

  @Test
  public void testGetNeighborPointsNL() {
    System.out.println("getNeighborPointsNL");
    Tile instance = TileCache.createTile(CROSS_SWITCH, NORTH, LEFT, 200, 200);

    Map<Orientation, Point> expResult = new HashMap<>();

    expResult.put(NORTH, new Point(200, 120));
    expResult.put(WEST, new Point(160, 160));
    expResult.put(SOUTH, new Point(200, 240));
    expResult.put(EAST, new Point(240, 200));

    Map<Orientation, Point> result = instance.getNeighborPoints();
    assertEquals(expResult, result);
  }

  @Test
  public void testGetNeighborPointsSL() {
    System.out.println("getNeighborPointsSL");
    Tile instance = TileCache.createTile(CROSS_SWITCH, SOUTH, LEFT, 200, 200);

    Map<Orientation, Point> expResult = new HashMap<>();

    expResult.put(NORTH, new Point(200, 160));
    expResult.put(WEST, new Point(160, 200));
    expResult.put(SOUTH, new Point(200, 280));
    expResult.put(EAST, new Point(240, 240));

    Map<Orientation, Point> result = instance.getNeighborPoints();
    assertEquals(expResult, result);
  }

  @Test
  public void testGetEdgePointsWL() {
    System.out.println("testGetEdgePointsWL");
    Tile instance = TileCache.createTile(CROSS_SWITCH, WEST, LEFT, 200, 200);

    Map<Orientation, Point> expResult = new HashMap<>();

    expResult.put(WEST, new Point(140, 200));
    expResult.put(EAST, new Point(220, 200));
    expResult.put(NORTH, new Point(200, 180));
    expResult.put(SOUTH, new Point(160, 220));

    Map<Orientation, Point> result = instance.getEdgePoints();
    assertEquals(expResult, result);
  }

  @Test
  public void testGetEdgePointsEL() {
    System.out.println("testGetEdgePointsEL");
    Tile instance = TileCache.createTile(CROSS_SWITCH, EAST, LEFT, 200, 200);

    Map<Orientation, Point> expResult = new HashMap<>();

    expResult.put(NORTH, new Point(240, 180));
    expResult.put(WEST, new Point(180, 200));
    expResult.put(SOUTH, new Point(200, 220));
    expResult.put(EAST, new Point(260, 200));

    Map<Orientation, Point> result = instance.getEdgePoints();
    assertEquals(expResult, result);
  }

  @Test
  public void testGetEdgePointsNL() {
    System.out.println("testGetEdgePointsNL");
    Tile instance = TileCache.createTile(CROSS_SWITCH, NORTH, LEFT, 200, 200);

    Map<Orientation, Point> expResult = new HashMap<>();

    expResult.put(NORTH, new Point(200, 140));
    expResult.put(WEST, new Point(180, 160));
    expResult.put(SOUTH, new Point(200, 220));
    expResult.put(EAST, new Point(220, 200));

    Map<Orientation, Point> result = instance.getEdgePoints();
    assertEquals(expResult, result);
  }

  @Test
  public void testGetEdgePointsSL() {
    System.out.println("testGetEdgePointsSL");
    Tile instance = TileCache.createTile(CROSS_SWITCH, SOUTH, LEFT, 200, 200);

    Map<Orientation, Point> expResult = new HashMap<>();

    expResult.put(NORTH, new Point(200, 180));
    expResult.put(WEST, new Point(180, 200));
    expResult.put(SOUTH, new Point(200, 260));
    expResult.put(EAST, new Point(220, 240));

    Map<Orientation, Point> result = instance.getEdgePoints();
    assertEquals(expResult, result);
  }

  @Test
  public void testGetEdgePointsWR() {
    System.out.println("testGetEdgePointsWR");
    Tile instance = TileCache.createTile(CROSS_SWITCH, WEST, RIGHT, 200, 200);

    Map<Orientation, Point> expResult = new HashMap<>();

    expResult.put(WEST, new Point(140, 200));
    expResult.put(EAST, new Point(220, 200));
    expResult.put(NORTH, new Point(160, 180));
    expResult.put(SOUTH, new Point(200, 220));

    Map<Orientation, Point> result = instance.getEdgePoints();
    assertEquals(expResult, result);
  }

  @Test
  public void testGetEdgePointsER() {
    System.out.println("testGetEdgePointsER");
    Tile instance = TileCache.createTile(CROSS_SWITCH, EAST, RIGHT, 200, 200);

    Map<Orientation, Point> expResult = new HashMap<>();

    expResult.put(NORTH, new Point(200, 180));
    expResult.put(WEST, new Point(180, 200));
    expResult.put(SOUTH, new Point(240, 220));
    expResult.put(EAST, new Point(260, 200));

    Map<Orientation, Point> result = instance.getEdgePoints();
    assertEquals(expResult, result);
  }

  @Test
  public void testGetEdgePointsNR() {
    System.out.println("testGetEdgePointsNR");
    Tile instance = TileCache.createTile(CROSS_SWITCH, NORTH, RIGHT, 200, 200);

    Map<Orientation, Point> expResult = new HashMap<>();

    expResult.put(NORTH, new Point(200, 140));
    expResult.put(WEST, new Point(180, 200));
    expResult.put(SOUTH, new Point(200, 220));
    expResult.put(EAST, new Point(220, 160));

    Map<Orientation, Point> result = instance.getEdgePoints();
    assertEquals(expResult, result);
  }

  @Test
  public void testGetEdgePointsSR() {
    System.out.println("testGetEdgePointsSR");
    Tile instance = TileCache.createTile(CROSS_SWITCH, SOUTH, RIGHT, 200, 200);

    Map<Orientation, Point> expResult = new HashMap<>();

    expResult.put(NORTH, new Point(200, 180));
    expResult.put(WEST, new Point(180, 240));
    expResult.put(SOUTH, new Point(200, 260));
    expResult.put(EAST, new Point(220, 200));

    Map<Orientation, Point> result = instance.getEdgePoints();
    assertEquals(expResult, result);
  }

  @Test
  public void testAccessoryValueForRouteNorthLeft() {
    System.out.println("AccessoryValueForRouteNorthLeft");
    Tile instance = TileCache.createTile(CROSS_SWITCH, NORTH, LEFT, 100, 100);

    assertEquals(GREEN, instance.accessoryValueForRoute(NORTH, SOUTH));
    assertEquals(GREEN, instance.accessoryValueForRoute(SOUTH, NORTH));

    assertEquals(RED, instance.accessoryValueForRoute(NORTH, EAST));
    assertEquals(RED, instance.accessoryValueForRoute(EAST, NORTH));

    assertEquals(RED, instance.accessoryValueForRoute(SOUTH, WEST));
    assertEquals(RED, instance.accessoryValueForRoute(WEST, SOUTH));

    assertEquals(OFF, instance.accessoryValueForRoute(WEST, NORTH));
    assertEquals(OFF, instance.accessoryValueForRoute(NORTH, WEST));

    assertEquals(OFF, instance.accessoryValueForRoute(SOUTH, EAST));
    assertEquals(OFF, instance.accessoryValueForRoute(EAST, SOUTH));
  }

  @Test
  public void testAccessoryValueForRouteSouthRight() {
    System.out.println("AccessoryValueForRouteSouthRight");
    Tile instance = TileCache.createTile(CROSS_SWITCH, SOUTH, RIGHT, 100, 100);

    assertEquals(GREEN, instance.accessoryValueForRoute(NORTH, SOUTH));
    assertEquals(GREEN, instance.accessoryValueForRoute(SOUTH, NORTH));

    assertEquals(RED, instance.accessoryValueForRoute(NORTH, WEST));
    assertEquals(RED, instance.accessoryValueForRoute(WEST, NORTH));

    assertEquals(RED, instance.accessoryValueForRoute(SOUTH, EAST));
    assertEquals(RED, instance.accessoryValueForRoute(EAST, SOUTH));

    assertEquals(OFF, instance.accessoryValueForRoute(WEST, SOUTH));
    assertEquals(OFF, instance.accessoryValueForRoute(SOUTH, WEST));

    assertEquals(OFF, instance.accessoryValueForRoute(NORTH, EAST));
    assertEquals(OFF, instance.accessoryValueForRoute(EAST, NORTH));
  }

  @Test
  public void testAccessoryValueForRouteEastRight() {
    System.out.println("AccessoryValueForRouteEastRight");
    Tile instance = TileCache.createTile(CROSS_SWITCH, EAST, RIGHT, 100, 100);

    assertEquals(GREEN, instance.accessoryValueForRoute(EAST, WEST));
    assertEquals(GREEN, instance.accessoryValueForRoute(WEST, EAST));

    assertEquals(RED, instance.accessoryValueForRoute(NORTH, EAST));
    assertEquals(RED, instance.accessoryValueForRoute(EAST, NORTH));

    assertEquals(RED, instance.accessoryValueForRoute(SOUTH, WEST));
    assertEquals(RED, instance.accessoryValueForRoute(WEST, SOUTH));

    assertEquals(OFF, instance.accessoryValueForRoute(WEST, NORTH));
    assertEquals(OFF, instance.accessoryValueForRoute(NORTH, WEST));

    assertEquals(OFF, instance.accessoryValueForRoute(SOUTH, EAST));
    assertEquals(OFF, instance.accessoryValueForRoute(EAST, SOUTH));
  }

  @Test
  public void testAccessoryValueForRouteWestLeft() {
    System.out.println("AccessoryValueForRouteEastRight");
    Tile instance = TileCache.createTile(CROSS_SWITCH, WEST, LEFT, 100, 100);

    assertEquals(GREEN, instance.accessoryValueForRoute(EAST, WEST));
    assertEquals(GREEN, instance.accessoryValueForRoute(WEST, EAST));

    assertEquals(RED, instance.accessoryValueForRoute(NORTH, WEST));
    assertEquals(RED, instance.accessoryValueForRoute(WEST, NORTH));

    assertEquals(RED, instance.accessoryValueForRoute(SOUTH, EAST));
    assertEquals(RED, instance.accessoryValueForRoute(EAST, SOUTH));

    assertEquals(OFF, instance.accessoryValueForRoute(WEST, SOUTH));
    assertEquals(OFF, instance.accessoryValueForRoute(SOUTH, WEST));

    assertEquals(OFF, instance.accessoryValueForRoute(NORTH, EAST));
    assertEquals(OFF, instance.accessoryValueForRoute(EAST, NORTH));
  }

}
