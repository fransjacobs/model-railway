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

import java.util.List;
import jcs.persistence.PersistenceFactory;
import jcs.ui.layout.Tile;
import jcs.ui.layout.tiles.TileFactory;
import org.tinylog.Logger;

/**
 * Build a Graph from the Layout Tiles
 *
 * @author frans
 */
public class GraphBuilder {

  private List<Tile> tiles;

  public GraphBuilder(List<Tile> tiles) {
    this.tiles = tiles;
  }

  void buildGraph() {
    //Iterate through the tiles. Each tile becomes a node. the connection beteen a tile is the vertex or edge.
    //Each tile is tested whether it has a neighbor to which it can traverse.
  }
  
  
  public static void main(String[] a) {
    List<Tile> tiles = TileFactory.convert(PersistenceFactory.getService().getTiles(), false, false);
    Logger.trace("Loaded "+tiles.size()+" tiles");
  }

}
