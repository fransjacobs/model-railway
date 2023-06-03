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
package jcs.ui.layout.pathfinding;

import jcs.entities.enums.Orientation;
import jcs.entities.enums.TileType;
import jcs.ui.layout.Tile;
import jcs.ui.layout.tiles.TileFactory;
import jcs.ui.layout.tiles.enums.Direction;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;

public class NodeTest {

  public NodeTest() {
  }

  @Before
  public void setUp() {

  }

  @After
  public void tearDown() {
  }

  @Test
  public void testGetX() {
    System.out.println("getX");
    Tile tile = TileFactory.createTile(TileType.STRAIGHT, Orientation.EAST, Direction.CENTER, 180, 100, false);
    Node instance = new Node(tile);
    int expResult = 180;
    int result = instance.getX();
    assertEquals(expResult, result);
  }

  @Test
  public void testGetGridX() {
    System.out.println("getGridX");
    Tile tile = TileFactory.createTile(TileType.STRAIGHT, Orientation.EAST, Direction.CENTER, 100, 100, false);
    Node instance = new Node(tile);
    int expResult = 2;
    int result = instance.getGridX();
    assertEquals(expResult, result);

    tile = TileFactory.createTile(TileType.STRAIGHT, Orientation.EAST, Direction.CENTER, 220, 220, false);
    instance = new Node(tile);

    expResult = 5;
    result = instance.getGridX();
    assertEquals(expResult, result);
  }

  @Test
  public void testGetY() {
    System.out.println("getY");
    Tile tile = TileFactory.createTile(TileType.STRAIGHT, Orientation.EAST, Direction.CENTER, 180, 100, false);
    Node instance = new Node(tile);
    int expResult = 100;
    int result = instance.getY();
    assertEquals(expResult, result);
  }

  @Test
  public void testGetGridY() {
    System.out.println("getGridY");
    Tile tile = TileFactory.createTile(TileType.STRAIGHT, Orientation.WEST, Direction.CENTER, 100, 140, false);
    Node instance = new Node(tile);
    int expResult = 3;
    int result = instance.getGridY();
    assertEquals(expResult, result);
  }

  @Test
  public void testCanTraverseToHorizontalStraight() {
    System.out.println("canTraverseToHorizontalStraight");
    Tile main = TileFactory.createTile(TileType.STRAIGHT, Orientation.EAST, Direction.CENTER, 100, 100, false);
    Node instance = new Node(main);

    Node west = new Node(TileFactory.createTile(TileType.STRAIGHT, Orientation.WEST, Direction.CENTER, 60, 100, false));
    Node east = new Node(TileFactory.createTile(TileType.STRAIGHT, Orientation.EAST, Direction.CENTER, 140, 100, false));
    Node north = new Node(TileFactory.createTile(TileType.STRAIGHT, Orientation.NORTH, Direction.CENTER, 100, 60, false));
    Node south = new Node(TileFactory.createTile(TileType.STRAIGHT, Orientation.SOUTH, Direction.CENTER, 100, 140, false));

    boolean expResult = true;
    boolean result = instance.canTravelTo(west);
    assertEquals(expResult, result);
    result = instance.canTravelTo(east);
    assertEquals(expResult, result);

    expResult = false;
    result = instance.canTravelTo(north);
    assertEquals(expResult, result);
    result = instance.canTravelTo(south);
    assertEquals(expResult, result);
  }

