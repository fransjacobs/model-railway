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
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.JComponent;
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
import static jcs.entities.TileBean.TileType.CROSS_SWITCH;
import jcs.persistence.PersistenceService;
import static jcs.ui.layout.tiles.LayoutScale.GRID;

/**
 * Factory object to create Tiles and cache pointMap
 *
 */
public class TileCache {

  // Keep the records of the used id sequence number
  private static final AtomicInteger straightIdSeq = new AtomicInteger(0);
  private static final AtomicInteger crossingIdSeq = new AtomicInteger(0);
  private static final AtomicInteger crossIdSeq = new AtomicInteger(0);
  private static final AtomicInteger curvedIdSeq = new AtomicInteger(0);
  private static final AtomicInteger switchIdSeq = new AtomicInteger(0);
  private static final AtomicInteger threeWaySwitchIdSeq = new AtomicInteger(0);
  private static final AtomicInteger crossSwitchIdSeq = new AtomicInteger(0);
  private static final AtomicInteger signalIdSeq = new AtomicInteger(0);
  private static final AtomicInteger sensorIdSeq = new AtomicInteger(0);
  private static final AtomicInteger blockIdSeq = new AtomicInteger(0);
  private static final AtomicInteger straightDirectionIdSeq = new AtomicInteger(0);
  private static final AtomicInteger endIdSeq = new AtomicInteger(0);

  static final Map<String, Tile> idMap = new ConcurrentHashMap<>();
  static final Map<Point, Tile> centerPointMap = new ConcurrentHashMap<>();
  static final Map<Point, Tile> altPointMap = new ConcurrentHashMap<>();

  private static final BlockingQueue<JCSActionEvent> eventsQueue = new LinkedBlockingQueue<>();
  private static final TileActionEventHandler actionEventQueueHandler = new TileActionEventHandler(eventsQueue);

  private static final AtomicInteger maxX = new AtomicInteger(0);
  private static final AtomicInteger maxY = new AtomicInteger(0);

  private static final PersistenceService persistenceService;

  static {
    persistenceService = PersistenceFactory.getService();
    actionEventQueueHandler.start();
  }

  private TileCache() {
  }

  public static int getIdSeq(String id) {
    if (id == null || id.length() < 4 || id.charAt(2) != '-') {
      throw new IllegalArgumentException("Invalid tile ID format: " + id);
    }
    try {
      return Integer.parseInt(id.substring(3));
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("Invalid tile ID format: " + id, e);
    }
  }

