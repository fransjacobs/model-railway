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

import java.awt.Dimension;
import java.awt.Point;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;
import jcs.JCS;
import jcs.commandStation.events.AccessoryEvent;
import jcs.commandStation.events.AccessoryEventListener;
import jcs.entities.TileBean;
import jcs.persistence.PersistenceFactory;
import org.tinylog.Logger;
import jcs.commandStation.events.SensorEventListener;
import jcs.entities.AccessoryBean;
import jcs.entities.SensorBean;
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
import jcs.commandStation.events.SensorEvent;
import jcs.entities.BlockBean;
import static jcs.ui.layout.tiles.Tile.GRID;

/**
 * Factory object to create Tiles and cache pointMap
 *
 * @author frans
 */
public class TileCache {

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

  static final Map<String, Tile> idMap = new HashMap<>();
  static final Map<Point, Tile> pointMap = new HashMap<>();
  static final Map<Point, Tile> altPointMap = new HashMap<>();

  private static final ConcurrentLinkedQueue<JCSActionEvent> eventsQueue = new ConcurrentLinkedQueue();
  private static final TileActionEventHandler actionEventQueueHandler = new TileActionEventHandler(eventsQueue);

  private static int maxX;
  private static int maxY;

  static {
    actionEventQueueHandler.start();
  }

  private TileCache() {
  }

  public static int getIdSeq(String id) {
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

  public static Tile createTile(TileBean tileBean, boolean showValues) {
    if (tileBean == null) {
      return null;
    }

    TileBean.TileType tileType = tileBean.getTileType();
    Tile tile = null;
    switch (tileType) {
      case STRAIGHT -> {
        tile = new Straight(tileBean);
        straightIdSeq = maxIdSeq(straightIdSeq, getIdSeq(tileBean.getId()));
      }
      case CROSSING -> {
        tile = new Crossing(tileBean);
        crossingIdSeq = maxIdSeq(crossingIdSeq, getIdSeq(tileBean.getId()));
      }
      case CURVED -> {
        tile = new Curved(tileBean);
        curvedIdSeq = maxIdSeq(curvedIdSeq, getIdSeq(tileBean.getId()));
      }
      case SWITCH -> {
        tile = new Switch(tileBean);
        tile.setAccessoryBean(tileBean.getAccessoryBean());

        switchIdSeq = maxIdSeq(switchIdSeq, getIdSeq(tileBean.getId()));
        if (showValues && tileBean.getAccessoryBean() != null) {
          tile.setAccessoryValue((tileBean.getAccessoryBean()).getAccessoryValue());
        }
        if (tileBean.getAccessoryBean() != null && tileBean.getAccessoryBean().getId() != null) {
          JCS.getJcsCommandStation().addAccessoryEventListener(tileBean.getAccessoryBean().getId(), (AccessoryEventListener) tile);
        } else {
          Logger.trace("Can't add tile " + tile.getId() + " as an AccessorListener as the AccessoryId is null...");
        }
      }
      case CROSS -> {
        tile = new Cross(tileBean);
        tile.setAccessoryBean(tileBean.getAccessoryBean());

        crossIdSeq = maxIdSeq(crossIdSeq, getIdSeq(tileBean.getId()));
        if (showValues && tileBean.getAccessoryBean() != null) {
          tile.setAccessoryValue((tileBean.getAccessoryBean()).getAccessoryValue());
        }

        if (tileBean.getAccessoryBean() != null && tileBean.getAccessoryBean().getId() != null) {
          JCS.getJcsCommandStation().addAccessoryEventListener(tileBean.getAccessoryBean().getId(), (AccessoryEventListener) tile);
        } else {
          Logger.trace("Can't add tile " + tile.getId() + " as an AccessorListener as the AccessoryId is null...");
        }
      }
      case SIGNAL -> {
        tile = new Signal(tileBean);
        tile.setAccessoryBean(tileBean.getAccessoryBean());

        signalIdSeq = maxIdSeq(signalIdSeq, getIdSeq(tileBean.getId()));
        if (showValues && tileBean.getAccessoryBean() != null) {
          ((Signal) tile).setSignalValue(((AccessoryBean) tileBean.getAccessoryBean()).getSignalValue());
        }
        if (tileBean.getAccessoryBean() != null && tileBean.getAccessoryBean().getId() != null) {
          JCS.getJcsCommandStation().addAccessoryEventListener(tileBean.getAccessoryBean().getId(), (AccessoryEventListener) tile);
        } else {
          Logger.trace("Can't add tile " + tile.getId() + " as an AccessorListener as the AccessoryId is null...");
        }
      }
      case SENSOR -> {
        tile = new Sensor(tileBean);
        tile.setSensorBean(tileBean.getSensorBean());
        sensorIdSeq = maxIdSeq(sensorIdSeq, getIdSeq(tileBean.getId()));

        if (showValues && tileBean.getSensorBean() != null) {
          ((Sensor) tile).setActive(((SensorBean) tileBean.getSensorBean()).isActive());
        }
        if (((Sensor) tile).getSensorBean() != null && ((Sensor) tile).getSensorBean().getId() != null) {
          JCS.getJcsCommandStation().addSensorEventListener(((Sensor) tile).getSensorBean().getId(), (SensorEventListener) tile);
        } else {
          Logger.warn("Can't register Sensor " + tile.getId() + " no Sensor ID available");
        }
      }
      case BLOCK -> {
        tile = new Block(tileBean);
        tile.setBlockBean(tileBean.getBlockBean());
        blockIdSeq = maxIdSeq(blockIdSeq, getIdSeq(tileBean.getId()));
      }
      case STRAIGHT_DIR -> {
        tile = new StraightDirection(tileBean);
        straightDirectionIdSeq = maxIdSeq(straightDirectionIdSeq, getIdSeq(tileBean.getId()));
      }
      case END -> {
        tile = new End(tileBean);
        endIdSeq = maxIdSeq(endIdSeq, getIdSeq(tileBean.getId()));
      }
      default ->
        Logger.warn("Unknown Tile Type " + tileType);
    }

    return (Tile) tile;
  }

  /**
   * @param tileType type of type to create
   * @param orientation whether the orientation of the Tile is EAST, WEST, NORTH or SOUTH
   * @param x the tile center X
   * @param y the tile center Y
   * @return a Tile object
   */
  public static Tile createTile(TileBean.TileType tileType, TileBean.Orientation orientation, int x, int y) {
    return createTile(tileType, orientation, TileBean.Direction.CENTER, x, y);
  }

  /**
   * @param tileType type of type to create
   * @param orientation whether the orientation of the Tile is EAST, WEST, NORTH or SOUTH
   * @param direction direction plays a role with Turnout tiles whether it goes to the Left or Right
   * @param x the tile center X
   * @param y the tile center Y
   * @return a Tile object
   */
  public static Tile createTile(TileBean.TileType tileType, TileBean.Orientation orientation, TileBean.Direction direction, int x, int y) {
    return createTile(tileType, orientation, direction, new Point(x, y));
  }

  public static Tile createTile(TileBean.TileType tileType, TileBean.Orientation orientation, TileBean.Direction direction, Point center) {
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
      tile.setId(nextTileId(tileType));
    }

    return (Tile) tile;
  }

