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

public class Grid {

  Cell[][] cells;

  int cols;
  int rows;

  public Grid(int cols, int rows) {
    this.cols = cols;
    this.rows = rows;
    cells = new Cell[rows][cols];

    for (int row = 0; row < rows; row++) {
      for (int col = 0; col < cols; col++) {
        cells[row][col] = new Cell(col, row, true);
      }
    }
  }

  public Cell getCell(int col, int row) {
    return cells[row][col];
  }

  /**
   * Get neighboring cells relative to the given cell. By default they are top/right/bottom/left. If allowDiagonals is enabled, then also top-left, top-right, bottom-left, bottom-right cells are in
   * the results.
   *
   * @param cell
   * @param allowDiagonals
   * @return
   */
  public Cell[] getNeighbors(Cell cell, boolean allowDiagonals) {

    Cell[] neighbors = new Cell[allowDiagonals ? 8 : 4];

    int currentColumn = cell.col;
    int currentRow = cell.row;

    int neighborColumn;
    int neighborRow;

    // top
    neighborColumn = currentColumn;
    neighborRow = currentRow - 1;

    if (neighborRow >= 0) {
      if (cells[neighborRow][neighborColumn].traversable) {
        neighbors[0] = cells[neighborRow][neighborColumn];
      }
    }

    // bottom
    neighborColumn = currentColumn;
    neighborRow = currentRow + 1;

    if (neighborRow < rows) {
      if (cells[neighborRow][neighborColumn].traversable) {
        neighbors[1] = cells[neighborRow][neighborColumn];
      }
    }

    // left
    neighborColumn = currentColumn - 1;
    neighborRow = currentRow;

    if (neighborColumn >= 0) {
      if (cells[neighborRow][neighborColumn].traversable) {
        neighbors[2] = cells[neighborRow][neighborColumn];
      }
    }

    // right
    neighborColumn = currentColumn + 1;
    neighborRow = currentRow;

    if (neighborColumn < cols) {
      if (cells[neighborRow][neighborColumn].traversable) {
        neighbors[3] = cells[neighborRow][neighborColumn];
      }
    }

    if (allowDiagonals) {

      // top/left
      neighborColumn = currentColumn - 1;
      neighborRow = currentRow - 1;

      if (neighborRow >= 0 && neighborColumn >= 0) {
        if (cells[neighborRow][neighborColumn].traversable) {
          neighbors[4] = cells[neighborRow][neighborColumn];
        }
      }

      // bottom/right
      neighborColumn = currentColumn + 1;
      neighborRow = currentRow + 1;

      if (neighborRow < rows && neighborColumn < cols) {
        if (cells[neighborRow][neighborColumn].traversable) {
          neighbors[5] = cells[neighborRow][neighborColumn];
        }
      }

      // top/right
      neighborColumn = currentColumn + 1;
      neighborRow = currentRow - 1;

      if (neighborRow >= 0 && neighborColumn < cols) {
        if (cells[neighborRow][neighborColumn].traversable) {
          neighbors[6] = cells[neighborRow][neighborColumn];
        }
      }

      // bottom/left
      neighborColumn = currentColumn - 1;
      neighborRow = currentRow + 1;

      if (neighborRow < rows && neighborColumn >= 0) {
        if (cells[neighborRow][neighborColumn].traversable) {
          neighbors[7] = cells[neighborRow][neighborColumn];
        }
      }

    }

    return neighbors;
  }

}
