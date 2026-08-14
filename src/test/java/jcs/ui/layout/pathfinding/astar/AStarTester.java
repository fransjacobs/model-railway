/*
 * Copyright 2026 Frans Jacobs.
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
package jcs.ui.layout.pathfinding.astar;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import jcs.entities.RouteBean;
import jcs.persistence.PersistenceFactory;
import jcs.ui.layout.tiles.Tile;
import jcs.ui.layout.tiles.TileCache;
import org.tinylog.Logger;

/**
 * Quick Route Test Class
 */
public class AStarTester extends AStar {

  @Override
  public List<RouteBean> routeAll() {
    routes.clear();
    List<List<Node>> blockToBlockList = getAllBlockToBlockNodes();
    Logger.trace("Overridden! Try to route {} Possible block to block routes...", (blockToBlockList.size() * 2 * 2));
    Logger.trace("=============================================================================");

    for (List<Node> fromTo : blockToBlockList) {
      Node from = fromTo.get(0);
      Node to = fromTo.get(1);
      Set<Edge> fromEdges = from.getEdges();
      Set<Edge> toEdges = from.getEdges();

      for (Edge fromEdge : fromEdges) {
        if (fromEdge.getFrom().isBlock()) {
          String fromSuffix = fromEdge.getFromSuffix();
          for (Edge toEdge : toEdges) {
            if (toEdge.getFrom().isBlock()) {
              String toSuffix = toEdge.getFromSuffix();

              String fid = from.getId() + fromSuffix;
              String tid = to.getId() + toSuffix;

              //if ("bk-1+".equals(fid) && "bk-2-".equals(tid)) {
              //|| ("bk-1-".equals(fid) && "bk-2-".equals(tid))) {
              //if ("bk-1+".equals(fid) && "bk-4+".equals(tid)) {
              //if ("bk-1+".equals(fid) && "bk-6+".equals(tid)) {
              List<Node> path = findPath(from, fromSuffix, to, toSuffix);

              if (path.isEmpty()) {
                Logger.debug("No Path from " + fid + " to " + tid);
              } else {
                RouteBean routeBean = createRouteBeanFromNodePath(path);
                routes.put(routeBean.getId(), routeBean);
              }

              //}
            }
          }
        }
      }
    }

    Logger.trace("Found " + routes.size() + " routes");
    return new ArrayList<>(routes.values());
  }

  public static void main(String[] a) {
    List<Tile> tiles = TileCache.loadTiles();

    AStar gb = new AStar();
    gb.buildGraph(tiles);

    List<RouteBean> routes = gb.routeAll();

    if (1 == 2) {
      gb.persistRoutes();
      routes = PersistenceFactory.getService().getRoutes();
    }
    for (RouteBean r : routes) {
      Logger.trace(r.toLogString());
    }
    System.exit(0);
  }

}
