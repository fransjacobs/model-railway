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
package jcs.ui.layout;

import jcs.ui.layout.tiles.Tile;
import java.awt.Point;

/**
 *
 * @author frans
 */
public class LayoutUtil {

//  private final static Map<Point, Tile> tiles = new HashMap<>();
//  private final static Map<Point, Tile> altTilesLookup = new HashMap<>();
//  private final static Map<String, Tile> tileLookup = new HashMap<>();

  private LayoutUtil() {
  }

  public static final Point snapToGrid(Point p) {
    return snapToGrid(p.x, p.y);
  }

  /**
   * Snap coordinates to the nearest grid point
   *
   * @param x the X
   * @param y the Y
   * @return Coordinates which are the X en Y wrapped
   */
  public static final Point snapToGrid(int x, int y) {
    int steps = x / Tile.DEFAULT_WIDTH;
    int sx = steps;
    sx = sx * Tile.DEFAULT_WIDTH + Tile.GRID;

    steps = y / Tile.DEFAULT_HEIGHT;
    int sy = steps;
    sy = sy * Tile.DEFAULT_HEIGHT + Tile.GRID;

    return new Point(sx, sy);
  }

  public static int getGridX(int x) {
    int steps = x / Tile.DEFAULT_WIDTH;
    int sx = steps * Tile.DEFAULT_WIDTH + Tile.GRID;
    return (sx - Tile.GRID) / (Tile.GRID * 2);
  }

  public static int getGridY(int y) {
    int steps = y / Tile.DEFAULT_HEIGHT;
    int sy = steps * Tile.DEFAULT_HEIGHT + Tile.GRID;
    return (sy - Tile.GRID) / (Tile.GRID * 2);
  }

//  private static void addRelatedBeans(TileBean tileBean) {
//    TileType tileType = tileBean.getTileType();
//    switch (tileType) {
//      case STRAIGHT -> {
//      }
//      case STRAIGHT_DIR -> {
//      }
//      case END -> {
//      }
//      case CURVED -> {
//      }
//      case SWITCH -> {
//        if (tileBean.getAccessoryBean() != null) {
//          tileBean.setAccessoryBean(PersistenceFactory.getService().getAccessoryById(tileBean.getAccessoryId()));
//        }
//      }
//      case CROSS -> {
//        if (tileBean.getAccessoryBean() != null) {
//          tileBean.setAccessoryBean(PersistenceFactory.getService().getAccessoryById(tileBean.getAccessoryId()));
//        }
//      }
//      case SIGNAL -> {
//        if (tileBean.getAccessoryBean() != null) {
//          tileBean.setAccessoryBean(PersistenceFactory.getService().getAccessoryById(tileBean.getAccessoryId()));
//        }
//      }
//      case SENSOR -> {
//        if (tileBean.getSensorId() != null) {
//          tileBean.setSensorBean(PersistenceFactory.getService().getSensor(tileBean.getSensorId()));
//        }
//      }
//      case BLOCK -> {
//      }
//      default ->
//        Logger.warn("Unknown Tile Type " + tileType);
//    }
//  }

//  public static final Map<Point, Tile> loadLayout(boolean drawGridLines, boolean showValues) {
//    return loadLayout(drawGridLines, null, showValues);
//  }

