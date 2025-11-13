/*
 * Copyright 2025 frans.
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

import java.util.ArrayList;
import java.util.List;
import jcs.entities.RouteBean;
import jcs.persistence.util.PersistenceTestHelper;
import jcs.ui.layout.tiles.Tile;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;

/**
 * Test for the Crossing
 */
public class AStarThreeWaySwitchWest {

  private final PersistenceTestHelper testHelper;

  public AStarThreeWaySwitchWest() {
    System.setProperty("persistenceService", "jcs.persistence.TestH2PersistenceService");
    testHelper = PersistenceTestHelper.getInstance();
  }

  @Before
  public void setUp() {
    testHelper.runTestDataInsertScript("layout_three_way_switch_west.sql");
  }

  @After
  public void tearDown() {
  }

  @Test
  public void testBuildGraph() {
    System.out.println("buildGraph");
    List<Tile> tiles = jcs.ui.layout.tiles.TileCache.loadTiles(false);
    assertEquals(33, tiles.size());
    AStar instance = new AStar();
    instance.buildGraph(tiles);
    List<Node> allNodes = instance.getNodes();
    assertEquals(tiles.size(), allNodes.size());
  }

  @Test
  public void testGetAllBlockToBlockNodes() {
    System.out.println("getAllBlockToBlockNodes");
    List<Tile> tiles = jcs.ui.layout.tiles.TileCache.loadTiles(false);
    AStar instance = new AStar();
    instance.buildGraph(tiles);
    List<List<Node>> result = instance.getAllBlockToBlockNodes();
    assertEquals(12, result.size());
  }

  @Test
  public void testFindPath_bk2p_bk1m() {
    System.out.println("findPath [bk-2+]->[bk-1-]");
    String fromNodeId = "bk-2";
    String fromSuffix = "+";
    String toNodeId = "bk-1";
    String toSuffix = "-";
    AStar instance = new AStar();
    List<Tile> tiles = jcs.ui.layout.tiles.TileCache.loadTiles(false);
    instance.buildGraph(tiles);

    String expPath = "[bk-2+]->[bk-1-]: bk-2+[bk-2] -> se-7 -> st-8 -> st-3 -> tw-1[GREEN] -> st-1 -> st-10 -> se-5 -> bk-1-[bk-1]";
    List<Node> path = instance.findPath(fromNodeId, fromSuffix, toNodeId, toSuffix);
    String result = instance.pathToString(path);

    System.out.println("#" + result);
    assertEquals(expPath, result);
  }

  @Test
  public void testFindPath_bk_1m_bk2p() {
    System.out.println("findPath [bk-1-]->[bk-2+]");
    String fromNodeId = "bk-1";
    String fromSuffix = "-";
    String toNodeId = "bk-2";
    String toSuffix = "+";
    AStar instance = new AStar();
    List<Tile> tiles = jcs.ui.layout.tiles.TileCache.loadTiles(false);
    instance.buildGraph(tiles);

    String expPath = "[bk-1-]->[bk-2+]: bk-1-[bk-1] -> se-5 -> st-10 -> st-1 -> tw-1[GREEN] -> st-3 -> st-8 -> se-7 -> bk-2+[bk-2]";
    List<Node> path = instance.findPath(fromNodeId, fromSuffix, toNodeId, toSuffix);
    String result = instance.pathToString(path);

    System.out.println("#" + result);
    assertEquals(expPath, result);
  }

  @Test
  public void testFindPath_bk1m_bk3m() {
    System.out.println("findPath [bk-1-]->[bk-3-]");
    String fromNodeId = "bk-1";
    String fromSuffix = "-";
    String toNodeId = "bk-3";
    String toSuffix = "-";
    AStar instance = new AStar();
    List<Tile> tiles = jcs.ui.layout.tiles.TileCache.loadTiles(false);
    instance.buildGraph(tiles);
    String expPath = "[bk-1-]->[bk-3-]: bk-1-[bk-1] -> se-5 -> st-10 -> st-1 -> tw-1[RED] -> ct-4 -> ct-1 -> ct-6 -> st-2 -> se-1 -> bk-3-[bk-3]";

    List<Node> path = instance.findPath(fromNodeId, fromSuffix, toNodeId, toSuffix);
    String result = instance.pathToString(path);

    System.out.println("#" + result);
    assertEquals(expPath, result);
  }