  @Test
  public void testCanTraverseToVerticalStraight() {
    System.out.println("canTraverseToHorizontalStraight");
    Tile main = TileFactory.createTile(TileType.STRAIGHT, Orientation.NORTH, Direction.CENTER, 100, 100, false);
    Node instance = new Node(main);

    Node west = new Node(TileFactory.createTile(TileType.STRAIGHT, Orientation.WEST, Direction.CENTER, 60, 100, false));
    Node east = new Node(TileFactory.createTile(TileType.STRAIGHT, Orientation.EAST, Direction.CENTER, 140, 100, false));
    Node north = new Node(TileFactory.createTile(TileType.STRAIGHT, Orientation.NORTH, Direction.CENTER, 100, 60, false));
    Node south = new Node(TileFactory.createTile(TileType.STRAIGHT, Orientation.SOUTH, Direction.CENTER, 100, 140, false));

    boolean expResult = false;
    boolean result = instance.canTravelTo(west);
    assertEquals(expResult, result);
    result = instance.canTravelTo(east);
    assertEquals(expResult, result);

    expResult = true;
    result = instance.canTravelTo(north);
    assertEquals(expResult, result);
    result = instance.canTravelTo(south);
    assertEquals(expResult, result);
  }

  @Test
  public void testCanTraverseToHorizontalBlock() {
    System.out.println("canTraverseToHorizontalBlock");
    Tile main = TileFactory.createTile(TileType.STRAIGHT, Orientation.EAST, Direction.CENTER, 220, 220, false);
    Node instance = new Node(main);

    Node west = new Node(TileFactory.createTile(TileType.BLOCK, Orientation.WEST, Direction.CENTER, 140, 220, false));
    Node east = new Node(TileFactory.createTile(TileType.BLOCK, Orientation.EAST, Direction.CENTER, 300, 220, false));

    boolean expResult = true;
    boolean result = instance.canTravelTo(west);
    assertEquals(expResult, result);
    result = instance.canTravelTo(east);
    assertEquals(expResult, result);
  }

  @Test
  public void testCanTraverseToVerticalBlock() {
    System.out.println("canTraverseToVerticalBlock");
    Tile main = TileFactory.createTile(TileType.STRAIGHT, Orientation.NORTH, Direction.CENTER, 220, 220, false);
    Node instance = new Node(main);

    Node north = new Node(TileFactory.createTile(TileType.BLOCK, Orientation.NORTH, Direction.CENTER, 220, 140, false));
    Node south = new Node(TileFactory.createTile(TileType.BLOCK, Orientation.SOUTH, Direction.CENTER, 220, 300, false));

    boolean expResult = true;
    boolean result = instance.canTravelTo(north);
    assertEquals(expResult, result);
    result = instance.canTravelTo(south);
    assertEquals(expResult, result);
  }

  @Test
  public void testCanTraverseToHorizontalCurved() {
    System.out.println("canTraverseToHorizontalCurved");
    Tile main = TileFactory.createTile(TileType.STRAIGHT, Orientation.EAST, Direction.CENTER, 100, 100, false);
    Node instance = new Node(main);

    Node west1 = new Node(TileFactory.createTile(TileType.CURVED, Orientation.NORTH, Direction.CENTER, 60, 100, false));
    Node west2 = new Node(TileFactory.createTile(TileType.CURVED, Orientation.EAST, Direction.CENTER, 60, 100, false));
    Node west3 = new Node(TileFactory.createTile(TileType.CURVED, Orientation.WEST, Direction.CENTER, 60, 100, false));
    Node west4 = new Node(TileFactory.createTile(TileType.CURVED, Orientation.SOUTH, Direction.CENTER, 60, 100, false));

    Node east1 = new Node(TileFactory.createTile(TileType.CURVED, Orientation.SOUTH, Direction.CENTER, 140, 100, false));
    Node east2 = new Node(TileFactory.createTile(TileType.CURVED, Orientation.WEST, Direction.CENTER, 140, 100, false));
    Node east3 = new Node(TileFactory.createTile(TileType.CURVED, Orientation.NORTH, Direction.CENTER, 140, 100, false));
    Node east4 = new Node(TileFactory.createTile(TileType.CURVED, Orientation.EAST, Direction.CENTER, 140, 100, false));

    boolean expResult = true;
    boolean result = instance.canTravelTo(west1);
    assertEquals(expResult, result);
    result = instance.canTravelTo(west2);
    assertEquals(expResult, result);
    result = instance.canTravelTo(east1);
    assertEquals(expResult, result);
    result = instance.canTravelTo(east2);
    assertEquals(expResult, result);

    expResult = false;
    result = instance.canTravelTo(west3);
    assertEquals(expResult, result);
    result = instance.canTravelTo(west4);
    assertEquals(expResult, result);
    result = instance.canTravelTo(east3);
    assertEquals(expResult, result);
    result = instance.canTravelTo(east4);
    assertEquals(expResult, result);
  }