  private static String nextTileId(TileBean.TileType tileType) {
    switch (tileType) {
      case STRAIGHT -> {
        straightIdSeq.incrementAndGet();
        return "st-" + straightIdSeq.get();
      }
      case CROSSING -> {
        crossingIdSeq.incrementAndGet();
        return "cr-" + crossingIdSeq.get();
      }
      case CROSS -> {
        crossIdSeq.incrementAndGet();
        return "cx-" + crossIdSeq.get();
      }
      case CURVED -> {
        curvedIdSeq.incrementAndGet();
        return "ct-" + curvedIdSeq.get();
      }
      case SWITCH -> {
        switchIdSeq.incrementAndGet();
        return "sw-" + switchIdSeq.get();
      }
      case THREEWAY -> {
        threeWaySwitchIdSeq.incrementAndGet();
        return "tw-" + threeWaySwitchIdSeq.get();
      }
      case CROSS_SWITCH -> {
        crossSwitchIdSeq.incrementAndGet();
        return "cs-" + crossSwitchIdSeq.get();
      }
      case SIGNAL -> {
        signalIdSeq.incrementAndGet();
        return "si-" + signalIdSeq.get();
      }
      case SENSOR -> {
        sensorIdSeq.incrementAndGet();
        return "se-" + sensorIdSeq.get();
      }
      case BLOCK -> {
        blockIdSeq.incrementAndGet();
        return "bk-" + blockIdSeq.get();
      }
      case STRAIGHT_DIR -> {
        straightDirectionIdSeq.incrementAndGet();
        return "sd-" + straightDirectionIdSeq.get();
      }
      case END -> {
        endIdSeq.incrementAndGet();
        return "et-" + endIdSeq.get();
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
      throw new IllegalArgumentException("TileBean cannot be null");
    }

    TileBean.TileType tileType = tileBean.getTileType();
    Tile tile = null;

    switch (tileType) {
      case STRAIGHT -> {
        tile = new Straight(tileBean);
        straightIdSeq.set(maxIdSeq(straightIdSeq.get(), getIdSeq(tileBean.getId())));
      }
      case CROSSING -> {
        tile = new Crossing(tileBean);
        crossingIdSeq.set(maxIdSeq(crossingIdSeq.get(), getIdSeq(tileBean.getId())));
      }
      case CROSS -> {
        tile = new Cross(tileBean);
        crossIdSeq.set(maxIdSeq(crossIdSeq.get(), getIdSeq(tileBean.getId())));
      }
      case CURVED -> {
        tile = new Curved(tileBean);
        curvedIdSeq.set(maxIdSeq(curvedIdSeq.get(), getIdSeq(tileBean.getId())));
      }
      case SWITCH -> {
        tile = new Switch(tileBean);
        tile.setAccessoryBean(tileBean.getAccessoryBean());

        switchIdSeq.set(maxIdSeq(switchIdSeq.get(), getIdSeq(tileBean.getId())));
        if (showValues && tileBean.getAccessoryBean() != null) {
          tile.setAccessoryValue((tileBean.getAccessoryBean()).getAccessoryValue());
        }
        if (tileBean.getAccessoryBean() != null && tileBean.getAccessoryBean().getId() != null) {
          JCS.getJcsCommandStation().addAccessoryEventListener(tileBean.getAccessoryBean().getId(), (AccessoryEventListener) tile);
        } else {
          Logger.trace("Can't add tile {} as an AccessorListener as the AccessoryId is null...", tile.getId());
        }
      }
      case THREEWAY -> {
        tile = new ThreeWaySwitch(tileBean);
        tile.setAccessoryBean(tileBean.getAccessoryBean());

        threeWaySwitchIdSeq.set(maxIdSeq(threeWaySwitchIdSeq.get(), getIdSeq(tileBean.getId())));
        if (showValues && tileBean.getAccessoryBean() != null) {
          tile.setAccessoryValue((tileBean.getAccessoryBean()).getAccessoryValue());
        }
        if (tileBean.getAccessoryBean() != null && tileBean.getAccessoryBean().getId() != null) {
          JCS.getJcsCommandStation().addAccessoryEventListener(tileBean.getAccessoryBean().getId(), (AccessoryEventListener) tile);
        } else {
          Logger.trace("Can't add tile " + tile.getId() + " as an AccessorListener as the AccessoryId is null...");
        }
      }
      case CROSS_SWITCH -> {
        tile = new CrossSwitch(tileBean);
        tile.setAccessoryBean(tileBean.getAccessoryBean());

        crossSwitchIdSeq.set(maxIdSeq(crossSwitchIdSeq.get(), getIdSeq(tileBean.getId())));
        if (showValues && tileBean.getAccessoryBean() != null) {
          tile.setAccessoryValue((tileBean.getAccessoryBean()).getAccessoryValue());
        }

        if (tileBean.getAccessoryBean() != null && tileBean.getAccessoryBean().getId() != null) {
          JCS.getJcsCommandStation().addAccessoryEventListener(tileBean.getAccessoryBean().getId(), (AccessoryEventListener) tile);
        } else {
          Logger.trace("Can't add tile {} as an AccessorListener as the AccessoryId is null...", tile.getId());
        }
      }
      case SIGNAL -> {
        tile = new Signal(tileBean);
        tile.setAccessoryBean(tileBean.getAccessoryBean());

        signalIdSeq.set(maxIdSeq(signalIdSeq.get(), getIdSeq(tileBean.getId())));
        if (showValues && tileBean.getAccessoryBean() != null) {
          ((Signal) tile).setSignalValue(((AccessoryBean) tileBean.getAccessoryBean()).getSignalValue());
        }
        if (tileBean.getAccessoryBean() != null && tileBean.getAccessoryBean().getId() != null) {
          JCS.getJcsCommandStation().addAccessoryEventListener(tileBean.getAccessoryBean().getId(), (AccessoryEventListener) tile);
        } else {
          Logger.trace("Can't add tile {} as an AccessorListener as the AccessoryId is null...", tile.getId());
        }
      }
      case SENSOR -> {
        tile = new Sensor(tileBean);
        tile.setSensorBean(tileBean.getSensorBean());
        sensorIdSeq.set(maxIdSeq(sensorIdSeq.get(), getIdSeq(tileBean.getId())));

        if (showValues && tileBean.getSensorBean() != null) {
          ((Sensor) tile).setActive(((SensorBean) tileBean.getSensorBean()).isActive());
        }
        if (((Sensor) tile).getSensorBean() != null && ((Sensor) tile).getSensorBean().getId() != null) {
          JCS.getJcsCommandStation().addSensorEventListener(((Sensor) tile).getSensorBean().getId(), (SensorEventListener) tile);
        } else {
          Logger.trace("Can't register Sensor {} no Sensor ID available!", tile.getId());
        }
      }
      case BLOCK -> {
        tile = new Block(tileBean);
        tile.setBlockBean(tileBean.getBlockBean());
        blockIdSeq.set(maxIdSeq(blockIdSeq.get(), getIdSeq(tileBean.getId())));
      }
      case STRAIGHT_DIR -> {
        tile = new StraightDirection(tileBean);
        straightDirectionIdSeq.set(maxIdSeq(straightDirectionIdSeq.get(), getIdSeq(tileBean.getId())));
      }
      case END -> {
        tile = new End(tileBean);
        endIdSeq.set(maxIdSeq(endIdSeq.get(), getIdSeq(tileBean.getId())));
      }
      default ->
        Logger.warn("Unknown Tile Type {}", tileType);
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

  public static Tile createTile(TileBean.TileType tileType, TileBean.Orientation orientation, TileBean.Direction direction, Point cp) {
    Point center = cp;
    Tile tile = null;
    switch (tileType) {
      case STRAIGHT ->
        tile = new Straight(orientation, center);
      case CROSSING ->
        tile = new Crossing(orientation, center);
      case CROSS ->
        tile = new Cross(orientation, center);
      case CURVED ->
        tile = new Curved(orientation, center);
      case SWITCH ->
        tile = new Switch(orientation, direction, center);
      case THREEWAY ->
        tile = new ThreeWaySwitch(orientation, direction, center);
      case CROSS_SWITCH ->
        tile = new CrossSwitch(orientation, direction, center);
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
        Logger.warn("Unknown Tile Type {}", tileType);
    }

    if (tile != null) {
      tile.setId(nextTileId(tileType));
    }

    return (Tile) tile;
  }

  public static void rollback(Tile tile) {
    switch (tile.tileType) {
      case STRAIGHT -> {
        straightIdSeq.decrementAndGet();
      }
      case CROSSING -> {
        crossingIdSeq.decrementAndGet();
      }
      case CROSS -> {
        crossIdSeq.decrementAndGet();
      }
      case CURVED -> {
        curvedIdSeq.decrementAndGet();
      }
      case SWITCH -> {
        switchIdSeq.decrementAndGet();
      }
      case THREEWAY -> {
        threeWaySwitchIdSeq.decrementAndGet();
      }
      case CROSS_SWITCH -> {
        crossSwitchIdSeq.decrementAndGet();
      }
      case SIGNAL -> {
        signalIdSeq.decrementAndGet();
      }
      case SENSOR -> {
        sensorIdSeq.decrementAndGet();
      }
      case BLOCK -> {
        blockIdSeq.decrementAndGet();
      }
      case STRAIGHT_DIR -> {
        straightDirectionIdSeq.decrementAndGet();
      }
      case END -> {
        endIdSeq.decrementAndGet();
      }
    }
  }

  /**
   * *
   * Empties, flushes the whole Cache and all id's
   */
  public static void flush() {
    deRegisterListeners();
    idMap.clear();
    centerPointMap.clear();
    altPointMap.clear();
    maxX.set(0);
    maxY.set(0);

    straightIdSeq.set(0);
    crossingIdSeq.set(0);
    curvedIdSeq.set(0);
    switchIdSeq.set(0);
    threeWaySwitchIdSeq.set(0);
    crossIdSeq.set(0);
    crossSwitchIdSeq.set(0);
    signalIdSeq.set(0);
    sensorIdSeq.set(0);
    blockIdSeq.set(0);
    straightDirectionIdSeq.set(0);
    endIdSeq.set(0);
  }

  public static List<Tile> loadTiles() {
    return loadTiles(false);
  }

  public static List<Tile> getTiles() {
    return new ArrayList<>(idMap.values());
  }

  static void deRegisterListeners() {
    for (Tile tile : idMap.values()) {
      if (tile instanceof AccessoryEventListener && tile.getTileBean() != null && tile.getTileBean().getAccessoryId() != null) {
        JCS.getJcsCommandStation().removeAccessoryEventListener(tile.getTileBean().getAccessoryId(), (AccessoryEventListener) tile);
      }

      if (tile instanceof SensorEventListener && tile.getTileBean() != null && tile.getTileBean().getSensorId() != null) {
        JCS.getJcsCommandStation().removeSensorEventListener(tile.getTileBean().getSensorId(), (SensorEventListener) tile);
      }
    }
  }

  public static List<Tile> loadTiles(boolean showvalues) {
    long now = System.currentTimeMillis();
    long start = now;
    deRegisterListeners();
    altPointMap.clear();
    centerPointMap.clear();
    idMap.clear();
    maxX.set(0);
    maxY.set(0);

    List<TileBean> tileBeans = persistenceService.getTileBeans();

    long end = System.currentTimeMillis();
    long start2 = end;

    Logger.info("Loaded {} tileBeans from database in {} ms.", tileBeans.size(), (end - start));

    BufferedImage bi = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
    Graphics2D g2 = bi.createGraphics();

    for (TileBean tb : tileBeans) {
      Tile tile = createTile(tb, showvalues);
      tile.paint(g2);
      addTile(tile);
    }

    end = System.currentTimeMillis();

    Logger.info("Created {} Tiles. Max: ({}, {}) in {} ms Total {} ms.", idMap.size(), maxX, maxX, (end - start2), (end - start));
    return new ArrayList<>(idMap.values());
  }

  static void addTile(Tile tile) {
    if (tile == null) {
      return;
    }
    calculateMaxCoordinates(tile.tileX, tile.tileY);
    idMap.put(tile.id, tile);
    centerPointMap.put(tile.getCenter(), tile);

    //Alternative point(s) to be able to find all pointIds
    if (!tile.getAltPoints().isEmpty()) {
      Set<Point> alt = tile.getAltPoints();
      for (Point ap : alt) {
        altPointMap.put(ap, tile);
      }
    }
  }

  static void calculateMaxCoordinates(int tileX, int tileY) {
    if (maxX.get() < tileX) {
      maxX.set(tileX);
    }
    if (maxY.get() < tileY) {
      maxY.set(tileY);
    }
  }

  public static Dimension getMinCanvasSize() {
    int w = maxX.get() + GRID;
    int h = maxY.get() + GRID;
    return new Dimension(w, h);
  }

  public static Tile addAndSaveTile(Tile tile) {
    if (tile == null) {
      throw new IllegalArgumentException("Tile cannot be null");
    }

    persistTile(tile);

    centerPointMap.put(tile.getCenter(), tile);
    idMap.put(tile.getId(), tile);

    //Alternative point(s) to be able to find all pointIds
    if (!tile.getAltPoints().isEmpty()) {
      Set<Point> alt = tile.getAltPoints();
      for (Point ap : alt) {
        altPointMap.put(ap, tile);
      }
    }

    //Logger.trace("Added " + tile + " There are now " + pointMap.size() + " pointMap...");
    return tile;
  }

  public static void persistTile(final Tile tile) {
    try {
      if (tile == null) {
        throw new IllegalArgumentException("Tile cannot be null");
      }
      TileBean tb = tile.getTileBean();
      PersistenceFactory.getService().persist(tb);
    } catch (IllegalArgumentException e) {
      Logger.error("Can't persist Tile! {}", e.getMessage());
    }
  }

  public static void persistBlock(final BlockBean block) {
    try {
      if (block == null) {
        throw new IllegalArgumentException("block cannot be null");
      }
      Tile tile = idMap.get(block.getTileId());
      TileBean tb = tile.getTileBean();
      PersistenceFactory.getService().persist(tb);
    } catch (IllegalArgumentException e) {
      Logger.error("Can't persist Block! {}", e.getMessage());
    }
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

    if (tile instanceof AccessoryEventListener && tile.getTileBean() != null && tile.getTileBean().getAccessoryId() != null) {
      JCS.getJcsCommandStation().removeAccessoryEventListener(tile.getTileBean().getAccessoryId(), (AccessoryEventListener) tile);
    }

    if (tile instanceof SensorEventListener && tile.getTileBean() != null && tile.getTileBean().getSensorId() != null) {
      JCS.getJcsCommandStation().removeSensorEventListener(tile.getTileBean().getSensorId(), (SensorEventListener) tile);
    }

    if (idMap.containsKey(tile.id)) {
      Set<Point> rps = tile.getAltPoints();
      //Also remove alt pointIds
      for (Point ap : rps) {
        altPointMap.remove(ap);
      }
      centerPointMap.remove(tile.getCenter());
      idMap.remove(tile.id);
      TileBean tb = tile.getTileBean();
      PersistenceFactory.getService().remove(tb);
      Logger.trace("Deleted " + tile.getId());
    } else {
      Logger.warn("Tile {} not found in cache", tile.getId());
    }
  }

  public static Tile findTile(Point cp) {
    Tile result = centerPointMap.get(cp);
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
    return centerPointMap.containsKey(p);
  }

  public static boolean canMoveTo(Tile tile, Point p) {
    boolean free = true;
    //check if a tile exist with point p
    //Check if the cache contains a Cp of a tile on p
    Logger.trace("Checking tile: {} on new position ({}, {})", tile.id, p.x, p.y);
    if (centerPointMap.containsKey(p) && !tile.getCenter().equals(p) && !tile.getAltPoints().contains(p)) {
      free = false;
    }
    //Check if the cache contains a Cp on any of the Alt point is case of a Block or Cross
    if (altPointMap.containsKey(p) && !tile.getAltPoints().contains(p)) {
      free = false;
    }

    //Check with the tile on the new Cp with the new alt pointIds if that is also free...
    Set<Point> altPoints = tile.getAltPoints(p);
    for (Point ap : altPoints) {
      if (centerPointMap.containsKey(ap) && !tile.getCenter().equals(ap) && !tile.getAltPoints().contains(ap)) {
        free = false;
      }
      if (altPointMap.containsKey(ap) && !tile.getAltPoints().contains(ap)) {
        free = false;
      }
    }
    Logger.info("Checked tile: {} for (new) position ({}, {}): {}", tile.id, p.x, p.y, (free ? "free" : "occupied"));

    return free;
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
      centerPointMap.remove(tile.getCenter());

      Logger.info("Moving {} from ({},{}) to ({},{})", tile.getId(), tile.getCenterX(), tile.getCenterY(), p.x, p.y);

      tile.setCenter(p);
      tile.getTileBean().setCenter(p);

      addAndSaveTile(tile);
    } else {
      Tile occ = findTile(p);
      Logger.trace("Can't Move tile " + tile.id + " from " + tile.xyToString() + " to (" + p.x + "," + p.y + ") Is occupied by " + occ.id);
    }
  }

  public static Tile rotateTile(Tile tile) {
    if (!centerPointMap.containsKey(tile.getCenter())) {
      Logger.warn("Tile {} NOT in cache!", tile.getId());
    }

    //Remove the alternative or extra pointIds...
    for (Point ep : tile.getAltPoints()) {
      altPointMap.remove(ep);
    }

    tile.rotate();

    //update
    centerPointMap.put(tile.getCenter(), tile);
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
    if (!centerPointMap.containsKey(tile.getCenter())) {
      Logger.warn("Tile {} NOT in cache!", tile.getId());
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
    centerPointMap.put(tile.getCenter(), tile);
    for (Point ep : tile.getAltPoints()) {
      altPointMap.put(ep, tile);
    }
    idMap.put(tile.id, tile);

    persistTile(tile);
    return tile;
  }

  public static void enqueTileAction(AccessoryEvent accessoryEvent) {
    eventsQueue.offer(new ActionEventWrapper(accessoryEvent));
  }

  public static void enqueTileAction(SensorEvent sensorEvent) {
    eventsQueue.offer(new ActionEventWrapper(sensorEvent));
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
      Logger.warn("Can't find a Tile with Id: {} in the cache!", tileId);
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

  public static void main(String[] a) {
    long now = System.currentTimeMillis();
    long start = now;

    List<Tile> tiles = loadTiles(true);

    long end = System.currentTimeMillis();

    Logger.info("Loaded {} tiles in {} ms", tiles.size(), (end - start));

  }

}
