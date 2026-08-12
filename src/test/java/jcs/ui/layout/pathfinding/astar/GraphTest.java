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
package jcs.ui.layout.pathfinding.astar;

import java.awt.Point;
import java.util.List;
import jcs.persistence.util.PersistenceTestHelper;
import jcs.ui.layout.tiles.Tile;
import jcs.ui.layout.tiles.TileCache;
import static org.junit.Assert.assertEquals;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.tinylog.Logger;

/**
 *
 */
public class GraphTest {

  private final PersistenceTestHelper testHelper;

  private Graph graph;

  public GraphTest() {
    System.setProperty("persistenceService", "jcs.persistence.TestH2PersistenceService");
    testHelper = PersistenceTestHelper.getInstance();
  }

  @BeforeAll
  public static void setUpClass() {
  }

  @AfterAll
  public static void tearDownClass() {
  }

  @BeforeEach
  public void setUp() {
    testHelper.runTestDataInsertScript("layout_issue_172.sql");

    AStar aStar = new AStar();
    //Refresh / load
    TileCache.loadTiles();
    graph = aStar.buildGraph(TileCache.getTiles());
  }

  @AfterEach
  public void tearDown() {
  }

  //@Test
  public void testSize() {
    System.out.println("size");
    int expResult = 82;
    int result = graph.size();
    assertEquals(expResult, result);
  }

  //@Test
  public void testGetNode() {
    System.out.println("getNode");
    String id = "cr-2";
    Node expResult = new Node(TileCache.findTile("cr-2"));
    Node result = graph.getNode(id);
    assertEquals(expResult, result);

    String id2 = "cr-3";
    Node expResult2 = new Node(TileCache.findTile("cr-3"));
    Node result2 = graph.getNode(id2);
    assertEquals(expResult2, result2);
  }

  //@Test
  public void testCanTravelTo() {
    System.out.println("canTravelTo");
    //Node from = new Node(TileCache.findTile("cr-2"));
    Node from = graph.getNode("cr-2");
    //Node to = new Node(TileCache.findTile("cr-3"));
    Node to = graph.getNode("cr-3");

    boolean result = graph.canTravelTo(from, to);
    assertTrue(result);
  }

  //@Test
  public void testLink() {
    System.out.println("link");
    Tile st22 = TileCache.findTile("st-22");
    Tile cr2 = TileCache.findTile("cr-2");
    
    //from st22 to cr2 find the connecting side
    //which point is shared?
    Point mutual = st22.getSharingPoint(cr2);
    
    Logger.trace(mutual);
    
    
    
  }
  
//TRACE	2026-08-12 17:48:38.246 [main] AStar.buildGraph(): Node: st-22 has 2 neighbor points.  id: st-22 
//TRACE	2026-08-12 17:48:38.246 [main] AStar.buildGraph(): Node st-22 check NeighborPoint: (340,500)
//TRACE	2026-08-12 17:48:38.246 [main] AStar.buildGraph(): Node st-22 NeighborPoint: (340,500) -> cr-2
//TRACE	2026-08-12 17:48:38.246 [main] Graph.link(): !Edge from: st-22 to: cr-2, distance: 40.0
//TRACE	2026-08-12 17:48:38.246 [main] AStar.buildGraph(): Node st-22 check NeighborPoint: (260,500)
//TRACE	2026-08-12 17:48:38.246 [main] AStar.buildGraph(): Node st-22 NeighborPoint: (260,500) -> st-6
//TRACE	2026-08-12 17:48:38.246 [main] Graph.link(): !Edge from: st-22 to: st-6, distance: 40.0
  
  
//TRACE	2026-08-12 17:48:38.273 [main] AStar.buildGraph(): Node: cr-2 has 4 neighbor points.  id: cr-2 
//TRACE	2026-08-12 17:48:38.273 [main] AStar.buildGraph(): Node cr-2 check NeighborPoint: (340,460)
//TRACE	2026-08-12 17:48:38.273 [main] AStar.buildGraph(): Node cr-2 NeighborPoint: (340,460) -> st-2
//TRACE	2026-08-12 17:48:38.273 [main] Graph.link(): !Edge from: cr-2 to: st-2, distance: 40.0 -> N
//TRACE	2026-08-12 17:48:38.274 [main] AStar.buildGraph(): Node cr-2 check NeighborPoint: (340,540)
//TRACE	2026-08-12 17:48:38.274 [main] AStar.buildGraph(): Node cr-2 NeighborPoint: (340,540) -> sd-1
//TRACE	2026-08-12 17:48:38.274 [main] Graph.link(): !Edge from: cr-2 to: sd-1, distance: 40.0  -> S
//TRACE	2026-08-12 17:48:38.274 [main] AStar.buildGraph(): Node cr-2 check NeighborPoint: (380,500)
//TRACE	2026-08-12 17:48:38.274 [main] AStar.buildGraph(): Node cr-2 NeighborPoint: (380,500) -> cr-3
//TRACE	2026-08-12 17:48:38.274 [main] Graph.link(): !Edge from: cr-2 to: cr-3, distance: 40.0  -> E
//TRACE	2026-08-12 17:48:38.274 [main] AStar.buildGraph(): Node cr-2 check NeighborPoint: (300,500)
//TRACE	2026-08-12 17:48:38.274 [main] AStar.buildGraph(): Node cr-2 NeighborPoint: (300,500) -> st-22
//TRACE	2026-08-12 17:48:38.274 [main] Graph.link(): !Edge from: cr-2 to: st-22, distance: 40.0 -> W
//  
  
  
  
  //@Test
  public void testFindPath() {
    System.out.println("findPath");
    Node start = graph.getNode("bk-1");
    String startSuffix = "+";
    Node destination = graph.getNode("bk-6");
    String destSuffix = "+";
    List<Node> expResult = null;
    
    List<Node> result = graph.findPath(start, startSuffix, destination, destSuffix);
    
    assertEquals(25, result.size());
    //assertEquals(expResult, result);
  }
  
  
  
  
  /**
   * Test of calculateHeuristic method, of class Graph.
   */
  //@Test
  public void testCalculateHeuristic() {
    System.out.println("calculateHeuristic");
    Node from = null;
    Node to = null;
    Graph instance = new Graph();
    double expResult = 0.0;
    double result = instance.calculateHeuristic(from, to);
    assertEquals(expResult, result, 0);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of manhattanDistance method, of class Graph.
   */
  //@Test
  public void testManhattanDistance_Node_Node() {
    System.out.println("manhattanDistance");
    Node from = null;
    Node to = null;
    double expResult = 0.0;
    double result = Graph.manhattanDistance(from, to);
    assertEquals(expResult, result, 0);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of manhattanDistance method, of class Graph.
   */
  //@Test
  public void testManhattanDistance_Point_Point() {
    System.out.println("manhattanDistance");
    Point from = null;
    Point to = null;
    double expResult = 0.0;
    double result = Graph.manhattanDistance(from, to);
    assertEquals(expResult, result, 0);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of link method, of class Graph.
   */
  //@Test
//  public void testLink() {
//    System.out.println("link");
//    Node from = null;
//    Node to = null;
//    double distance = 0.0;
//    Graph instance = new Graph();
//    Edge expResult = null;
//    Edge result = instance.link(from, to, distance);
//    assertEquals(expResult, result);
//    // TODO review the generated test code and remove the default call to fail.
//    fail("The test case is a prototype.");
//  }

  

}