  @Test
  public void testCanTraverseToVerticalCurved() {
    System.out.println("canTraverseToVerticalCurved");
    Tile main = TileFactory.createTile(TileType.STRAIGHT, Orientation.NORTH, Direction.CENTER, 100, 100, false);
    Node instance = new Node(main);

    Node north1 = new Node(TileFactory.createTile(TileType.CURVED, Orientation.EAST, Direction.CENTER, 100, 60, false));
    Node north2 = new Node(TileFactory.createTile(TileType.CURVED, Orientation.SOUTH, Direction.CENTER, 100, 60, false));
    Node north3 = new Node(TileFactory.createTile(TileType.CURVED, Orientation.WEST, Direction.CENTER, 100, 60, false));
    Node north4 = new Node(TileFactory.createTile(TileType.CURVED, Orientation.NORTH, Direction.CENTER, 100, 60, false));

    Node south1 = new Node(TileFactory.createTile(TileType.CURVED, Orientation.WEST, Direction.CENTER, 100, 140, false));
    Node south2 = new Node(TileFactory.createTile(TileType.CURVED, Orientation.NORTH, Direction.CENTER, 100, 140, false));
    Node south3 = new Node(TileFactory.createTile(TileType.CURVED, Orientation.EAST, Direction.CENTER, 100, 140, false));
    Node south4 = new Node(TileFactory.createTile(TileType.CURVED, Orientation.SOUTH, Direction.CENTER, 100, 140, false));

    boolean expResult = true;
    boolean result = instance.canTravelTo(north1);
    assertEquals(expResult, result);
    result = instance.canTravelTo(north2);
    assertEquals(expResult, result);
    result = instance.canTravelTo(south1);
    assertEquals(expResult, result);
    result = instance.canTravelTo(south2);
    assertEquals(expResult, result);

    expResult = false;
    result = instance.canTravelTo(north3);
    assertEquals(expResult, result);
    result = instance.canTravelTo(north4);
    assertEquals(expResult, result);

    result = instance.canTravelTo(south3);
    assertEquals(expResult, result);
    result = instance.canTravelTo(south4);
    assertEquals(expResult, result);
  }

  @Test
  public void testCanTraverseToHorizontalEnd() {
    System.out.println("canTraverseToHorizontalEnd");
    Tile main = TileFactory.createTile(TileType.STRAIGHT, Orientation.EAST, Direction.CENTER, 100, 100, false);
    Node instance = new Node(main);

    Node east = new Node(TileFactory.createTile(TileType.END, Orientation.EAST, Direction.CENTER, 140, 100, false));
    Node east1 = new Node(TileFactory.createTile(TileType.END, Orientation.EAST, Direction.CENTER, 60, 100, false));

    Node west = new Node(TileFactory.createTile(TileType.END, Orientation.WEST, Direction.CENTER, 60, 100, false));
    Node west1 = new Node(TileFactory.createTile(TileType.END, Orientation.WEST, Direction.CENTER, 140, 100, false));

    boolean expResult = true;
    boolean result = instance.canTravelTo(east);
    assertEquals(expResult, result);
    result = instance.canTravelTo(west);
    assertEquals(expResult, result);

    expResult = false;
    result = instance.canTravelTo(east1);
    assertEquals(expResult, result);
    result = instance.canTravelTo(west1);
    assertEquals(expResult, result);
  }

