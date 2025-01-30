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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import jcs.entities.AccessoryBean.AccessoryValue;
import jcs.entities.AccessoryBean.SignalValue;
import jcs.entities.TileBean;
import jcs.entities.TileBean.Orientation;
import static jcs.entities.TileBean.Orientation.EAST;
import static jcs.entities.TileBean.Orientation.SOUTH;
import static jcs.entities.TileBean.Orientation.WEST;
import jcs.entities.TileBean.TileType;
import static jcs.entities.TileBean.TileType.BLOCK;
import static jcs.entities.TileBean.TileType.CROSS;
import static jcs.entities.TileBean.TileType.SENSOR;
import static jcs.entities.TileBean.TileType.SIGNAL;
import jcs.persistence.PersistenceFactory;
import jcs.ui.layout.events.TileEvent;
import jcs.ui.layout.events.TileEventListener;
import org.tinylog.Logger;

/**
 * Factory object to create Tiles and cache tiles
 *
 * @author frans
 */
public class TileCache {

  //private static final Map<String, TileEventListener> tileEventListeners = new HashMap<>();
  private static boolean showValues;

  static final Map<Point, Tile> tiles = new HashMap<>();
  static final Map<Point, Tile> tileAltPoints = new HashMap<>();
  static final Map<String, Point> points = new HashMap<>();
  //static final Map<Point, Tile> altTiles = new HashMap<>();

  private TileCache() {
  }

  @Deprecated
  public static void setShowValues(boolean showValues) {
    TileCache.showValues = showValues;

    for (Tile tile : tiles.values()) {
      TileType tileType = tile.getTileType();
      switch (tileType) {
        case SWITCH -> {
          if (showValues && ((Switch) tile).getTileBean().getAccessoryBean() != null) {
            tile.setAccessoryValue(tile.getTileBean().getAccessoryBean().getAccessoryValue());
          } else {
            tile.setAccessoryValue(AccessoryValue.OFF);
          }
        }
        case CROSS -> {
          if (showValues && ((Cross) tile).getTileBean().getAccessoryBean() != null) {
            tile.setAccessoryValue(tile.getTileBean().getAccessoryBean().getAccessoryValue());
          } else {
            tile.setAccessoryValue(AccessoryValue.OFF);
          }
        }
        case SIGNAL -> {
          if (showValues && ((Signal) tile).getTileBean().getAccessoryBean() != null) {
            tile.setSignalValue(tile.getTileBean().getAccessoryBean().getSignalValue());
          } else {
            tile.setSignalValue(SignalValue.OFF);
          }
        }
        case SENSOR -> {
          if (showValues && ((Sensor) tile).getTileBean().getSensorBean() != null) {
            tile.setActive(tile.getTileBean().getSensorBean().isActive());
          } else {
            tile.setActive(false);
          }
        }
        case BLOCK -> {
        }
      }
    }
  }

  public static List<Tile> loadTiles() {
    tileAltPoints.clear();
    points.clear();
    tiles.clear();

    List<TileBean> tileBeans = PersistenceFactory.getService().getTileBeans();

    for (TileBean tb : tileBeans) {
      Tile tile = TileFactory.createTile(tb, showValues);
      tiles.put(tile.getCenter(), tile);
      points.put(tile.getId(), tile.getCenter());
      //Alternative point(s) to be able to find all points
      if (!tile.getAltPoints().isEmpty()) {
        Set<Point> alt = tile.getAltPoints();
        for (Point ap : alt) {
          tileAltPoints.put(ap, tile);
        }
      }
    }

    Logger.trace("Loaded " + tiles.size() + " Tiles...");
    return tiles.values().stream().collect(Collectors.toList());
  }

  public static List<Tile> getTiles() {
    return tiles.values().stream().collect(Collectors.toList());
  }

