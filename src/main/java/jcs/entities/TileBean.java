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
package jcs.entities;

import java.awt.Point;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import jcs.entities.enums.Orientation;
import jcs.entities.enums.SignalType;
import jcs.entities.enums.TileType;
import jcs.ui.layout.tiles.enums.Direction;

/**
 * Bean to store Tile properties
 *
 * @author frans
 */
public class TileBean implements JCSEntity, Serializable, Comparable {

    private TileType tileType;
    private Orientation orientation;
    private Direction direction;
    private SignalType signalType;
    private Point center;
    private String id;

    private List<TileBean> neighbours;

    //THe default width and height of a Tile is 40x40 px
    public static final int DEFAULT_WIDTH = 40;
    public static final int DEFAULT_HEIGHT = 40;

    public TileBean() {
        this(TileType.STRAIGHT, Orientation.EAST, Direction.CENTER, new Point(0, 0), null, null);
    }

    public TileBean(TileType tileType, Orientation orientation, Direction direction, int x, int y, String id) {
        this(tileType, orientation, direction, new Point(x, y), id, null);
    }

    public TileBean(TileType tileType, Orientation orientation, Direction direction, int x, int y, String id, SignalType signalType) {
        this(tileType, orientation, direction, new Point(x, y), id, signalType);
    }

    public TileBean(TileType tileType, Orientation orientation, Direction direction, Point center, String id, SignalType signalType) {
        this.tileType = tileType;
        this.orientation = orientation;
        this.direction = direction;
        this.center = center;
        this.id = id;
        this.signalType = signalType;

        neighbours = new ArrayList<>();
    }

    public TileType getTileType() {
        return tileType;
    }

    public void setTileType(TileType tileType) {
        this.tileType = tileType;
    }

    public Orientation getOrientation() {
        return orientation;
    }

    public void setOrientation(Orientation orientation) {
        this.orientation = orientation;
    }

    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    public SignalType getSignalType() {
        return signalType;
    }

    public void setSignalType(SignalType signalType) {
        this.signalType = signalType;
    }

    public Point getCenter() {
        return center;
    }

    public void setCenter(Point center) {
        this.center = center;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getX() {
        if (this.center != null) {
            return this.center.x;
        } else {
            return 0;
        }
    }

    public void setX(int x) {
        Point p = new Point(x, this.getY());
        this.center = p;
    }

    public int getY() {
        if (this.center != null) {
            return this.center.y;
        } else {
            return 0;
        }
    }

    public void setY(int y) {
        Point p = new Point(this.getX(), y);
        this.center = p;
    }

    @Override
    public int compareTo(Object other) {
        int resx = Integer.compare(this.getX(), ((TileBean) other).getX());

        if (resx == 0) {
            return Integer.compare(this.getY(), ((TileBean) other).getY());
        } else {
            return resx;
        }
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 11 * hash + Objects.hashCode(this.tileType);
        hash = 11 * hash + Objects.hashCode(this.orientation);
        hash = 11 * hash + Objects.hashCode(this.direction);
        hash = 11 * hash + Objects.hashCode(this.signalType);
        hash = 11 * hash + Objects.hashCode(this.center);
        hash = 11 * hash + Objects.hashCode(this.id);
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
        final TileBean other = (TileBean) obj;
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        if (this.tileType != other.tileType) {
            return false;
        }
        if (this.orientation != other.orientation) {
            return false;
        }
        if (this.direction != other.direction) {
            return false;
        }
        if (this.signalType != other.signalType) {
            return false;
        }
        if (!Objects.equals(this.center, other.center)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("TileBean{id=").append(id);
        sb.append(", tileType=").append(tileType);
        sb.append(", orientation=").append(orientation);
        sb.append(", direction=").append(direction);
        sb.append(", center=").append(center);
        sb.append(", signalType=").append(signalType);
        sb.append('}');
        return sb.toString();
    }

    @Override
    public String toLogString() {
        return toString();
    }

    public int getWidth() {
        switch (this.tileType) {
            case BLOCK:
                if (Orientation.EAST.equals(orientation) || Orientation.WEST.equals(orientation)) {
                    return DEFAULT_WIDTH * 3;
                } else {
                    return DEFAULT_WIDTH;
                }
            case CROSS:
                if (Orientation.EAST.equals(orientation) || Orientation.WEST.equals(orientation)) {
                    return DEFAULT_WIDTH * 2;
                } else {
                    return DEFAULT_WIDTH;
                }
            default:
                //Straight,Curved,Sensor,Signal,Switch
                return DEFAULT_WIDTH;
        }
    }

    public int getHeight() {
        switch (this.tileType) {
            case BLOCK:
                if (Orientation.EAST.equals(orientation) || Orientation.WEST.equals(orientation)) {
                    return DEFAULT_HEIGHT;
                } else {
                    return DEFAULT_HEIGHT * 3;
                }
            case CROSS:
                if (Orientation.EAST.equals(orientation) || Orientation.WEST.equals(orientation)) {
                    return DEFAULT_HEIGHT;
                } else {
                    return DEFAULT_HEIGHT * 2;
                }
            default:
                //Straight,Curved,Sensor,Signal,Switch
                return DEFAULT_HEIGHT;
        }
    }

    public List<TileBean> getNeighbours() {
        return neighbours;
    }

    public void setNeighbours(List<TileBean> neighbours) {
        this.neighbours = neighbours;
    }

}
