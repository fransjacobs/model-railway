/*
 * Copyright 2023 Frans Jacobs.
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
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import jcs.JCS;
import jcs.commandStation.events.AccessoryEventListener;
import jcs.commandStation.events.SensorEventListener;
import jcs.entities.AccessoryBean;
import jcs.entities.AccessoryBean.AccessoryValue;
import jcs.entities.AccessoryBean.SignalValue;
import jcs.entities.SensorBean;
import jcs.entities.TileBean;
import jcs.entities.TileBean.Direction;
import jcs.entities.TileBean.Orientation;
import static jcs.entities.TileBean.Orientation.EAST;
import static jcs.entities.TileBean.Orientation.SOUTH;
import static jcs.entities.TileBean.Orientation.WEST;
import static jcs.entities.TileBean.TileType.BLOCK;
import static jcs.entities.TileBean.TileType.CROSS;
import static jcs.entities.TileBean.TileType.CROSSING;
import static jcs.entities.TileBean.TileType.CURVED;
import static jcs.entities.TileBean.TileType.END;
import static jcs.entities.TileBean.TileType.SENSOR;
import static jcs.entities.TileBean.TileType.SIGNAL;
import static jcs.entities.TileBean.TileType.STRAIGHT;
import static jcs.entities.TileBean.TileType.STRAIGHT_DIR;
import static jcs.entities.TileBean.TileType.SWITCH;
import jcs.persistence.PersistenceFactory;
import jcs.ui.layout.LayoutCanvas;
import jcs.ui.layout.LayoutUtil;
import jcs.ui.layout.events.TileEvent;
import jcs.ui.layout.events.TileEventListener;
import org.tinylog.Logger;

/**
 * Factory object to create Tiles and cache tiles
 *
 * @author frans
 */
public class TileFactory {

  // Keep the records of the used id sequence number
  private static int straightIdSeq;
  private static int crossingIdSeq;
  private static int curvedIdSeq;
  private static int switchIdSeq;
  private static int crossIdSeq;
  private static int signalIdSeq;
  private static int sensorIdSeq;
  private static int blockIdSeq;
  private static int straightDirectionIdSeq;
  private static int endIdSeq;

  private static final Map<String, TileEventListener> tileEventListeners = new HashMap<>();

  private static boolean drawOutline;
  private static boolean showValues;

  public static final Map<Point, Tile> tiles = new HashMap<>();
  public static final Map<Point, Tile> altTiles = new HashMap<>();

  private TileFactory() {
  }

  private static int nextIdSeq(String id) {
    String idnr = id.substring(3);
    int idSeq = Integer.parseInt(idnr);
    return idSeq;
  }

  private static String nextTileId(TileBean.TileType tileType) {
    switch (tileType) {
      case STRAIGHT -> {
        straightIdSeq++;
        return "st-" + straightIdSeq;
      }
      case CROSSING -> {
        crossingIdSeq++;
        return "cr-" + crossingIdSeq;
      }
      case CURVED -> {
        curvedIdSeq++;
        return "ct-" + curvedIdSeq;
      }
      case SWITCH -> {
        switchIdSeq++;
        return "sw-" + switchIdSeq;
      }
      case CROSS -> {
        crossIdSeq++;
        return "cs-" + crossIdSeq;
      }
      case SIGNAL -> {
        signalIdSeq++;
        return "si-" + signalIdSeq;
      }
      case SENSOR -> {
        sensorIdSeq++;
        return "se-" + sensorIdSeq;
      }
      case BLOCK -> {
        blockIdSeq++;
        return "bk-" + blockIdSeq;
      }
      case STRAIGHT_DIR -> {
        straightDirectionIdSeq++;
        return "sd-" + straightDirectionIdSeq;
      }
      case END -> {
        endIdSeq++;
        return "et-" + endIdSeq;
      }
      default -> {
        Logger.warn("Unknown Tile Type " + tileType);
        return null;
      }
    }
  }

  private static int maxIdSeq(int currentId, int newId) {
    if (currentId < newId) {
      return newId;
    } else {
      return currentId;
    }
  }

  public static Tile createTile(String tileId) {
    TileBean tileBean = JCS.getPersistenceService().getTileBean(tileId);
    return createTile(tileBean);
  }

  public static Tile createTile(TileBean tileBean) {
    return createTile(tileBean, false, false);
  }

