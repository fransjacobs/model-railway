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
package jcs.entities;

import java.awt.Point;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.persistence.Transient;
import jcs.entities.enums.SignalType;

@Table(
    name = "tiles",
    indexes = {@Index(name = "tile_x_y", columnList = "x, y", unique = true)})
public class TileBean implements Serializable, Comparable {

  protected String id;
  protected Integer x;
  protected Integer y;
  protected String type;
  protected String tileOrientation;
  protected String tileDirection;
  protected String signalAccessoryType;
  protected String accessoryId;
  protected String sensorId;

  protected List<TileBean> neighbours;

  protected AccessoryBean accessoryBean;
  protected SensorBean sensorBean;
  protected BlockBean blockBean;

  public TileBean() {
    this(null, TileType.STRAIGHT, Orientation.EAST, Direction.CENTER, 0, 0, null, null, null);
  }

  public TileBean(
      String id,
      TileType tileType,
      Orientation orientation,
      Direction direction,
      Integer x,
      Integer y) {
    this(id, tileType, orientation, direction, x, y, null, null, null);
  }

  public TileBean(
      String id,
      TileType tileType,
      Orientation orientation,
      Direction direction,
      Point center,
      SignalType signalType,
      String accessoryId,
      String sensorId) {
    this(id, tileType, orientation, direction, center.x, center.y, signalType, null, sensorId);
  }

  public TileBean(
      String id,
      TileType tileType,
      Orientation orientation,
      Direction direction,
      Integer x,
      Integer y,
      SignalType signalType,
      String accessoryId,
      String sensorId) {
    this.id = id;
    this.setTileType(tileType);
    this.tileOrientation = orientation.getOrientation();
    this.tileDirection = direction.getDirection();
    this.x = x;
    this.y = y;
    this.setSignalType(signalType);
    this.accessoryId = accessoryId;
    this.sensorId = sensorId;

    neighbours = new ArrayList<>();
  }

  @Id
  @Column(name = "id")
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  @Column(name = "x", nullable = false)
  public Integer getX() {
    return this.x;
  }

  public void setX(Integer x) {
    this.x = x;
  }

  @Column(name = "y", nullable = false)
  public Integer getY() {
    return this.y;
  }

  public void setY(Integer y) {
    this.y = y;
  }

  @Column(name = "tile_type", length = 255, nullable = false)
  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  @Transient
  public TileBean.TileType getTileType() {
    if (type != null) {
      return TileType.get(type);
    } else {
      return null;
    }
  }

  public final void setTileType(TileBean.TileType tileType) {
    this.type = tileType.getTileType();
  }

  @Column(name = "orientation", length = 255, nullable = false)
  public String getTileOrientation() {
    return tileOrientation;
  }

  public void setTileOrientation(String tileOrientation) {
    this.tileOrientation = tileOrientation;
  }

  @Transient
  public Orientation getOrientation() {
    return Orientation.get(this.tileOrientation);
  }

  public void setOrientation(Orientation orientation) {
    this.tileOrientation = orientation.getOrientation();
  }

  @Column(name = "direction", length = 255, nullable = false)
  public String getTileDirection() {
    return tileDirection;
  }

  public void setTileDirection(String tileDirection) {
    this.tileDirection = tileDirection;
  }

  @Transient
  public Direction getDirection() {
    return Direction.get(tileDirection);
  }

  public void setDirection(Direction direction) {
    this.tileDirection = direction.getDirection();
  }

  @Column(name = "signal_type", length = 255, nullable = false)
  public String getSignalAccessoryType() {
    return signalAccessoryType;
  }

  public void setSignalAccessoryType(String signalAccessoryType) {
    this.signalAccessoryType = signalAccessoryType;
  }

  @Transient
  public SignalType getSignalType() {
    return SignalType.get(signalAccessoryType);
  }

  public final void setSignalType(SignalType signalType) {
    if (signalType != null) {
      this.signalAccessoryType = signalType.getSignalType();
    }
  }

  @Transient
  public Point getCenter() {
    return new Point(this.x, this.y);
  }

  public void setCenter(Point center) {
    this.x = center.x;
    this.y = center.y;
  }

  @Column(name = "accessory_id")
  public String getAccessoryId() {
    return accessoryId;
  }

  public void setAccessoryId(String accessoryId) {
    this.accessoryId = accessoryId;
  }

  @Column(name = "sensor_id")
  public String getSensorId() {
    return sensorId;
  }

  public void setSensorId(String sensorId) {
    this.sensorId = sensorId;
  }

  @Transient
  public AccessoryBean getAccessoryBean() {
    return accessoryBean;
  }

  public void setAccessoryBean(AccessoryBean accessoryBean) {
    this.accessoryBean = accessoryBean;
    if (accessoryBean != null) {
      this.accessoryId = accessoryBean.getId();
    } else {
      this.accessoryId = null;
    }
  }