  @Test
  public void testFindPath_bk3m_bk1m() {
    System.out.println("findPath [bk-3-]->[bk-1-]");
    String fromNodeId = "bk-3";
    String fromSuffix = "-";
    String toNodeId = "bk-1";
    String toSuffix = "-";
    AStar instance = new AStar();
    List<Tile> tiles = jcs.ui.layout.tiles.TileCache.loadTiles(false);
    instance.buildGraph(tiles);
    String expPath = "[bk-3-]->[bk-1-]: bk-3-[bk-3] -> se-1 -> st-2 -> ct-6 -> ct-1 -> ct-4 -> tw-1[RED] -> st-1 -> st-10 -> se-5 -> bk-1-[bk-1]";

    List<Node> path = instance.findPath(fromNodeId, fromSuffix, toNodeId, toSuffix);
    String result = instance.pathToString(path);

    System.out.println("#" + result);
    assertEquals(expPath, result);
  }

  @Test
  public void testFindPath_bk1m_bk4m() {
    System.out.println("findPath [bk-1-]->[bk-4-]");
    String fromNodeId = "bk-1";
    String fromSuffix = "-";
    String toNodeId = "bk-4";
    String toSuffix = "-";
    AStar instance = new AStar();
    List<Tile> tiles = jcs.ui.layout.tiles.TileCache.loadTiles(false);
    instance.buildGraph(tiles);
    String expPath = "[bk-1-]->[bk-4-]: bk-1-[bk-1] -> se-5 -> st-10 -> st-1 -> tw-1[RED2] -> ct-3 -> ct-2 -> ct-5 -> st-6 -> se-6 -> bk-4-[bk-4]";
    List<Node> path = instance.findPath(fromNodeId, fromSuffix, toNodeId, toSuffix);
    String result = instance.pathToString(path);

    System.out.println("#" + result);
    assertEquals(expPath, result);
  }

  @Test
  public void testFindPath_bk4m_bk1m() {
    System.out.println("findPath [bk-4-]->[bk-1-]");
    String fromNodeId = "bk-4";
    String fromSuffix = "-";
    String toNodeId = "bk-1";
    String toSuffix = "-";
    AStar instance = new AStar();
    List<Tile> tiles = jcs.ui.layout.tiles.TileCache.loadTiles(false);
    instance.buildGraph(tiles);
    String expPath = "[bk-4-]->[bk-1-]: bk-4-[bk-4] -> se-6 -> st-6 -> ct-5 -> ct-2 -> ct-3 -> tw-1[RED2] -> st-1 -> st-10 -> se-5 -> bk-1-[bk-1]";

    List<Node> path = instance.findPath(fromNodeId, fromSuffix, toNodeId, toSuffix);
    String result = instance.pathToString(path);

    System.out.println("#" + result);
    assertEquals(expPath, result);
  }

  @Test
  public void testRouteAll() {
    System.out.println("routeAll");
    List<Tile> tiles = jcs.ui.layout.tiles.TileCache.loadTiles(false);

    List<String> expRouteDesc = new ArrayList<>();

    String r1 = "Route: [bk-1-]->[bk-3-]: bk-1 -> bk-1-[bk-1] -> se-5 -> st-10 -> st-1 -> tw-1[RED] -> ct-4 -> ct-1 -> ct-6 -> st-2 -> se-1 -> bk-3-[bk-3]";
    String r2 = "Route: [bk-1-]->[bk-2+]: bk-1 -> bk-1-[bk-1] -> se-5 -> st-10 -> st-1 -> tw-1[GREEN] -> st-3 -> st-8 -> se-7 -> bk-2+[bk-2]";
    String r3 = "Route: [bk-1-]->[bk-4-]: bk-1 -> bk-1-[bk-1] -> se-5 -> st-10 -> st-1 -> tw-1[RED2] -> ct-3 -> ct-2 -> ct-5 -> st-6 -> se-6 -> bk-4-[bk-4]";
    String r4 = "Route: [bk-2+]->[bk-1-]: bk-2 -> bk-2+[bk-2] -> se-7 -> st-8 -> st-3 -> tw-1[GREEN] -> st-1 -> st-10 -> se-5 -> bk-1-[bk-1]";
    String r5 = "Route: [bk-3-]->[bk-1-]: bk-3 -> bk-3-[bk-3] -> se-1 -> st-2 -> ct-6 -> ct-1 -> ct-4 -> tw-1[RED] -> st-1 -> st-10 -> se-5 -> bk-1-[bk-1]";
    String r6 = "Route: [bk-4-]->[bk-1-]: bk-4 -> bk-4-[bk-4] -> se-6 -> st-6 -> ct-5 -> ct-2 -> ct-3 -> tw-1[RED2] -> st-1 -> st-10 -> se-5 -> bk-1-[bk-1]";

    expRouteDesc.add(r1);
    expRouteDesc.add(r2);
    expRouteDesc.add(r3);
    expRouteDesc.add(r4);
    expRouteDesc.add(r5);
    expRouteDesc.add(r6);

    AStar instance = new AStar();
    instance.buildGraph(tiles);
    List<RouteBean> routeBeans = instance.routeAll();
    assertEquals(6, routeBeans.size());

    List<String> resultRouteDesc = new ArrayList<>();

    for (RouteBean r : routeBeans) {
      resultRouteDesc.add(r.toLogString());
      System.out.println("#" + r.toLogString());
    }

    assertEquals(expRouteDesc, resultRouteDesc);
  }

