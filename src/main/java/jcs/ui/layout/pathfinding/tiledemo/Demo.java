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
package jcs.ui.layout.pathfinding.tiledemo;

import java.util.List;
import java.util.Set;

/**
 *
 * https://stackoverflow.com/questions/30181525/find-inside-coordinates-of-polygon-in-tile-based-map
 * 
 * 
 * primitive flood-fill algorithm 
 */
public class Demo {

  public static void main(String[] args) {

    // create grid like in the example
    int cols = 9;
    int rows = 9;

    Grid grid = new Grid(cols, rows);

    // create walls like in the example
    grid.getCell(1, 1).setTraversable(false);
    grid.getCell(2, 1).setTraversable(false);
    grid.getCell(3, 1).setTraversable(false);
    grid.getCell(1, 2).setTraversable(false);
    grid.getCell(3, 2).setTraversable(false);
    grid.getCell(6, 2).setTraversable(false);
    grid.getCell(7, 2).setTraversable(false);
    grid.getCell(8, 2).setTraversable(false);
    grid.getCell(1, 3).setTraversable(false);
    grid.getCell(2, 3).setTraversable(false);
    grid.getCell(3, 3).setTraversable(false);
    grid.getCell(6, 3).setTraversable(false);
    grid.getCell(6, 4).setTraversable(false);
    grid.getCell(7, 4).setTraversable(false);
    grid.getCell(1, 5).setTraversable(false);
    grid.getCell(2, 5).setTraversable(false);
    grid.getCell(3, 5).setTraversable(false);
    grid.getCell(4, 5).setTraversable(false);
    grid.getCell(5, 5).setTraversable(false);
    grid.getCell(7, 5).setTraversable(false);
    grid.getCell(8, 5).setTraversable(false);
    grid.getCell(1, 6).setTraversable(false);
    grid.getCell(5, 6).setTraversable(false);
    grid.getCell(1, 7).setTraversable(false);
    grid.getCell(2, 7).setTraversable(false);
    grid.getCell(3, 7).setTraversable(false);
    grid.getCell(4, 7).setTraversable(false);
    grid.getCell(5, 7).setTraversable(false);

    // find traversables
    // -------------------------
    AStarAlgorithm alg = new AStarAlgorithm();

    Cell start;
    Cell goal;

    // reference point = 0/0
    start = grid.getCell(0, 0);
    Set<Cell> visited = alg.getFloodFillCells(grid, start, true);

    // find inside cells
    for (int row = 0; row < rows; row++) {
      for (int col = 0; col < cols; col++) {

        Cell cell = grid.getCell(col, row);

        if (!cell.traversable) {
          cell.setType(Type.WALL);
        } else if (visited.contains(cell)) {
          cell.setType(Type.OUTSIDE);
        } else {
          cell.setType(Type.INSIDE);
        }

      }
    }

    // log inside cells
    for (int row = 0; row < rows; row++) {
      for (int col = 0; col < cols; col++) {
        Cell cell = grid.getCell(col, row);
        if (cell.getType() == Type.INSIDE) {
          System.out.println("Inside: " + cell);
        }
      }
    }

    // path finding
    // -------------------------
    // start = top/left, goal = bottom/right
    start = grid.getCell(0, 0);
    goal = grid.getCell(8, 8);

    // find a* path
    List<Cell> path = alg.findPath(grid, start, goal, true);

    // log path
    System.out.println(path);

    System.exit(0);

  }

}
