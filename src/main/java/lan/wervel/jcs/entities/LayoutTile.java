/*
 * Copyright (C) 2019 frans.
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
package lan.wervel.jcs.entities;

import java.awt.Point;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import lan.wervel.jcs.entities.enums.Orientation;
import lan.wervel.jcs.entities.enums.TileType;
import org.tinylog.Logger;

public class LayoutTile extends ControllableDevice {

    private TileType tiletype;

    private Orientation orientation;
    private String direction;
    private Integer x;
    private Integer y;

    private BigDecimal soacId;
    private BigDecimal sensId;

    private SolenoidAccessory solenoidAccessoiry;
    private SensorBean sensor;

    public static final int BASE_GRID = 20;

    private Set<LayoutTile> neighbours;

    public LayoutTile() {
        this(null, TileType.STRAIGHT, Orientation.EAST, "Center", null, null, null, null);
    }

    public LayoutTile(TileType tiletype, Orientation orientation, String direction, Point center) {
        this(null, tiletype, orientation, direction, center.x, center.y);
    }

    public LayoutTile(TileType tiletype, Orientation orientation, String direction, Integer x, Integer y) {
        this(null, tiletype, orientation, direction, x, y);
    }

    public LayoutTile(BigDecimal id, TileType tiletype, Orientation orientation, String direction, Integer x, Integer y) {
        this(id, tiletype, orientation, direction, x, y, null, null);
    }

    public LayoutTile(BigDecimal id, TileType tiletype, Orientation orientation, String direction, Integer x, Integer y, BigDecimal soacId, BigDecimal sensId) {
        super(id);
        this.tiletype = tiletype;
        this.orientation = orientation;
        this.direction = direction;
        this.x = x;
        this.y = y;
        this.soacId = soacId;
        this.sensId = sensId;
        neighbours = new HashSet<>();
    }

    public TileType getTiletype() {
        return tiletype;
    }

    public void setTiletype(TileType tiletype) {
        this.tiletype = tiletype;
    }

    public Orientation getOrientation() {
        return orientation;
    }

    public void setOrientation(Orientation orientation) {
        this.orientation = orientation;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public Integer getX() {
        return x;
    }

    public void setX(Integer x) {
        this.x = x;
    }

    public Integer getY() {
        return y;
    }

    public void setY(Integer y) {
        this.y = y;
    }

    public Point getCenter() {
        return new Point(this.x, this.y);
    }

    public void setCenter(Point center) {
        this.x = center.x;
        this.y = center.y;
    }

    public BigDecimal getSoacId() {
        return soacId;
    }

    public void setSoacId(BigDecimal soacId) {
        this.soacId = soacId;
    }

    public BigDecimal getSensId() {
        return sensId;
    }

    public void setSensId(BigDecimal femoId) {
        this.sensId = femoId;
    }

    public SolenoidAccessory getSolenoidAccessoiry() {
        return solenoidAccessoiry;
    }

    public SwitchBean getTurnout() {
        if (solenoidAccessoiry instanceof SwitchBean) {
            return (SwitchBean) solenoidAccessoiry;
        } else {
            return null;
        }
    }

    public SignalBean getSignal() {
        if (solenoidAccessoiry instanceof SignalBean) {
            return (SignalBean) solenoidAccessoiry;
        } else {
            return null;
        }
    }

    public void setSolenoidAccessoiry(SolenoidAccessory solenoidAccessoiry) {
        this.solenoidAccessoiry = solenoidAccessoiry;

        if (solenoidAccessoiry != null) {
            this.soacId = solenoidAccessoiry.getId();
        } else {
            this.soacId = null;
        }
    }

    public SensorBean getSensor() {
        return sensor;
    }

    public void setSensor(SensorBean sensor) {
        this.sensor = sensor;
        if (sensor != null) {
            this.sensId = sensor.getId();
        } else {
            this.sensId = null;
        }
    }

    public void addNeighbour(LayoutTile tile) {
        this.neighbours.add(tile);
    }

    public void setNeighbours(Set<LayoutTile> neighbours) {
        this.neighbours = neighbours;
    }

    public Set<LayoutTile> getNeighbours() {
        return neighbours;
    }

    public boolean hasNeighbour() {
        return !neighbours.isEmpty();
    }

    private int getWidth() {
        switch (this.tiletype) {
            case SWITCH:
                return BASE_GRID * 4;
            case BLOCK:
                if (Orientation.EAST.equals(this.orientation) || Orientation.WEST.equals(this.orientation)) {
                    return BASE_GRID * 4;
                } else {
                    return BASE_GRID * 2;
                }
            default:
                return BASE_GRID * 2;
        }
    }

    private int getHeight() {
        switch (this.tiletype) {
            case SWITCH:
                return BASE_GRID * 4;
            case BLOCK:
                if (Orientation.EAST.equals(orientation) || Orientation.WEST.equals(orientation)) {
                    return BASE_GRID * 2;
                } else {
                    return BASE_GRID * 4;
                }
            default:
                return BASE_GRID * 2;
        }
    }

    public Set<Point> getAdjacentPoints() {
        Set<Point> points = new HashSet<>();

        int w = getWidth();
        int h = getHeight();

        if (tiletype == null) {
            return points;
        }

        switch (tiletype) {
            case CURVED:
                if (Orientation.EAST.equals(orientation) || Orientation.WEST.equals(orientation)) {
                    points.add(new Point(x - w, y - BASE_GRID));
                    points.add(new Point(x - w, y - h));
                    points.add(new Point(x - BASE_GRID, y - h));
                    points.add(new Point(x, y - h));
                    points.add(new Point(x + w, y + BASE_GRID));
                    points.add(new Point(x + w, y + h));
                    points.add(new Point(x + BASE_GRID, y + h));
                    points.add(new Point(x, y + h));
                } else {
                    points.add(new Point(x + w, y - h));
                    points.add(new Point(x + w, y - BASE_GRID));
                    points.add(new Point(x + BASE_GRID, y - h));
                    points.add(new Point(x, y - h));
                    points.add(new Point(x - w, y + h));
                    points.add(new Point(x - w, y + BASE_GRID));
                    points.add(new Point(x - BASE_GRID, y + h));
                    points.add(new Point(x, y + h));
                }
                break;
            case STRAIGHT:
                if (Orientation.EAST.equals(orientation) || Orientation.WEST.equals(orientation)) {
                    points.add(new Point(x + w, y));
                    points.add(new Point(x + w, y + BASE_GRID));
                    points.add(new Point(x + w, y - BASE_GRID));

                    points.add(new Point(x - w, y));
                    points.add(new Point(x - w, y + BASE_GRID));
                    points.add(new Point(x - w, y - BASE_GRID));
                } else {
                    points.add(new Point(x, y - h));
                    points.add(new Point(x + BASE_GRID, y - h));
                    points.add(new Point(x - BASE_GRID, y - h));
                    points.add(new Point(x, y + h));
                    points.add(new Point(x + BASE_GRID, y + h));
                    points.add(new Point(x - BASE_GRID, y + h));
                }
                break;
            default:
                break;
        }

        return points;
    }

    private boolean isNeigbourForDiagonal(LayoutTile adjacent) {
        //this is the center and is a diagonal track
        Orientation adjacentOrientation = adjacent.getOrientation();
        TileType adjacentType = adjacent.tiletype;
        int adjX = adjacent.getX();
        int adjY = adjacent.getY();
        int w = getWidth();
        int h = getHeight();

        switch (adjacentType) {
            case CURVED:
                if (Orientation.EAST.equals(orientation) || Orientation.WEST.equals(orientation)) {
                    return (Orientation.EAST.equals(adjacentOrientation) || Orientation.WEST.equals(adjacentOrientation)) && ((adjX == this.x - w && adjY == this.y - h) || adjX == this.x + w && adjY == this.y + h)
                            || (Orientation.NORTH.equals(adjacentOrientation) || Orientation.SOUTH.equals(adjacentOrientation)) && ((adjX == this.x && adjY == this.y - h) || adjX == this.x && adjY == this.y + h);
                } else {
                    return ((Orientation.NORTH.equals(adjacentOrientation) || Orientation.SOUTH.equals(adjacentOrientation)) && adjX == this.x + w && adjY == this.y - h)
                            || ((Orientation.EAST.equals(adjacentOrientation) || Orientation.WEST.equals(adjacentOrientation)) && adjX == this.x && adjY == this.y - h)
                            || ((Orientation.EAST.equals(adjacentOrientation) || Orientation.WEST.equals(adjacentOrientation)) && adjX == this.x && adjY == this.y + h)
                            || ((Orientation.NORTH.equals(adjacentOrientation) || Orientation.SOUTH.equals(adjacentOrientation)) && adjX == this.x - w && adjY == this.y + h);
                }
            case STRAIGHT:
                if (Orientation.EAST.equals(orientation) || Orientation.WEST.equals(orientation)) {
                    return (Orientation.EAST.equals(adjacentOrientation) || Orientation.WEST.equals(adjacentOrientation)) && ((adjX == this.x - w && adjY == this.y - BASE_GRID) || (adjX == this.x + w && adjY == this.y + BASE_GRID))
                            || (Orientation.NORTH.equals(adjacentOrientation) || Orientation.SOUTH.equals(adjacentOrientation)) && ((adjX == this.x - BASE_GRID && adjY == this.y - h) || (adjX == this.x + BASE_GRID && adjY == this.y + h));
                } else {
                    return (Orientation.EAST.equals(adjacentOrientation) || Orientation.WEST.equals(adjacentOrientation)) && ((adjX == this.x + w && adjY == this.y - BASE_GRID) || (adjX == this.x - w && adjY == this.y + BASE_GRID))
                            || (Orientation.NORTH.equals(adjacentOrientation) || Orientation.SOUTH.equals(adjacentOrientation)) && ((adjX == this.x + BASE_GRID && adjY == this.y - h) || (adjX == this.x - BASE_GRID && adjY == this.y + h));
                }
            case SWITCH:
                break;
            case SIGNAL:
                break;
            case SENSOR:
                break;
            case BLOCK:
                break;
            default:
                Logger.error("Unknown tile: " + tiletype);
                break;
        }
        return false;
    }

    private boolean isNeigbourForStraight(LayoutTile adjacent) {
        //Regard this as the center TileType is StraightTrack
        Orientation adjacentOrientation = adjacent.getOrientation();
        TileType adjacentType = adjacent.tiletype;
        int adjX = adjacent.getX();
        int adjY = adjacent.getY();
        int w = this.getWidth();
        int h = this.getHeight();

        switch (adjacentType) {
            case CURVED:
                if (Orientation.EAST.equals(orientation) || Orientation.WEST.equals(orientation)) {
                    return ((Orientation.NORTH.equals(adjacentOrientation) || Orientation.SOUTH.equals(adjacentOrientation)) && adjX == this.x - w && adjY == this.y + BASE_GRID)
                            || ((Orientation.EAST.equals(adjacentOrientation) || Orientation.WEST.equals(adjacentOrientation)) && adjX == this.x - w && adjY == this.y - BASE_GRID)
                            || ((Orientation.EAST.equals(adjacentOrientation) || Orientation.WEST.equals(adjacentOrientation)) && adjX == this.x + w && adjY == this.y + BASE_GRID)
                            || ((Orientation.NORTH.equals(adjacentOrientation) || Orientation.SOUTH.equals(adjacentOrientation)) && adjX == this.x + w && adjY == this.y - BASE_GRID);
                } else {
                    return ((Orientation.NORTH.equals(adjacentOrientation) || Orientation.SOUTH.equals(adjacentOrientation)) && adjX == this.x + BASE_GRID && adjY == this.y - h)
                            || ((Orientation.EAST.equals(adjacentOrientation) || Orientation.WEST.equals(adjacentOrientation)) && adjX == this.x - BASE_GRID && adjY == this.y - h)
                            || ((Orientation.EAST.equals(adjacentOrientation) || Orientation.WEST.equals(adjacentOrientation)) && adjX == this.x + BASE_GRID && adjY == this.y + h)
                            || ((Orientation.NORTH.equals(adjacentOrientation) || Orientation.SOUTH.equals(adjacentOrientation)) && adjX == this.x - BASE_GRID && adjY == this.y + h);
                }
            case STRAIGHT:
                if (Orientation.EAST.equals(orientation) || Orientation.WEST.equals(orientation)) {
                    return (((Orientation.EAST.equals(adjacentOrientation) || Orientation.WEST.equals(adjacentOrientation)) && adjX == this.x + w && adjY == this.y)
                            || (Orientation.EAST.equals(adjacentOrientation) || Orientation.WEST.equals(adjacentOrientation)) && adjX == this.x - w && adjY == this.y);
                } else {
                    return (((Orientation.NORTH.equals(adjacentOrientation) || Orientation.SOUTH.equals(adjacentOrientation)) && adjX == this.x && adjY == this.y + h)
                            || (Orientation.NORTH.equals(adjacentOrientation) || Orientation.SOUTH.equals(adjacentOrientation)) && adjX == this.x && adjY == this.y - h);
                }
            case SWITCH:
                break;
            case SIGNAL:
                break;
            case SENSOR:
                break;
            case BLOCK:
                break;
            default:
                Logger.error("Unknown tile: " + tiletype);
                break;
        }
        return false;
    }

    public boolean isNeighbour(LayoutTile adjacent) {
        //this is the center
        switch (tiletype) {
            case CURVED:
                return isNeigbourForDiagonal(adjacent);
            case STRAIGHT:
                return isNeigbourForStraight(adjacent);
            case SWITCH:
                break;
            case SIGNAL:
                break;
            case SENSOR:
                break;
            case BLOCK:
                break;
            default:
                Logger.error("Unknown tile: " + tiletype);
                break;
        }
        return false;
    }

    @Override
    public String toLogString() {
        return this.toString();
    }

    @Override
    public String toString() {
        return this.tiletype.getTileType() + ";" + this.orientation + ";" + this.direction + ";(" + this.x + "," + this.y + ");" + this.id + ", sensId: " + this.sensId;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + Objects.hashCode(this.id);
        hash = 29 * hash + Objects.hashCode(this.tiletype);
        hash = 29 * hash + Objects.hashCode(this.orientation);
        hash = 29 * hash + Objects.hashCode(this.direction);
        hash = 29 * hash + Objects.hashCode(this.x);
        hash = 29 * hash + Objects.hashCode(this.y);

        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final LayoutTile other = (LayoutTile) obj;

        if (!Objects.equals(this.tiletype, other.tiletype)) {
            return false;
        }
        if (!Objects.equals(this.orientation, other.orientation)) {
            return false;
        }
        if (!Objects.equals(this.direction, other.direction)) {
            return false;
        }
        if (!Objects.equals(this.x, other.x)) {
            return false;
        }

        return Objects.equals(this.y, other.y);
    }

}
