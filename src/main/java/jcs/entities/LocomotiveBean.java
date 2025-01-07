/*
 * Copyright 2024 Frans Jacobs.
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

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.awt.Image;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import jcs.persistence.util.ColumnPosition;

@Table(name = "locomotives")
public class LocomotiveBean implements Serializable {

  private Long id;
  private String name;
  private Long uid;
  private Integer address;
  private String icon;
  private String decoderTypeString;
  private Integer tachoMax;
  private Integer vMin;
  private Integer velocity;
  private Integer richtung;
  private boolean commuter;
  private boolean show;
  private boolean active;

  private String source;
  private String commandStationId;
  private boolean synchronize;

  private String dispatcherDirection;
  private String locomotiveDirection;

  private Image locIcon;
  private CommandStationBean commandStationBean;

  private final Map<Integer, FunctionBean> functions;

  public LocomotiveBean() {
    functions = new HashMap<>();
  }

  public LocomotiveBean(Long id, String name, Long uid, Integer address, String icon, String decoderTypeString,
          Integer tachoMax, Integer vMin, Integer velocity, Integer direction, boolean commuter, boolean show) {

    this(id, name, uid, address, icon, decoderTypeString, tachoMax, vMin, velocity, direction, commuter, show, false);
  }

  public LocomotiveBean(Long id, String name, Long uid, Integer address, String icon, String decoderTypeString, Integer tachoMax,
          Integer vMin, Integer velocity, Integer direction, boolean commuter, boolean show, boolean synchronize) {

    this.id = id;
    this.name = name;
    this.uid = uid;
    this.address = address;
    this.icon = icon;
    this.decoderTypeString = decoderTypeString;
    this.tachoMax = tachoMax;
    this.vMin = vMin;
    this.velocity = velocity;
    if (direction != null) {
      this.locomotiveDirection = Direction.getDirectionMarkin(direction).getDirection();
    }

    this.commuter = commuter;
    this.show = show;
    this.synchronize = synchronize;

    functions = new HashMap<>();
  }

  @Id
  @Column(name = "id")
  @ColumnPosition(position = 0)
  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  @Column(name = "name", length = 255, nullable = false)
  @ColumnPosition(position = 2)
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Column(name = "uid")
  @ColumnPosition(position = 20)
  public Long getUid() {
    return uid;
  }

  public void setUid(Long uid) {
    this.uid = uid;
  }

  @Column(name = "address", nullable = false)
  @ColumnPosition(position = 3)
  public Integer getAddress() {
    return address;
  }

  public void setAddress(Integer address) {
    this.address = address;
  }

  @Column(name = "icon", length = 255)
  @ColumnPosition(position = 12)
  public String getIcon() {
    return icon;
  }

  public void setIcon(String icon) {
    this.icon = icon;
  }

  @Column(name = "decoder_type", length = 255, nullable = false)
  @ColumnPosition(position = 24)
  public String getDecoderTypeString() {
    return decoderTypeString;
  }

  public void setDecoderTypeString(String decoderTypeString) {
    this.decoderTypeString = decoderTypeString;
  }

  @Transient
  @Column(name = "decoder")
  @ColumnPosition(position = 4)
  public DecoderType getDecoderType() {
    DecoderType dt = DecoderType.get(this.decoderTypeString);
    return dt;
  }

  @Column(name = "tacho_max")
  @ColumnPosition(position = 5)
  public Integer getTachoMax() {
    return tachoMax;
  }

  public void setTachoMax(Integer tachoMax) {
    this.tachoMax = tachoMax;
  }

  @Column(name = "v_min")
  @ColumnPosition(position = 6)
  public Integer getvMin() {
    return vMin;
  }

  public void setvMin(Integer vMin) {
    this.vMin = vMin;
  }

  @Column(name = "velocity")
  @ColumnPosition(position = 7)
  public Integer getVelocity() {
    return velocity;
  }

  public void setVelocity(Integer velocity) {
    this.velocity = velocity;
  }

  @Transient
  @Column(name = "richtung")
  @ColumnPosition(position = 21)
  public Integer getRichtung() {
    return getDirection().getMarklinValue();
  }

  public void setRichtung(Integer richtung) {
    if (richtung != null) {
      this.locomotiveDirection = Direction.getDirectionMarkin(richtung).getDirection();
    } else {
      this.locomotiveDirection = null;
    }
  }

  @Column(name = "locomotive_direction", length = 255, nullable = true)
  @ColumnPosition(position = 8)
  public Direction getDirection() {
    if (locomotiveDirection != null) {
      return Direction.get(locomotiveDirection);
    } else {
      return Direction.FORWARDS;
    }
  }

  public void setDirection(Direction direction) {
    if (direction != null) {
      this.locomotiveDirection = direction.getDirection();
    } else {
      this.locomotiveDirection = null;
    }
  }

  @Transient
  public Direction toggleDirection() {
    Direction d = getDirection();
    if (Direction.FORWARDS == d) {
      return Direction.BACKWARDS;
    } else {
      return Direction.FORWARDS;
    }
  }

  @Transient
  public Direction toggleDispatcherDirection() {
    Direction d = getDispatcherDirection();
    if (Direction.BACKWARDS == d) {
      return Direction.FORWARDS;
    } else {
      return Direction.BACKWARDS;
    }
  }

  @Transient
  public static Direction toggle(Direction direction) {
    if (Direction.BACKWARDS == direction) {
      return Direction.FORWARDS;
    } else {
      return Direction.BACKWARDS;
    }
  }

  @Column(name = "dispatcher_direction", length = 255, nullable = true)
  public Direction getDispatcherDirection() {
    if (dispatcherDirection != null) {
      return Direction.get(dispatcherDirection);
    } else {
      return null;
    }
  }

  public void setDispatcherDirection(Direction dispatcherDirection) {
    if (dispatcherDirection != null) {
      this.dispatcherDirection = dispatcherDirection.getDirection();
    } else {
      this.dispatcherDirection = null;
    }
  }

  @Column(name = "commuter", columnDefinition = "commuter bool default '0'")
  @ColumnPosition(position = 9)
  public boolean isCommuter() {
    return commuter;
  }

  public void setCommuter(boolean commuter) {
    this.commuter = commuter;
  }

  @Column(name = "show", nullable = false, columnDefinition = "show bool default '1'")
  @ColumnPosition(position = 10)
  public boolean isShow() {
    return show;
  }

  public void setShow(boolean show) {
    this.show = show;
  }

  @Transient
  @Column(name = "image")
  @ColumnPosition(position = 1)
  public Image getLocIcon() {
    return locIcon;
  }

  public void setLocIcon(Image locIcon) {
    this.locIcon = locIcon;
  }

  @Column(name = "imported", length = 255)
  @ColumnPosition(position = 12)
  public String getImported() {
    return source;
  }

  public void setImported(String imported) {
    this.source = imported;
  }

  @Column(name = "command_station_id", length = 255, nullable = false)
  @ColumnPosition(position = 13)
  public String getCommandStationId() {
    return commandStationId;
  }

  public void setCommandStationId(String commandStationId) {
    this.commandStationId = commandStationId;
  }

  @Transient
  public CommandStationBean getCommandStationBean() {
    return commandStationBean;
  }

  public void setCommandStationBean(CommandStationBean commandStationBean) {
    this.commandStationBean = commandStationBean;
    if (commandStationBean != null) {
      this.commandStationId = commandStationBean.getId();
    }
  }

  @Column(name = "synchronize", nullable = false, columnDefinition = "synchronize bool default '0'")
  @ColumnPosition(position = 14)
  public boolean isSynchronize() {
    return synchronize;
  }

  public void setSynchronize(boolean synchronize) {
    this.synchronize = synchronize;
  }

  @Transient
  public int getFunctionCount() {
    return this.functions.size();
  }

  @Transient
  public Map<Integer, FunctionBean> getFunctions() {
    return functions;
  }

  public void addFunction(FunctionBean function) {
    this.functions.put(function.getNumber(), function);
  }

  public void addAllFunctions(List<FunctionBean> functions) {
    for (FunctionBean function : functions) {
      this.functions.put(function.getNumber(), function);
    }
  }

  public void setFunctions(List<FunctionBean> functions) {
    this.functions.clear();
    for (FunctionBean function : functions) {
      //function.setLocomotiveId(id);
      this.functions.put(function.getNumber(), function);
    }
  }

  @Transient
  public FunctionBean getFunctionBean(Integer functionNumber) {
    return this.functions.get(functionNumber);
  }

  @Transient
  public boolean isActive() {
    return active;
  }

  public void setActive(boolean active) {
    this.active = active;
  }

  @Override
  public String toString() {
    return this.name;
  }

  public String toLogString() {
    return "LocomotiveBean{" + "id=" + id + ", name=" + name + ", uid=" + uid + ", address=" + address + ", icon=" + icon + ", decoderType=" + decoderTypeString + ", tachoMax=" + tachoMax + ", vMin=" + vMin + ", velocity=" + velocity + ", richtung=" + richtung + "}";
  }

  //Convenience
  @Transient
  public boolean isFunctionValue(Integer number) {
    if (this.functions.containsKey(number)) {
      FunctionBean f = this.functions.get(number);

      return f.getValue() == 1;
    } else {
      return false;
    }
  }

  @Transient
  public boolean hasFunction(Integer number) {
    return this.functions.containsKey(number);
  }

  public void setFunctionValue(Integer number, boolean value) {
    if (this.functions.containsKey(number)) {
      FunctionBean f = this.functions.get(number);
      f.setValue(value ? 1 : 0);
    }
  }

  public void setFunctionValue(Integer number, Integer value) {
    if (this.functions.containsKey(number)) {
      FunctionBean f = this.functions.get(number);
      f.setValue(value);
    }
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 53 * hash + Objects.hashCode(this.id);
    hash = 53 * hash + Objects.hashCode(this.name);
    hash = 53 * hash + Objects.hashCode(this.uid);
    hash = 53 * hash + Objects.hashCode(this.address);
    hash = 53 * hash + Objects.hashCode(this.icon);
    hash = 53 * hash + Objects.hashCode(this.decoderTypeString);
    hash = 53 * hash + Objects.hashCode(this.tachoMax);
    hash = 53 * hash + Objects.hashCode(this.vMin);
    hash = 53 * hash + Objects.hashCode(this.velocity);
    hash = 53 * hash + Objects.hashCode(this.richtung);
    hash = 53 * hash + Objects.hashCode(this.locIcon);
    hash = 53 * hash + Objects.hashCode(this.show);
    hash = 53 * hash + Objects.hashCode(this.source);
    hash = 53 * hash + Objects.hashCode(this.synchronize);
    hash = 53 * hash + Objects.hashCode(this.commandStationId);

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
    final LocomotiveBean other = (LocomotiveBean) obj;
    if (!Objects.equals(this.name, other.name)) {
      return false;
    }
    if (!Objects.equals(this.icon, other.icon)) {
      return false;
    }
    if (!Objects.equals(this.decoderTypeString, other.decoderTypeString)) {
      return false;
    }
    if (!Objects.equals(this.id, other.id)) {
      return false;
    }
    if (!Objects.equals(this.uid, other.uid)) {
      return false;
    }
    if (!Objects.equals(this.address, other.address)) {
      return false;
    }
    if (!Objects.equals(this.tachoMax, other.tachoMax)) {
      return false;
    }
    if (!Objects.equals(this.vMin, other.vMin)) {
      return false;
    }
    if (!Objects.equals(this.velocity, other.velocity)) {
      return false;
    }
    if (!Objects.equals(this.richtung, other.richtung)) {
      return false;
    }
    if (!Objects.equals(this.source, other.source)) {
      return false;
    }
    if (!Objects.equals(this.commandStationId, other.commandStationId)) {
      return false;
    }
    if (!Objects.equals(this.synchronize, other.synchronize)) {
      return false;
    }
    return Objects.equals(this.show, other.show);
  }

  public enum Direction {
    SAME("Same"), FORWARDS("Forwards"), BACKWARDS("Backwards"), SWITCH("Switch");

    private final String direction;

    private static final Map<String, Direction> ENUM_MAP;

    Direction(String direction) {
      this.direction = direction;
    }

    public String getDirection() {
      return this.direction;
    }

    static {
      Map<String, Direction> map = new ConcurrentHashMap<>();
      for (Direction instance : Direction.values()) {
        map.put(instance.getDirection(), instance);
      }
      ENUM_MAP = Collections.unmodifiableMap(map);
    }

    public static Direction get(String direction) {
      return ENUM_MAP.get(direction);
    }

    private static int translate2MarklinValue(String value) {
      return switch (value) {
        case "Forwards" ->
          1;
        case "Backwards" ->
          2;
        case "Switch" ->
          3;
        default ->
          0;
      };
    }

    public int getMarklinValue() {
      return translate2MarklinValue(this.direction);
    }

    private static int translate2DccExValue(String value) {
      return switch (value) {
        case "Forwards" ->
          1;
        case "Backwards" ->
          0;
        default ->
          1;
      };
    }

    private static String translate2DccExDirectionString(int value) {
      return switch (value) {
        case 1 ->
          "Forwards";
        case 0 ->
          "Backwards";
        default ->
          "Forwards";
      };
    }

    public int getDccExValue() {
      return translate2DccExValue(this.direction);
    }

    private static int translate2EcosValue(String value) {
      return switch (value) {
        case "Forwards" ->
          0;
        case "Backwards" ->
          1;
        default ->
          0;
      };
    }

    private static String translate2EcosDirectionString(String value) {
      return switch (value) {
        case "0" ->
          "Forwards";
        case "1" ->
          "Backwards";
        default ->
          "Forwards";
      };
    }

    public int getEcosValue() {
      return translate2EcosValue(this.direction);
    }

    private static String translate2MarklinDirectionString(int value) {
      return switch (value) {
        case 1 ->
          "Forwards";
        case 2 ->
          "Backwards";
        case 3 ->
          "Switch";
        default ->
          //"Same";
          "Forwards";
      };
    }

    public static Direction getDirectionMarkin(int marklinValue) {
      return ENUM_MAP.get(translate2MarklinDirectionString(marklinValue));
    }

    public static Direction getDirectionDccEx(int dccExValue) {
      return ENUM_MAP.get(translate2DccExDirectionString(dccExValue));
    }

    public static Direction getDirectionEcos(String ecosValue) {
      return ENUM_MAP.get(translate2EcosDirectionString(ecosValue));
    }

    public Direction toggle() {
      return switch (this.direction) {
        case "Forwards" ->
          BACKWARDS;
        case "Backwards" ->
          FORWARDS;
        default ->
          SAME;
      };
    }
  }

  public enum DecoderType {
    //TODO: make more generic, incorporate the speedsteps
    //Marklin types
    MM("mm"),
    MM_DIL("mm2_dil8"),
    MFX("mfx"),
    MFXP("mfx+"),
    DCC("dcc"),
    SX1("sx1"),
    MM_PRG("mm_prg"),
    MM2_PRG("mm2_prg"),
    //ESU types
    DCC14("dcc14"),
    DCC28("dcc28"),
    DCC128("dcc128"),
    MM14("mm14"),
    MM28("mm28"),;

    private final String decoderType;

    private static final Map<String, DecoderType> ENUM_MAP;

    DecoderType(String decoderType) {
      this.decoderType = decoderType;
    }

    public String getDecoderType() {
      return this.decoderType;
    }

    static {
      Map<String, DecoderType> map = new ConcurrentHashMap<>();
      for (DecoderType instance : DecoderType.values()) {
        map.put(instance.getDecoderType(), instance);
      }
      ENUM_MAP = Collections.unmodifiableMap(map);
    }

    public static DecoderType get(String decoderType) {
      if (decoderType == null) {
        return null;
      }
      return ENUM_MAP.get(decoderType.toLowerCase());

    }
  }

}
