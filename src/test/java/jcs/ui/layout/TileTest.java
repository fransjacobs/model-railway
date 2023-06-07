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
package jcs.ui.layout;

import jcs.entities.enums.Orientation;
import jcs.entities.enums.TileType;

import jcs.ui.layout.tiles.TileFactory;
import jcs.ui.layout.tiles.enums.Direction;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author frans
 */
public class TileTest {

  public TileTest() {
  }

  @BeforeClass
  public static void setUpClass() {
  }

  @AfterClass
  public static void tearDownClass() {
  }

  @Before
  public void setUp() {
  }

  @After
  public void tearDown() {
  }

  @Test
  public void testgetCenterX() {
    System.out.println("getCenterX");
    Tile instance = TileFactory.createTile(TileType.STRAIGHT, Orientation.EAST, Direction.CENTER, 180, 100, false);
    int expResult = 180;
    int result = instance.getCenterX();
    assertEquals(expResult, result);
  }

  @Test
  public void testGetGridX() {
    System.out.println("getGridX");
    Tile instance = TileFactory.createTile(TileType.STRAIGHT, Orientation.EAST, Direction.CENTER, 100, 100, false);
    int expResult = 2;
    int result = instance.getGridX();
    assertEquals(expResult, result);

    instance = TileFactory.createTile(TileType.STRAIGHT, Orientation.EAST, Direction.CENTER, 220, 220, false);

    expResult = 5;
    result = instance.getGridX();
    assertEquals(expResult, result);
  }

  @Test
  public void testgetCenterY() {
    System.out.println("getCenterY");
    Tile instance = TileFactory.createTile(TileType.STRAIGHT, Orientation.EAST, Direction.CENTER, 180, 100, false);
    int expResult = 100;
    int result = instance.getCenterY();
    assertEquals(expResult, result);
  }

  @Test
  public void testGetGridY() {
    System.out.println("getGridY");
    Tile instance = TileFactory.createTile(TileType.STRAIGHT, Orientation.WEST, Direction.CENTER, 100, 140, false);
    int expResult = 3;
    int result = instance.getGridY();
    assertEquals(expResult, result);
  }

  @Test
  public void testCanTraverseToHorizontalStraight() {
    System.out.println("canTraverseToHorizontalStraight");
    Tile instance = TileFactory.createTile(TileType.STRAIGHT, Orientation.EAST, Direction.CENTER, 100, 100, false);

    Tile west = TileFactory.createTile(TileType.STRAIGHT, Orientation.WEST, Direction.CENTER, 60, 100, false);
    Tile east = TileFactory.createTile(TileType.STRAIGHT, Orientation.EAST, Direction.CENTER, 140, 100, false);
    Tile north = TileFactory.createTile(TileType.STRAIGHT, Orientation.NORTH, Direction.CENTER, 100, 60, false);
    Tile south = TileFactory.createTile(TileType.STRAIGHT, Orientation.SOUTH, Direction.CENTER, 100, 140, false);

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
    Tile instance = TileFactory.createTile(TileType.STRAIGHT, Orientation.NORTH, Direction.CENTER, 100, 100, false);

    Tile west = TileFactory.createTile(TileType.STRAIGHT, Orientation.WEST, Direction.CENTER, 60, 100, false);
    Tile east = TileFactory.createTile(TileType.STRAIGHT, Orientation.EAST, Direction.CENTER, 140, 100, false);
    Tile north = TileFactory.createTile(TileType.STRAIGHT, Orientation.NORTH, Direction.CENTER, 100, 60, false);
    Tile south = TileFactory.createTile(TileType.STRAIGHT, Orientation.SOUTH, Direction.CENTER, 100, 140, false);

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
    Tile instance = TileFactory.createTile(TileType.STRAIGHT, Orientation.EAST, Direction.CENTER, 220, 220, false);

    Tile west = TileFactory.createTile(TileType.BLOCK, Orientation.WEST, Direction.CENTER, 140, 220, false);
    Tile east = TileFactory.createTile(TileType.BLOCK, Orientation.EAST, Direction.CENTER, 300, 220, false);

    boolean expResult = true;
    boolean result = instance.canTraverseTo(west);
    assertEquals(expResult, result);
    result = instance.canTraverseTo(east);
    assertEquals(expResult, result);
  }