  @Transient
  public SensorBean getSensorBean() {
    return sensorBean;
  }

  public void setSensorBean(SensorBean sensorBean) {
    this.sensorBean = sensorBean;
    if (sensorBean != null) {
      this.sensorId = sensorBean.getId();
    } else {
      this.sensorId = null;
    }
  }

  @Transient
  public BlockBean getBlockBean() {
    return blockBean;
  }

  public void setBlockBean(BlockBean blockBean) {
    this.blockBean = blockBean;
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
    hash = 11 * hash + Objects.hashCode(this.type);
    hash = 11 * hash + Objects.hashCode(this.tileDirection);
    hash = 11 * hash + Objects.hashCode(this.tileDirection);
    hash = 11 * hash + Objects.hashCode(this.signalAccessoryType);
    hash = 11 * hash + Objects.hashCode(this.x);
    hash = 11 * hash + Objects.hashCode(this.y);
    hash = 11 * hash + Objects.hashCode(this.id);
    hash = 11 * hash + Objects.hashCode(this.accessoryId);
    hash = 11 * hash + Objects.hashCode(this.sensorId);
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
    if (!Objects.equals(this.type, other.type)) {
      return false;
    }
    if (!Objects.equals(this.tileOrientation, other.tileOrientation)) {
      return false;
    }
    if (!Objects.equals(this.tileDirection, other.tileDirection)) {
      return false;
    }
    if (!Objects.equals(this.signalAccessoryType, other.signalAccessoryType)) {
      return false;
    }
    if (!Objects.equals(this.x, other.x)) {
      return false;
    }
    if (!Objects.equals(this.y, other.y)) {
      return false;
    }
    if (!Objects.equals(this.accessoryId, other.accessoryId)) {
      return false;
    }
    return Objects.equals(this.sensorId, other.sensorId);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("TileBean{id=").append(id);
    sb.append(", tileType=").append(type);
    sb.append(", orientation=").append(tileOrientation);
    sb.append(", direction=").append(tileDirection);
    sb.append(", center=[(x=").append(x);
    sb.append(",y=").append(y);
    sb.append(")]");
    sb.append(", signalType=").append(this.signalAccessoryType);
    sb.append(", accessoryId=").append(this.accessoryId);
    sb.append(", sensorId=").append(this.sensorId);

    sb.append('}');
    return sb.toString();
  }

  public String toLogString() {
    return toString();
  }

  public enum TileType {
    STRAIGHT("Straight"),
    STRAIGHT_DIR("StraightDirection"),
    CURVED("Curved"),
    SWITCH("Switch"),
    CROSS("Cross"),
    SIGNAL("Signal"),
    SENSOR("Sensor"),
    BLOCK("Block"),
    END("End");

    private final String tileType;

    private static final Map<String, TileType> ENUM_MAP;

    TileType(String tileType) {
      this.tileType = tileType;
    }

    public String getTileType() {
      return this.tileType;
    }

    static {
      Map<String, TileType> map = new ConcurrentHashMap<>();
      for (TileType instance : TileType.values()) {
        map.put(instance.getTileType(), instance);
      }
      ENUM_MAP = Collections.unmodifiableMap(map);
    }

    public static TileType get(String tileType) {
      if (tileType == null) {
        return null;
      }
      return ENUM_MAP.get(tileType);
    }
  }

  public enum Orientation {
    NORTH("North"),
    SOUTH("South"),
    EAST("East"),
    WEST("West");

    private final String orientation;
    private static final Map<String, Orientation> ENUM_MAP;

    Orientation(String orientation) {
      this.orientation = orientation;
    }

    static {
      Map<String, Orientation> map = new ConcurrentHashMap<>();
      for (Orientation instance : Orientation.values()) {
        map.put(instance.getOrientation(), instance);
      }
      ENUM_MAP = Collections.unmodifiableMap(map);
    }

    public String getOrientation() {
      return this.orientation;
    }

    public static Orientation get(String direction) {
      return ENUM_MAP.get(direction);
    }
  }

  public enum Direction {
    RIGHT("Right"),
    LEFT("Left"),
    CENTER("Center");

    private final String direction;
    private static final Map<String, Direction> ENUM_MAP;

    Direction(String direction) {
      this.direction = direction;
    }

    static {
      Map<String, Direction> map = new ConcurrentHashMap<>();
      for (Direction instance : Direction.values()) {
        map.put(instance.getDirection(), instance);
      }
      ENUM_MAP = Collections.unmodifiableMap(map);
    }

    public String getDirection() {
      return this.direction;
    }

    public static Direction get(String direction) {
      return ENUM_MAP.get(direction);
    }
  }
}
