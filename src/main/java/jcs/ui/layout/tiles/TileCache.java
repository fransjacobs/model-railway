/*
 * Copyright 2025 Frans Jacobs.
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
package jcs.ui.layout.tiles;

import java.awt.Point;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;
import jcs.entities.TileBean;
import jcs.persistence.PersistenceFactory;
import org.tinylog.Logger;
import jcs.commandStation.events.JCSActionEvent;

/**
 * Factory object to create Tiles and cache pointMap
 *
 * @author frans
 */
public class TileCache {

  static final Map<String, Tile> idMap = new ConcurrentHashMap<>();
  static final Map<Point, Tile> pointMap = new ConcurrentHashMap<>();
  static final Map<Point, Tile> altPointMap = new ConcurrentHashMap<>();

  private static final ConcurrentLinkedQueue<JCSActionEvent> eventsQueue = new ConcurrentLinkedQueue();

  private static final TileActionEventHandler actionEventQueueHandler = new TileActionEventHandler(eventsQueue);

  static {
    actionEventQueueHandler.start();
  }

  private TileCache() {
  }

  public static List<Tile> loadTiles() {
    return loadTiles(false);
  }

  public static List<Tile> loadTiles(boolean showvalues) {
    altPointMap.clear();
    pointMap.clear();
    idMap.clear();

    List<TileBean> tileBeans = PersistenceFactory.getService().getTileBeans();

    for (TileBean tb : tileBeans) {
      Tile tile = TileFactory.createTile(tb, showvalues);
      idMap.put(tile.id, tile);
      pointMap.put(tile.getCenter(), tile);
      //Alternative point(s) to be able to find all pointIds
      if (!tile.getAltPoints().isEmpty()) {
        Set<Point> alt = tile.getAltPoints();
        for (Point ap : alt) {
          altPointMap.put(ap, tile);
        }
      }
    }

    Logger.trace("Loaded " + idMap.size() + " Tiles...");
    return idMap.values().stream().collect(Collectors.toList());
  }

  public static List<Tile> getTiles() {
    return idMap.values().stream().collect(Collectors.toList());
  }

  public static Tile addAndSaveTile(Tile tile) {
    if (tile == null) {
      throw new IllegalArgumentException("Tile cannot be null");
    }

    pointMap.put(tile.getCenter(), tile);
    idMap.put(tile.getId(), tile);

    //Alternative point(s) to be able to find all pointIds
    if (!tile.getAltPoints().isEmpty()) {
      Set<Point> alt = tile.getAltPoints();
      for (Point ap : alt) {
        altPointMap.put(ap, tile);
      }
    }

    persistTile(tile);
    //Logger.trace("Added " + tile + " There are now " + pointMap.size() + " pointMap...");
    return tile;
  }

  public static void persistTile(final Tile tile) {
    if (tile == null) {
      throw new IllegalArgumentException("Tile cannot be null");
    }
    TileBean tb = tile.getTileBean();
    PersistenceFactory.getService().persist(tb);
  }

  public static void persistAllTiles() {
    for (Tile tile : idMap.values()) {
      persistTile(tile);
    }
  }

  public static void deleteTile(final Tile tile) {
    if (tile == null) {
      throw new IllegalArgumentException("Tile cannot be null");
    }
    if (idMap.containsKey(tile.id)) {
      Set<Point> rps = tile.getAltPoints();
      //Also remove alt pointIds
      for (Point ap : rps) {
        altPointMap.remove(ap);
      }
      pointMap.remove(tile.getCenter());
      idMap.remove(tile.id);
      TileBean tb = tile.getTileBean();
      PersistenceFactory.getService().remove(tb);
      Logger.trace("Deleted " + tile.getId());
    } else {
      Logger.warn("Tile " + tile.getId() + " not found in cache");
    }
  }

  public static Tile findTile(Point cp) {
    Tile result = pointMap.get(cp);
    if (result == null) {
      result = altPointMap.get(cp);
    }
    return result;
  }

  public static Tile findTile(String id) {
    Tile tile = idMap.get(id);
    return tile;
  }