  public static Tile addAndSaveTile(Tile tile) {
    tiles.put(tile.getCenter(), tile);
    points.put(tile.getId(), tile.getCenter());

    //Alternative point(s) to be able to find all points
    if (!tile.getAltPoints().isEmpty()) {
      Set<Point> alt = tile.getAltPoints();
      for (Point ap : alt) {
        tileAltPoints.put(ap, tile);
      }
    }

    saveTile(tile);
    //Logger.trace("Added " + tile + " There are now " + tiles.size() + " tiles...");
    return tile;
  }

  public static void saveTile(final Tile tile) {
    if (tile != null) {
      TileBean tb = tile.getTileBean();
      PersistenceFactory.getService().persist(tb);
    } else {
      Logger.warn("Tile is null?");
    }
  }

  public static void saveTiles() {
    for (Tile tile : tiles.values()) {
      saveTile(tile);
    }
  }

  public static void deleteTile(final Tile tile) {
    if (tile != null) {
      if (tiles.containsKey(tile.getCenter())) {
        Set<Point> rps = tile.getAltPoints();
        //Also remove alt points
        for (Point ap : rps) {
          tileAltPoints.remove(ap);
        }
        points.remove(tile.getId());
        tiles.remove(tile.getCenter());

        TileBean tb = tile.getTileBean();
        PersistenceFactory.getService().remove(tb);
        Logger.trace("Deleted " + tile.getId());
      } else {
        Logger.warn("Tile " + tile.getId() + " not found in cache");
      }
    } else {
      Logger.warn("Tile is null?");
    }
  }

  public static Tile findTile(Point cp) {
    Tile result = tiles.get(cp);
    if (result == null) {
      result = tileAltPoints.get(cp);
    }
    return result;
  }

  public static Tile findTile(String id) {
    Point p = points.get(id);
    if (p != null) {
      return findTile(p);
    } else {
      return null;
    }
  }

  public static boolean canMoveTo(Tile tile, Point p) {
    //check if a tile exist with point p
    //Check if the cache contains a Cp of a tile on p
    //Logger.trace("Checking " + tile.id + " New Point (" + p.x + "," + p.y + ")");
    if (tiles.containsKey(p) && !tile.getCenter().equals(p) && !tile.getAltPoints().contains(p)) {
      return false;
    }
    //Check if the cache contains a Cp on any of the Alt point is case of a Block or Cross
    if (tileAltPoints.containsKey(p) && !tile.getAltPoints().contains(p)) {
      return false;
    }

    //Check with the tile on the new Cp with the new alt points if that is also free...
    Set<Point> altPoints = tile.getAltPoints(p);
    for (Point ap : altPoints) {
      if (tiles.containsKey(ap) && !tile.getCenter().equals(ap) && !tile.getAltPoints().contains(ap)) {
        return false;
      }
      if (tileAltPoints.containsKey(ap) && !tile.getAltPoints().contains(ap)) {
        return false;
      }
    }
    //Logger.trace("Checked " + tile.id + " can move to (" + p.x + "," + p.y + ")");
    return true;
  }

  public static void moveTo(Tile tile, Point p) {
    if (canMoveTo(tile, p)) {
      //Logger.trace("Moving " + tile.getId() + " from " + tile.xyToString() + " to (" + p.x + "," + p.y + ")");
      Set<Point> rps = tile.getAltPoints();
      //remove alt points
      for (Point ap : rps) {
        tileAltPoints.remove(ap);
      }
      points.remove(tile.getId());
      tiles.remove(tile.getCenter());

      tile.setCenter(p);
      Tile t = addAndSaveTile(tile);
      //Logger.trace("Moved " + t.id + " to " + t.xyToString());
    } else {
      Tile occ = findTile(p);
      Logger.trace("Can't Move tile " + tile.id + " from " + tile.xyToString() + " to (" + p.x + "," + p.y + ") Is occupied by " + occ.id);
    }
  }

  @Deprecated
  public static boolean checkTileOccupation(Tile tile) {
    Set<Point> tilePoints = tile.getAllPoints();
    for (Point p : tilePoints) {
      if (tiles.containsKey(p) || tileAltPoints.containsKey(p)) {
        //The is a match, check if it is an other tile
        Tile mt = findTile(p);
        if (tile.getId().equals(mt.getId())) {
          //same tile continue
        } else {
          //Other tile so really occupied
          return true;
        }
      }
    }
    return false;
  }

