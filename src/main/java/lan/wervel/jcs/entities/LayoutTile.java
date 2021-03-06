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

import java.math.BigDecimal;
import java.util.Objects;

/**
 *
 * @author frans
 */
public class LayoutTile extends ControllableDevice {

    private String tiletype;
    private String rotation;
    private String direction;
    private Integer x;
    private Integer y;
    private Integer offsetX;
    private Integer offsetY;

    private BigDecimal soacId;
    private BigDecimal sensId;
    private BigDecimal ltgrId;

    private SolenoidAccessory solenoidAccessoiry;
    private Sensor sensor;
    private LayoutTileGroup layoutTileGroup;

    public LayoutTile() {
        this(null, null, null, null, null, null, null, null, null, null, null);
    }

    public LayoutTile(String tiletype, String rotation, String direction, Integer x, Integer y) {
        this(null, tiletype, rotation, direction, x, y);
    }

    public LayoutTile(BigDecimal id, String tiletype, String rotation, String direction, Integer x, Integer y) {
        this(id, tiletype, rotation, direction, x, y, null, null, null, null, null);
    }

    public LayoutTile(BigDecimal id, String tiletype, String rotation, String direction, Integer x, Integer y, Integer offsetX, Integer offsetY, BigDecimal soacId, BigDecimal sensId, BigDecimal ltgrId) {
        super(id);
        this.tiletype = tiletype;
        this.rotation = rotation;
        this.direction = direction;
        this.x = x;
        this.y = y;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.soacId = soacId;
        this.sensId = sensId;
        //this.port = port;
        this.ltgrId = ltgrId;
    }

    public String getTiletype() {
        return tiletype;
    }

    public void setTiletype(String tiletype) {
        this.tiletype = tiletype;
    }

    public String getRotation() {
        return rotation;
    }

    public void setRotation(String rotation) {
        this.rotation = rotation;
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

    public Integer getOffsetX() {
        return offsetX;
    }

    public void setOffsetX(Integer offsetX) {
        this.offsetX = offsetX;
    }

    public Integer getOffsetY() {
        return offsetY;
    }

    public void setOffsetY(Integer offsetY) {
        this.offsetY = offsetY;
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

    public BigDecimal getLtgrId() {
        return ltgrId;
    }

    public void setLtgrId(BigDecimal ltgrId) {
        this.ltgrId = ltgrId;
    }

    public SolenoidAccessory getSolenoidAccessoiry() {
        return solenoidAccessoiry;
    }

    public Turnout getTurnout() {
        if (this.solenoidAccessoiry instanceof Turnout) {
            return (Turnout) solenoidAccessoiry;
        } else {
            return null;
        }
    }

    public Signal getSignal() {
        if (this.solenoidAccessoiry instanceof Signal) {
            return (Signal) solenoidAccessoiry;
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

    public Sensor getSensor() {
        return sensor;
    }

    public void setSensor(Sensor sensor) {
        this.sensor = sensor;
        if (sensor != null) {
            this.sensId = sensor.getId();
        } else {
            this.sensId = null;
        }
    }

    public LayoutTileGroup getLayoutTileGroup() {
        return layoutTileGroup;
    }

    public void setLayoutTileGroup(LayoutTileGroup layoutTileGroup) {
        this.layoutTileGroup = layoutTileGroup;
        if (layoutTileGroup != null) {
            this.ltgrId = layoutTileGroup.getId();
        } else {
            this.ltgrId = null;
        }
    }

    @Override
    public String toLogString() {
        return this.toString();
    }

    @Override
    public String toString() {
        return this.tiletype + ";" + this.rotation + ";" + this.direction + ";(" + this.x + "," + this.y + ");" + this.id + "," + this.ltgrId + ", sensId: " + this.sensId;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + Objects.hashCode(this.id);
        hash = 29 * hash + Objects.hashCode(this.tiletype);
        hash = 29 * hash + Objects.hashCode(this.rotation);
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
        if (!Objects.equals(this.rotation, other.rotation)) {
            return false;
        }
        if (!Objects.equals(this.direction, other.direction)) {
            return false;
        }
        if (!Objects.equals(this.x, other.x)) {
            return false;
        }
        if (!Objects.equals(this.ltgrId, other.ltgrId)) {
            return false;
        }

        return Objects.equals(this.y, other.y);
    }

}
