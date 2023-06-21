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

}
