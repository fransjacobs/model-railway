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

import java.awt.Point;
import java.util.HashSet;
import java.util.Set;
import jcs.entities.enums.Orientation;
import jcs.entities.enums.TileType;

import jcs.ui.layout.tiles.TileFactory;
import jcs.ui.layout.tiles.enums.Direction;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author frans
 */
public class TileTest {

  public TileTest() {
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
  public void testgetCenterXZero() {
    System.out.println("getCenterX");
    Tile instance = TileFactory.createTile(TileType.STRAIGHT, Orientation.EAST, Direction.CENTER, 0, 0, false);
    int expResult = 20;
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
  public void testgetCenterYZero() {
    System.out.println("getCenterY");
    Tile instance = TileFactory.createTile(TileType.STRAIGHT, Orientation.EAST, Direction.CENTER, 0, 0, false);
    int expResult = 20;
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
  public void testGetAllPoints() {
    System.out.println("getAllPoints");
    Tile instance = TileFactory.createTile(TileType.BLOCK, Orientation.WEST, Direction.CENTER, 220, 220, false);
    Set<Point> expResult = new HashSet<>();
    expResult.add(new Point(220, 220));
    expResult.add(new Point(180, 220));
    expResult.add(new Point(260, 220));

    Set<Point> result = instance.getAllPoints();

    assertEquals(expResult, result);
  }

  @Test
  public void testGetAltPoints() {
    System.out.println("getAltPoints");
    Tile instance = TileFactory.createTile(TileType.BLOCK, Orientation.WEST, Direction.CENTER, 220, 220, false);
    Set<Point> expResult = new HashSet<>();
    expResult.add(new Point(180, 220));
    expResult.add(new Point(260, 220));
    Set<Point> result = instance.getAltPoints();
    assertEquals(expResult, result);
  }

  @Test
  public void testIsAdjacentStraight() {
    System.out.println("isAdjacentStraight");
    Tile instanceE = TileFactory.createTile(TileType.STRAIGHT, Orientation.EAST, Direction.CENTER, 100, 100, false);
    Tile instanceN = TileFactory.createTile(TileType.STRAIGHT, Orientation.NORTH, Direction.CENTER, 100, 100, false);

    Tile west = TileFactory.createTile(TileType.STRAIGHT, Orientation.WEST, Direction.CENTER, 60, 100, false);
    Tile east = TileFactory.createTile(TileType.STRAIGHT, Orientation.EAST, Direction.CENTER, 140, 100, false);
    Tile north = TileFactory.createTile(TileType.STRAIGHT, Orientation.NORTH, Direction.CENTER, 100, 60, false);
    Tile south = TileFactory.createTile(TileType.STRAIGHT, Orientation.SOUTH, Direction.CENTER, 100, 140, false);

    assertTrue(instanceE.isAdjacent(west));
    assertTrue(instanceE.isAdjacent(east));
    assertFalse(instanceE.isAdjacent(north));
    assertFalse(instanceE.isAdjacent(south));

    assertFalse(instanceN.isAdjacent(west));
    assertFalse(instanceN.isAdjacent(east));
    assertTrue(instanceN.isAdjacent(north));
    assertTrue(instanceN.isAdjacent(south));
  }

  @Test
  public void testIsAdjacentBlock() {
    System.out.println("isAdjacentBlock");

    Tile instanceE = TileFactory.createTile(TileType.BLOCK, Orientation.EAST, Direction.CENTER, 220, 220, false);
    Tile instanceW = TileFactory.createTile(TileType.BLOCK, Orientation.WEST, Direction.CENTER, 220, 220, false);

    Tile instanceS = TileFactory.createTile(TileType.BLOCK, Orientation.SOUTH, Direction.CENTER, 220, 220, false);
    Tile instanceN = TileFactory.createTile(TileType.BLOCK, Orientation.NORTH, Direction.CENTER, 220, 220, false);

    Tile west = TileFactory.createTile(TileType.STRAIGHT, Orientation.WEST, Direction.CENTER, 140, 220, false);
    Tile east = TileFactory.createTile(TileType.STRAIGHT, Orientation.EAST, Direction.CENTER, 300, 220, false);

    Tile north = TileFactory.createTile(TileType.STRAIGHT, Orientation.NORTH, Direction.CENTER, 220, 140, false);
    Tile south = TileFactory.createTile(TileType.STRAIGHT, Orientation.SOUTH, Direction.CENTER, 220, 300, false);

    assertTrue(instanceE.isAdjacent(west));
    assertTrue(instanceE.isAdjacent(east));
    assertTrue(instanceW.isAdjacent(west));
    assertTrue(instanceW.isAdjacent(east));

    assertTrue(instanceN.isAdjacent(north));
    assertTrue(instanceN.isAdjacent(south));
    assertTrue(instanceS.isAdjacent(north));
    assertTrue(instanceS.isAdjacent(south));

    assertFalse(instanceE.isAdjacent(south));
    assertFalse(instanceE.isAdjacent(north));
    assertFalse(instanceS.isAdjacent(east));
    assertFalse(instanceS.isAdjacent(west));
  }

  @Test
  public void testIsAdjacentCurved() {
    System.out.println("isAdjacentCurved");
    Tile instanceE = TileFactory.createTile(TileType.CURVED, Orientation.EAST, Direction.CENTER, 860, 140, false);
    Tile instanceN = TileFactory.createTile(TileType.CURVED, Orientation.NORTH, Direction.CENTER, 860, 140, false);
    Tile instanceW = TileFactory.createTile(TileType.CURVED, Orientation.WEST, Direction.CENTER, 860, 140, false);
    Tile instanceS = TileFactory.createTile(TileType.CURVED, Orientation.SOUTH, Direction.CENTER, 860, 140, false);

    Tile straightE = TileFactory.createTile(TileType.STRAIGHT, Orientation.EAST, Direction.CENTER, 900, 140, false);
    Tile straightN = TileFactory.createTile(TileType.STRAIGHT, Orientation.NORTH, Direction.CENTER, 860, 100, false);
    Tile straightW = TileFactory.createTile(TileType.STRAIGHT, Orientation.WEST, Direction.CENTER, 820, 140, false);
    Tile straightS = TileFactory.createTile(TileType.STRAIGHT, Orientation.SOUTH, Direction.CENTER, 860, 180, false);

    assertTrue(instanceE.isAdjacent(straightE));
    assertFalse(instanceE.isAdjacent(straightN));
    assertFalse(instanceE.isAdjacent(straightW));
    assertTrue(instanceE.isAdjacent(straightS));

    boolean b = instanceN.isAdjacent(straightE);

    assertTrue(instanceN.isAdjacent(straightE));
    assertTrue(instanceN.isAdjacent(straightN));
    assertFalse(instanceN.isAdjacent(straightW));
    assertFalse(instanceN.isAdjacent(straightS));

    assertFalse(instanceW.isAdjacent(straightE));
    assertTrue(instanceW.isAdjacent(straightN));
    assertTrue(instanceW.isAdjacent(straightW));
    assertFalse(instanceW.isAdjacent(straightS));

    assertFalse(instanceS.isAdjacent(straightE));
    assertFalse(instanceS.isAdjacent(straightN));
    assertTrue(instanceS.isAdjacent(straightW));
    assertTrue(instanceS.isAdjacent(straightS));
  }

  @Test
  public void testIsAdjacentEnd() {
    System.out.println("isAdjacentEnd");

    Tile instanceE = TileFactory.createTile(TileType.END, Orientation.EAST, Direction.CENTER, 860, 140, false);
    Tile instanceS = TileFactory.createTile(TileType.END, Orientation.SOUTH, Direction.CENTER, 860, 140, false);
    Tile instanceW = TileFactory.createTile(TileType.END, Orientation.WEST, Direction.CENTER, 860, 140, false);
    Tile instanceN = TileFactory.createTile(TileType.END, Orientation.NORTH, Direction.CENTER, 860, 140, false);

    Tile straightE = TileFactory.createTile(TileType.STRAIGHT, Orientation.EAST, Direction.CENTER, 900, 140, false);
    Tile straightN = TileFactory.createTile(TileType.STRAIGHT, Orientation.NORTH, Direction.CENTER, 860, 100, false);
    Tile straightW = TileFactory.createTile(TileType.STRAIGHT, Orientation.WEST, Direction.CENTER, 820, 140, false);
    Tile straightS = TileFactory.createTile(TileType.STRAIGHT, Orientation.SOUTH, Direction.CENTER, 860, 180, false);

    assertFalse(instanceE.isAdjacent(straightE));
    assertFalse(instanceE.isAdjacent(straightS));
    assertTrue(instanceE.isAdjacent(straightW));
    assertFalse(instanceE.isAdjacent(straightN));

    assertFalse(instanceS.isAdjacent(straightE));
    assertFalse(instanceS.isAdjacent(straightS));
    assertFalse(instanceS.isAdjacent(straightW));
    assertTrue(instanceS.isAdjacent(straightN));

    assertTrue(instanceW.isAdjacent(straightE));
    assertFalse(instanceW.isAdjacent(straightS));
    assertFalse(instanceW.isAdjacent(straightW));
    assertFalse(instanceW.isAdjacent(straightN));

    assertFalse(instanceN.isAdjacent(straightE));
    assertTrue(instanceN.isAdjacent(straightS));
    assertFalse(instanceN.isAdjacent(straightW));
    assertFalse(instanceN.isAdjacent(straightN));
  }

  @Test
  public void testgetIdSuffix() {
    System.out.println("getGetIdSuffix");
    Tile instanceE = TileFactory.createTile(TileType.BLOCK, Orientation.EAST, Direction.CENTER, 220, 220, false);
    Tile instanceW = TileFactory.createTile(TileType.BLOCK, Orientation.WEST, Direction.CENTER, 220, 220, false);
    Tile instanceN = TileFactory.createTile(TileType.BLOCK, Orientation.NORTH, Direction.CENTER, 220, 220, false);
    Tile instanceS = TileFactory.createTile(TileType.BLOCK, Orientation.SOUTH, Direction.CENTER, 220, 220, false);

    Tile west = TileFactory.createTile(TileType.STRAIGHT, Orientation.WEST, Direction.CENTER, 140, 220, false);
    Tile east = TileFactory.createTile(TileType.STRAIGHT, Orientation.EAST, Direction.CENTER, 300, 220, false);

    Tile north = TileFactory.createTile(TileType.STRAIGHT, Orientation.NORTH, Direction.CENTER, 220, 140, false);
    Tile south = TileFactory.createTile(TileType.STRAIGHT, Orientation.NORTH, Direction.CENTER, 220, 300, false);

    String expResult = "-";
    String result = instanceE.getIdSuffix(west);
    assertEquals(expResult, result);
    result = instanceW.getIdSuffix(east);
    assertEquals(expResult, result);

    result = instanceN.getIdSuffix(south);
    assertEquals(expResult, result);

    result = instanceS.getIdSuffix(north);
    assertEquals(expResult, result);

    expResult = "+";
    result = instanceE.getIdSuffix(east);
    assertEquals(expResult, result);
    result = instanceW.getIdSuffix(west);
    assertEquals(expResult, result);

    result = instanceN.getIdSuffix(north);
    assertEquals(expResult, result);
    result = instanceS.getIdSuffix(south);
    assertEquals(expResult, result);
  }

  @Test
  public void testIsAdjacentSwitchL() {
    System.out.println("isAdjacentSwitchL");
    Tile instanceE = TileFactory.createTile(TileType.SWITCH, Orientation.EAST, Direction.LEFT, 1060, 140, false);
    Tile instanceS = TileFactory.createTile(TileType.SWITCH, Orientation.SOUTH, Direction.LEFT, 1060, 140, false);
    Tile instanceW = TileFactory.createTile(TileType.SWITCH, Orientation.WEST, Direction.LEFT, 1060, 140, false);
    Tile instanceN = TileFactory.createTile(TileType.SWITCH, Orientation.NORTH, Direction.LEFT, 1060, 140, false);

    Tile north = TileFactory.createTile(TileType.STRAIGHT, Orientation.NORTH, Direction.CENTER, 1060, 100, false);
    Tile west = TileFactory.createTile(TileType.STRAIGHT, Orientation.WEST, Direction.CENTER, 1020, 140, false);
    Tile east = TileFactory.createTile(TileType.STRAIGHT, Orientation.EAST, Direction.CENTER, 1100, 140, false);
    Tile south = TileFactory.createTile(TileType.STRAIGHT, Orientation.SOUTH, Direction.CENTER, 1060, 180, false);

    Tile northCS = TileFactory.createTile(TileType.CURVED, Orientation.SOUTH, Direction.CENTER, 1060, 100, false);
    Tile northCE = TileFactory.createTile(TileType.CURVED, Orientation.EAST, Direction.CENTER, 1060, 100, false);
    Tile northCW = TileFactory.createTile(TileType.CURVED, Orientation.WEST, Direction.CENTER, 1060, 100, false);
    Tile northCN = TileFactory.createTile(TileType.CURVED, Orientation.NORTH, Direction.CENTER, 1060, 100, false);

    Tile southCS = TileFactory.createTile(TileType.CURVED, Orientation.SOUTH, Direction.CENTER, 1060, 180, false);
    Tile southCE = TileFactory.createTile(TileType.CURVED, Orientation.EAST, Direction.CENTER, 1060, 180, false);
    Tile southCW = TileFactory.createTile(TileType.CURVED, Orientation.WEST, Direction.CENTER, 1060, 180, false);
    Tile southCN = TileFactory.createTile(TileType.CURVED, Orientation.NORTH, Direction.CENTER, 1060, 180, false);

    assertTrue(instanceE.isAdjacent(west));
    assertTrue(instanceE.isAdjacent(east));
    assertFalse(instanceE.isAdjacent(north));
    assertTrue(instanceE.isAdjacent(south));
     
    assertTrue(instanceS.isAdjacent(west));
    assertFalse(instanceS.isAdjacent(east));
    assertTrue(instanceS.isAdjacent(north));
    assertTrue(instanceS.isAdjacent(south));

    assertTrue(instanceW.isAdjacent(west));
    assertTrue(instanceW.isAdjacent(east));
    assertTrue(instanceW.isAdjacent(north));
    assertFalse(instanceW.isAdjacent(south));
    
    assertFalse(instanceN.isAdjacent(west));
    assertTrue(instanceN.isAdjacent(east));
    assertTrue(instanceN.isAdjacent(north));
    assertTrue(instanceN.isAdjacent(south));
    
    assertFalse(instanceE.isAdjacent(southCS));
    assertFalse(instanceE.isAdjacent(southCE));
    assertTrue(instanceE.isAdjacent(southCW));
    assertTrue(instanceE.isAdjacent(southCN));
    
//    assertFalse(instanceS.isAdjacent(southCS));
//    assertFalse(instanceS.isAdjacent(southCE));
//    assertTrue(instanceS.isAdjacent(southCW));
//    assertTrue(instanceS.isAdjacent(southCN));
    
    
    
    
    
  }

  @Test
  public void testIsAdjacentSwitchR() {
    System.out.println("isAdjacentSwitchR");
    Tile instanceER = TileFactory.createTile(TileType.SWITCH, Orientation.EAST, Direction.RIGHT, 1060, 140, false);

    Tile northS = TileFactory.createTile(TileType.STRAIGHT, Orientation.NORTH, Direction.CENTER, 1060, 100, false);
    Tile west = TileFactory.createTile(TileType.STRAIGHT, Orientation.WEST, Direction.CENTER, 1020, 140, false);
    Tile east = TileFactory.createTile(TileType.STRAIGHT, Orientation.EAST, Direction.CENTER, 1100, 140, false);
    Tile southS = TileFactory.createTile(TileType.STRAIGHT, Orientation.SOUTH, Direction.CENTER, 1060, 180, false);

    Tile northCS = TileFactory.createTile(TileType.CURVED, Orientation.SOUTH, Direction.CENTER, 1060, 100, false);
    Tile northCE = TileFactory.createTile(TileType.CURVED, Orientation.EAST, Direction.CENTER, 1060, 100, false);
    Tile northCW = TileFactory.createTile(TileType.CURVED, Orientation.WEST, Direction.CENTER, 1060, 100, false);
    Tile northCN = TileFactory.createTile(TileType.CURVED, Orientation.NORTH, Direction.CENTER, 1060, 100, false);

    Tile southCS = TileFactory.createTile(TileType.CURVED, Orientation.SOUTH, Direction.CENTER, 1060, 180, false);
    Tile southCE = TileFactory.createTile(TileType.CURVED, Orientation.EAST, Direction.CENTER, 1060, 180, false);
    Tile southCW = TileFactory.createTile(TileType.CURVED, Orientation.WEST, Direction.CENTER, 1060, 180, false);
    Tile southCN = TileFactory.createTile(TileType.CURVED, Orientation.NORTH, Direction.CENTER, 1060, 180, false);

    assertTrue(instanceER.isAdjacent(west));
    assertTrue(instanceER.isAdjacent(east));
    assertTrue(instanceER.isAdjacent(northS));
    assertFalse(instanceER.isAdjacent(southS));

    assertTrue(instanceER.isAdjacent(northCS));
    assertTrue(instanceER.isAdjacent(northCE));
    assertFalse(instanceER.isAdjacent(northCW));
    assertFalse(instanceER.isAdjacent(northCN));

  }
  
  
  //@Test
  public void testIsSwitchSide() {
    System.out.println("isSwitchSide");
    Tile instance = TileFactory.createTile(TileType.SWITCH, Orientation.EAST, Direction.RIGHT, 220, 220, false);

    Tile curvedN = TileFactory.createTile(TileType.CURVED, Orientation.SOUTH, Direction.CENTER, 220, 180, false);
    Tile straighW = TileFactory.createTile(TileType.STRAIGHT, Orientation.WEST, Direction.CENTER, 180, 220, false);

    Tile straighE = TileFactory.createTile(TileType.STRAIGHT, Orientation.SOUTH, Direction.CENTER, 260, 220, false);

    boolean expResult = true;
    boolean result = instance.isAdjacent(straighE);
    assertTrue(instance.isAdjacent(straighE));

    //assertTrue(instanceE.isSwitchSide(straighE));
    //assertFalse(instanceE.isSwitchSide(straighW));
    //assertFalse(instanceE.isSwitchSide(curvedN));
  }

  //@Test
  public void testIsDivergingSide() {

  }

  //@Test
  public void testIsStraightSide() {

  }

}