  public static Tile createTile(TileBean tileBean, boolean drawOutline, boolean showValues) {
    if (tileBean == null) {
      return null;
    }

    TileBean.TileType tileType = tileBean.getTileType();
    AbstractTile tile = null;
    switch (tileType) {
      case STRAIGHT -> {
        tile = new Straight(tileBean);
        straightIdSeq = maxIdSeq(straightIdSeq, nextIdSeq(tileBean.getId()));
      }
      case CROSSING -> {
        tile = new Crossing(tileBean);
        crossingIdSeq = maxIdSeq(crossingIdSeq, nextIdSeq(tileBean.getId()));
      }
      case CURVED -> {
        tile = new Curved(tileBean);
        curvedIdSeq = maxIdSeq(curvedIdSeq, nextIdSeq(tileBean.getId()));
      }
      case SWITCH -> {
        tile = new Switch(tileBean);
        tile.setAccessoryBean(tileBean.getAccessoryBean());

        switchIdSeq = maxIdSeq(switchIdSeq, nextIdSeq(tileBean.getId()));
        if (showValues && tileBean.getAccessoryBean() != null) {
          ((Switch) tile).setValue((tileBean.getAccessoryBean()).getAccessoryValue());
        }
        JCS.getJcsCommandStation().addAccessoryEventListener((AccessoryEventListener) tile);
      }
      case CROSS -> {
        tile = new Cross(tileBean);
        tile.setAccessoryBean(tileBean.getAccessoryBean());

        crossIdSeq = maxIdSeq(crossIdSeq, nextIdSeq(tileBean.getId()));
        if (showValues && tileBean.getAccessoryBean() != null) {
          ((Switch) tile).setValue((tileBean.getAccessoryBean()).getAccessoryValue());
        }
        JCS.getJcsCommandStation().addAccessoryEventListener((AccessoryEventListener) tile);
      }
      case SIGNAL -> {
        tile = new Signal(tileBean);
        tile.setAccessoryBean(tileBean.getAccessoryBean());

        signalIdSeq = maxIdSeq(signalIdSeq, nextIdSeq(tileBean.getId()));
        if (showValues && tileBean.getAccessoryBean() != null) {
          ((Signal) tile).setSignalValue(((AccessoryBean) tileBean.getAccessoryBean()).getSignalValue());
        }
        JCS.getJcsCommandStation().addAccessoryEventListener((AccessoryEventListener) tile);
      }
      case SENSOR -> {
        tile = new Sensor(tileBean);
        tile.setSensorBean(tileBean.getSensorBean());
        sensorIdSeq = maxIdSeq(sensorIdSeq, nextIdSeq(tileBean.getId()));
        if (showValues && tileBean.getSensorBean() != null) {
          ((Sensor) tile).setActive(((SensorBean) tileBean.getSensorBean()).isActive());
        }
        JCS.getJcsCommandStation().addSensorEventListener((SensorEventListener) tile);
      }
      case BLOCK -> {
        tile = new Block(tileBean);
        tile.setBlockBean(tileBean.getBlockBean());
        blockIdSeq = maxIdSeq(blockIdSeq, nextIdSeq(tileBean.getId()));
      }
      case STRAIGHT_DIR -> {
        tile = new StraightDirection(tileBean);
        straightDirectionIdSeq = maxIdSeq(straightDirectionIdSeq, nextIdSeq(tileBean.getId()));
      }
      case END -> {
        tile = new End(tileBean);
        endIdSeq = maxIdSeq(endIdSeq, nextIdSeq(tileBean.getId()));
      }
      default ->
        Logger.warn("Unknown Tile Type " + tileType);
    }

    if (tile != null) {
      tile.setDrawOutline(drawOutline);
    }

    addTileEventListener((TileEventListener) tile);

    return (Tile) tile;
  }

  /**
   * @param tileType type of type to create
   * @param orientation whether the orientation of the Tile is EAST, WEST, NORTH or SOUTH
   * @param x the tile center X
   * @param y the tile center Y
   * @param drawOutline whether the outline of the tile must be rendered
   * @return a Tile object
   */
  public static Tile createTile(TileBean.TileType tileType, Orientation orientation, int x, int y, boolean drawOutline) {
    return createTile(tileType, orientation, Direction.CENTER, x, y, drawOutline);
  }

