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
package lan.wervel.jcs.ui.layout2;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lan.wervel.jcs.entities.TileBean;
import lan.wervel.jcs.entities.enums.AccessoryValue;
import lan.wervel.jcs.entities.enums.Orientation;
import static lan.wervel.jcs.entities.enums.Orientation.NORTH;
import static lan.wervel.jcs.entities.enums.Orientation.SOUTH;
import static lan.wervel.jcs.entities.enums.Orientation.WEST;
import lan.wervel.jcs.trackservice.TrackServiceFactory;
import lan.wervel.jcs.ui.layout.tiles.enums.Direction;
import lan.wervel.jcs.ui.layout2.tiles2.Switch;
import lan.wervel.jcs.ui.layout2.tiles2.TileFactory2;
import org.tinylog.Logger;

/**
 *
 * @author frans
 */
public class LayoutUtil {

    private static final int GRID = 20;
    public static final int DEFAULT_WIDTH = GRID * 2;
    public static final int DEFAULT_HEIGHT = GRID * 2;

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

    /**
     * Load Tiles from the persistent store
     *
     * @param drawGridLines
     * @return A Map of tiles, key is the center point of the tile
     */
    public static final Map<Point, Tile> loadTiles(boolean drawGridLines) {
        if (TrackServiceFactory.getTrackService() == null) {
            return Collections.EMPTY_MAP;
        }

        Map<Point, Tile> tiles = new HashMap<>();

        Set<TileBean> beans = TrackServiceFactory.getTrackService().getTiles();
        Logger.trace("Loading " + beans.size() + " TileBeans from persistent store...");

        Set<Tile> snapshot = new HashSet<>();

        for (TileBean tb : beans) {
            Tile tile = TileFactory2.createTile(tb, drawGridLines);
            snapshot.add(tile);
        }

        for (Tile t : snapshot) {
            tiles.put(t.getCenter(), t);
        }

        Logger.debug("Loaded " + tiles.size() + " Tiles...");
        return tiles;
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
                                adjacent.add(new Point(x, y + oY));
                                adjacent.add(new Point(x, y - oY));
                                break;
                            case RED:
                                adjacent.add(new Point(x, y + oY));
                                if (Direction.LEFT.equals(direction)) {
                                    adjacent.add(new Point(x - oX, y));
                                } else {
                                    adjacent.add(new Point(x + oX, y));
                                }
                                break;
                            default:
                                adjacent.add(new Point(x, y + oY));
                                adjacent.add(new Point(x, y - oY));

                                if (Direction.RIGHT.equals(direction)) {
                                    adjacent.add(new Point(x + oX, y));
                                } else if (Direction.LEFT.equals(direction)) {
                                    adjacent.add(new Point(x - oX, y));
                                }
                        }
                        break;
                    case WEST:
                        switch (accessoryValue) {
                            case GREEN:
                                adjacent.add(new Point(x - oX, y));
                                adjacent.add(new Point(x + oX, y));
                                break;
                            case RED:
                                adjacent.add(new Point(x - oX, y));
                                if (Direction.LEFT.equals(direction)) {
                                    adjacent.add(new Point(x, y - oY));
                                } else {
                                    adjacent.add(new Point(x, y + oY));
                                }
                                break;
                            default:
                                adjacent.add(new Point(x + oX, y));
                                adjacent.add(new Point(x - oX, y));

                                if (Direction.RIGHT.equals(direction)) {
                                    adjacent.add(new Point(x, y + oY));
                                } else if (Direction.LEFT.equals(direction)) {
                                    adjacent.add(new Point(x, y - oY));
                                }
                                break;
                        }
                    case NORTH:
                        switch (accessoryValue) {
                            case GREEN:
                                adjacent.add(new Point(x, y - oY));
                                adjacent.add(new Point(x, y + oY));
                                break;
                            case RED:
                                adjacent.add(new Point(x, y - oY));
                                if (Direction.LEFT.equals(direction)) {
                                    adjacent.add(new Point(x + oX, y));
                                } else {
                                    adjacent.add(new Point(x - oX, y));
                                }
                                break;
                            default:
                                adjacent.add(new Point(x, y + oY));
                                adjacent.add(new Point(x, y - oY));

                                if (Direction.RIGHT.equals(direction)) {
                                    adjacent.add(new Point(x - oX, y));
                                } else if (Direction.LEFT.equals(direction)) {
                                    adjacent.add(new Point(x + oX, y));
                                }
                                break;
                        }
                    default:
                        //EAST
                        switch (accessoryValue) {
                            case GREEN:
                                adjacent.add(new Point(x + oX, y));
                                adjacent.add(new Point(x - oX, y));
                                break;
                            case RED:
                                adjacent.add(new Point(x + oX, y));
                                if (Direction.LEFT.equals(direction)) {
                                    adjacent.add(new Point(x, y + oY));
                                } else {
                                    adjacent.add(new Point(x, y - oY));
                                }
                                break;
                            default:
                                adjacent.add(new Point(x + oX, y));
                                adjacent.add(new Point(x - oX, y));
                                if (Direction.RIGHT.equals(direction)) {
                                    adjacent.add(new Point(x, y - oY));
                                } else if (Direction.LEFT.equals(direction)) {
                                    adjacent.add(new Point(x, y + oY));
                                }
                                break;
                        }
                        break;
                }
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
                        // South
                        nodeIds.add(switchTile.getId() + "-G");
                    } else {
                        // North, common point
                        nodeIds.add(switchTile.getId() + "-G");
                        nodeIds.add(switchTile.getId() + "-R");
                    }
                } else {
                    //East or West
                    nodeIds.add(switchTile.getId() + "-R");
                }
                break;
            case WEST:
                if (adjX != tileX && adjY == tileY) {
                    //east or west
                    if (adjX > tileX) {
                        // east, a the switch common point so
                        nodeIds.add(switchTile.getId() + "-G");
                        nodeIds.add(switchTile.getId() + "-R");
                    } else {
                        //west
                        nodeIds.add(switchTile.getId() + "-G");
                    }
                } else {
                    //North or South
                    nodeIds.add(switchTile.getId() + "-R");
                }
                break;
            case NORTH:
                if (adjX == tileX && adjY != tileY) {
                    //North or South
                    if (adjX > tileX) {
                        // North
                        nodeIds.add(switchTile.getId() + "-G");
                    } else {
                        //South, common point
                        nodeIds.add(switchTile.getId() + "-G");
                        nodeIds.add(switchTile.getId() + "-R");
                    }
                } else {
                    //East or West
                    nodeIds.add(switchTile.getId() + "-R");
                }
                break;
            default:
                //East
                if (adjX != tileX && adjY == tileY) {
                    //east or west
                    if (adjX < tileX) {
                        // west, a the switch common point so
                        nodeIds.add(switchTile.getId() + "-G");
                        nodeIds.add(switchTile.getId() + "-R");
                    } else {
                        //east
                        nodeIds.add(switchTile.getId() + "-G");
                    }
                } else {
                    //North or South
                    nodeIds.add(switchTile.getId() + "-R");
                }
                break;
        }
        return nodeIds;
    }

}
