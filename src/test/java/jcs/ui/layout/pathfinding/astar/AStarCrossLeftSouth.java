/*
 * Copyright 2023 frans.
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
public class AStarCrossLeftSouth {

  private final PersistenceTestHelper testHelper;

  public AStarCrossLeftSouth() {
    System.setProperty("persistenceService", "jcs.persistence.H2PersistenceService");
    testHelper = PersistenceTestHelper.getInstance();
  }

  @Before
  public void setUp() {
    testHelper.runTestDataInsertScript("layout_cross_left_south.sql");
  }

  @After
  public void tearDown() {
  }

  /**
   * Test of buildGraph method, of class AStar.
   */
  @Test
  public void testBuildGraph() {
    System.out.println("buildGraph");
    List<Tile> tiles = jcs.ui.layout.tiles.TileCache.loadTiles(false);
    assertEquals(21, tiles.size());
    AStar instance = new AStar();
    instance.buildGraph(tiles);
    List<Node> allNodes = instance.getNodes();
    assertEquals(tiles.size(), allNodes.size());
  }

  /**
   * Test of getAllBlockToBlockNodes method, of class AStar.
   */
  @Test
  public void testGetAllBlockToBlockNodes() {
    System.out.println("getAllBlockToBlockNodes");
    List<Tile> tiles = jcs.ui.layout.tiles.TileCache.loadTiles(false);
    AStar instance = new AStar();
    instance.buildGraph(tiles);
    List<List<Node>> result = instance.getAllBlockToBlockNodes();
    assertEquals(12, result.size());
  }

  /**
   * Test of findPath method, of class AStar.
   */
  @Test
  public void testFindPath_bk_1p_bk2m() {
    System.out.println("findPath [bk-1+]->[bk-2-]");
    String fromNodeId = "bk-1";
    String fromSuffix = "+";
    String toNodeId = "bk-2";
    String toSuffix = "-";
    AStar instance = new AStar();
    List<Tile> tiles = jcs.ui.layout.tiles.TileCache.loadTiles(false);
    instance.buildGraph(tiles);

    String expPath = "[bk-1+]->[bk-2-]: bk-1+[bk-1] -> st-1 -> st-2 -> cs-3[GREEN] -> st-5 -> st-6 -> bk-2-[bk-2]";

    List<Node> path = instance.findPath(fromNodeId, fromSuffix, toNodeId, toSuffix);
    String result = instance.pathToString(path);

    System.out.println("#" + result);

    assertEquals(expPath, result);
  }

  @Test
  public void testFindPath_bk_4m_bk3p() {
    System.out.println("findPath [bk-4-]->[bk-3+]");
    String fromNodeId = "bk-4";
    String fromSuffix = "-";
    String toNodeId = "bk-3";
    String toSuffix = "+";
    AStar instance = new AStar();
    List<Tile> tiles = jcs.ui.layout.tiles.TileCache.loadTiles(false);
    instance.buildGraph(tiles);
    String expPath = "[bk-4-]->[bk-3+]: bk-4-[bk-4] -> st-29 -> st-30 -> cs-3[GREEN] -> st-25 -> st-26 -> bk-3+[bk-3]";

    List<Node> path = instance.findPath(fromNodeId, fromSuffix, toNodeId, toSuffix);
    String result = instance.pathToString(path);
    assertEquals(expPath, result);
  }

  @Test
  public void testFindPath_bk_2m_bk4m() {
    System.out.println("findPath [bk-2-]->[bk-4-]");
    String fromNodeId = "bk-2";
    String fromSuffix = "-";
    String toNodeId = "bk-4";
    String toSuffix = "-";
    AStar instance = new AStar();
    List<Tile> tiles = jcs.ui.layout.tiles.TileCache.loadTiles(false);
    instance.buildGraph(tiles);
    String expPath = "[bk-2-]->[bk-4-]: bk-2-[bk-2] -> st-6 -> st-5 -> cs-3[RED] -> st-30 -> st-29 -> bk-4-[bk-4]";

    List<Node> path = instance.findPath(fromNodeId, fromSuffix, toNodeId, toSuffix);
    String result = instance.pathToString(path);
    assertEquals(expPath, result);
  }

  @Test
  public void testFindPath_bk_2m_bk3p() {
    System.out.println("findPath [bk-2-]->[bk-3+]");
    String fromNodeId = "bk-2";
    String fromSuffix = "-";
    String toNodeId = "bk-3";
    String toSuffix = "+";
    AStar instance = new AStar();
    List<Tile> tiles = jcs.ui.layout.tiles.TileCache.loadTiles(false);
    instance.buildGraph(tiles);
    String expPath = "";

    List<Node> path = instance.findPath(fromNodeId, fromSuffix, toNodeId, toSuffix);
    String result = instance.pathToString(path);
    assertEquals(expPath, result);
  }

  /**
   * Test of routeAll method, of class AStar.
   */
  @Test
  public void testRouteAll() {
    System.out.println("routeAll");
    List<Tile> tiles = jcs.ui.layout.tiles.TileCache.loadTiles(false);

    List<String> expRouteDesc = new ArrayList<>();

    String r1 = "Route: [bk-2-]->[bk-4-]: bk-2 -> bk-2-[bk-2] -> st-6 -> st-5 -> cs-3[RED] -> st-30 -> st-29 -> bk-4-[bk-4]";
    String r2 = "Route: [bk-2-]->[bk-1+]: bk-2 -> bk-2-[bk-2] -> st-6 -> st-5 -> cs-3[GREEN] -> st-2 -> st-1 -> bk-1+[bk-1]";
    String r3 = "Route: [bk-3+]->[bk-4-]: bk-3 -> bk-3+[bk-3] -> st-26 -> st-25 -> cs-3[GREEN] -> st-30 -> st-29 -> bk-4-[bk-4]";
    String r4 = "Route: [bk-3+]->[bk-1+]: bk-3 -> bk-3+[bk-3] -> st-26 -> st-25 -> cs-3[RED] -> st-2 -> st-1 -> bk-1+[bk-1]";
    String r5 = "Route: [bk-1+]->[bk-2-]: bk-1 -> bk-1+[bk-1] -> st-1 -> st-2 -> cs-3[GREEN] -> st-5 -> st-6 -> bk-2-[bk-2]";
    String r6 = "Route: [bk-4-]->[bk-3+]: bk-4 -> bk-4-[bk-4] -> st-29 -> st-30 -> cs-3[GREEN] -> st-25 -> st-26 -> bk-3+[bk-3]";
    String r7 = "Route: [bk-1+]->[bk-3+]: bk-1 -> bk-1+[bk-1] -> st-1 -> st-2 -> cs-3[RED] -> st-25 -> st-26 -> bk-3+[bk-3]";
    String r8 = "Route: [bk-4-]->[bk-2-]: bk-4 -> bk-4-[bk-4] -> st-29 -> st-30 -> cs-3[RED] -> st-5 -> st-6 -> bk-2-[bk-2]";

    expRouteDesc.add(r1);
    expRouteDesc.add(r2);
    expRouteDesc.add(r3);
    expRouteDesc.add(r4);
    expRouteDesc.add(r5);
    expRouteDesc.add(r6);
    expRouteDesc.add(r7);
    expRouteDesc.add(r8);

    AStar instance = new AStar();
    instance.buildGraph(tiles);
    List<RouteBean> routeBeans = instance.routeAll();
    assertEquals(8, routeBeans.size());

    List<String> resultRouteDesc = new ArrayList<>();

    for (RouteBean r : routeBeans) {
      resultRouteDesc.add(r.toLogString());
    }

    assertEquals(expRouteDesc, resultRouteDesc);
  }

}