  /**
   * @param tileType type of type to create
   * @param orientation whether the orientation of the Tile is EAST, WEST, NORTH or SOUTH
   * @param direction direction plays a role with Turnout tiles whether it goes to the Left or Right
   * @param x the tile center X
   * @param y the tile center Y
   * @param drawOutline whether the outline of the tile must be rendered
   * @return a Tile object
   */
  public static Tile createTile(TileBean.TileType tileType, Orientation orientation, Direction direction, int x, int y, boolean drawOutline) {
    return createTile(tileType, orientation, direction, new Point(x, y), drawOutline);
  }

  public static Tile createTile(TileBean.TileType tileType, Orientation orientation, Direction direction, Point center, boolean drawOutline) {
    Tile tile = null;
    switch (tileType) {
      case STRAIGHT -> {
        tile = new Straight(orientation, center);
      }
      case CROSSING -> {
        tile = new Crossing(orientation, center);
      }
      case CURVED ->
        tile = new Curved(orientation, center);
      case SWITCH ->
        tile = new Switch(orientation, direction, center);
      case CROSS ->
        tile = new Cross(orientation, direction, center);
      case SIGNAL ->
        tile = new Signal(orientation, center);
      case SENSOR ->
        tile = new Sensor(orientation, center);
      case BLOCK ->
        tile = new Block(orientation, center);
      case STRAIGHT_DIR ->
        tile = new StraightDirection(orientation, center);
      case END ->
        tile = new End(orientation, center);
      default ->
        Logger.warn("Unknown Tile Type " + tileType);
    }

    if (tile != null) {
      tile.setDrawOutline(drawOutline);
      tile.setId(nextTileId(tileType));
    }

    addTileEventListener((TileEventListener) tile);
    return (Tile) tile;
  }

  public static List<Tile> toTiles(List<TileBean> tileBeans, boolean drawOutline, boolean showValues) {
    List<Tile> tileList = new LinkedList<>();

    for (TileBean tileBean : tileBeans) {
      Tile tile = createTile(tileBean, drawOutline, showValues);
      tileList.add(tile);
    }
    return tileList;
  }

  private static void addTileEventListener(TileEventListener listener) {
    String key = listener.getId();
    tileEventListeners.put(key, listener);
  }

  public static void removeTileEventListener(Tile tile) {
    if (tile instanceof TileEventListener tileEventListener) {
      removeTileEventListener(tileEventListener);
    }
  }

  public static void removeTileEventListener(TileEventListener listener) {
    String key = listener.getId();
    tileEventListeners.remove(key, listener);
  }

  public static void fireTileEventListener(TileEvent tileEvent) {
    String key = tileEvent.getTileId();
    TileEventListener listener = tileEventListeners.get(key);
    if (listener != null) {
      listener.onTileChange(tileEvent);
    } else {
      //Logger.trace("Tile " + key + " not available");
    }
  }

  public static void fireAllTileEventListeners(TileEvent tileEvent) {
    for (TileEventListener listener : tileEventListeners.values()) {
      listener.onTileChange(tileEvent);
    }
  }

  public static void setPropertyListener(PropertyChangeListener propertyChangeListener) {
    for (Tile tile : tiles.values()) {
      tile.setPropertyChangeListener(propertyChangeListener);
    }
  }

  public static void setDrawOutline(boolean drawOutline) {
    TileFactory.drawOutline = drawOutline;

    for (Tile tile : tiles.values()) {
      tile.setDrawOutline(drawOutline);
    }
  }

  public static void setShowValues(boolean showValues) {
    TileFactory.showValues = showValues;

    for (Tile tile : tiles.values()) {
      TileBean.TileType tileType = tile.getTileType();
      //AbstractTile tile = null;
      switch (tileType) {
        case SWITCH -> {
          if (showValues && ((AbstractTile) tile).getAccessoryBean() != null) {
            ((Switch) tile).setValue((((AbstractTile) tile).getAccessoryBean()).getAccessoryValue());
          } else {
            ((Switch) tile).setValue(AccessoryValue.OFF);
          }
        }
        case CROSS -> {
          if (showValues && ((AbstractTile) tile).getAccessoryBean() != null) {
            ((Switch) tile).setValue((((AbstractTile) tile).getAccessoryBean()).getAccessoryValue());
          } else {
            ((Switch) tile).setValue(AccessoryValue.OFF);
          }
        }
        case SIGNAL -> {
          if (showValues && ((AbstractTile) tile).getAccessoryBean() != null) {
            ((Signal) tile).setSignalValue(((AccessoryBean) ((AbstractTile) tile).getAccessoryBean()).getSignalValue());
          } else {
            ((Signal) tile).setSignalValue(SignalValue.OFF);
          }
        }
        case SENSOR -> {
          if (showValues && ((AbstractTile) tile).getSensorBean() != null) {
            ((Sensor) tile).setActive(((SensorBean) ((AbstractTile) tile).getSensorBean()).isActive());
          } else {
            ((Sensor) tile).setActive(false);
          }
        }
        case BLOCK -> {
        }
      }
    }
  }