  @Test
  public void testCanTraverseToVerticalEnd() {
    System.out.println("canTraverseToVerticalEnd");
    Tile main = TileFactory.createTile(TileType.STRAIGHT, Orientation.SOUTH, Direction.CENTER, 100, 100, false);
    Node instance = new Node(main);

    Node north = new Node(TileFactory.createTile(TileType.END, Orientation.NORTH, Direction.CENTER, 100, 60, false));
    Node north1 = new Node(TileFactory.createTile(TileType.END, Orientation.NORTH, Direction.CENTER, 100, 140, false));

    Node south = new Node(TileFactory.createTile(TileType.END, Orientation.SOUTH, Direction.CENTER, 100, 140, false));
    Node south1 = new Node(TileFactory.createTile(TileType.END, Orientation.SOUTH, Direction.CENTER, 100, 60, false));

    boolean expResult = true;
    boolean result = instance.canTravelTo(north);
    assertEquals(expResult, result);
    result = instance.canTravelTo(south);
    assertEquals(expResult, result);

    expResult = false;
    result = instance.canTravelTo(north1);
    assertEquals(expResult, result);
    result = instance.canTravelTo(south1);
    assertEquals(expResult, result);
  }

  @Test
  public void testCanTraverseToHorizontalSwitch() {
    System.out.println("canTraverseToHorizontalSwitch");
    Tile main = TileFactory.createTile(TileType.STRAIGHT, Orientation.EAST, Direction.CENTER, 220, 220, false);
    Node instance = new Node(main);

    Node west1 = new Node(TileFactory.createTile(TileType.SWITCH, Orientation.EAST, Direction.LEFT, 180, 220, false));
    Node west2 = new Node(TileFactory.createTile(TileType.SWITCH, Orientation.WEST, Direction.LEFT, 180, 220, false));
    Node west3 = new Node(TileFactory.createTile(TileType.SWITCH, Orientation.NORTH, Direction.LEFT, 180, 220, false));
    Node west4 = new Node(TileFactory.createTile(TileType.SWITCH, Orientation.EAST, Direction.RIGHT, 180, 220, false));
    Node west5 = new Node(TileFactory.createTile(TileType.SWITCH, Orientation.WEST, Direction.RIGHT, 180, 220, false));
    Node west6 = new Node(TileFactory.createTile(TileType.SWITCH, Orientation.SOUTH, Direction.RIGHT, 180, 220, false));
    Node west7 = new Node(TileFactory.createTile(TileType.SWITCH, Orientation.NORTH, Direction.RIGHT, 180, 220, false));

    Node east1 = new Node(TileFactory.createTile(TileType.SWITCH, Orientation.EAST, Direction.RIGHT, 260, 220, false));
    Node east2 = new Node(TileFactory.createTile(TileType.SWITCH, Orientation.WEST, Direction.RIGHT, 260, 220, false));
    Node east3 = new Node(TileFactory.createTile(TileType.SWITCH, Orientation.NORTH, Direction.RIGHT, 260, 220, false));
    Node east4 = new Node(TileFactory.createTile(TileType.SWITCH, Orientation.WEST, Direction.LEFT, 260, 220, false));
    Node east5 = new Node(TileFactory.createTile(TileType.SWITCH, Orientation.EAST, Direction.LEFT, 260, 220, false));
    Node east6 = new Node(TileFactory.createTile(TileType.SWITCH, Orientation.SOUTH, Direction.LEFT, 260, 220, false));
    Node east7 = new Node(TileFactory.createTile(TileType.SWITCH, Orientation.NORTH, Direction.LEFT, 260, 220, false));

    boolean expResult = true;
    boolean result = instance.canTravelTo(west1);
    assertEquals(expResult, result);
    result = instance.canTravelTo(west2);
    assertEquals(expResult, result);
    result = instance.canTravelTo(west3);
    assertEquals(expResult, result);
    result = instance.canTravelTo(west4);
    assertEquals(expResult, result);
    result = instance.canTravelTo(west5);
    assertEquals(expResult, result);
    result = instance.canTravelTo(west6);
    assertEquals(expResult, result);

    result = instance.canTravelTo(east1);
    assertEquals(expResult, result);
    result = instance.canTravelTo(east2);
    assertEquals(expResult, result);
    result = instance.canTravelTo(east3);
    assertEquals(expResult, result);
    result = instance.canTravelTo(east4);
    assertEquals(expResult, result);
    result = instance.canTravelTo(east5);
    assertEquals(expResult, result);
    result = instance.canTravelTo(east6);
    assertEquals(expResult, result);

    expResult = false;
    result = instance.canTravelTo(west7);
    assertEquals(expResult, result);
    result = instance.canTravelTo(east7);
    assertEquals(expResult, result);
  }

