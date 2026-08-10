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
    //List<Tile> expResult = null;
    List<Tile> result = TileCache.getTiles();
    assertEquals(51, result.size());
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
    assertEquals(51, result.size());

    TileCache.flush();
    result = TileCache.getTiles();
    assertEquals(0, result.size());
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

    assertEquals("sw-1", neigborN.getId());
    assertEquals("cs-2", neigborE.getId());
    assertEquals("ct-4", neigborS.getId());
    assertEquals("ct-7", neigborW.getId());
    
    
//TRACE	2026-08-10 17:46:29.781 [main] AStar.buildGraph(): Node cx-5 check NeighborPoint: (340,340)
//TRACE	2026-08-10 17:46:29.781 [main] AStar.buildGraph(): Node cx-5 NeighborPoint: (340,340) -> sw-1
//TRACE	2026-08-10 17:46:44.512 [main] AStar.buildGraph(): Node cx-5 check NeighborPoint: (260,340)
//TRACE	2026-08-10 17:46:48.560 [main] AStar.buildGraph(): Node cx-5 NeighborPoint: (260,340) -> ct-7

//TRACE	2026-08-10 17:47:00.086 [main] AStar.buildGraph(): Node cx-5 check NeighborPoint: (340,380)

//TRACE	2026-08-10 17:47:08.555 [main] AStar.buildGraph(): Node cx-5 check NeighborPoint: (260,380)
//TRACE	2026-08-10 17:47:46.447 [main] AStar.buildGraph(): Node cx-5 NeighborPoint: (260,380) -> ct-4
    
    
//TRACE	2026-08-10 19:04:26.044 [main] AStar.buildGraph(): Node cs-2 check NeighborPoint: (340,340)
//TRACE	2026-08-10 19:04:26.932 [main] AStar.buildGraph(): Node cs-2 NeighborPoint: (340,340) -> sw-1
//TRACE	2026-08-10 19:04:26.932 [main] AStar.buildGraph(): Node cs-2 check NeighborPoint: (300,380)
//TRACE	2026-08-10 19:05:04.007 [main] AStar.buildGraph(): Node cs-2 check NeighborPoint: (380,420)
//TRACE	2026-08-10 19:05:21.867 [main] AStar.buildGraph(): Node cs-2 NeighborPoint: (380,420) -> st-15
//TRACE	2026-08-10 19:05:45.512 [main] AStar.buildGraph(): Node cs-2 check NeighborPoint: (340,460)
//TRACE	2026-08-10 19:05:51.294 [main] AStar.buildGraph(): Node cs-2 NeighborPoint: (340,460) -> st-7    
    

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
    assertEquals("sw-1", neighbor2N.getId());
    
    assertNotNull(neighbor2E);
    assertEquals("st-15", neighbor2E.getId());
    
    assertNotNull(neighbor2W);
    assertEquals("cx-5", neighbor2W.getId());
    
    assertNotNull(neighbor2S);
    assertEquals("st-7", neighbor2S.getId());
    
    assertTrue(result.isAdjacent(result2));
    assertTrue(result2.isAdjacent(result));

