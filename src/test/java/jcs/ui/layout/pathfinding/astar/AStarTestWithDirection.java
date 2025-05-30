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
 *
 * @author frans
 */
public class AStarTestWithDirection {

  private final PersistenceTestHelper testHelper;

  public AStarTestWithDirection() {
    System.setProperty("persistenceService", "jcs.persistence.TestH2PersistenceService");
    testHelper = PersistenceTestHelper.getInstance();
  }

  @Before
  public void setUp() {
    testHelper.insertSimpleLayoutDirectionTestData();
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
    assertEquals(37, tiles.size());
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
    assertEquals(6, result.size());
  }

  /**
   * Test of findPath method, of class AStar.
   */
  @Test
  public void testFindPathbk_2p_bkm3() {
    System.out.println("findPath [bk-2+]->[bk-3-]");
    String fromNodeId = "bk-2";
    String fromSuffix = "+";
    String toNodeId = "bk-3";
    String toSuffix = "-";
    AStar instance = new AStar();
    List<Tile> tiles = jcs.ui.layout.tiles.TileCache.loadTiles(false);
    instance.buildGraph(tiles);

    String expPath = "[bk-2+]->[bk-3-]: bk-2+[bk-2] -> se-3 -> sd-3 -> ct-1 -> sw-2[RED] -> st-5 -> ct-4 -> st-11 -> st-12 -> st-13 -> st-14 -> ct-6 -> st-20 -> st-19 -> st-18 -> se-6 -> bk-3-[bk-3]";

    List<Node> path = instance.findPath(fromNodeId, fromSuffix, toNodeId, toSuffix);
    String result = instance.pathToString(path);

    System.out.println("#" + result);

    assertEquals(expPath, result);
  }

  @Test
  public void testFindPathbk_2p_bk3m() {
    System.out.println("findPath [bk-2+]->[bk-3-]");
    String fromNodeId = "bk-2";
    String fromSuffix = "+";
    String toNodeId = "bk-3";
    String toSuffix = "-";
    AStar instance = new AStar();
    List<Tile> tiles = jcs.ui.layout.tiles.TileCache.loadTiles(false);
    instance.buildGraph(tiles);

    String expPath = "[bk-2+]->[bk-3-]: bk-2+[bk-2] -> se-3 -> sd-3 -> ct-1 -> sw-2[RED] -> st-5 -> ct-4 -> st-11 -> st-12 -> st-13 -> st-14 -> ct-6 -> st-20 -> st-19 -> st-18 -> se-6 -> bk-3-[bk-3]";

    List<Node> path = instance.findPath(fromNodeId, fromSuffix, toNodeId, toSuffix);
    String result = instance.pathToString(path);

    System.out.println("#" + result);

    assertEquals(expPath, result);
  }

  @Test
  public void testFindPathbk_2m_bk3p() {
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

    String r1 = "Route: [bk-1-]->[bk-3+]: bk-1 -> bk-1-[bk-1] -> se-1 -> sd-2 -> sw-1[GREEN] -> st-6 -> ct-3 -> st-7 -> st-8 -> st-9 -> st-10 -> ct-5 -> st-15 -> st-16 -> st-17 -> se-5 -> bk-3+[bk-3]";
    String r2 = "Route: [bk-2+]->[bk-3-]: bk-2 -> bk-2+[bk-2] -> se-3 -> sd-3 -> ct-1 -> sw-2[RED] -> st-5 -> ct-4 -> st-11 -> st-12 -> st-13 -> st-14 -> ct-6 -> st-20 -> st-19 -> st-18 -> se-6 -> bk-3-[bk-3]";
    String r3 = "Route: [bk-3+]->[bk-2-]: bk-3 -> bk-3+[bk-3] -> se-5 -> st-17 -> st-16 -> st-15 -> ct-5 -> st-10 -> st-9 -> st-8 -> st-7 -> ct-3 -> st-6 -> sw-1[RED] -> ct-2 -> sd-1 -> se-4 -> bk-2-[bk-2]";
    String r4 = "Route: [bk-3-]->[bk-1+]: bk-3 -> bk-3-[bk-3] -> se-6 -> st-18 -> st-19 -> st-20 -> ct-6 -> st-14 -> st-13 -> st-12 -> st-11 -> ct-4 -> st-5 -> sw-2[GREEN] -> sd-4 -> se-2 -> bk-1+[bk-1]";

    expRouteDesc.add(r1);
    expRouteDesc.add(r2);
    expRouteDesc.add(r3);
    expRouteDesc.add(r4);

    AStar instance = new AStar();
    instance.buildGraph(tiles);
    List<RouteBean> routeBeans = instance.routeAll();
    assertEquals(4, routeBeans.size());

    List<String> resultRouteDesc = new ArrayList<>();

    for (RouteBean r : routeBeans) {
      resultRouteDesc.add(r.toLogString());
    }

    assertEquals(expRouteDesc, resultRouteDesc);
  }

  /**
   * Test of getRoute method, of class AStar.
   */
  //@Test
  public void testGetRoute() {
    System.out.println("getRoute");
    List<Tile> tiles = jcs.ui.layout.tiles.TileCache.loadTiles(false);
    AStar instance = new AStar();
    instance.buildGraph(tiles);
    List<RouteBean> routeBeans = instance.routeAll();
    assertEquals(4, routeBeans.size());

    String expPath = "Route: [bk-2+]->[bk-3-]: bk-2 -> bk-2+[bk-2] -> se-3 -> st-4 -> ct-1 -> sw-2[RED] -> st-5 -> ct-4 -> st-11 -> st-12 -> st-13 -> st-14 -> ct-6 -> st-20 -> st-19 -> st-18 -> se-6 -> bk-3-[bk-3]";

    String id = "[bk-2+]->[bk-3-]";
    RouteBean result = instance.getRoute(id);
    String resultPath = result.toLogString();

    assertEquals(expPath, resultPath);
  }

}
