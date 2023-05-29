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
    boolean result = instance.canTraverseTo(west);
    assertEquals(expResult, result);
    result = instance.canTraverseTo(east);
    assertEquals(expResult, result);

    expResult = false;
    result = instance.canTraverseTo(north);
    assertEquals(expResult, result);
    result = instance.canTraverseTo(south);
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
    boolean result = instance.canTraverseTo(west);
    assertEquals(expResult, result);
    result = instance.canTraverseTo(east);
    assertEquals(expResult, result);

    expResult = true;
    result = instance.canTraverseTo(north);
    assertEquals(expResult, result);
    result = instance.canTraverseTo(south);
    assertEquals(expResult, result);
  }

  @Test
  public void testCanTraverseToHorizontalBlock() {
    System.out.println("canTraverseToHorizontalBlock");
    Tile main = TileFactory.createTile(TileType.STRAIGHT, Orientation.EAST, Direction.CENTER, 220, 220, false);
    Node instance = new Node(main);

    Node west = new Node(TileFactory.createTile(TileType.BLOCK, Orientation.WEST, Direction.CENTER, 140, 220, false));
    Node east = new Node(TileFactory.createTile(TileType.BLOCK, Orientation.EAST, Direction.CENTER, 300, 220, false));
    Node north = new Node(TileFactory.createTile(TileType.BLOCK, Orientation.NORTH, Direction.CENTER, 220, 140, false));
    Node south = new Node(TileFactory.createTile(TileType.BLOCK, Orientation.SOUTH, Direction.CENTER, 200, 300, false));

    boolean expResult = true;
    boolean result = instance.canTraverseTo(west);
    assertEquals(expResult, result);
    result = instance.canTraverseTo(east);
    assertEquals(expResult, result);
  }

  @Test
  public void testCanTraverseToVerticalBlock() {
    System.out.println("canTraverseToVerticalBlock");
    Tile main = TileFactory.createTile(TileType.STRAIGHT, Orientation.EAST, Direction.CENTER, 220, 220, false);
    Node instance = new Node(main);

    Node west = new Node(TileFactory.createTile(TileType.BLOCK, Orientation.WEST, Direction.CENTER, 140, 220, false));
    Node east = new Node(TileFactory.createTile(TileType.BLOCK, Orientation.EAST, Direction.CENTER, 300, 220, false));
    Node north = new Node(TileFactory.createTile(TileType.BLOCK, Orientation.NORTH, Direction.CENTER, 220, 140, false));
    Node south = new Node(TileFactory.createTile(TileType.BLOCK, Orientation.SOUTH, Direction.CENTER, 200, 300, false));

    boolean expResult = true;
    boolean result = instance.canTraverseTo(west);
    assertEquals(expResult, result);
    result = instance.canTraverseTo(east);
    assertEquals(expResult, result);
  }

  @Test
  public void testCanTraverseToHorizontalCurved() {
    System.out.println("canTraverseToHorizontalCurved");
    Tile main = TileFactory.createTile(TileType.STRAIGHT, Orientation.EAST, Direction.CENTER, 100, 100, false);
    Node instance = new Node(main);

    Node north = new Node(TileFactory.createTile(TileType.CURVED, Orientation.NORTH, Direction.CENTER, 60, 100, false));
    Node east = new Node(TileFactory.createTile(TileType.CURVED, Orientation.EAST, Direction.CENTER, 60, 100, false));
    Node north2 = new Node(TileFactory.createTile(TileType.CURVED, Orientation.NORTH, Direction.CENTER, 140, 100, false));
    Node east2 = new Node(TileFactory.createTile(TileType.CURVED, Orientation.EAST, Direction.CENTER, 140, 100, false));

    Node west = new Node(TileFactory.createTile(TileType.CURVED, Orientation.WEST, Direction.CENTER, 140, 100, false));
    Node south = new Node(TileFactory.createTile(TileType.CURVED, Orientation.SOUTH, Direction.CENTER, 140, 100, false));
    Node west1 = new Node(TileFactory.createTile(TileType.CURVED, Orientation.WEST, Direction.CENTER, 60, 100, false));
    Node south1 = new Node(TileFactory.createTile(TileType.CURVED, Orientation.SOUTH, Direction.CENTER, 60, 100, false));

    Node north3 = new Node(TileFactory.createTile(TileType.CURVED, Orientation.NORTH, Direction.CENTER, 100, 60, false));

    boolean expResult = true;
    boolean result = instance.canTraverseTo(north);
    assertEquals(expResult, result);
    result = instance.canTraverseTo(east);
    assertEquals(expResult, result);

    expResult = false;
    result = instance.canTraverseTo(north2);
    assertEquals(expResult, result);
    result = instance.canTraverseTo(east2);
    assertEquals(expResult, result);

    expResult = true;
    result = instance.canTraverseTo(west);
    assertEquals(expResult, result);
    result = instance.canTraverseTo(south);
    assertEquals(expResult, result);

    expResult = false;
    result = instance.canTraverseTo(west1);
    assertEquals(expResult, result);
    result = instance.canTraverseTo(south1);
    assertEquals(expResult, result);

    result = instance.canTraverseTo(north3);
    assertEquals(expResult, result);
  }

  @Test
  public void testCanTraverseToVerticalCurved() {
    System.out.println("canTraverseToVerticalCurved");
    Tile main = TileFactory.createTile(TileType.STRAIGHT, Orientation.NORTH, Direction.CENTER, 100, 100, false);
    Node instance = new Node(main);

    Node north = new Node(TileFactory.createTile(TileType.CURVED, Orientation.NORTH, Direction.CENTER, 100, 140, false));
    Node west = new Node(TileFactory.createTile(TileType.CURVED, Orientation.WEST, Direction.CENTER, 100, 140, false));
    Node north1 = new Node(TileFactory.createTile(TileType.CURVED, Orientation.NORTH, Direction.CENTER, 100, 60, false));
    Node west1 = new Node(TileFactory.createTile(TileType.CURVED, Orientation.WEST, Direction.CENTER, 100, 60, false));

    Node east = new Node(TileFactory.createTile(TileType.CURVED, Orientation.EAST, Direction.CENTER, 100, 60, false));
    Node south = new Node(TileFactory.createTile(TileType.CURVED, Orientation.SOUTH, Direction.CENTER, 100, 60, false));
    Node east1 = new Node(TileFactory.createTile(TileType.CURVED, Orientation.EAST, Direction.CENTER, 100, 140, false));
    Node south1 = new Node(TileFactory.createTile(TileType.CURVED, Orientation.SOUTH, Direction.CENTER, 100, 140, false));

    Node north3 = new Node(TileFactory.createTile(TileType.CURVED, Orientation.NORTH, Direction.CENTER, 140, 100, false));

    boolean expResult = true;
    boolean result = instance.canTraverseTo(south);
    assertEquals(expResult, result);
    result = instance.canTraverseTo(east);
    assertEquals(expResult, result);

    expResult = false;
    result = instance.canTraverseTo(south1);
    assertEquals(expResult, result);
    result = instance.canTraverseTo(east1);
    assertEquals(expResult, result);

    expResult = true;
    result = instance.canTraverseTo(north);
    assertEquals(expResult, result);
    result = instance.canTraverseTo(west);
    assertEquals(expResult, result);

    expResult = false;
    result = instance.canTraverseTo(north1);
    assertEquals(expResult, result);
    result = instance.canTraverseTo(west1);
    assertEquals(expResult, result);

    result = instance.canTraverseTo(north3);
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
    boolean result = instance.canTraverseTo(east);
    assertEquals(expResult, result);
    result = instance.canTraverseTo(west);
    assertEquals(expResult, result);

    expResult = false;
    result = instance.canTraverseTo(east1);
    assertEquals(expResult, result);
    result = instance.canTraverseTo(west1);
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
    boolean result = instance.canTraverseTo(north);
    assertEquals(expResult, result);
    result = instance.canTraverseTo(south);
    assertEquals(expResult, result);

    expResult = false;
    result = instance.canTraverseTo(north1);
    assertEquals(expResult, result);
    result = instance.canTraverseTo(south1);
    assertEquals(expResult, result);
  }

}