  //Check the impossible routes
  @Test
  public void testFindPath_bk2p_bk4m() {
    System.out.println("findPath [bk-2+]->[bk-4-]");
    String fromNodeId = "bk-2";
    String fromSuffix = "+";
    String toNodeId = "bk-4";
    String toSuffix = "-";
    AStar instance = new AStar();
    List<Tile> tiles = jcs.ui.layout.tiles.TileCache.loadTiles(false);
    instance.buildGraph(tiles);
    String expPath = "";

    List<Node> path = instance.findPath(fromNodeId, fromSuffix, toNodeId, toSuffix);
    String result = instance.pathToString(path);

    System.out.println("#" + result);
    assertEquals(expPath, result);
  }

  @Test
  public void testFindPath_bk4m_bk2p() {
    System.out.println("findPath [bk-4-]->[bk-2+]");
    String fromNodeId = "bk-4";
    String fromSuffix = "-";
    String toNodeId = "bk-2";
    String toSuffix = "+";
    AStar instance = new AStar();
    List<Tile> tiles = jcs.ui.layout.tiles.TileCache.loadTiles(false);
    instance.buildGraph(tiles);
    String expPath = "";

    List<Node> path = instance.findPath(fromNodeId, fromSuffix, toNodeId, toSuffix);
    String result = instance.pathToString(path);

    System.out.println("#" + result);
    assertEquals(expPath, result);
  }

  @Test
  public void testFindPath_bk2p_bk3m() {
    System.out.println("findPath [bk-2+]->[bk-3-]");
    String fromNodeId = "bk-2";
    String fromSuffix = "+";
    String toNodeId = "bk-3";
    String toSuffix = "-";
    AStar instance = new AStar();
    List<Tile> tiles = jcs.ui.layout.tiles.TileCache.loadTiles(false);
    instance.buildGraph(tiles);
    String expPath = "";

    List<Node> path = instance.findPath(fromNodeId, fromSuffix, toNodeId, toSuffix);
    String result = instance.pathToString(path);

    System.out.println("#" + result);
    assertEquals(expPath, result);
  }

  @Test
  public void testFindPath_bk3m_bk2p() {
    System.out.println("findPath [bk-3-]->[bk-2+]");
    String fromNodeId = "bk-3";
    String fromSuffix = "-";
    String toNodeId = "bk-2";
    String toSuffix = "+";
    AStar instance = new AStar();
    List<Tile> tiles = jcs.ui.layout.tiles.TileCache.loadTiles(false);
    instance.buildGraph(tiles);
    String expPath = "";

    List<Node> path = instance.findPath(fromNodeId, fromSuffix, toNodeId, toSuffix);
    String result = instance.pathToString(path);

    System.out.println("#" + result);
    assertEquals(expPath, result);
  }

  @Test
  public void testFindPath_bk3m_bk4m() {
    System.out.println("findPath [bk-3-]->[bk-4-]");
    String fromNodeId = "bk-3";
    String fromSuffix = "-";
    String toNodeId = "bk-4";
    String toSuffix = "-";
    AStar instance = new AStar();
    List<Tile> tiles = jcs.ui.layout.tiles.TileCache.loadTiles(false);
    instance.buildGraph(tiles);
    String expPath = "";

    List<Node> path = instance.findPath(fromNodeId, fromSuffix, toNodeId, toSuffix);
    String result = instance.pathToString(path);

    System.out.println("#" + result);
    assertEquals(expPath, result);
  }

  @Test
  public void testFindPath_bk4m_bk3m() {
    System.out.println("findPath [bk-4-]->[bk-3-]");
    String fromNodeId = "bk-4";
    String fromSuffix = "-";
    String toNodeId = "bk-3";
    String toSuffix = "-";
    AStar instance = new AStar();
    List<Tile> tiles = jcs.ui.layout.tiles.TileCache.loadTiles(false);
    instance.buildGraph(tiles);
    String expPath = "";

    List<Node> path = instance.findPath(fromNodeId, fromSuffix, toNodeId, toSuffix);
    String result = instance.pathToString(path);

    System.out.println("#" + result);
    assertEquals(expPath, result);
  }

}