  /**
   * Load Tiles from the persistent store
   *
   * @param drawGridLines
   * @param listener
   * @param showValues
   * @return A Map of tiles, key is the center point of the tile
   */
//  public static final Map<Point, Tile> loadLayout(boolean drawGridLines, PropertyChangeListener listener, boolean showValues) {
//    //synchronized (LayoutUtil.tiles) {
//      LayoutUtil.tiles.clear();
//      LayoutUtil.altTilesLookup.clear();
//      LayoutUtil.tileLookup.clear();
//
//      if (PersistenceFactory.getService() != null) {
//        List<TileBean> tbl = PersistenceFactory.getService().getTiles();
//
//        Set<TileBean> beans = new HashSet<>(tbl);
//
//        for (TileBean tb : beans) {
//          addRelatedBeans(tb);
//          Tile tile = TileFactory.createTile(tb, drawGridLines, showValues);
//
//          if (listener != null) {
//            tile.setPropertyChangeListener(listener);
//          }
//
//          registerAsEventListener(tile);
//
//          LayoutUtil.tiles.put(tile.getCenter(), tile);
//          for (Point ap : tile.getAltPoints()) {
//            LayoutUtil.altTilesLookup.put(ap, tile);
//          }
//
//          LayoutUtil.tileLookup.put(tile.getId(), tile);
//        }
//
//        Logger.debug("Loaded " + tiles.size() + " Tiles...");
//      } else {
//        Logger.error("Can't load tiles, no PersistenceService available");
//      }
//    //}
//    return LayoutUtil.tiles;
//  }

//  private static void registerAsEventListener(Tile tile) {
//
////        switch (tile.getTileType()) {
////            case SENSOR:
////                TrackServiceFactory.getTrackService().addSensorListener((SensorListener) tile);
////                break;
////            case SWITCH:
////                TrackServiceFactory.getTrackService().addAccessoryListener((AccessoryListener) tile);
////                break;
////            case SIGNAL:
////                TrackServiceFactory.getTrackService().addAccessoryListener((AccessoryListener) tile);
////                break;
////
////            default:
////                //Do nothing
////                break;
////        }
//  }

//  private static boolean isNotLoaded() {
//    return LayoutUtil.tiles == null || LayoutUtil.tiles.isEmpty();
//  }

//  public static Tile findTile(Point cp) {
//    if (LayoutUtil.tiles == null || LayoutUtil.tiles.isEmpty()) {
//      LayoutUtil.loadLayout(true, false);
//    }
//    Tile result = LayoutUtil.tiles.get(cp);
//
//    if (result == null) {
//      result = LayoutUtil.altTilesLookup.get(cp);
//      if (result != null) {
//      }
//    }
//    return result;
//  }

//  public static boolean isTile(Point cp) {
//    return findTile(cp) != null;
//  }

//  public static boolean isBlock(Point cp) {
//    Tile t = findTile(cp);
//    if (t == null) {
//      return false;
//    }
//    return TileType.BLOCK.equals(t.getTileType());
//  }

//  public static boolean isTrack(Point cp) {
//    Tile t = findTile(cp);
//    if (t == null) {
//      return false;
//    }
//    TileType tt = t.getTileType();
//    return TileType.CURVED.equals(tt) || TileType.CURVED.equals(tt) || TileType.SENSOR.equals(tt) || TileType.SIGNAL.equals(tt) || TileType.STRAIGHT.equals(tt);
//  }

//  public static final Map<Point, Tile> getTiles() {
//    if (LayoutUtil.tiles == null || LayoutUtil.tiles.isEmpty()) {
//      LayoutUtil.loadLayout(true, false);
//    }
//
//    return LayoutUtil.tiles;
//  }