  @Test
  public void testCanTraverseToVerticalSwitch() {
    System.out.println("canTraverseToVerticalSwitch");
    Tile main = TileFactory.createTile(TileType.STRAIGHT, Orientation.NORTH, Direction.CENTER, 220, 220, false);
    Node instance = new Node(main);

    Node north1 = new Node(TileFactory.createTile(TileType.SWITCH, Orientation.SOUTH, Direction.LEFT, 220, 180, false));
    Node north2 = new Node(TileFactory.createTile(TileType.SWITCH, Orientation.NORTH, Direction.LEFT, 220, 180, false));
    Node north3 = new Node(TileFactory.createTile(TileType.SWITCH, Orientation.EAST, Direction.LEFT, 220, 180, false));
    Node north4 = new Node(TileFactory.createTile(TileType.SWITCH, Orientation.SOUTH, Direction.RIGHT, 220, 180, false));
    Node north5 = new Node(TileFactory.createTile(TileType.SWITCH, Orientation.NORTH, Direction.RIGHT, 220, 180, false));
    Node north6 = new Node(TileFactory.createTile(TileType.SWITCH, Orientation.WEST, Direction.RIGHT, 220, 180, false));
    Node north7 = new Node(TileFactory.createTile(TileType.SWITCH, Orientation.WEST, Direction.LEFT, 220, 180, false));

    Node south1 = new Node(TileFactory.createTile(TileType.SWITCH, Orientation.SOUTH, Direction.RIGHT, 220, 260, false));
    Node south2 = new Node(TileFactory.createTile(TileType.SWITCH, Orientation.NORTH, Direction.RIGHT, 220, 260, false));
    Node south3 = new Node(TileFactory.createTile(TileType.SWITCH, Orientation.EAST, Direction.RIGHT, 220, 260, false));
    Node south4 = new Node(TileFactory.createTile(TileType.SWITCH, Orientation.SOUTH, Direction.LEFT, 220, 260, false));
    Node south5 = new Node(TileFactory.createTile(TileType.SWITCH, Orientation.NORTH, Direction.LEFT, 220, 260, false));
    Node south6 = new Node(TileFactory.createTile(TileType.SWITCH, Orientation.WEST, Direction.LEFT, 220, 260, false));
    Node south7 = new Node(TileFactory.createTile(TileType.SWITCH, Orientation.WEST, Direction.RIGHT, 220, 260, false));

    boolean expResult = true;
    boolean result = instance.canTravelTo(north1);
    assertEquals(expResult, result);
    result = instance.canTravelTo(north2);
    assertEquals(expResult, result);
    result = instance.canTravelTo(north3);
    assertEquals(expResult, result);
    result = instance.canTravelTo(north4);
    assertEquals(expResult, result);
    result = instance.canTravelTo(north5);
    assertEquals(expResult, result);
    result = instance.canTravelTo(north6);
    assertEquals(expResult, result);

    result = instance.canTravelTo(south1);
    assertEquals(expResult, result);
    result = instance.canTravelTo(south2);
    assertEquals(expResult, result);
    result = instance.canTravelTo(south3);
    assertEquals(expResult, result);
    result = instance.canTravelTo(south4);
    assertEquals(expResult, result);
    result = instance.canTravelTo(south5);
    assertEquals(expResult, result);
    result = instance.canTravelTo(south6);
    assertEquals(expResult, result);

    expResult = false;
    result = instance.canTravelTo(north7);
    assertEquals(expResult, result);
    result = instance.canTravelTo(south7);
    assertEquals(expResult, result);

  }

}
