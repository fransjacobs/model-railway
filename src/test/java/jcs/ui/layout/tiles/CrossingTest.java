/*
 * Copyright 2026 frans.
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
import jcs.entities.TileBean.Orientation;
import static jcs.entities.TileBean.Orientation.EAST;
import static jcs.entities.TileBean.Orientation.NORTH;
import static jcs.entities.TileBean.Orientation.SOUTH;
import static jcs.entities.TileBean.Orientation.WEST;
import static jcs.entities.TileBean.TileType.CROSSING;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

/**
 *
 * @author frans
 */
public class CrossingTest {

  public CrossingTest() {
  }

  @BeforeAll
  public static void setUpClass() {
  }

  @AfterAll
  public static void tearDownClass() {
  }

  @BeforeEach
  public void setUp() {
  }

  @AfterEach
  public void tearDown() {
  }

  @Test
  public void testGetNeighborPointsE() {
    System.out.println("getNeighborPointsE");
    Tile instance = TileCache.createTile(CROSSING, EAST, 200, 200);

    Map<TileBean.Orientation, Point> expResult = new HashMap<>();
    expResult.put(NORTH, new Point(200, 160));
    expResult.put(WEST, new Point(160, 200));
    expResult.put(SOUTH, new Point(200, 240));
    expResult.put(EAST, new Point(240, 200));

    Map<TileBean.Orientation, Point> result = instance.getNeighborPoints();
    assertEquals(expResult, result);
  }

  @Test
  public void testGetNeighborPointsN() {
    System.out.println("getNeighborPointsN");
    Tile instance = TileCache.createTile(CROSSING, NORTH, 200, 200);

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
    Tile instance = TileCache.createTile(CROSSING, WEST, 200, 200);

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
    Tile instance = TileCache.createTile(CROSSING, SOUTH, 200, 200);

    Map<TileBean.Orientation, Point> expResult = new HashMap<>();
    expResult.put(NORTH, new Point(200, 160));
    expResult.put(WEST, new Point(160, 200));
    expResult.put(SOUTH, new Point(200, 240));
    expResult.put(EAST, new Point(240, 200));

    Map<TileBean.Orientation, Point> result = instance.getNeighborPoints();
    assertEquals(expResult, result);
  }

  @Test
  public void testGetEdgePointsE() {
    System.out.println("getEdgePointsE");

    Tile instance = TileCache.createTile(CROSSING, EAST, 200, 200);

    Map<TileBean.Orientation, Point> expResult = new HashMap<>();
    expResult.put(NORTH, new Point(200, 180));
    expResult.put(WEST, new Point(180, 200));
    expResult.put(SOUTH, new Point(200, 220));
    expResult.put(EAST, new Point(220, 200));

    Map<TileBean.Orientation, Point> result = instance.getEdgePoints();
    assertEquals(expResult, result);
  }

  @Test
  public void testGetEdgePointsN() {
    System.out.println("getEdgePointsN");

    Tile instance = TileCache.createTile(CROSSING, NORTH, 200, 200);

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

    Tile instance = TileCache.createTile(CROSSING, WEST, 200, 200);

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

    Tile instance = TileCache.createTile(CROSSING, SOUTH, 200, 200);

    Map<TileBean.Orientation, Point> expResult = new HashMap<>();
    expResult.put(NORTH, new Point(200, 180));
    expResult.put(WEST, new Point(180, 200));
    expResult.put(SOUTH, new Point(200, 220));
    expResult.put(EAST, new Point(220, 200));

    Map<TileBean.Orientation, Point> result = instance.getEdgePoints();
    assertEquals(expResult, result);
  }

  @Test
  public void testGetEdgeOrientationsE() {
    System.out.println("getEdgeOrientationsE");

    Tile instance = TileCache.createTile(CROSSING, EAST, 200, 200);

    Map<TileBean.Orientation, Point> expResultH = new HashMap<>();
    expResultH.put(WEST, new Point(180, 200));
    expResultH.put(EAST, new Point(220, 200));

    Map<TileBean.Orientation, Point> expResultV = new HashMap<>();
    expResultV.put(NORTH, new Point(200, 180));
    expResultV.put(SOUTH, new Point(200, 220));
    
    Map<TileBean.Orientation, Point> resultH = instance.getEdgeConnections(false);
    Map<TileBean.Orientation, Point> resultV = instance.getEdgeConnections(true);
    
    assertEquals(expResultH, resultH);
    assertEquals(expResultV, resultV);
    
    
    Map<Point, Orientation> expResult2 = new HashMap<>();
    expResult2.put(new Point(200, 180), NORTH);
    expResult2.put(new Point(180, 200), WEST);
    expResult2.put(new Point(200, 220), SOUTH);
    expResult2.put(new Point(220, 200), EAST);
    
    //Map<Point, Orientation> result = instance.getEdgeOrientations();

    //assertEquals(expResult, result);
  }

//Point inComingEdgePoint = from.getIncomingPoint();
//
//      TileBean.Orientation inComingSide = from.getConnectingSide(inComingEdgePoint);
//      //find the connection edge point on the opposite side
//      TileBean.Orientation exitSide = Node.getOppositeSide(inComingSide);
//
//      Point toInComingPoint = from.getTile().getEdgePoints().get(exitSide);
//      Logger.trace("From {} inComing point: ({},{}) incoming side: {} exit side: {} toInComingPoint: ({},{}) ", from.getId(), inComingEdgePoint.x, inComingEdgePoint.y, inComingSide.getOrientation(), exitSide.getOrientation(), toInComingPoint.x, toInComingPoint.y);
//
//      Map<TileBean.Orientation, Point> toEgdePoints = to.getTile().getEdgePoints();
//      boolean cont = toEgdePoints.containsValue(toInComingPoint);
}
