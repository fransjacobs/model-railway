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

/**
 *
 * @author frans
 */
public class NodeTest {

  private Tile s1h, s1v, s2, s3, s4, s5, s6;

  public NodeTest() {
  }

  @Before
  public void setUp() {

    s1h = TileFactory.createTile(TileType.STRAIGHT, Orientation.EAST, Direction.CENTER, 100, 100, false);
    s1v = TileFactory.createTile(TileType.STRAIGHT, Orientation.SOUTH, Direction.CENTER, 100, 100, false);

    s2 = TileFactory.createTile(TileType.STRAIGHT, Orientation.NORTH, Direction.CENTER, 100, 60, false);
    s3 = TileFactory.createTile(TileType.STRAIGHT, Orientation.WEST, Direction.CENTER, 100, 140, false);

    s4 = TileFactory.createTile(TileType.STRAIGHT, Orientation.EAST, Direction.CENTER, 60, 100, false);
    s5 = TileFactory.createTile(TileType.STRAIGHT, Orientation.WEST, Direction.CENTER, 140, 100, false);

    s6 = TileFactory.createTile(TileType.STRAIGHT, Orientation.EAST, Direction.CENTER, 180, 100, false);

  }

  @After
  public void tearDown() {
  }

  @Test
  public void testGetX() {
    System.out.println("getX");
    Node instance = new Node(s6);
    int expResult = 180;
    int result = instance.getX();
    assertEquals(expResult, result);
  }

  @Test
  public void testGetGridX() {
    System.out.println("getGridX");
    Node instance = new Node(s1h);
    int expResult = 2;
    int result = instance.getGridX();
    assertEquals(expResult, result);
  }

  @Test
  public void testGetY() {
    System.out.println("getY");
    Node instance = new Node(s6);
    int expResult = 100;
    int result = instance.getY();
    assertEquals(expResult, result);
  }

  @Test
  public void testGetGridY() {
    System.out.println("getGridY");
    Node instance = new Node(s3);
    int expResult = 3;
    int result = instance.getGridY();
    assertEquals(expResult, result);
  }

  /**
   * Test of canTraverseTo method, of class Node2.
   */
//  @Test
//  public void testCanTraverseToS2() {
//    System.out.println("canTraverseToS2");
//    Node2 other = new Node2(s2);
//    Node2 instance = new Node2(s1);
//    boolean expResult = false;
//    boolean result = instance.canTraverseTo(other);
//    assertEquals(expResult, result);
//  }
//  @Test
//  public void testCanTraverseToS3() {
//    System.out.println("canTraverseToS3");
//    Node2 other = new Node2(s3);
//    Node2 instance = new Node2(s1);
//    boolean expResult = false;
//    boolean result = instance.canTraverseTo(other);
//    assertEquals(expResult, result);
//  }
//  @Test
//  public void testCanTraverseToS4() {
//    System.out.println("canTraverseToS4");
//    Node2 other = new Node2(s4);
//    Node2 instance = new Node2(s1);
//    boolean expResult = true;
//    boolean result = instance.canTraverseTo(other);
//    assertEquals(expResult, result);
//  }
//  @Test
//  public void testCanTraverseToS5() {
//    System.out.println("canTraverseToS5");
//    Node2 other = new Node2(s5);
//    Node2 instance = new Node2(s1);
//    boolean expResult = true;
//    boolean result = instance.canTraverseTo(other);
//    assertEquals(expResult, result);
//  }
}