//    Point cp = new Point(300, 340);
//    Point cp2 = new Point(340, 420);
//    Point ap = new Point(300, 380);
//    Point ap2 = new Point(340, 380);
//    Tile expResult = new Cross(SOUTH, 300, 340);
//    expResult.setId("cx-5");
//
//    Tile result = TileCache.findTile(cp);
//    assertEquals(expResult, result);
//    // Us the alt point
//
//    Tile resultA = TileCache.findTile(ap);
//    assertEquals(expResult, resultA);
//
//    Tile expResult2 = new CrossSwitch(NORTH, LEFT, 340, 420);
//    expResult2.setId("cs-2");
//
//    Tile result2 = TileCache.findTile(cp2);
//    assertEquals(expResult2, result2);
//
//    Tile resultA2 = TileCache.findTile(ap2);
//    assertEquals(expResult2, resultA2);
  }

  /**
   * Test of contains method, of class TileCache.
   */
  //@Test
  public void testContains() {
    System.out.println("contains");
    Point p = null;
    boolean expResult = false;
    boolean result = TileCache.contains(p);
    assertEquals(expResult, result);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of createTile method, of class TileCache.
   */
  //@Test
  public void testCreateTile_TileBean_boolean() {
    System.out.println("createTile");
    TileBean tileBean = null;
    boolean showValues = false;
    Tile expResult = null;
    Tile result = TileCache.createTile(tileBean, showValues);
    assertEquals(expResult, result);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of createTile method, of class TileCache.
   */
  //@Test
  public void testCreateTile_4args_1() {
    System.out.println("createTile");
    TileBean.TileType tileType = null;
    TileBean.Orientation orientation = null;
    int x = 0;
    int y = 0;
    Tile expResult = null;
    Tile result = TileCache.createTile(tileType, orientation, x, y);
    assertEquals(expResult, result);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of createTile method, of class TileCache.
   */
  //@Test
  public void testCreateTile_5args() {
    System.out.println("createTile");
    TileBean.TileType tileType = null;
    TileBean.Orientation orientation = null;
    TileBean.Direction direction = null;
    int x = 0;
    int y = 0;
    Tile expResult = null;
    Tile result = TileCache.createTile(tileType, orientation, direction, x, y);
    assertEquals(expResult, result);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of createTile method, of class TileCache.
   */
  //@Test
  public void testCreateTile_4args_2() {
    System.out.println("createTile");
    TileBean.TileType tileType = null;
    TileBean.Orientation orientation = null;
    TileBean.Direction direction = null;
    Point cp = null;
    Tile expResult = null;
    Tile result = TileCache.createTile(tileType, orientation, direction, cp);
    assertEquals(expResult, result);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of rollback method, of class TileCache.
   */
  //@Test
  public void testRollback() {
    System.out.println("rollback");
    Tile tile = null;
    TileCache.rollback(tile);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of flush method, of class TileCache.
   */
  /**
   * Test of loadTiles method, of class TileCache.
   */
  //@Test
  public void testLoadTiles_0args() {
    System.out.println("loadTiles");
    List<Tile> expResult = null;
    List<Tile> result = TileCache.loadTiles();
    assertEquals(expResult, result);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of addTile method, of class TileCache.
   */
  //@Test
  public void testAddTile() {
    System.out.println("addTile");
    Tile tile = null;
    TileCache.addTile(tile);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of calculateMaxCoordinates method, of class TileCache.
   */
  //@Test
  public void testCalculateMaxCoordinates() {
    System.out.println("calculateMaxCoordinates");
    int tileX = 0;
    int tileY = 0;
    TileCache.calculateMaxCoordinates(tileX, tileY);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of getMinCanvasSize method, of class TileCache.
   */
  //@Test
  public void testGetMinCanvasSize() {
    System.out.println("getMinCanvasSize");
    Dimension expResult = null;
    Dimension result = TileCache.getMinCanvasSize();
    assertEquals(expResult, result);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of addAndSaveTile method, of class TileCache.
   */
  //@Test
  public void testAddAndSaveTile() {
    System.out.println("addAndSaveTile");
    Tile tile = null;
    Tile expResult = null;
    Tile result = TileCache.addAndSaveTile(tile);
    assertEquals(expResult, result);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of persistTile method, of class TileCache.
   */
  //@Test
  public void testPersistTile() {
    System.out.println("persistTile");
    Tile tile = null;
    TileCache.persistTile(tile);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of persistBlock method, of class TileCache.
   */
  //@Test
  public void testPersistBlock() {
    System.out.println("persistBlock");
    BlockBean block = null;
    TileCache.persistBlock(block);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of persistAllTiles method, of class TileCache.
   */
  //@Test
  public void testPersistAllTiles() {
    System.out.println("persistAllTiles");
    TileCache.persistAllTiles();
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of deleteTile method, of class TileCache.
   */
  //@Test
  public void testDeleteTile() {
    System.out.println("deleteTile");
    Tile tile = null;
    TileCache.deleteTile(tile);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of canMoveTo method, of class TileCache.
   */
  //@Test
  public void testCanMoveTo() {
    System.out.println("canMoveTo");
    Tile tile = null;
    Point p = null;
    boolean expResult = false;
    boolean result = TileCache.canMoveTo(tile, p);
    assertEquals(expResult, result);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of moveTo method, of class TileCache.
   */
  //@Test
  public void testMoveTo() {
    System.out.println("moveTo");
    Tile tile = null;
    Point p = null;
    TileCache.moveTo(tile, p);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of rotateTile method, of class TileCache.
   */
  //@Test
  public void testRotateTile() {
    System.out.println("rotateTile");
    Tile tile = null;
    Tile expResult = null;
    Tile result = TileCache.rotateTile(tile);
    assertEquals(expResult, result);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of flipHorizontal method, of class TileCache.
   */
  //@Test
  public void testFlipHorizontal() {
    System.out.println("flipHorizontal");
    Tile tile = null;
    Tile expResult = null;
    Tile result = TileCache.flipHorizontal(tile);
    assertEquals(expResult, result);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of flipVertical method, of class TileCache.
   */
  //@Test
  public void testFlipVertical() {
    System.out.println("flipVertical");
    Tile tile = null;
    Tile expResult = null;
    Tile result = TileCache.flipVertical(tile);
    assertEquals(expResult, result);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

}