  @Test
  public void testCanTraverseToVerticalBlock() {
    System.out.println("canTraverseToVerticalBlock");
    Tile instance = TileFactory.createTile(TileType.STRAIGHT, Orientation.NORTH, Direction.CENTER, 220, 220, false);

    Tile north = TileFactory.createTile(TileType.BLOCK, Orientation.NORTH, Direction.CENTER, 220, 140, false);
    Tile south = TileFactory.createTile(TileType.BLOCK, Orientation.SOUTH, Direction.CENTER, 220, 300, false);

    boolean expResult = true;
    boolean result = instance.canTraverseTo(north);
    assertEquals(expResult, result);
    result = instance.canTraverseTo(south);
    assertEquals(expResult, result);
  }

  @Test
  public void testCanTraverseToHorizontalCurved() {
    System.out.println("canTraverseToHorizontalCurved");
    Tile instance = TileFactory.createTile(TileType.STRAIGHT, Orientation.EAST, Direction.CENTER, 100, 100, false);

    Tile west1 = TileFactory.createTile(TileType.CURVED, Orientation.NORTH, Direction.CENTER, 60, 100, false);
    Tile west2 = TileFactory.createTile(TileType.CURVED, Orientation.EAST, Direction.CENTER, 60, 100, false);
    Tile west3 = TileFactory.createTile(TileType.CURVED, Orientation.WEST, Direction.CENTER, 60, 100, false);
    Tile west4 = TileFactory.createTile(TileType.CURVED, Orientation.SOUTH, Direction.CENTER, 60, 100, false);

    Tile east1 = TileFactory.createTile(TileType.CURVED, Orientation.SOUTH, Direction.CENTER, 140, 100, false);
    Tile east2 = TileFactory.createTile(TileType.CURVED, Orientation.WEST, Direction.CENTER, 140, 100, false);
    Tile east3 = TileFactory.createTile(TileType.CURVED, Orientation.NORTH, Direction.CENTER, 140, 100, false);
    Tile east4 = TileFactory.createTile(TileType.CURVED, Orientation.EAST, Direction.CENTER, 140, 100, false);

    boolean expResult = true;
    boolean result = instance.canTraverseTo(west1);
    assertEquals(expResult, result);
    result = instance.canTraverseTo(west2);
    assertEquals(expResult, result);
    result = instance.canTraverseTo(east1);
    assertEquals(expResult, result);
    result = instance.canTraverseTo(east2);
    assertEquals(expResult, result);

    expResult = false;
    result = instance.canTraverseTo(west3);
    assertEquals(expResult, result);
    result = instance.canTraverseTo(west4);
    assertEquals(expResult, result);
    result = instance.canTraverseTo(east3);
    assertEquals(expResult, result);
    result = instance.canTraverseTo(east4);
    assertEquals(expResult, result);
  }

  @Test
  public void testCanTraverseToVerticalCurved() {
    System.out.println("canTraverseToVerticalCurved");
    Tile instance = TileFactory.createTile(TileType.STRAIGHT, Orientation.NORTH, Direction.CENTER, 100, 100, false);

    Tile north1 = TileFactory.createTile(TileType.CURVED, Orientation.EAST, Direction.CENTER, 100, 60, false);
    Tile north2 = TileFactory.createTile(TileType.CURVED, Orientation.SOUTH, Direction.CENTER, 100, 60, false);
    Tile north3 = TileFactory.createTile(TileType.CURVED, Orientation.WEST, Direction.CENTER, 100, 60, false);
    Tile north4 = TileFactory.createTile(TileType.CURVED, Orientation.NORTH, Direction.CENTER, 100, 60, false);

    Tile south1 = TileFactory.createTile(TileType.CURVED, Orientation.WEST, Direction.CENTER, 100, 140, false);
    Tile south2 = TileFactory.createTile(TileType.CURVED, Orientation.NORTH, Direction.CENTER, 100, 140, false);
    Tile south3 = TileFactory.createTile(TileType.CURVED, Orientation.EAST, Direction.CENTER, 100, 140, false);
    Tile south4 = TileFactory.createTile(TileType.CURVED, Orientation.SOUTH, Direction.CENTER, 100, 140, false);

    boolean expResult = true;
    boolean result = instance.canTraverseTo(north1);
    assertEquals(expResult, result);
    result = instance.canTraverseTo(north2);
    assertEquals(expResult, result);
    result = instance.canTraverseTo(south1);
    assertEquals(expResult, result);
    result = instance.canTraverseTo(south2);
    assertEquals(expResult, result);

    expResult = false;
    result = instance.canTraverseTo(north3);
    assertEquals(expResult, result);
    result = instance.canTraverseTo(north4);
    assertEquals(expResult, result);

    result = instance.canTraverseTo(south3);
    assertEquals(expResult, result);
    result = instance.canTraverseTo(south4);
    assertEquals(expResult, result);
  }