  /**
   * Returns the euclidean distance of 2 Points
   *
   * @param p1
   * @param p2
   * @return the distance between p1 and p2
   */
  public static double euclideanDistance(Point p1, Point p2) {
    double a = (p2.x - p1.x);
    double b = (p2.y - p1.y);
    double d = Math.hypot(a, b);
    return d;
  }

//  public static Set<Point> adjacentPointsFor(Tile tile) {
//    return adjacentPointsFor(tile, AccessoryValue.OFF);
//  }
//  public static Set<Point> adjacentPointsFor(Tile tile, AccessoryValue accessoryValue) {
//    Set<Point> adjacent = new HashSet<>();
//    int x = tile.getCenterX();
//    int y = tile.getCenterY();
//    int w = tile.getWidth();
//    int h = tile.getHeight();
//    Orientation orientation = tile.getOrientation();
//    Direction direction = tile.getDirection();
//
//    int oX = w / 2 + Tile.GRID;
//    int oY = h / 2 + Tile.GRID;
//
//    switch (tile.getTileType()) {
//      case CURVED -> {
//        switch (orientation) {
//          case SOUTH -> {
//            adjacent.add(new Point(x - oX, y));
//            adjacent.add(new Point(x, y + oY));
//          }
//          case WEST -> {
//            adjacent.add(new Point(x - oX, y));
//            adjacent.add(new Point(x, y - oY));
//          }
//          case NORTH -> {
//            adjacent.add(new Point(x + oX, y));
//            adjacent.add(new Point(x, y - oY));
//          }
//          default -> {
//            //EAST
//            adjacent.add(new Point(x + oX, y));
//            adjacent.add(new Point(x, y + oY));
//          }
//        }
//      }
//      case CROSS -> {
//        if (Orientation.EAST.equals(orientation) || Orientation.WEST.equals(orientation)) {
//          adjacent.add(new Point(x + oX, y));
//          adjacent.add(new Point(x - oX, y));
//        } else {
//          adjacent.add(new Point(x, y + oY));
//          adjacent.add(new Point(x, y - oY));
//        }
//      }
//      case SWITCH -> {
//        switch (orientation) {
//          case SOUTH:
//            switch (accessoryValue) {
//              case GREEN:
//                adjacent.add(new Point(x, y - oY));
//                break;
//              case RED:
//                if (Direction.LEFT.equals(direction)) {
//                  adjacent.add(new Point(x - oX, y));
//                } else {
//                  adjacent.add(new Point(x + oX, y));
//                }
//                break;
//              default:
//                adjacent.add(new Point(x, y + oY));
//            }
//            break;
//          case WEST:
//            switch (accessoryValue) {
//              case GREEN:
//                adjacent.add(new Point(x + oX, y));
//                break;
//              case RED:
//                if (Direction.LEFT.equals(direction)) {
//                  adjacent.add(new Point(x, y - oY));
//                } else {
//                  adjacent.add(new Point(x, y + oY));
//                }
//                break;
//              default:
//                adjacent.add(new Point(x - oX, y));
//                break;
//            }
//            break;
//          case NORTH:
//            switch (accessoryValue) {
//              case GREEN:
//                adjacent.add(new Point(x, y + oY));
//                break;
//              case RED:
//                if (Direction.LEFT.equals(direction)) {
//                  adjacent.add(new Point(x + oX, y));
//                } else {
//                  adjacent.add(new Point(x - oX, y));
//                }
//                break;
//              default:
//                adjacent.add(new Point(x, y - oY));
//                break;
//            }
//            break;
//          default:
//            //EAST
//            switch (accessoryValue) {
//              case GREEN:
//                adjacent.add(new Point(x - oX, y));
//                break;
//              case RED:
//                if (Direction.LEFT.equals(direction)) {
//                  adjacent.add(new Point(x, y + oY));
//                } else {
//                  adjacent.add(new Point(x, y - oY));
//                }
//                break;
//              default:
//                adjacent.add(new Point(x + oX, y));
//                break;
//            }
//            break;
//        }
//      }
//      default -> {
//        if (Orientation.EAST.equals(orientation) || Orientation.WEST.equals(orientation)) {
//          adjacent.add(new Point(x + oX, y));
//          adjacent.add(new Point(x - oX, y));
//        } else {
//          adjacent.add(new Point(x, y + oY));
//          adjacent.add(new Point(x, y - oY));
//        }
//      }
//    }
//    return adjacent;
//  }
//  private static Point getAdjacentPoint(Tile block, String plusMinus) {
//    int x = block.getCenterX();
//    int y = block.getCenterY();
//    int w = block.getWidth();
//    int h = block.getHeight();
//    Orientation o = block.getOrientation();
//
//    Point neighborPlus, neighborMin;
//    switch (o) {
//      case SOUTH:
//        neighborPlus = new Point(x, y + h / 3 + Tile.GRID * 2);
//        neighborMin = new Point(x, y - h / 3 - Tile.GRID * 2);
//        break;
//      case WEST:
//        neighborPlus = new Point(x - w / 3 - Tile.GRID * 2, y);
//        neighborMin = new Point(x + w / 3 + Tile.GRID * 2, y);
//        break;
//      case NORTH:
//        neighborPlus = new Point(x, y - h / 3 - Tile.GRID * 2);
//        neighborMin = new Point(x, y + h / 3 + Tile.GRID * 2);
//        break;
//      default:
//        //East 
//        neighborPlus = new Point(x + w / 3 + Tile.GRID * 2, y);
//        neighborMin = new Point(x - w / 3 - Tile.GRID * 2, y);
//        break;
//    }
//    if ("+".equals(plusMinus)) {
//      return neighborPlus;
//    } else {
//      return neighborMin;
//    }
//  }
//  public static boolean isPlusAdjacent(Tile block, Point point) {
//    Point p = getAdjacentPoint(block, "+");
//    return p.equals(point);
//  }
//  public static Point getPlusAdjacent(Tile block) {
//    Point p = getAdjacentPoint(block, "+");
//    return p;
//  }
//  public static Point getMinusAdjacent(Tile block) {
//    Point p = getAdjacentPoint(block, "-");
//    return p;
//  }
//  public static boolean isMinusAdjacent(Tile block, Point point) {
//    Point p = getAdjacentPoint(block, "-");
//    return p.equals(point);
//  }
//  private static Point getPlusMinus(Tile block, String plusMinus) {
//    int x = block.getCenterX();
//    int y = block.getCenterY();
//    int w = block.getWidth();
//    int h = block.getHeight();
//    Orientation o = block.getOrientation();
//
//    Point cpPlus, cpMin;
//    switch (o) {
//      case SOUTH -> {
//        cpPlus = new Point(x, y + h / 3);
//        cpMin = new Point(x, y - h / 3);
//      }
//      case WEST -> {
//        cpPlus = new Point(x - w / 3, y);
//        cpMin = new Point(x + w / 3, y);
//      }
//      case NORTH -> {
//        cpPlus = new Point(x, y - h / 3);
//        cpMin = new Point(x, y + h / 3);
//      }
//      default -> {
//        //East 
//        cpPlus = new Point(x + w / 3, y);
//        cpMin = new Point(x - w / 3, y);
//      }
//    }
//    if ("+".equals(plusMinus)) {
//      return cpPlus;
//    } else {
//      return cpMin;
//    }
//  }
  /**
   * Obtain the "center" of the plus (+) of a Block Tile
   *
   * @param block the block
   * @return a Point
   */
//  public static Point getPlusCenter(Tile block) {
//    return getPlusMinus(block, "+");
//  }
  /**
   * Obtain the "center" of the minus (-) of a Block Tile
   *
   * @param block the block
   * @return a Point
   */
//  public static Point getMinusCenter(Tile block) {
//    return getPlusMinus(block, "-");
//  }
  /**
   * The Adjacent Ids depend on the direction of the switch. The common point has 2 Ids one for R and one for G The opposite side has one id for G, the fork side the R
   *
   * @param tile the "from"
   * @param switchTile the "target"
   * @return a List with the nodeIds which are adjacent nodes
   */
//  public static List<String> getNodeIdsForAdjacentSwitch(Tile tile, Tile switchTile) {
//    List<String> nodeIds = new ArrayList<>();
//    Orientation o = switchTile.getOrientation();
//    int tileX = tile.getCenterX();
//    int tileY = tile.getCenterY();
//    int adjX = switchTile.getCenterX();
//    int adjY = switchTile.getCenterY();
//    switch (o) {
//      case SOUTH -> {
//        if (adjX == tileX && adjY != tileY) {
//          //North or South
//          if (adjX < tileX) {
//            //Common
//            nodeIds.add(switchTile.getId());
//          } else {
//            //Green
//            nodeIds.add(switchTile.getId() + "-G");
//          }
//        } else {
//          //Red
//          nodeIds.add(switchTile.getId() + "-R");
//        }
//      }
//      case WEST -> {
//        //East
//        if (adjX != tileX && adjY == tileY) {
//          //The common of a East L or R Switch 
//          if (adjX > tileX) {
//            //Common    
//            nodeIds.add(switchTile.getId());
//          } else {
//            //Green    
//            nodeIds.add(switchTile.getId() + "-G");
//          }
//        } else {
//          //Red
//          nodeIds.add(switchTile.getId() + "-R");
//        }
//      }
//      case NORTH -> {
//        if (adjX == tileX && adjY != tileY) {
//          //North or South
//          if (adjX > tileX) {
//            //Common
//            nodeIds.add(switchTile.getId());
//          } else {
//            //Green
//            nodeIds.add(switchTile.getId() + "-G");
//          }
//        } else {
//          //Red
//          nodeIds.add(switchTile.getId() + "-R");
//        }
//      }
//      default -> {
//        //East
//        if (adjX != tileX && adjY == tileY) {
//          //The common of a East L or R Switch 
//          if (adjX < tileX) {
//            //Common    
//            nodeIds.add(switchTile.getId());
//          } else {
//            //Green    
//            nodeIds.add(switchTile.getId() + "-G");
//          }
//        } else {
//          //Red
//          nodeIds.add(switchTile.getId() + "-R");
//        }
//      }
//    }
//    return nodeIds;
//  }
}
