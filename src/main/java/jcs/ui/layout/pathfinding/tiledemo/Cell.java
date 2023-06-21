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

public class Cell implements Cloneable {

  int col;
  int row;
  boolean traversable;
  Type type;

  double g;
  double f;
  double h;

  Cell cameFrom;

  public Cell(int col, int row, boolean traversable) {
    this.col = col;
    this.row = row;
    this.traversable = traversable;
  }

  public double getF() {
    return f;
  }

  public double getG() {
    return g;
  }

  public double getH() {
    return h;
  }

  public void setTraversable(boolean traversable) {
    this.traversable = traversable;
  }

  public void setType(Type type) {
    this.type = type;
  }

  public Type getType() {
    return this.type;
  }

  public String toString() {
    return col + "/" + row;
  }
}
