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
package jcs.ui.layout;

import jcs.ui.layout.tiles.*;
import java.awt.Point;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import jcs.entities.AccessoryBean.AccessoryValue;
import jcs.entities.AccessoryBean.SignalValue;
import jcs.entities.TileBean;
import jcs.entities.TileBean.Orientation;
import static jcs.entities.TileBean.Orientation.EAST;
import static jcs.entities.TileBean.Orientation.SOUTH;
import static jcs.entities.TileBean.Orientation.WEST;
import static jcs.entities.TileBean.TileType.CROSS;
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

  private static final Map<String, TileEventListener> tileEventListeners = new HashMap<>();

  private static boolean drawCenterPoint;
  private static boolean showValues;

  static final Map<Point, Tile> tiles = new HashMap<>();
  static final Map<String, Point> points = new HashMap<>();
  static final Map<Point, Tile> altTiles = new HashMap<>();

  private TileCache() {
  }

//  static void setDrawCenterPoint(boolean drawCenterPoint) {
//    TileCache.drawCenterPoint = drawCenterPoint;
//
//    for (Tile tile : tiles.values()) {
//      //if (tile instanceof AbstractTile abstractTile) {
//      tile.setDrawCenterPoint(drawCenterPoint);
//      //}
//    }
//  }
  public static void setShowValues(boolean showValues) {
    TileCache.showValues = showValues;

    for (Tile tile : tiles.values()) {
      TileBean.TileType tileType = tile.getTileType();
      //AbstractTile tile = null;
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

  static void loadTiles() {
    List<TileBean> tileBeans = PersistenceFactory.getService().getTileBeans();

    tileEventListeners.clear();
    altTiles.clear();
    tiles.clear();

    for (TileBean tb : tileBeans) {
      Tile tile = TileFactory.createTile(tb, showValues);
      //tile.setPropertyChangeListener(listener);
      tiles.put(tile.getCenter(), tile);
      points.put(tile.getId(), tile.getCenter());

      //addTileEventListener((TileEventListener) tile);
      //Alternative point(s) to be able to find all points
      if (!tile.getAltPoints().isEmpty()) {
        Set<Point> alt = tile.getAltPoints();
        for (Point ap : alt) {
          altTiles.put(ap, tile);
        }
      }
    }

    Logger.trace("Loaded " + tiles.size() + " Tiles...");
  }

  static List<Tile> getTiles() {
    return tiles.values().stream().collect(Collectors.toList());
  }

  static void addAndSaveTile(Tile tile) {
    tiles.put(tile.getCenter(), tile);
    points.put(tile.getId(), tile.getCenter());

    //addTileEventListener((TileEventListener) tile);
    //Alternative point(s) to be able to find all points
    if (!tile.getAltPoints().isEmpty()) {
      Set<Point> alt = tile.getAltPoints();
      for (Point ap : alt) {
        altTiles.put(ap, tile);
      }
    }

    saveTile(tile);
    Logger.trace("Added " + tile + " There are now " + TileCache.tiles.size() + " tiles...");
  }

  static void deleteTile(final Tile tile) {
    if (tile != null) {
      if (tiles.containsKey(tile.getCenter())) {
        tiles.remove(tile.getCenter());
        points.remove(tile.getId());
        Set<Point> rps = tile.getAltPoints();
        //Also remove alt points
        for (Point ap : rps) {
          altTiles.remove(ap);
        }

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

  static void saveTile(final Tile tile) {
    if (tile != null) {
      TileBean tb = tile.getTileBean();
      PersistenceFactory.getService().persist(tb);
    } else {
      Logger.warn("Tile is null?");
    }
  }

  static void saveTiles() {
    for (Tile tile : tiles.values()) {
      saveTile(tile);
    }
  }

  public static Tile findTile(Point cp) {
    Tile result = tiles.get(cp);
    if (result == null) {
      result = altTiles.get(cp);
      if (result != null) {
      }
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

  static boolean checkTileOccupation(Tile tile) {
    Set<Point> tilePoints = tile.getAllPoints();
    for (Point p : tilePoints) {
      if (tiles.containsKey(p) || altTiles.containsKey(p)) {
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

  static boolean containsPoints(Set<Point> points) {
    for (Point p : points) {
      return tiles.containsKey(p) || altTiles.containsKey(p);
    }
    return false;

  }

  static boolean containsPoint(Point point) {
    return tiles.containsKey(point) || altTiles.containsKey(point);
  }

  static Point checkAvailable(Point newPoint, Orientation orientation) {
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

  static Tile rotateTile(Tile tile) {
    if (!tiles.containsKey(tile.getCenter())) {
      Logger.warn("Tile " + tile.getId() + " NOT in cache!");
    }

    //Remove the alternative or extra points...
    for (Point ep : tile.getAltPoints()) {
      altTiles.remove(ep);
    }

    tile.rotate();

    //update
    tiles.put(tile.getCenter(), tile);
    for (Point ep : tile.getAltPoints()) {
      altTiles.put(ep, tile);
    }

    saveTile(tile);
    return tile;
  }

  static Tile flipHorizontal(Tile tile) {
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
      altTiles.remove(ep);
    }

    if (horizontal) {
      tile.flipHorizontal();
    } else {
      tile.flipVertical();
    }
    //update
    tiles.put(tile.getCenter(), tile);
    for (Point ep : tile.getAltPoints()) {
      altTiles.put(ep, tile);
    }

    saveTile(tile);
    return tile;
  }

  static void moveTile(Point snapPoint, Tile tile) {
    Point tp = tile.getCenter();
    if (!tp.equals(snapPoint)) {
      //Check if new position is free
      boolean canMove = !TileCache.containsPoint(snapPoint);

      if (canMove) {
        Logger.trace("Moving from tile " + tile.getId() + " from " + tile.xyToString() + " to (" + snapPoint.x + "," + snapPoint.y + ")");
        //Remove the original tile center from the tiles
        Tile movingTile = tiles.remove(tp);
        if (movingTile != null) {
          //Also remove from the alt points
          Point oldCenter = movingTile.getCenter();
          Set<Point> oldAltPoints = movingTile.getAltPoints();
          //Logger.trace("Removing " + oldAltPoints.size() + " alt tile points");
          for (Point ep : oldAltPoints) {
            altTiles.remove(ep);
            tiles.remove(ep);
          }

          //Set the new center position
          movingTile.setCenter(snapPoint);
          //Check again, needed for tiles which are longer then 1 square, like a block
          if (!checkTileOccupation(movingTile)) {
            Logger.trace("Moved Tile " + movingTile.getId() + " from " + tp + " to " + snapPoint + "...");
            tiles.put(snapPoint, movingTile);
            for (Point ep : movingTile.getAltPoints()) {
              altTiles.put(ep, movingTile);
            }
          } else {
            //Do not move Tile, put back where it was
            movingTile.setCenter(oldCenter);
            tiles.put(oldCenter, movingTile);
            for (Point ep : movingTile.getAltPoints()) {
              altTiles.put(ep, movingTile);
            }
          }

          saveTile(movingTile);
        }
      }
      //}
    }
  }

  static void addTileEventListener(TileEventListener listener) {
    String key = listener.getId();
    tileEventListeners.put(key, listener);
  }

  static void removeTileEventListener(Tile tile) {
    if (tile instanceof TileEventListener tileEventListener) {
      removeTileEventListener(tileEventListener);
    }
  }

  static void removeTileEventListener(TileEventListener listener) {
    String key = listener.getId();
    tileEventListeners.remove(key, listener);
  }

  public static void fireTileEventListener(TileEvent tileEvent) {
    String key = tileEvent.getTileId();
    TileEventListener listener = tileEventListeners.get(key);
    if (listener != null) {
      listener.onTileChange(tileEvent);
      Logger.trace("Fire listener on tile " + key);
    } else {
      //Logger.trace("Tile " + key + " not available");
    }
  }

  static void fireAllTileEventListeners(TileEvent tileEvent) {
    for (TileEventListener listener : tileEventListeners.values()) {
      listener.onTileChange(tileEvent);
    }
  }

}