  @Deprecated
  public static boolean containsPoint(Set<Point> points) {
    for (Point p : points) {
      return tiles.containsKey(p) || tileAltPoints.containsKey(p);
    }
    return false;
  }

  public static boolean containsPoint(Point point) {
    return tiles.containsKey(point) || tileAltPoints.containsKey(point);
  }

  @Deprecated
  public static Point checkAvailable(Point newPoint, Orientation orientation) {
    if (tiles.containsKey(newPoint)) {
      Tile et = tiles.get(newPoint);
      Logger.trace("@ " + newPoint + " is allready occcupied by: " + et + "...");
      //Search for the nearest avalaible free point 
      //first get the Center point of the tile which is occuping this slot
      // show warning!
      Point ecp = et.getCenter();

      int w = et.getWidth();
      int h = et.getHeight();

      Point np;
      np = switch (orientation) {
        case EAST ->
          new Point(ecp.x + w, ecp.y);
        case WEST ->
          new Point(newPoint.x - w, ecp.y);
        case SOUTH ->
          new Point(ecp.x, newPoint.y + h);
        default ->
          new Point(ecp.x, newPoint.y - h);
      };

      Logger.trace("Alternative CP: " + np);
      // recursive check
      return checkAvailable(np, orientation);
    } else {
      Logger.trace("@ " + newPoint + " is not yet used...");

      return newPoint;
    }
  }

  public static Tile rotateTile(Tile tile) {
    if (!tiles.containsKey(tile.getCenter())) {
      Logger.warn("Tile " + tile.getId() + " NOT in cache!");
    }

    //Remove the alternative or extra points...
    for (Point ep : tile.getAltPoints()) {
      tileAltPoints.remove(ep);
    }

    tile.rotate();

    //update
    tiles.put(tile.getCenter(), tile);
    for (Point ep : tile.getAltPoints()) {
      tileAltPoints.put(ep, tile);
    }

    saveTile(tile);
    return tile;
  }

  public static Tile flipHorizontal(Tile tile) {
    return flipTile(tile, true);
  }

  public static Tile flipVertical(Tile tile) {
    return flipTile(tile, false);
  }

  private static Tile flipTile(Tile tile, boolean horizontal) {
    if (!tiles.containsKey(tile.getCenter())) {
      Logger.warn("Tile " + tile.getId() + " NOT in cache!");
    }

    //Remove the alternative or extra points...
    for (Point ep : tile.getAltPoints()) {
      tileAltPoints.remove(ep);
    }

    if (horizontal) {
      tile.flipHorizontal();
    } else {
      tile.flipVertical();
    }
    //update
    tiles.put(tile.getCenter(), tile);
    for (Point ep : tile.getAltPoints()) {
      tileAltPoints.put(ep, tile);
    }

    saveTile(tile);
    return tile;
  }

  @Deprecated
  public static void addTileEventListener(TileEventListener listener) {
    String key = listener.getId();
    //tileEventListeners.put(key, listener);
  }

  @Deprecated
  public static void removeTileEventListener(Tile tile) {
    if (tile instanceof TileEventListener tileEventListener) {
      removeTileEventListener(tileEventListener);
    }
  }

  static void removeTileEventListener(TileEventListener listener) {
    String key = listener.getId();
    //tileEventListeners.remove(key, listener);
  }

  @Deprecated
  public static void fireTileEventListener(TileEvent tileEvent) {
//    String key = tileEvent.getTileId();
//    TileEventListener listener = tileEventListeners.get(key);
//    if (listener != null) {
//      listener.onTileChange(tileEvent);
//      Logger.trace("Fire listener on tile " + key);
//    } else {
//      //Logger.trace("Tile " + key + " not available");
//    }
  }

  @Deprecated
  public static void fireAllTileEventListeners(TileEvent tileEvent) {
//    for (TileEventListener listener : tileEventListeners.values()) {
//      listener.onTileChange(tileEvent);
//    }
  }

}