  public static void rollback(Tile tile) {
    switch (tile.tileType) {
      case STRAIGHT -> {
        straightIdSeq--;
      }
      case CROSSING -> {
        crossingIdSeq--;
      }
      case CURVED -> {
        curvedIdSeq--;
      }
      case SWITCH -> {
        switchIdSeq--;
      }
      case CROSS -> {
        crossIdSeq--;
      }
      case SIGNAL -> {
        signalIdSeq--;
      }
      case SENSOR -> {
        sensorIdSeq--;
      }
      case BLOCK -> {
        blockIdSeq--;
      }
      case STRAIGHT_DIR -> {
        straightDirectionIdSeq--;
      }
      case END -> {
        endIdSeq--;
      }
    }
  }

  public static List<Tile> loadTiles() {
    return loadTiles(false);
  }

  public static List<Tile> loadTiles(boolean showvalues) {
    altPointMap.clear();
    pointMap.clear();
    idMap.clear();
    maxX = 0;
    maxY = 0;

    List<TileBean> tileBeans = PersistenceFactory.getService().getTileBeans();

    for (TileBean tb : tileBeans) {
      Tile tile = createTile(tb, showvalues);
      calculateMaxCoordinates(tile.tileX, tile.tileY);
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

    Logger.trace("Loaded " + idMap.size() + " Tiles. Max: (" + maxX + "," + maxY + ")");
    return idMap.values().stream().collect(Collectors.toList());
  }

  public static List<Tile> getTiles() {
    return idMap.values().stream().collect(Collectors.toList());
  }

  static void calculateMaxCoordinates(int tileX, int tileY) {
    if (maxX < tileX) {
      maxX = tileX;
    }
    if (maxY < tileY) {
      maxY = tileY;
    }
  }

  public static Dimension getMinCanvasSize() {
    int w = maxX + GRID;
    int h = maxY + GRID;
    return new Dimension(w, h);
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

  public static void persistBlock(final BlockBean block) {
    if (block == null) {
      throw new IllegalArgumentException("block cannot be null");
    }
    Tile tile = idMap.get(block.getTileId());
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

  public static boolean contains(Point p) {
    return pointMap.containsKey(p);
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

//  public static void enqueTileAction(JCSActionEvent jcsEvent) {
//    eventsQueue.offer(jcsEvent);
//    synchronized (TileCache.actionEventQueueHandler) {
//      actionEventQueueHandler.notifyAll();
//    }
//  }
  public static void enqueTileAction(AccessoryEvent accessoryEvent) {
    eventsQueue.offer(new ActionEventWrapper(accessoryEvent));
    synchronized (TileCache.actionEventQueueHandler) {
      actionEventQueueHandler.notifyAll();
    }
  }

  public static void enqueTileAction(SensorEvent sensorEvent) {
    eventsQueue.offer(new ActionEventWrapper(sensorEvent));
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

  private static class ActionEventWrapper implements JCSActionEvent {

    private final Object eventObject;

    ActionEventWrapper(Object eventObject) {
      this.eventObject = eventObject;
    }

    @Override
    public Object getEventObject() {
      return eventObject;
    }

    @Override
    public String getIdString() {
      switch (eventObject) {
        case SensorEvent sensorEvent -> {
          return sensorEvent.getSensorId().toString();
        }
        case AccessoryEvent accessoryEvent -> {
          return accessoryEvent.getAccessoryBean().getId();
        }
        default -> {
          return null;
        }
      }
    }

  }
}