  @Test
  public void testCanTraverseToHorizontalEnd() {
    System.out.println("canTraverseToHorizontalEnd");
    Tile instance = TileFactory.createTile(TileType.STRAIGHT, Orientation.EAST, Direction.CENTER, 100, 100, false);

    Tile east = TileFactory.createTile(TileType.END, Orientation.EAST, Direction.CENTER, 140, 100, false);
    Tile east1 = TileFactory.createTile(TileType.END, Orientation.EAST, Direction.CENTER, 60, 100, false);

    Tile west = TileFactory.createTile(TileType.END, Orientation.WEST, Direction.CENTER, 60, 100, false);
    Tile west1 = TileFactory.createTile(TileType.END, Orientation.WEST, Direction.CENTER, 140, 100, false);

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
    Tile instance = TileFactory.createTile(TileType.STRAIGHT, Orientation.SOUTH, Direction.CENTER, 100, 100, false);

    Tile north = TileFactory.createTile(TileType.END, Orientation.NORTH, Direction.CENTER, 100, 60, false);
    Tile north1 = TileFactory.createTile(TileType.END, Orientation.NORTH, Direction.CENTER, 100, 140, false);

    Tile south = TileFactory.createTile(TileType.END, Orientation.SOUTH, Direction.CENTER, 100, 140, false);
    Tile south1 = TileFactory.createTile(TileType.END, Orientation.SOUTH, Direction.CENTER, 100, 60, false);

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

  @Test
  public void testCanTraverseToHorizontalSwitch() {
    System.out.println("canTraverseToHorizontalSwitch");
    Tile instance = TileFactory.createTile(TileType.STRAIGHT, Orientation.EAST, Direction.CENTER, 220, 220, false);

    Tile west1 = TileFactory.createTile(TileType.SWITCH, Orientation.EAST, Direction.LEFT, 180, 220, false);
    Tile west2 = TileFactory.createTile(TileType.SWITCH, Orientation.WEST, Direction.LEFT, 180, 220, false);
    Tile west3 = TileFactory.createTile(TileType.SWITCH, Orientation.NORTH, Direction.LEFT, 180, 220, false);
    Tile west4 = TileFactory.createTile(TileType.SWITCH, Orientation.EAST, Direction.RIGHT, 180, 220, false);
    Tile west5 = TileFactory.createTile(TileType.SWITCH, Orientation.WEST, Direction.RIGHT, 180, 220, false);
    Tile west6 = TileFactory.createTile(TileType.SWITCH, Orientation.SOUTH, Direction.RIGHT, 180, 220, false);
    Tile west7 = TileFactory.createTile(TileType.SWITCH, Orientation.NORTH, Direction.RIGHT, 180, 220, false);

    Tile east1 = TileFactory.createTile(TileType.SWITCH, Orientation.EAST, Direction.RIGHT, 260, 220, false);
    Tile east2 = TileFactory.createTile(TileType.SWITCH, Orientation.WEST, Direction.RIGHT, 260, 220, false);
    Tile east3 = TileFactory.createTile(TileType.SWITCH, Orientation.NORTH, Direction.RIGHT, 260, 220, false);
    Tile east4 = TileFactory.createTile(TileType.SWITCH, Orientation.WEST, Direction.LEFT, 260, 220, false);
    Tile east5 = TileFactory.createTile(TileType.SWITCH, Orientation.EAST, Direction.LEFT, 260, 220, false);
    Tile east6 = TileFactory.createTile(TileType.SWITCH, Orientation.SOUTH, Direction.LEFT, 260, 220, false);
    Tile east7 = TileFactory.createTile(TileType.SWITCH, Orientation.NORTH, Direction.LEFT, 260, 220, false);

    boolean expResult = true;
    boolean result = instance.canTraverseTo(west1);
    assertEquals(expResult, result);
    result = instance.canTraverseTo(west2);
    assertEquals(expResult, result);
    result = instance.canTraverseTo(west3);
    assertEquals(expResult, result);
    result = instance.canTraverseTo(west4);
    assertEquals(expResult, result);
    result = instance.canTraverseTo(west5);
    assertEquals(expResult, result);
    result = instance.canTraverseTo(west6);
    assertEquals(expResult, result);

    result = instance.canTraverseTo(east1);
    assertEquals(expResult, result);
    result = instance.canTraverseTo(east2);
    assertEquals(expResult, result);
    result = instance.canTraverseTo(east3);
    assertEquals(expResult, result);
    result = instance.canTraverseTo(east4);
    assertEquals(expResult, result);
    result = instance.canTraverseTo(east5);
    assertEquals(expResult, result);
    result = instance.canTraverseTo(east6);
    assertEquals(expResult, result);

    expResult = false;
    result = instance.canTraverseTo(west7);
    assertEquals(expResult, result);
    result = instance.canTraverseTo(east7);
    assertEquals(expResult, result);
  }

  @Test
  public void testCanTraverseToVerticalSwitch() {
    System.out.println("canTraverseToVerticalSwitch");
    Tile instance = TileFactory.createTile(TileType.STRAIGHT, Orientation.NORTH, Direction.CENTER, 220, 220, false);

    Tile north1 = TileFactory.createTile(TileType.SWITCH, Orientation.SOUTH, Direction.LEFT, 220, 180, false);
    Tile north2 = TileFactory.createTile(TileType.SWITCH, Orientation.NORTH, Direction.LEFT, 220, 180, false);
    Tile north3 = TileFactory.createTile(TileType.SWITCH, Orientation.EAST, Direction.LEFT, 220, 180, false);
    Tile north4 = TileFactory.createTile(TileType.SWITCH, Orientation.SOUTH, Direction.RIGHT, 220, 180, false);
    Tile north5 = TileFactory.createTile(TileType.SWITCH, Orientation.NORTH, Direction.RIGHT, 220, 180, false);
    Tile north6 = TileFactory.createTile(TileType.SWITCH, Orientation.WEST, Direction.RIGHT, 220, 180, false);
    Tile north7 = TileFactory.createTile(TileType.SWITCH, Orientation.WEST, Direction.LEFT, 220, 180, false);

    Tile south1 = TileFactory.createTile(TileType.SWITCH, Orientation.SOUTH, Direction.RIGHT, 220, 260, false);
    Tile south2 = TileFactory.createTile(TileType.SWITCH, Orientation.NORTH, Direction.RIGHT, 220, 260, false);
    Tile south3 = TileFactory.createTile(TileType.SWITCH, Orientation.EAST, Direction.RIGHT, 220, 260, false);
    Tile south4 = TileFactory.createTile(TileType.SWITCH, Orientation.SOUTH, Direction.LEFT, 220, 260, false);
    Tile south5 = TileFactory.createTile(TileType.SWITCH, Orientation.NORTH, Direction.LEFT, 220, 260, false);
    Tile south6 = TileFactory.createTile(TileType.SWITCH, Orientation.WEST, Direction.LEFT, 220, 260, false);
    Tile south7 = TileFactory.createTile(TileType.SWITCH, Orientation.WEST, Direction.RIGHT, 220, 260, false);

    boolean expResult = true;
    boolean result = instance.canTraverseTo(north1);
    assertEquals(expResult, result);
    result = instance.canTraverseTo(north2);
    assertEquals(expResult, result);
    result = instance.canTraverseTo(north3);
    assertEquals(expResult, result);
    result = instance.canTraverseTo(north4);
    assertEquals(expResult, result);
    result = instance.canTraverseTo(north5);
    assertEquals(expResult, result);
    result = instance.canTraverseTo(north6);
    assertEquals(expResult, result);

    result = instance.canTraverseTo(south1);
    assertEquals(expResult, result);
    result = instance.canTraverseTo(south2);
    assertEquals(expResult, result);
    result = instance.canTraverseTo(south3);
    assertEquals(expResult, result);
    result = instance.canTraverseTo(south4);
    assertEquals(expResult, result);
    result = instance.canTraverseTo(south5);
    assertEquals(expResult, result);
    result = instance.canTraverseTo(south6);
    assertEquals(expResult, result);

    expResult = false;
    result = instance.canTraverseTo(north7);
    assertEquals(expResult, result);
    result = instance.canTraverseTo(south7);
    assertEquals(expResult, result);

  }

}
