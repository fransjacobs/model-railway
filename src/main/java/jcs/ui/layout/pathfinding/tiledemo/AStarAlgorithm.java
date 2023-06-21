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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;

/**
 * A* algorithm from http://en.wikipedia.org/wiki/A*_search_algorithm
 */
public class AStarAlgorithm {

  public class CellComparator implements Comparator<Cell> {

    @Override
    public int compare(Cell a, Cell b) {
      return Double.compare(a.f, b.f);
    }

  }

  /**
   * Find all cells that we can traverse from a given reference start point that's an outside cell. Algorithm is like the A* path finding, but we don't stop when we found the goal, neither do we
   * consider the calculation of the distance.
   *
   * @param g
   * @param start
   * @param goal
   * @param allowDiagonals
   * @return
   */
  public Set<Cell> getFloodFillCells(Grid g, Cell start, boolean allowDiagonals) {

    Cell current = null;

    Set<Cell> closedSet = new HashSet<>();

    Set<Cell> openSet = new HashSet<Cell>();
    openSet.add(start);

    while (!openSet.isEmpty()) {

      current = openSet.iterator().next();

      openSet.remove(current);

      closedSet.add(current);

      for (Cell neighbor : g.getNeighbors(current, allowDiagonals)) {

        if (neighbor == null) {
          continue;
        }

        if (closedSet.contains(neighbor)) {
          continue;
        }

        openSet.add(neighbor);
      }

    }

    return closedSet;

  }

  /**
   * Find path from start to goal.
   *
   * @param g
   * @param start
   * @param goal
   * @param allowDiagonals
   * @return
   */
  public List<Cell> findPath(Grid g, Cell start, Cell goal, boolean allowDiagonals) {

    Cell current = null;
    boolean containsNeighbor;

    int cellCount = g.rows * g.cols;

    Set<Cell> closedSet = new HashSet<>(cellCount);

    PriorityQueue<Cell> openSet = new PriorityQueue<Cell>(cellCount, new CellComparator());
    openSet.add(start);

    start.g = 0d;
    start.f = start.g + heuristicCostEstimate(start, goal);

    while (!openSet.isEmpty()) {

      current = openSet.poll();

      if (current == goal) {
        return reconstructPath(goal);
      }

      closedSet.add(current);

      for (Cell neighbor : g.getNeighbors(current, allowDiagonals)) {

        if (neighbor == null) {
          continue;
        }

        if (closedSet.contains(neighbor)) {
          continue;
        }

        double tentativeScoreG = current.g + distBetween(current, neighbor);

        if (!(containsNeighbor = openSet.contains(neighbor)) || Double.compare(tentativeScoreG, neighbor.g) < 0) {

          neighbor.cameFrom = current;

          neighbor.g = tentativeScoreG;

          neighbor.h = heuristicCostEstimate(neighbor, goal);
          neighbor.f = neighbor.g + neighbor.h;

          if (!containsNeighbor) {
            openSet.add(neighbor);
          }
        }
      }

    }

    return new ArrayList<>();
  }

  private List<Cell> reconstructPath(Cell current) {

    List<Cell> totalPath = new ArrayList<>(200); // arbitrary value, we'll most likely have more than 10 which is default for java

    totalPath.add(current);

    while ((current = current.cameFrom) != null) {

      totalPath.add(current);

    }

    return totalPath;
  }

  private double distBetween(Cell current, Cell neighbor) {
    return heuristicCostEstimate(current, neighbor); // TODO: dist_between is heuristic_cost_estimate for our use-case; use various other heuristics
  }

  private double heuristicCostEstimate(Cell from, Cell to) {

    return Math.sqrt((from.col - to.col) * (from.col - to.col) + (from.row - to.row) * (from.row - to.row));

  }

}
