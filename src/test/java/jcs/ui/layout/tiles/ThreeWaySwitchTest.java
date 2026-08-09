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

import jcs.entities.TileBean;

import java.awt.Point;
import java.util.Map;
import java.util.HashMap;
import static jcs.entities.AccessoryBean.AccessoryValue.GREEN;
import static jcs.entities.AccessoryBean.AccessoryValue.OFF;
import static jcs.entities.AccessoryBean.AccessoryValue.RED;
import static jcs.entities.AccessoryBean.AccessoryValue.RED2;
import static jcs.entities.TileBean.Direction.LEFT;
import static jcs.entities.TileBean.Orientation.EAST;
import static jcs.entities.TileBean.Orientation.NORTH;
import static jcs.entities.TileBean.Orientation.SOUTH;
import static jcs.entities.TileBean.Orientation.WEST;
import static jcs.entities.TileBean.TileType.THREEWAY;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class ThreeWaySwitchTest {

  public ThreeWaySwitchTest() {
  }

  @Test
  public void testGetNeighborPointsN() {
    System.out.println("getNeighborPointsN");
    Tile instance = TileCache.createTile(THREEWAY, NORTH, 200, 200);

    Map<TileBean.Orientation, Point> expResult = new HashMap<>();

    expResult.put(NORTH, new Point(200, 160));
    expResult.put(WEST, new Point(160, 200));
    expResult.put(SOUTH, new Point(200, 240));
    expResult.put(EAST, new Point(240, 200));

    Map<TileBean.Orientation, Point> result = instance.getNeighborPoints();
    assertEquals(expResult, result);
  }

  @Test
  public void testGetNeighborPointsW() {
    System.out.println("getNeighborPointsW");
    Tile instance = TileCache.createTile(THREEWAY, WEST, 200, 200);

    Map<TileBean.Orientation, Point> expResult = new HashMap<>();

    expResult.put(NORTH, new Point(200, 160));
    expResult.put(WEST, new Point(160, 200));
    expResult.put(SOUTH, new Point(200, 240));
    expResult.put(EAST, new Point(240, 200));

    Map<TileBean.Orientation, Point> result = instance.getNeighborPoints();
    assertEquals(expResult, result);
  }

  @Test
  public void testGetNeighborPointsS() {
    System.out.println("getNeighborPointsS");
    Tile instance = TileCache.createTile(THREEWAY, SOUTH, 200, 200);

    Map<TileBean.Orientation, Point> expResult = new HashMap<>();

    expResult.put(NORTH, new Point(200, 160));
    expResult.put(WEST, new Point(160, 200));
    expResult.put(SOUTH, new Point(200, 240));
    expResult.put(EAST, new Point(240, 200));

    Map<TileBean.Orientation, Point> result = instance.getNeighborPoints();
    assertEquals(expResult, result);
  }

  @Test
  public void testGetNeighborPointsE() {
    System.out.println("getNeighborPointsE");
    Tile instance = TileCache.createTile(THREEWAY, EAST, 200, 200);

    Map<TileBean.Orientation, Point> expResult = new HashMap<>();

    expResult.put(NORTH, new Point(200, 160));
    expResult.put(WEST, new Point(160, 200));
    expResult.put(SOUTH, new Point(200, 240));
    expResult.put(EAST, new Point(240, 200));

    Map<TileBean.Orientation, Point> result = instance.getNeighborPoints();
    assertEquals(expResult, result);
  }

  @Test
  public void testGetEdgePointsN() {
    System.out.println("getEdgePointsN");
    Tile instance = TileCache.createTile(THREEWAY, NORTH, 200, 200);

    Map<TileBean.Orientation, Point> expResult = new HashMap<>();

    expResult.put(NORTH, new Point(200, 180));
    expResult.put(WEST, new Point(180, 200));
    expResult.put(SOUTH, new Point(200, 220));
    expResult.put(EAST, new Point(220, 200));

    Map<TileBean.Orientation, Point> result = instance.getEdgePoints();
    assertEquals(expResult, result);
  }

  @Test
  public void testGetEdgePointsW() {
    System.out.println("getEdgePointsW");
    Tile instance = TileCache.createTile(THREEWAY, WEST, 200, 200);

    Map<TileBean.Orientation, Point> expResult = new HashMap<>();

    expResult.put(NORTH, new Point(200, 180));
    expResult.put(WEST, new Point(180, 200));
    expResult.put(SOUTH, new Point(200, 220));
    expResult.put(EAST, new Point(220, 200));

    Map<TileBean.Orientation, Point> result = instance.getEdgePoints();
    assertEquals(expResult, result);
  }

  @Test
  public void testGetEdgePointsS() {
    System.out.println("getEdgePointsS");
    Tile instance = TileCache.createTile(THREEWAY, SOUTH, 200, 200);

    Map<TileBean.Orientation, Point> expResult = new HashMap<>();

    expResult.put(NORTH, new Point(200, 180));
    expResult.put(WEST, new Point(180, 200));
    expResult.put(SOUTH, new Point(200, 220));
    expResult.put(EAST, new Point(220, 200));

    Map<TileBean.Orientation, Point> result = instance.getEdgePoints();
    assertEquals(expResult, result);
  }

  @Test
  public void testGetEdgePointsE() {
    System.out.println("getEdgePointsE");
    Tile instance = TileCache.createTile(THREEWAY, EAST, 200, 200);

    Map<TileBean.Orientation, Point> expResult = new HashMap<>();

    expResult.put(NORTH, new Point(200, 180));
    expResult.put(WEST, new Point(180, 200));
    expResult.put(SOUTH, new Point(200, 220));
    expResult.put(EAST, new Point(220, 200));

    Map<TileBean.Orientation, Point> result = instance.getEdgePoints();
    assertEquals(expResult, result);
  }

  @Test
  public void testAccessoryValueForRouteSouth() {
    System.out.println("AccessoryValueForRouteSouth");
    Tile instance = TileCache.createTile(THREEWAY, SOUTH, LEFT, 100, 100);

    assertEquals(GREEN, instance.accessoryValueForRoute(NORTH, SOUTH));
    assertEquals(GREEN, instance.accessoryValueForRoute(SOUTH, NORTH));

    assertEquals(RED, instance.accessoryValueForRoute(SOUTH, WEST));
    assertEquals(RED, instance.accessoryValueForRoute(WEST, SOUTH));

    assertEquals(RED2, instance.accessoryValueForRoute(SOUTH, EAST));
    assertEquals(RED2, instance.accessoryValueForRoute(EAST, SOUTH));

    assertEquals(OFF, instance.accessoryValueForRoute(WEST, NORTH));
    assertEquals(OFF, instance.accessoryValueForRoute(NORTH, WEST));

    assertEquals(OFF, instance.accessoryValueForRoute(NORTH, EAST));
    assertEquals(OFF, instance.accessoryValueForRoute(EAST, NORTH));

    assertEquals(OFF, instance.accessoryValueForRoute(WEST, EAST));
    assertEquals(OFF, instance.accessoryValueForRoute(EAST, EAST));
  }

  @Test
  public void testAccessoryValueForRouteNorth() {
    System.out.println("AccessoryValueForRouteNorth");
    Tile instance = TileCache.createTile(THREEWAY, NORTH, 100, 100);

    assertEquals(GREEN, instance.accessoryValueForRoute(NORTH, SOUTH));
    assertEquals(GREEN, instance.accessoryValueForRoute(SOUTH, NORTH));

    assertEquals(RED2, instance.accessoryValueForRoute(NORTH, WEST));
    assertEquals(RED2, instance.accessoryValueForRoute(WEST, NORTH));

    assertEquals(RED, instance.accessoryValueForRoute(NORTH, EAST));
    assertEquals(RED, instance.accessoryValueForRoute(EAST, NORTH));

    assertEquals(OFF, instance.accessoryValueForRoute(WEST, SOUTH));
    assertEquals(OFF, instance.accessoryValueForRoute(SOUTH, WEST));

    assertEquals(OFF, instance.accessoryValueForRoute(SOUTH, EAST));
    assertEquals(OFF, instance.accessoryValueForRoute(EAST, SOUTH));

    assertEquals(OFF, instance.accessoryValueForRoute(WEST, EAST));
    assertEquals(OFF, instance.accessoryValueForRoute(EAST, EAST));
  }

  @Test
  public void testAccessoryValueForRouteWest() {
    System.out.println("AccessoryValueForRouteWest");
    Tile instance = TileCache.createTile(THREEWAY, WEST, 100, 100);

    assertEquals(GREEN, instance.accessoryValueForRoute(EAST, WEST));
    assertEquals(GREEN, instance.accessoryValueForRoute(WEST, EAST));

    assertEquals(RED2, instance.accessoryValueForRoute(SOUTH, WEST));
    assertEquals(RED2, instance.accessoryValueForRoute(WEST, SOUTH));

    assertEquals(RED, instance.accessoryValueForRoute(NORTH, WEST));
    assertEquals(RED, instance.accessoryValueForRoute(WEST, NORTH));

    assertEquals(OFF, instance.accessoryValueForRoute(EAST, SOUTH));
    assertEquals(OFF, instance.accessoryValueForRoute(SOUTH, EAST));

    assertEquals(OFF, instance.accessoryValueForRoute(NORTH, EAST));
    assertEquals(OFF, instance.accessoryValueForRoute(EAST, NORTH));

    assertEquals(OFF, instance.accessoryValueForRoute(NORTH, SOUTH));
    assertEquals(OFF, instance.accessoryValueForRoute(SOUTH, NORTH));
  }

  @Test
  public void testAccessoryValueForRouteEast() {
    System.out.println("AccessoryValueForRouteEast");
    Tile instance = TileCache.createTile(THREEWAY, EAST, 100, 100);

    assertEquals(GREEN, instance.accessoryValueForRoute(EAST, WEST));
    assertEquals(GREEN, instance.accessoryValueForRoute(WEST, EAST));

    assertEquals(RED, instance.accessoryValueForRoute(SOUTH, EAST));
    assertEquals(RED, instance.accessoryValueForRoute(EAST, SOUTH));

    assertEquals(RED2, instance.accessoryValueForRoute(NORTH, EAST));
    assertEquals(RED2, instance.accessoryValueForRoute(EAST, NORTH));

    assertEquals(OFF, instance.accessoryValueForRoute(WEST, SOUTH));
    assertEquals(OFF, instance.accessoryValueForRoute(SOUTH, WEST));

    assertEquals(OFF, instance.accessoryValueForRoute(NORTH, WEST));
    assertEquals(OFF, instance.accessoryValueForRoute(WEST, NORTH));

    assertEquals(OFF, instance.accessoryValueForRoute(NORTH, SOUTH));
    assertEquals(OFF, instance.accessoryValueForRoute(SOUTH, NORTH));
  }

}
