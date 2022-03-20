/*
 * Copyright (C) 2021 frans.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package jcs.ui.layout;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import jcs.entities.TileBean;
import jcs.entities.enums.AccessoryValue;
import jcs.entities.enums.Orientation;
import static jcs.entities.enums.Orientation.NORTH;
import static jcs.entities.enums.Orientation.SOUTH;
import static jcs.entities.enums.Orientation.WEST;
import jcs.entities.enums.TileType;
import jcs.trackservice.TrackServiceFactory;
import jcs.trackservice.events.AccessoryListener;
import jcs.trackservice.events.SensorListener;
import jcs.ui.layout.tiles.enums.Direction;
import jcs.ui.layout.tiles.TileFactory;
import org.tinylog.Logger;

/**
 *
 * @author frans
 */
public class LayoutUtil {

    private static final int GRID = 20;
    public static final int DEFAULT_WIDTH = GRID * 2;
    public static final int DEFAULT_HEIGHT = GRID * 2;

    private final static Map<String, Tile> tileIdLookup = new HashMap<>();
    private final static Map<Point, Tile> tiles = new HashMap<>();
    private final static Map<Point, Tile> altTilesLookup = new HashMap<>();

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
        int steps = x / DEFAULT_WIDTH;
        int sx = steps;
        sx = sx * DEFAULT_WIDTH + GRID;

        steps = y / DEFAULT_HEIGHT;
        int sy = steps;
        sy = sy * DEFAULT_HEIGHT + GRID;

