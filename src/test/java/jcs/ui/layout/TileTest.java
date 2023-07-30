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

import jcs.ui.layout.tiles.Tile;
import java.awt.Point;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
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
  public void testGetAltPointsBlock() {
    System.out.println("getAltPointsBlock");
    Tile instance = TileFactory.createTile(TileType.BLOCK, Orientation.WEST, Direction.CENTER, 220, 220, false);
    Set<Point> expResult = new HashSet<>();
    expResult.add(new Point(180, 220));
    expResult.add(new Point(260, 220));
    Set<Point> result = instance.getAltPoints();
    assertEquals(expResult, result);
  }

  @Test
  public void testGetAltPointsCross() {
    System.out.println("getAltPointsCross");
    Tile instanceE = TileFactory.createTile(TileType.CROSS, Orientation.EAST, Direction.CENTER, 220, 220, false);
    Tile instanceS = TileFactory.createTile(TileType.CROSS, Orientation.SOUTH, Direction.CENTER, 220, 220, false);
    Tile instanceW = TileFactory.createTile(TileType.CROSS, Orientation.WEST, Direction.CENTER, 220, 220, false);
    Tile instanceN = TileFactory.createTile(TileType.CROSS, Orientation.NORTH, Direction.CENTER, 220, 220, false);

    Set<Point> expResultE = new HashSet<>();
    expResultE.add(new Point(260, 220));

    Set<Point> expResultS = new HashSet<>();
    expResultS.add(new Point(220, 260));

    Set<Point> expResultW = new HashSet<>();
    expResultW.add(new Point(180, 220));

    Set<Point> expResultN = new HashSet<>();
    expResultN.add(new Point(220, 180));

    Set<Point> resultE = instanceE.getAltPoints();

    assertEquals(expResultE.size(), resultE.size());
    assertEquals(expResultE, resultE);

    Set<Point> resultS = instanceS.getAltPoints();
    assertEquals(expResultS.size(), resultS.size());
    assertEquals(expResultS, resultS);

    Set<Point> resultW = instanceW.getAltPoints();
    assertEquals(expResultW.size(), resultW.size());
    assertEquals(expResultW, resultW);

    Set<Point> resultN = instanceN.getAltPoints();
    assertEquals(expResultN.size(), resultN.size());
    assertEquals(expResultN, resultN);
  }

  @Test
  public void testGetNeighborPointsCross() {
    System.out.println("getNeighborPointsCross");
    Tile instanceEL = TileFactory.createTile(TileType.CROSS, Orientation.EAST, Direction.LEFT, 220, 220, false);
    Tile instanceER = TileFactory.createTile(TileType.CROSS, Orientation.EAST, Direction.RIGHT, 220, 220, false);

    Tile instanceWL = TileFactory.createTile(TileType.CROSS, Orientation.WEST, Direction.LEFT, 220, 220, false);
    Tile instanceWR = TileFactory.createTile(TileType.CROSS, Orientation.WEST, Direction.RIGHT, 220, 220, false);

    Tile instanceSL = TileFactory.createTile(TileType.CROSS, Orientation.SOUTH, Direction.LEFT, 220, 220, false);
    Tile instanceSR = TileFactory.createTile(TileType.CROSS, Orientation.SOUTH, Direction.RIGHT, 220, 220, false);

    Tile instanceNL = TileFactory.createTile(TileType.CROSS, Orientation.NORTH, Direction.LEFT, 220, 220, false);
    Tile instanceNR = TileFactory.createTile(TileType.CROSS, Orientation.NORTH, Direction.RIGHT, 220, 220, false);

    Map<Orientation, Point> expResultEL = new HashMap<>();
    expResultEL.put(Orientation.EAST, new Point(300, 220));
    expResultEL.put(Orientation.SOUTH, new Point(220, 260));
    expResultEL.put(Orientation.WEST, new Point(180, 220));
    expResultEL.put(Orientation.NORTH, new Point(260, 180));

    Map<Orientation, Point> expResultER = new HashMap<>();
    expResultER.put(Orientation.EAST, new Point(300, 220));
    expResultER.put(Orientation.SOUTH, new Point(260, 260));
    expResultER.put(Orientation.WEST, new Point(180, 220));
    expResultER.put(Orientation.NORTH, new Point(220, 180));

    Map<Orientation, Point> expResultSL = new HashMap<>();
    expResultSL.put(Orientation.EAST, new Point(260, 260));
    expResultSL.put(Orientation.SOUTH, new Point(220, 300));
    expResultSL.put(Orientation.WEST, new Point(180, 220));
    expResultSL.put(Orientation.NORTH, new Point(220, 180));

    Map<Orientation, Point> expResultSR = new HashMap<>();
    expResultSR.put(Orientation.EAST, new Point(260, 220));
    expResultSR.put(Orientation.SOUTH, new Point(220, 300));
    expResultSR.put(Orientation.WEST, new Point(180, 260));
    expResultSR.put(Orientation.NORTH, new Point(220, 180));

    Map<Orientation, Point> expResultWL = new HashMap<>();
    expResultWL.put(Orientation.WEST, new Point(140, 220));
    expResultWL.put(Orientation.SOUTH, new Point(180, 260));
    expResultWL.put(Orientation.EAST, new Point(260, 220));
    expResultWL.put(Orientation.NORTH, new Point(220, 180));

    Map<Orientation, Point> expResultWR = new HashMap<>();
    expResultWR.put(Orientation.WEST, new Point(140, 220));
    expResultWR.put(Orientation.SOUTH, new Point(220, 260));
    expResultWR.put(Orientation.EAST, new Point(260, 220));
    expResultWR.put(Orientation.NORTH, new Point(180, 180));

    Map<Orientation, Point> expResultNL = new HashMap<>();
    expResultNL.put(Orientation.EAST, new Point(260, 220));
    expResultNL.put(Orientation.SOUTH, new Point(220, 260));
    expResultNL.put(Orientation.WEST, new Point(180, 180));
    expResultNL.put(Orientation.NORTH, new Point(220, 140));

    Map<Orientation, Point> expResultNR = new HashMap<>();
    expResultNR.put(Orientation.EAST, new Point(260, 180));
    expResultNR.put(Orientation.SOUTH, new Point(220, 260));
    expResultNR.put(Orientation.WEST, new Point(180, 220));
    expResultNR.put(Orientation.NORTH, new Point(220, 140));

    Map<Orientation, Point> resultEL = instanceEL.getNeighborPoints();
    assertEquals(expResultEL.size(), resultEL.size());
    assertEquals(expResultEL, resultEL);

    Map<Orientation, Point> resultER = instanceER.getNeighborPoints();
    assertEquals(expResultER.size(), resultER.size());
    assertEquals(expResultER, resultER);

    Map<Orientation, Point> resultWL = instanceWL.getNeighborPoints();
    assertEquals(expResultWL.size(), resultWL.size());
    assertEquals(expResultWL, resultWL);

    Map<Orientation, Point> resultWR = instanceWR.getNeighborPoints();
    assertEquals(expResultWR.size(), resultWR.size());
    assertEquals(expResultWR, resultWR);

    Map<Orientation, Point> resultSL = instanceSL.getNeighborPoints();
    assertEquals(expResultSL.size(), resultSL.size());
    assertEquals(expResultSL, resultSL);

    Map<Orientation, Point> resultSR = instanceSR.getNeighborPoints();
    assertEquals(expResultSR.size(), resultSR.size());
    assertEquals(expResultSR, resultSR);

    Map<Orientation, Point> resultNL = instanceNL.getNeighborPoints();
    assertEquals(expResultNL.size(), resultNL.size());
    assertEquals(expResultNL, resultNL);

    Map<Orientation, Point> resultNR = instanceNR.getNeighborPoints();
    assertEquals(expResultNR.size(), resultNR.size());
    assertEquals(expResultNR, resultNR);

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

    Tile westCS = TileFactory.createTile(TileType.CURVED, Orientation.SOUTH, Direction.CENTER, 1020, 140, false);
    Tile westCE = TileFactory.createTile(TileType.CURVED, Orientation.EAST, Direction.CENTER, 1020, 140, false);
    Tile westCW = TileFactory.createTile(TileType.CURVED, Orientation.WEST, Direction.CENTER, 1020, 140, false);
    Tile westCN = TileFactory.createTile(TileType.CURVED, Orientation.NORTH, Direction.CENTER, 1020, 140, false);

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

    assertFalse(instanceS.isAdjacent(westCS));
    assertTrue(instanceS.isAdjacent(westCE));
    assertFalse(instanceS.isAdjacent(westCW));
    assertTrue(instanceS.isAdjacent(westCN));
  }

  @Test
  public void testIsAdjacentSwitchR() {
    System.out.println("isAdjacentSwitchR");
    Tile instanceE = TileFactory.createTile(TileType.SWITCH, Orientation.EAST, Direction.RIGHT, 1060, 140, false);
    Tile instanceS = TileFactory.createTile(TileType.SWITCH, Orientation.SOUTH, Direction.RIGHT, 1060, 140, false);
    Tile instanceW = TileFactory.createTile(TileType.SWITCH, Orientation.WEST, Direction.RIGHT, 1060, 140, false);
    Tile instanceN = TileFactory.createTile(TileType.SWITCH, Orientation.NORTH, Direction.RIGHT, 1060, 140, false);

    Tile north = TileFactory.createTile(TileType.STRAIGHT, Orientation.NORTH, Direction.CENTER, 1060, 100, false);
    Tile west = TileFactory.createTile(TileType.STRAIGHT, Orientation.WEST, Direction.CENTER, 1020, 140, false);
    Tile east = TileFactory.createTile(TileType.STRAIGHT, Orientation.EAST, Direction.CENTER, 1100, 140, false);
    Tile south = TileFactory.createTile(TileType.STRAIGHT, Orientation.SOUTH, Direction.CENTER, 1060, 180, false);

    Tile eastCS = TileFactory.createTile(TileType.CURVED, Orientation.SOUTH, Direction.CENTER, 1100, 140, false);
    Tile eastCE = TileFactory.createTile(TileType.CURVED, Orientation.EAST, Direction.CENTER, 1100, 140, false);
    Tile eastCW = TileFactory.createTile(TileType.CURVED, Orientation.WEST, Direction.CENTER, 1100, 140, false);
    Tile eastCN = TileFactory.createTile(TileType.CURVED, Orientation.NORTH, Direction.CENTER, 1100, 140, false);

    Tile northCS = TileFactory.createTile(TileType.CURVED, Orientation.SOUTH, Direction.CENTER, 1060, 100, false);
    Tile northCE = TileFactory.createTile(TileType.CURVED, Orientation.EAST, Direction.CENTER, 1060, 100, false);
    Tile northCW = TileFactory.createTile(TileType.CURVED, Orientation.WEST, Direction.CENTER, 1060, 100, false);
    Tile northCN = TileFactory.createTile(TileType.CURVED, Orientation.NORTH, Direction.CENTER, 1060, 100, false);

    assertTrue(instanceE.isAdjacent(west));
    assertTrue(instanceE.isAdjacent(east));
    assertTrue(instanceE.isAdjacent(north));
    assertFalse(instanceE.isAdjacent(south));

    assertFalse(instanceS.isAdjacent(west));
    assertTrue(instanceS.isAdjacent(east));
    assertTrue(instanceS.isAdjacent(north));
    assertTrue(instanceS.isAdjacent(south));

    assertTrue(instanceW.isAdjacent(west));
    assertTrue(instanceW.isAdjacent(east));
    assertFalse(instanceW.isAdjacent(north));
    assertTrue(instanceW.isAdjacent(south));

    assertTrue(instanceN.isAdjacent(west));
    assertFalse(instanceN.isAdjacent(east));
    assertTrue(instanceN.isAdjacent(north));
    assertTrue(instanceN.isAdjacent(south));

    assertTrue(instanceE.isAdjacent(northCS));
    assertTrue(instanceE.isAdjacent(northCE));
    assertFalse(instanceE.isAdjacent(northCW));
    assertFalse(instanceE.isAdjacent(northCN));

    assertTrue(instanceS.isAdjacent(eastCS));
    assertFalse(instanceS.isAdjacent(eastCE));
    assertTrue(instanceS.isAdjacent(eastCW));
    assertFalse(instanceS.isAdjacent(eastCN));
  }

  @Test
  public void testIsSwitchSideR() {
    System.out.println("isSwitchSideR");
    Tile instanceE = TileFactory.createTile(TileType.SWITCH, Orientation.EAST, Direction.RIGHT, 860, 140, false);
    Tile instanceS = TileFactory.createTile(TileType.SWITCH, Orientation.SOUTH, Direction.RIGHT, 860, 140, false);

    Tile curvedN = TileFactory.createTile(TileType.CURVED, Orientation.SOUTH, Direction.CENTER, 860, 100, false);
    Tile straighW = TileFactory.createTile(TileType.STRAIGHT, Orientation.WEST, Direction.CENTER, 820, 140, false);
    Tile straighE = TileFactory.createTile(TileType.STRAIGHT, Orientation.EAST, Direction.CENTER, 900, 140, false);
    Tile straighS = TileFactory.createTile(TileType.STRAIGHT, Orientation.SOUTH, Direction.CENTER, 860, 180, false);

    assertTrue(instanceE.isSwitchSide(straighE));
    assertFalse(instanceE.isSwitchSide(straighW));
    assertFalse(instanceE.isSwitchSide(curvedN));

    assertFalse(instanceS.isSwitchSide(straighE));
    assertTrue(instanceS.isSwitchSide(straighS));
    assertFalse(instanceS.isSwitchSide(straighW));
    assertFalse(instanceS.isSwitchSide(curvedN));
  }

  @Test
  public void testIsDivergingSide() {
    System.out.println("isDivergingSideR");
    Tile instanceE = TileFactory.createTile(TileType.SWITCH, Orientation.EAST, Direction.RIGHT, 860, 140, false);
    Tile instanceS = TileFactory.createTile(TileType.SWITCH, Orientation.SOUTH, Direction.RIGHT, 860, 140, false);

    Tile curvedN = TileFactory.createTile(TileType.CURVED, Orientation.SOUTH, Direction.CENTER, 860, 100, false);
    Tile straighW = TileFactory.createTile(TileType.STRAIGHT, Orientation.WEST, Direction.CENTER, 820, 140, false);
    Tile straighE = TileFactory.createTile(TileType.STRAIGHT, Orientation.EAST, Direction.CENTER, 900, 140, false);
    Tile straighS = TileFactory.createTile(TileType.STRAIGHT, Orientation.SOUTH, Direction.CENTER, 860, 180, false);

    assertFalse(instanceE.isDivergingSide(straighE));
    assertFalse(instanceE.isDivergingSide(straighW));
    assertTrue(instanceE.isDivergingSide(curvedN));

    assertTrue(instanceS.isDivergingSide(straighE));
    assertFalse(instanceS.isDivergingSide(straighS));
    assertFalse(instanceS.isDivergingSide(straighW));
    assertFalse(instanceS.isDivergingSide(curvedN));
  }

  @Test
  public void testIsStraightSideR() {
    System.out.println("isStraightSideR");
    Tile instanceE = TileFactory.createTile(TileType.SWITCH, Orientation.EAST, Direction.RIGHT, 860, 140, false);
    Tile instanceS = TileFactory.createTile(TileType.SWITCH, Orientation.SOUTH, Direction.RIGHT, 860, 140, false);

    Tile curvedN = TileFactory.createTile(TileType.CURVED, Orientation.SOUTH, Direction.CENTER, 860, 100, false);
    Tile straighW = TileFactory.createTile(TileType.STRAIGHT, Orientation.WEST, Direction.CENTER, 820, 140, false);
    Tile straighE = TileFactory.createTile(TileType.STRAIGHT, Orientation.EAST, Direction.CENTER, 900, 140, false);
    Tile straighS = TileFactory.createTile(TileType.STRAIGHT, Orientation.SOUTH, Direction.CENTER, 860, 180, false);

    assertFalse(instanceE.isStraightSide(straighE));
    assertTrue(instanceE.isStraightSide(straighW));
    assertFalse(instanceE.isStraightSide(curvedN));

    assertFalse(instanceS.isStraightSide(straighE));
    assertFalse(instanceS.isStraightSide(straighS));
    assertFalse(instanceS.isStraightSide(straighW));
    assertTrue(instanceS.isStraightSide(curvedN));
  }

  @Test
  public void testIsArrowSwitchSide() {
    System.out.println("isArrowSwitchSide");
    Tile instanceE = TileFactory.createTile(TileType.STRAIGHT_DIR, Orientation.EAST, Direction.RIGHT, 860, 140, false);
    Tile instanceS = TileFactory.createTile(TileType.STRAIGHT_DIR, Orientation.SOUTH, Direction.RIGHT, 860, 140, false);
    Tile instanceW = TileFactory.createTile(TileType.STRAIGHT_DIR, Orientation.WEST, Direction.RIGHT, 860, 140, false);
    Tile instanceN = TileFactory.createTile(TileType.STRAIGHT_DIR, Orientation.NORTH, Direction.RIGHT, 860, 140, false);

    Tile straighE = TileFactory.createTile(TileType.STRAIGHT, Orientation.EAST, Direction.CENTER, 900, 140, false);
    Tile straighS = TileFactory.createTile(TileType.STRAIGHT, Orientation.SOUTH, Direction.CENTER, 860, 180, false);
    Tile straighW = TileFactory.createTile(TileType.STRAIGHT, Orientation.WEST, Direction.CENTER, 820, 140, false);
    Tile straighN = TileFactory.createTile(TileType.STRAIGHT, Orientation.NORTH, Direction.CENTER, 860, 100, false);

    assertTrue(instanceE.isArrowDirection(straighE));
    assertFalse(instanceE.isArrowDirection(straighW));

    assertTrue(instanceS.isArrowDirection(straighS));
    assertFalse(instanceS.isArrowDirection(straighN));

    assertTrue(instanceW.isArrowDirection(straighW));
    assertFalse(instanceW.isArrowDirection(straighE));

    assertTrue(instanceN.isArrowDirection(straighN));
    assertFalse(instanceN.isArrowDirection(straighS));
  }

  @Test
  public void testIsAdjacentCrossL() {
    System.out.println("iIsAdjacentCrossL");
    Tile instanceE = TileFactory.createTile(TileType.CROSS, Orientation.EAST, Direction.LEFT, 860, 100, false);
    Tile instanceS = TileFactory.createTile(TileType.CROSS, Orientation.SOUTH, Direction.LEFT, 860, 100, false);
    Tile instanceW = TileFactory.createTile(TileType.CROSS, Orientation.WEST, Direction.LEFT, 860, 100, false);
    Tile instanceN = TileFactory.createTile(TileType.CROSS, Orientation.NORTH, Direction.LEFT, 860, 100, false);

    Tile north = TileFactory.createTile(TileType.STRAIGHT, Orientation.NORTH, Direction.CENTER, 900, 60, false);
    Tile north2 = TileFactory.createTile(TileType.STRAIGHT, Orientation.NORTH, Direction.CENTER, 860, 60, false);
    Tile north3 = TileFactory.createTile(TileType.STRAIGHT, Orientation.NORTH, Direction.CENTER, 860, 20, false);
    Tile west = TileFactory.createTile(TileType.STRAIGHT, Orientation.WEST, Direction.CENTER, 820, 100, false);
    Tile west2 = TileFactory.createTile(TileType.STRAIGHT, Orientation.WEST, Direction.CENTER, 780, 100, false);
    Tile west3 = TileFactory.createTile(TileType.STRAIGHT, Orientation.WEST, Direction.CENTER, 820, 60, false);
    Tile east = TileFactory.createTile(TileType.STRAIGHT, Orientation.EAST, Direction.CENTER, 940, 100, false);
    Tile east2 = TileFactory.createTile(TileType.STRAIGHT, Orientation.EAST, Direction.CENTER, 900, 100, false);
    Tile east3 = TileFactory.createTile(TileType.STRAIGHT, Orientation.EAST, Direction.CENTER, 900, 140, false);
    Tile south = TileFactory.createTile(TileType.STRAIGHT, Orientation.SOUTH, Direction.CENTER, 860, 140, false);
    Tile south2 = TileFactory.createTile(TileType.STRAIGHT, Orientation.SOUTH, Direction.CENTER, 820, 140, false);
    Tile south3 = TileFactory.createTile(TileType.STRAIGHT, Orientation.SOUTH, Direction.CENTER, 860, 180, false);

    assertTrue(instanceE.isAdjacent(west));
    assertTrue(instanceE.isAdjacent(east));
    assertTrue(instanceE.isAdjacent(north));
    assertTrue(instanceE.isAdjacent(south));

    assertTrue(instanceS.isAdjacent(west));
    assertTrue(instanceS.isAdjacent(east3));
    assertTrue(instanceS.isAdjacent(north2));
    assertTrue(instanceS.isAdjacent(south3));

    assertTrue(instanceW.isAdjacent(west2));
    assertTrue(instanceW.isAdjacent(east2));
    assertTrue(instanceW.isAdjacent(north2));
    assertTrue(instanceW.isAdjacent(south2));

    assertTrue(instanceN.isAdjacent(west3));
    assertTrue(instanceN.isAdjacent(east2));
    assertTrue(instanceN.isAdjacent(north3));
    assertTrue(instanceN.isAdjacent(south));

  }

  @Test
  public void testIsAdjacentCrossR() {
    System.out.println("iIsAdjacentCrossR");
    Tile instanceE = TileFactory.createTile(TileType.CROSS, Orientation.EAST, Direction.RIGHT, 860, 100, false);
    Tile instanceS = TileFactory.createTile(TileType.CROSS, Orientation.SOUTH, Direction.RIGHT, 860, 100, false);
    Tile instanceW = TileFactory.createTile(TileType.CROSS, Orientation.WEST, Direction.RIGHT, 860, 100, false);
    Tile instanceN = TileFactory.createTile(TileType.CROSS, Orientation.NORTH, Direction.RIGHT, 860, 100, false);

    Tile north = TileFactory.createTile(TileType.STRAIGHT, Orientation.NORTH, Direction.CENTER, 860, 60, false);
    Tile north2 = TileFactory.createTile(TileType.STRAIGHT, Orientation.NORTH, Direction.CENTER, 860, 60, false);
    Tile north3 = TileFactory.createTile(TileType.STRAIGHT, Orientation.NORTH, Direction.CENTER, 860, 20, false);
    Tile north4 = TileFactory.createTile(TileType.STRAIGHT, Orientation.NORTH, Direction.CENTER, 820, 60, false);
    Tile west = TileFactory.createTile(TileType.STRAIGHT, Orientation.WEST, Direction.CENTER, 820, 140, false);
    Tile west2 = TileFactory.createTile(TileType.STRAIGHT, Orientation.WEST, Direction.CENTER, 820, 100, false);
    Tile west3 = TileFactory.createTile(TileType.STRAIGHT, Orientation.WEST, Direction.CENTER, 860, 100, false);
    Tile west4 = TileFactory.createTile(TileType.STRAIGHT, Orientation.WEST, Direction.CENTER, 780, 100, false);

    Tile east = TileFactory.createTile(TileType.STRAIGHT, Orientation.EAST, Direction.CENTER, 940, 100, false);
    Tile east2 = TileFactory.createTile(TileType.STRAIGHT, Orientation.EAST, Direction.CENTER, 900, 60, false);
    Tile east3 = TileFactory.createTile(TileType.STRAIGHT, Orientation.EAST, Direction.CENTER, 900, 100, false);
    Tile south = TileFactory.createTile(TileType.STRAIGHT, Orientation.SOUTH, Direction.CENTER, 860, 140, false);
    Tile south2 = TileFactory.createTile(TileType.STRAIGHT, Orientation.SOUTH, Direction.CENTER, 900, 140, false);
    Tile south3 = TileFactory.createTile(TileType.STRAIGHT, Orientation.SOUTH, Direction.CENTER, 860, 180, false);

    assertTrue(instanceE.isAdjacent(west2));
    assertTrue(instanceE.isAdjacent(east));
    assertTrue(instanceE.isAdjacent(north2));
    assertTrue(instanceE.isAdjacent(south2));

    assertTrue(instanceS.isAdjacent(west));
    assertTrue(instanceS.isAdjacent(east3));
    assertTrue(instanceS.isAdjacent(north));
    assertTrue(instanceS.isAdjacent(south3));

    assertTrue(instanceW.isAdjacent(west4));
    assertTrue(instanceW.isAdjacent(east3));
    assertTrue(instanceW.isAdjacent(north4));
    assertTrue(instanceW.isAdjacent(south));

    assertTrue(instanceN.isAdjacent(west3));
    assertTrue(instanceN.isAdjacent(east2));
    assertTrue(instanceN.isAdjacent(north3));
    assertTrue(instanceN.isAdjacent(south));

  }

}