  public static void loadTiles() {
    List<TileBean> tileBeans = PersistenceFactory.getService().getTileBeans();

    altTiles.clear();
    tiles.clear();

    for (TileBean tb : tileBeans) {
      Tile tile = createTile(tb, drawOutline, showValues);
      //tile.setPropertyChangeListener(this);
      tiles.put(tile.getCenter(), tile);

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

  public static void addTile(Tile tile) {
    tiles.put(tile.getCenter(), tile);
    //Alternative point(s) to be able to find all points
    if (!tile.getAltPoints().isEmpty()) {
      Set<Point> alt = tile.getAltPoints();
      for (Point ap : alt) {
        altTiles.put(ap, tile);
      }
    }

    if ("false".equals(System.getProperty("batch.tile.persist", "true"))) {
      saveTile(tile);
    }
  }

  public void removeTiles(Set<Point> pointsToRemove) {
    for (Point p : pointsToRemove) {
      Tile removed = tiles.remove(p);

      if ("false".equals(System.getProperty("batch.tile.persist", "true"))) {
        deleteTile(removed);
      }

      if (removed != null && removed.getAllPoints() != null) {
        Set<Point> rps = removed.getAltPoints();
        //Also remove alt points
        for (Point ap : rps) {
          altTiles.remove(ap);
        }

      }
    }
  }

  private void deleteTile(final Tile tile) {
    if (tile != null) {
      TileBean tb = tile.getTileBean();
      PersistenceFactory.getService().remove(tb);
    }
  }

  private static void saveTile(final Tile tile) {
    if (tile != null) {
      TileBean tb = tile.getTileBean();
      PersistenceFactory.getService().persist(tb);
    }
  }

  public static void saveTiles() {
    List<TileBean> beans = new LinkedList<>();

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

  public static boolean checkTileOccupation(Tile tile) {
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

  public static void rotateTile(Set<Point> centerPoints) {
    for (Point p : centerPoints) {
      if (tiles.containsKey(p)) {
        Tile t = tiles.get(p);
        //Remove the alternative or extra points...
        for (Point ep : t.getAltPoints()) {
          altTiles.remove(ep);
        }

        t.rotate();

        //update
        tiles.put(p, t);
        for (Point ep : t.getAltPoints()) {
          altTiles.put(ep, t);
        }

        if ("false".equals(System.getProperty("batch.tile.persist", "true"))) {
          saveTile(t);
        }
      }
    }
  }

  public static void flipHorizontal(Set<Point> points) {
    flipTile(points, true);
  }

  public static void flipVertical(Set<Point> points) {
    flipTile(points, false);
  }

  private static void flipTile(Set<Point> points, boolean horizontal) {
    for (Point p : points) {
      if (tiles.containsKey(p)) {
        Tile t = tiles.get(p);
        //Remove the alternative or extra points...
        for (Point ep : t.getAltPoints()) {
          altTiles.remove(ep);
        }

        if (horizontal) {
          t.flipHorizontal();
        } else {
          t.flipVertical();
        }
        //Update
        tiles.put(p, t);
        for (Point ep : t.getAltPoints()) {
          altTiles.put(ep, t);
        }

        if ("false".equals(System.getProperty("batch.tile.persist", "true"))) {
          saveTile(t);
        }
      }
    }
  }

  public static void moveTile(Point snapPoint, Tile tile) {
    Point tp = tile.getCenter();
    if (!tp.equals(snapPoint)) {
      //Check if new position is free
      boolean canMove = true;
      if (tiles.containsKey(snapPoint) || altTiles.containsKey(snapPoint)) {
        Tile t = findTile(snapPoint);
        if (tile.getId().equals(t.getId())) {
          //same tile so we can move
          canMove = true;
        } else {
          Logger.trace("Position " + snapPoint + " is occupied with tile: " + t + ", can't move tile " + tile.getId());
          canMove = false;
        }

        if (canMove) {
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

            if ("false".equals(System.getProperty("batch.tile.persist", "true"))) {
              saveTile(movingTile);
            }
          }
        }
      }
    }
  }
}