        return new Point(sx, sy);
    }

    private static void addRelatedBeans(TileBean tileBean) {
        TileType tileType = tileBean.getTileType();
        switch (tileType) {
            case STRAIGHT:
                break;
            case CURVED:
                break;
            case SWITCH:
                if (tileBean.getBeanId() != null) {
                    tileBean.setEntityBean(TrackServiceFactory.getTrackService().getAccessory(tileBean.getBeanId()));
                }
                break;
            case CROSS:
                if (tileBean.getBeanId() != null) {
                    tileBean.setEntityBean(TrackServiceFactory.getTrackService().getAccessory(tileBean.getBeanId()));
                }
                break;
            case SIGNAL:
                break;
            case SENSOR:
                if (tileBean.getBeanId() != null) {
                    tileBean.setEntityBean(TrackServiceFactory.getTrackService().getSensor(tileBean.getBeanId()));
                }
                break;
            case BLOCK:
                break;
            default:
                Logger.warn("Unknown Tile Type " + tileType);
        }
    }

    public static final Map<Point, Tile> loadLayout(boolean drawGridLines) {
        return loadLayout(drawGridLines, null);
    }

    /**
     * Load Tiles from the persistent store
     *
     * @param drawGridLines
     * @param listener
     * @return A Map of tiles, key is the center point of the tile
     */
    public static final Map<Point, Tile> loadLayout(boolean drawGridLines, RepaintListener listener) {
        synchronized (LayoutUtil.tileIdLookup) {
            LayoutUtil.tileIdLookup.clear();
            LayoutUtil.tiles.clear();
            LayoutUtil.altTilesLookup.clear();

            if (TrackServiceFactory.getTrackService() != null) {
                Set<TileBean> beans = TrackServiceFactory.getTrackService().getTiles();

                for (TileBean tb : beans) {
                    addRelatedBeans(tb);
                    Tile tile = TileFactory.createTile(tb, drawGridLines);

                    if (listener != null) {
                        tile.setRepaintListener(listener);
                    }

                    registerAsEventListener(tile);

                    LayoutUtil.tileIdLookup.put(tile.getId(), tile);
                    LayoutUtil.tiles.put(tile.getCenter(), tile);
                    for (Point ap : tile.getAltPoints()) {
                        LayoutUtil.altTilesLookup.put(ap, tile);
                    }
                }

                Logger.debug("Loaded " + tileIdLookup.size() + " Tiles...");
            } else {
                Logger.error("Can't load tiles, no Trackservice available");
            }
        }
        return LayoutUtil.tiles;
    }

    private static void registerAsEventListener(Tile tile) {

        switch (tile.getTileType()) {
            case SENSOR:
                TrackServiceFactory.getTrackService().addSensorListener((SensorListener) tile);
                break;
            case SWITCH:
                TrackServiceFactory.getTrackService().addAccessoryListener((AccessoryListener) tile);
                break;
            default:
                //Do nothing
                break;
        }

    }

    private static boolean isNotLoaded() {
        return LayoutUtil.tileIdLookup == null || LayoutUtil.tileIdLookup.isEmpty();
    }

    public static Tile findTile(String id) {
        if (isNotLoaded()) {
            LayoutUtil.loadLayout(true);
        }
        Tile result = LayoutUtil.tileIdLookup.get(id);
        if (result == null) {
            //check also with the original Id
            String orgId;
            if (id.endsWith("-") || id.endsWith("+")) {
                orgId = id.substring(0, id.length() - 1);
            } else {
                orgId = id.replaceAll("-G", "").replaceAll("-R", "");
            }
            result = LayoutUtil.tileIdLookup.get(orgId);
        }
        return result;
    }

    public static Tile findTile(Point cp) {
        if (isNotLoaded()) {
            LayoutUtil.loadLayout(true);
        }
        Tile result = LayoutUtil.tiles.get(cp);

        if (result == null) {
            result = LayoutUtil.altTilesLookup.get(cp);
            if (result != null) {
            }
        }
        return result;
    }

    public static boolean isTile(Point cp) {
        return findTile(cp) != null;
    }

    public static boolean isTile(String id) {
        return findTile(id) != null;
    }

    public static boolean isBlock(Point cp) {
        Tile t = findTile(cp);
        if (t == null) {
            return false;
        }
        return TileType.BLOCK.equals(t.getTileType());
    }

    public static boolean isBlock(String id) {
        Tile t = findTile(id);
        if (t == null) {
            return false;
        }
        return TileType.BLOCK.equals(t.getTileType());
    }

    public static boolean isTrack(Point cp) {
        Tile t = findTile(cp);
        if (t == null) {
            return false;
        }
        TileType tt = t.getTileType();
        return TileType.CURVED.equals(tt) || TileType.CURVED.equals(tt) || TileType.SENSOR.equals(tt) || TileType.SIGNAL.equals(tt) || TileType.STRAIGHT.equals(tt);
    }

    public static boolean isTrack(String id) {
        Tile t = findTile(id);
        if (t == null) {
            return false;
        }
        TileType tt = t.getTileType();
        return TileType.CURVED.equals(tt) || TileType.CURVED.equals(tt) || TileType.SENSOR.equals(tt) || TileType.SIGNAL.equals(tt) || TileType.STRAIGHT.equals(tt);
    }

    public static final Map<Point, Tile> getTiles() {
        if (isNotLoaded()) {
            LayoutUtil.loadLayout(true);
        }

        return LayoutUtil.tiles;
    }

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

    public static Set<Point> adjacentPointsFor(Tile tile) {
        return adjacentPointsFor(tile, AccessoryValue.OFF);
    }

    public static Set<Point> adjacentPointsFor(Tile tile, AccessoryValue accessoryValue) {
        Set<Point> adjacent = new HashSet<>();
        int x = tile.getCenterX();
        int y = tile.getCenterY();
        int w = tile.getWidth();
        int h = tile.getHeight();
        Orientation orientation = tile.getOrientation();
        Direction direction = tile.getDirection();

        int oX = w / 2 + Tile.GRID;
        int oY = h / 2 + Tile.GRID;

        switch (tile.getTileType()) {
            case CURVED:
                switch (orientation) {
                    case SOUTH:
                        adjacent.add(new Point(x - oX, y));
                        adjacent.add(new Point(x, y + oY));
                        break;
                    case WEST:
                        adjacent.add(new Point(x - oX, y));
                        adjacent.add(new Point(x, y - oY));
                        break;
                    case NORTH:
                        adjacent.add(new Point(x + oX, y));
                        adjacent.add(new Point(x, y - oY));
                        break;
                    default:
                        //EAST
                        adjacent.add(new Point(x + oX, y));
                        adjacent.add(new Point(x, y + oY));
                        break;
                }
                break;
            case CROSS:
                if (Orientation.EAST.equals(orientation) || Orientation.WEST.equals(orientation)) {
                    adjacent.add(new Point(x + oX, y));
                    adjacent.add(new Point(x - oX, y));
                } else {
                    adjacent.add(new Point(x, y + oY));
                    adjacent.add(new Point(x, y - oY));
                }
                break;
            case SWITCH:
                switch (orientation) {
                    case SOUTH:
                        switch (accessoryValue) {
                            case GREEN:
                                adjacent.add(new Point(x, y - oY));
                                break;
                            case RED:
                                if (Direction.LEFT.equals(direction)) {
                                    adjacent.add(new Point(x - oX, y));
                                } else {
                                    adjacent.add(new Point(x + oX, y));
                                }
                                break;
                            default:
                                adjacent.add(new Point(x, y + oY));
                        }
                        break;
                    case WEST:
                        switch (accessoryValue) {
                            case GREEN:
                                adjacent.add(new Point(x + oX, y));
                                break;
                            case RED:
                                if (Direction.LEFT.equals(direction)) {
                                    adjacent.add(new Point(x, y - oY));
                                } else {
                                    adjacent.add(new Point(x, y + oY));
                                }
                                break;
                            default:
                                adjacent.add(new Point(x - oX, y));
                                break;
                        }
                    case NORTH:
                        switch (accessoryValue) {
                            case GREEN:
                                adjacent.add(new Point(x, y + oY));
                                break;
                            case RED:
                                if (Direction.LEFT.equals(direction)) {
                                    adjacent.add(new Point(x + oX, y));
                                } else {
                                    adjacent.add(new Point(x - oX, y));
                                }
                                break;
                            default:
                                adjacent.add(new Point(x, y - oY));
                                break;
                        }
                    default:
                        //EAST
                        switch (accessoryValue) {
                            case GREEN:
                                adjacent.add(new Point(x - oX, y));
                                break;
                            case RED:
                                if (Direction.LEFT.equals(direction)) {
                                    adjacent.add(new Point(x, y + oY));
                                } else {
                                    adjacent.add(new Point(x, y - oY));
                                }
                                break;
                            default:
                                adjacent.add(new Point(x + oX, y));
                                break;
                        }
                        break;
                }
                break;
            default:
                if (Orientation.EAST.equals(orientation) || Orientation.WEST.equals(orientation)) {
                    adjacent.add(new Point(x + oX, y));
                    adjacent.add(new Point(x - oX, y));
                } else {
                    adjacent.add(new Point(x, y + oY));
                    adjacent.add(new Point(x, y - oY));
                }
                break;
        }
        return adjacent;
    }

    private static Point getAdjacentPoint(Tile block, String plusMinus) {
        int x = block.getCenterX();
        int y = block.getCenterY();
        int w = block.getWidth();
        int h = block.getHeight();
        Orientation o = block.getOrientation();

        Point neighborPlus, neighborMin;
        switch (o) {
            case SOUTH:
                neighborPlus = new Point(x, y + h / 3 + Tile.GRID);
                neighborMin = new Point(x, y - h / 3 - Tile.GRID);
                break;
            case WEST:
                neighborPlus = new Point(x - w / 3 - Tile.GRID, y);
                neighborMin = new Point(x + w / 3 + Tile.GRID, y);
                break;
            case NORTH:
                neighborPlus = new Point(x, y - h / 3 - Tile.GRID);
                neighborMin = new Point(x, y + h / 3 + Tile.GRID);
                break;
            default:
                //East 
                neighborPlus = new Point(x + w / 3 + 40, y);
                neighborMin = new Point(x - w / 3 - 40, y);
                break;
        }
        if ("+".equals(plusMinus)) {
            return neighborPlus;
        } else {
            return neighborMin;
        }
    }

    /**
     *
     * @param block the block to investigate
     * @param point the adjacent point
     * @return true when the point is adjacent at the plus (+) side of the Block
     * Tile
     */
    public static boolean isPlusAdjacent(Tile block, Point point) {
        Point p = getAdjacentPoint(block, "+");
        return p.equals(point);
    }

    public static Point getPlusAdjacent(Tile block) {
        Point p = getAdjacentPoint(block, "+");
        return p;
    }

    public static Point getMinusAdjacent(Tile block) {
        Point p = getAdjacentPoint(block, "-");
        return p;
    }

    /**
     *
     * @param block the block to investigate
     * @param point the adjacent point
     * @return true when the point is adjacent at the minus (-) side of the
     * Block Tile
     */
    public static boolean isMinusAdjacent(Tile block, Point point) {
        Point p = getAdjacentPoint(block, "-");
        return p.equals(point);
    }

    private static Point getPlusMinus(Tile block, String plusMinus) {
        int x = block.getCenterX();
        int y = block.getCenterY();
        int w = block.getWidth();
        int h = block.getHeight();
        Orientation o = block.getOrientation();

        Point cpPlus, cpMin;
        switch (o) {
            case SOUTH:
                cpPlus = new Point(x, y + h / 3);
                cpMin = new Point(x, y - h / 3);
                break;
            case WEST:
                cpPlus = new Point(x - w / 3, y);
                cpMin = new Point(x + w / 3, y);
                break;
            case NORTH:
                cpPlus = new Point(x, y - h / 3);
                cpMin = new Point(x, y + h / 3);
                break;
            default:
                //East 
                cpPlus = new Point(x + w / 3, y);
                cpMin = new Point(x - w / 3, y);
                break;
        }
        if ("+".equals(plusMinus)) {
            return cpPlus;
        } else {
            return cpMin;
        }
    }

    /**
     * Obtain the "center" of the plus (+) of a Block Tile
     *
     * @param block the block
     * @return a Point
     */
    public static Point getPlusCenter(Tile block) {
        return getPlusMinus(block, "+");
    }

    /**
     * Obtain the "center" of the minus (-) of a Block Tile
     *
     * @param block the block
     * @return a Point
     */
    public static Point getMinusCenter(Tile block) {
        return getPlusMinus(block, "-");
    }

    /**
     * The Adjacent Ids depend on the direction of the switch. The common point
     * has 2 Ids one for R and one for G The opposite side has one id for G, the
     * fork side the R
     *
     * @param tile the "from"
     * @param switchTile the "target"
     * @return a List with the nodeIds which are adjacent nodes
     */
    public static List<String> nodeIdsForAdjacentSwitch(Tile tile, Tile switchTile) {
        List<String> nodeIds = new ArrayList<>();
        Orientation o = switchTile.getOrientation();
        int tileX = tile.getCenterX();
        int tileY = tile.getCenterY();
        int adjX = switchTile.getCenterX();
        int adjY = switchTile.getCenterY();
        switch (o) {
            case SOUTH:
                if (adjX == tileX && adjY != tileY) {
                    //North or South
                    if (adjX < tileX) {
                        //Common
                        nodeIds.add(switchTile.getId());
                    } else {
                        //Green
                        nodeIds.add(switchTile.getId() + "-G");
                    }
                } else {
                    //Red
                    nodeIds.add(switchTile.getId() + "-R");
                }
                break;
            case WEST:
                //East
                if (adjX != tileX && adjY == tileY) {
                    //The common of a East L or R Switch 
                    if (adjX > tileX) {
                        //Common    
                        nodeIds.add(switchTile.getId());
                    } else {
                        //Green    
                        nodeIds.add(switchTile.getId() + "-G");
                    }
                } else {
                    //Red
                    nodeIds.add(switchTile.getId() + "-R");
                }
                break;
            case NORTH:
                if (adjX == tileX && adjY != tileY) {
                    //North or South
                    if (adjX > tileX) {
                        //Common
                        nodeIds.add(switchTile.getId());
                    } else {
                        //Green
                        nodeIds.add(switchTile.getId() + "-G");
                    }
                } else {
                    //Red
                    nodeIds.add(switchTile.getId() + "-R");
                }
                break;
            default:
                //East
                if (adjX != tileX && adjY == tileY) {
                    //The common of a East L or R Switch 
                    if (adjX < tileX) {
                        //Common    
                        nodeIds.add(switchTile.getId());
                    } else {
                        //Green    
                        nodeIds.add(switchTile.getId() + "-G");
                    }
                } else {
                    //Red
                    nodeIds.add(switchTile.getId() + "-R");
                }
                break;
        }
        return nodeIds;
    }

}