  public static boolean canMoveTo(Tile tile, Point p) {
    //check if a tile exist with point p
    //Check if the cache contains a Cp of a tile on p
    //Logger.trace("Checking " + tile.id + " New Point (" + p.x + "," + p.y + ")");
    if (pointMap.containsKey(p) && !tile.getCenter().equals(p) && !tile.getAltPoints().contains(p)) {
      return false;
    }
    //Check if the cache contains a Cp on any of the Alt point is case of a Block or Cross
    if (altPointMap.containsKey(p) && !tile.getAltPoints().contains(p)) {
      return false;
    }

    //Check with the tile on the new Cp with the new alt pointIds if that is also free...
    Set<Point> altPoints = tile.getAltPoints(p);
    for (Point ap : altPoints) {
      if (pointMap.containsKey(ap) && !tile.getCenter().equals(ap) && !tile.getAltPoints().contains(ap)) {
        return false;
      }
      if (altPointMap.containsKey(ap) && !tile.getAltPoints().contains(ap)) {
        return false;
      }
    }
    //Logger.trace("Checked " + tile.id + " can move to (" + p.x + "," + p.y + ")");
    return true;
  }

  public static void moveTo(Tile tile, Point p) {
    if (tile == null || p == null) {
      throw new IllegalArgumentException("Tile or new Center Point cannot be null");
    }

    if (canMoveTo(tile, p)) {
      //Logger.trace("Moving " + tile.getId() + " from " + tile.xyToString() + " to (" + p.x + "," + p.y + ")");
      Set<Point> rps = tile.getAltPoints();
      //remove alt pointIds
      for (Point ap : rps) {
        altPointMap.remove(ap);
      }
      //pointIds.remove(tile.getId());
      idMap.remove(tile.id);
      pointMap.remove(tile.getCenter());

      tile.setCenter(p);
      addAndSaveTile(tile);
    } else {
      Tile occ = findTile(p);
      Logger.trace("Can't Move tile " + tile.id + " from " + tile.xyToString() + " to (" + p.x + "," + p.y + ") Is occupied by " + occ.id);
    }
  }

  public static Tile rotateTile(Tile tile) {
    if (!pointMap.containsKey(tile.getCenter())) {
      Logger.warn("Tile " + tile.getId() + " NOT in cache!");
    }

    //Remove the alternative or extra pointIds...
    for (Point ep : tile.getAltPoints()) {
      altPointMap.remove(ep);
    }

    tile.rotate();

    //update
    pointMap.put(tile.getCenter(), tile);
    for (Point ep : tile.getAltPoints()) {
      altPointMap.put(ep, tile);
    }
    idMap.put(tile.id, tile);

    persistTile(tile);
    return tile;
  }

  public static Tile flipHorizontal(Tile tile) {
    return flipTile(tile, true);
  }

  public static Tile flipVertical(Tile tile) {
    return flipTile(tile, false);
  }

  private static Tile flipTile(Tile tile, boolean horizontal) {
    if (!pointMap.containsKey(tile.getCenter())) {
      Logger.warn("Tile " + tile.getId() + " NOT in cache!");
    }

    //Remove the alternative or extra pointIds...
    for (Point ep : tile.getAltPoints()) {
      altPointMap.remove(ep);
    }

    if (horizontal) {
      tile.flipHorizontal();
    } else {
      tile.flipVertical();
    }
    //update
    pointMap.put(tile.getCenter(), tile);
    for (Point ep : tile.getAltPoints()) {
      altPointMap.put(ep, tile);
    }
    idMap.put(tile.id, tile);

    persistTile(tile);
    return tile;
  }

  public static void enqueTileAction(JCSActionEvent jcsEvent) {
    eventsQueue.offer(jcsEvent);
    synchronized (TileCache.actionEventQueueHandler) {
      actionEventQueueHandler.notifyAll();
    }
  }

  public static void repaintTile(Tile tile) {
    repaintTile(tile.id);
  }

  public static void repaintTile(String tileId) {
    if (tileId == null) {
      throw new IllegalArgumentException("TileId cannot be null");
    }

    if (idMap.containsKey(tileId)) {
      Tile tile = idMap.get(tileId);
      tile.repaint();
    } else {
      Logger.warn("Can't find a Tile with Id: " + tileId + " in the cache");
    }
  }

}
