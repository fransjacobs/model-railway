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

import java.awt.Dimension;
import java.awt.Point;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jcs.entities.BlockBean;
import jcs.entities.TileBean;
import static jcs.entities.TileBean.Direction.LEFT;
import jcs.entities.TileBean.Orientation;
import static jcs.entities.TileBean.Orientation.EAST;
import static jcs.entities.TileBean.Orientation.NORTH;
import static jcs.entities.TileBean.Orientation.SOUTH;
import static jcs.entities.TileBean.Orientation.WEST;
import jcs.persistence.util.PersistenceTestHelper;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TileCacheTest {

  private final PersistenceTestHelper testHelper;

  public TileCacheTest() {
    System.setProperty("persistenceService", "jcs.persistence.TestH2PersistenceService");
    testHelper = PersistenceTestHelper.getInstance();
  }

  @BeforeEach
  public void setUp() {
    testHelper.runTestDataInsertScript("layout_issue_172.sql");
    //Refresh / load
    TileCache.loadTiles();
  }

  @AfterEach
  public void tearDown() {
  }

  @Test
  public void testGetTiles() {
    System.out.println("getTiles");
    List<Tile> result = TileCache.getTiles();
    assertEquals(82, result.size());
  }

  @Test
  public void testGetIdSeq() {
    System.out.println("getIdSeq");
    String id = "cx-5";
    int expResult = 5;
    int result = TileCache.getIdSeq(id);
    assertEquals(expResult, result);
  }

  @Test
  public void testFlush() {
    System.out.println("flush");

    List<Tile> result = TileCache.getTiles();
    assertEquals(82, result.size());

    TileCache.flush();
    result = TileCache.getTiles();
    assertEquals(0, result.size());
  }

  @Test
  public void testGetMinCanvasSize() {
    System.out.println("getMinCanvasSize");
    Dimension expResult = new Dimension(680, 600);
    Dimension result = TileCache.getMinCanvasSize();
    assertEquals(expResult, result);
  }

  @Test
  public void testFindTile_String() {
    System.out.println("findTile");
    String id = "cx-5";
    Tile expResult = new Cross(SOUTH, 300, 340);
    expResult.setId("cx-5");

    Tile result = TileCache.findTile(id);
    assertEquals(expResult, result);

    String id2 = "cs-2";
    Tile expResult2 = new CrossSwitch(NORTH, LEFT, 340, 420);
    expResult2.setId("cs-2");

    Tile result2 = TileCache.findTile(id2);
    assertEquals(expResult2, result2);
  }

  @Test
  public void testFindTilePoint() {
    System.out.println("findTilePoint");
    Point cp = new Point(300, 340);
    Point cp2 = new Point(340, 420);
    Point ap = new Point(300, 380);
    Point ap2 = new Point(340, 380);
    Tile expResult = new Cross(SOUTH, 300, 340);
    expResult.setId("cx-5");

    Tile result = TileCache.findTile(cp);
    assertEquals(expResult, result);
    // Us the alt point

    Tile resultA = TileCache.findTile(ap);
    assertEquals(expResult, resultA);

    Tile expResult2 = new CrossSwitch(NORTH, LEFT, 340, 420);
    expResult2.setId("cs-2");

    Tile result2 = TileCache.findTile(cp2);
    assertEquals(expResult2, result2);

    Tile resultA2 = TileCache.findTile(ap2);
    assertEquals(expResult2, resultA2);
  }

  @Test
  public void testFindTilePointNeighbors() {
    System.out.println("findTilePointNeigbors");

    Tile expResult = new Cross(SOUTH, 300, 340);
    expResult.setId("cx-5");

    Tile result = TileCache.findTile("cx-5");
    assertEquals(expResult, result);

    Map<Orientation, Point> resultNeighbors = result.getNeighborPoints();
    Map<TileBean.Orientation, Point> expNeighbors = new HashMap<>();

    expNeighbors.put(NORTH, new Point(340, 340));
    expNeighbors.put(EAST, new Point(340, 380));
    expNeighbors.put(SOUTH, new Point(260, 380));
    expNeighbors.put(WEST, new Point(260, 340));

    assertEquals(expNeighbors, resultNeighbors);

    //check for each neighbor
    Tile neigborN = TileCache.findTile(new Point(340, 340));
    Tile neigborE = TileCache.findTile(new Point(340, 380));
    Tile neigborS = TileCache.findTile(new Point(260, 380));
    Tile neigborW = TileCache.findTile(new Point(260, 340));

    assertEquals("cs-3", neigborN.getId());
    assertEquals("cs-2", neigborE.getId());
    assertEquals("cs-10", neigborS.getId());
    assertEquals("cs-1", neigborW.getId());

    Tile expResult2 = new CrossSwitch(NORTH, LEFT, 340, 420);
    expResult2.setId("cs-2");
    Tile result2 = TileCache.findTile("cs-2");
    assertEquals(expResult2, result2);

    Map<Orientation, Point> result2Neighbors = result2.getNeighborPoints();
    Map<Orientation, Point> exp2Neighbors = new HashMap<>();

    exp2Neighbors.put(NORTH, new Point(340, 340));
    exp2Neighbors.put(WEST, new Point(300, 380));
    exp2Neighbors.put(SOUTH, new Point(340, 460));
    exp2Neighbors.put(EAST, new Point(380, 420));

    assertEquals(exp2Neighbors, result2Neighbors);

    //check for each neighbor
    Tile neighbor2N = TileCache.findTile(new Point(340, 340));
    Tile neighbor2E = TileCache.findTile(new Point(380, 420));
    Tile neighbor2S = TileCache.findTile(new Point(340, 460));
    Tile neighbor2W = TileCache.findTile(new Point(300, 380));

    assertNotNull(neighbor2N);
    assertEquals("cs-3", neighbor2N.getId());

    assertNotNull(neighbor2E);
    assertEquals("tw-1", neighbor2E.getId());

    assertNotNull(neighbor2W);
    assertEquals("cx-5", neighbor2W.getId());

    assertNotNull(neighbor2S);
    assertEquals("st-2", neighbor2S.getId());

    assertTrue(result.isAdjacent(result2));
    assertTrue(result2.isAdjacent(result));
  }

  @Test
  public void testContains() {
    System.out.println("contains");

    Point expCenterPoint = new Point(340, 420);
    Point expAltPoint = new Point(340, 380);

    Tile expResult = new CrossSwitch(NORTH, LEFT, 340, 420);
    expResult.setId("cs-2");
    Tile result = TileCache.findTile("cs-2");
    assertEquals(expResult, result);

    assertEquals(expCenterPoint, result.getCenter());

    Tile result2 = TileCache.findTile(expCenterPoint);
    assertEquals(expResult, result2);

    Tile result3 = TileCache.findTile(expAltPoint);
    assertEquals(expResult, result3);

    assertTrue(TileCache.contains(expCenterPoint));
    assertTrue(TileCache.contains(expAltPoint));

    assertTrue(TileCache.contains(new Point(260,260)));
  }

  @Test
  public void testIncomingPointsCrossing() {
    System.out.println("incomingPointsIncomingPoints");
    Tile crossing2 = TileCache.findTile("cr-2");
    Tile crossing3 = TileCache.findTile("cr-3");
    
    assertNotNull(crossing2);
    assertNotNull(crossing3);

    assertEquals(new Point(340,500), crossing2.getCenter());
    assertEquals(new Point(380,500), crossing3.getCenter());
    assertEquals(NORTH, crossing2.getOrientation());
    assertEquals(NORTH, crossing2.getOrientation());
    
    // ->] | [->] | [ ->
    //incoming point is (340,480) -> N maar het zou W moeten zijn...
    //assertEquals()  crossing2.getIncomingSide()
    

    
  }
  
  
//TRACE	2026-08-12 16:25:00.265 [main] Graph.findPath(): Check cr-2 -> cr-3 
//TRACE	2026-08-12 16:25:00.265 [main] Graph.canTravelTo(): From cr-2 isCrossing true
//TRACE	2026-08-12 16:25:01.453 [main] Graph.canTravelTo(): From cr-2 inComing point: (340,480) incoming side: North exit side: South toInComingPoint: (340,520) 
//TRACE	2026-08-12 16:25:01.453 [main] Graph.canTravelTo(): To cr-3 edge points (size: 4) contain point: (340,520): false) 
//TRACE	2026-08-12 16:25:01.453 [main] Graph.findPath(): ##Can't travel from cr-2 to cr-3
  
//TRACE	2026-08-12 16:25:04.109 [main] Graph.findPath(): Check cr-2 -> st-22 
//TRACE	2026-08-12 16:25:04.109 [main] Graph.canTravelTo(): From cr-2 isCrossing true
//TRACE	2026-08-12 16:27:25.643 [main] Graph.canTravelTo(): From cr-2 inComing point: (340,480) incoming side: North exit side: South toInComingPoint: (340,520) 
//TRACE	2026-08-12 16:27:25.643 [main] Graph.canTravelTo(): To st-22 edge points (size: 2) contain point: (340,520): false) 
//TRACE	2026-08-12 16:27:25.643 [main] Graph.findPath(): ##Can't travel from cr-2 to st-22
  
  
  
  
  
//TRACE	2026-08-12 14:45:08.034 [main] Graph.findPath(): Check cr-2 -> cr-3 
//TRACE	2026-08-12 14:45:08.034 [main] Graph.canTravelTo(): From cr-2 isCrossing true
//TRACE	2026-08-12 14:45:08.034 [main] Graph.canTravelTo(): From cr-2 inComing point: (340,480) incoming side: North exit side: South toInComingPoint: (340,520) 
//TRACE	2026-08-12 14:45:08.034 [main] Graph.canTravelTo(): To cr-3 edge points (size: 4) contain point: (340,520): false) 
//TRACE	2026-08-12 14:45:08.035 [main] Graph.findPath(): ##Can't travel from cr-2 to cr-3
//TRACE	2026-08-12 14:45:08.035 [main] Graph.canTravelTo(): From cr-2 isCrossing true
//TRACE	2026-08-12 14:45:08.035 [main] Graph.canTravelTo(): From cr-2 inComing point: (340,480) incoming side: North exit side: South toInComingPoint: (340,520) 
//TRACE	2026-08-12 14:45:08.035 [main] Graph.canTravelTo(): To cr-3 edge points (size: 4) contain point: (340,520): false) 
 
 
}
