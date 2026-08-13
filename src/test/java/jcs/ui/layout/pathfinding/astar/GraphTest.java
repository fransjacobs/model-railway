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

/**
 *
 */
public class GraphTest {

  private final PersistenceTestHelper testHelper;

  private AStar aStar;
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
    aStar = new AStar();
    TileCache.loadTiles();
    graph = aStar.buildGraph(TileCache.getTiles());
  }

  @AfterEach
  public void tearDown() {
  }

  @Test
  public void testSize() {
    System.out.println("size");
    int expResult = 85;
    int result = graph.size();
    assertEquals(expResult, result);
  }

  @Test
  public void testGetNode() {
    System.out.println("getNode");
    String id = "cr-2-v";
    Node expResult = new Node(TileCache.findTile("cr-2"));
    Node result = graph.getNode(id);
    assertEquals(expResult, result);

    String id2 = "cr-3-h";
    Node expResult2 = new Node(TileCache.findTile("cr-3"));
    Node result2 = graph.getNode(id2);
    assertEquals(expResult2, result2);
  }

  @Test
  public void testCanTravelTo() {
    System.out.println("canTravelTo");
    Node from = graph.getNode("cr-2-h");
    Node to = graph.getNode("cr-3-h");

    boolean result = graph.canTravelTo(from, to);
    assertTrue(result);
  }

  @Test
  public void testLink() {
    System.out.println("link");
    Tile st22 = TileCache.findTile("st-22");
    Tile cr2 = TileCache.findTile("cr-2");

    Point mutual = st22.getSharingPoint(cr2);
    assertEquals(new Point(320, 500), mutual);
  }

  @Test
  public void testFindPath() {
    System.out.println("findPath");
    Node start = graph.getNode("bk-1");
    String startSuffix = "+";
    Node destination = graph.getNode("bk-6");
    String destSuffix = "+";

    String expPath = "[bk-1+]->[bk-6+]: bk-1+[bk-1] -> se-1 -> sw-2[GREEN] -> st-9 -> st-1 -> sw-4[RED] -> cr-1-h -> sd-4 -> ct-8 -> st-31 -> ct-13 -> st-35 -> st-34 -> st-28 -> st-6 -> st-22 -> cr-2-h -> cr-3-h -> st-36 -> ct-12 -> ct-11 -> st-37 -> bk-6+[bk-6]";

    List<Node> path = graph.findPath(start, startSuffix, destination, destSuffix);
    String result = aStar.pathToString(path);

    assertEquals(23, path.size());

    assertEquals(expPath, result);
  }

}
