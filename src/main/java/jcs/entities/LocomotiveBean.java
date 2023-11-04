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

import java.awt.Image;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import jcs.entities.enums.DecoderType;
import jcs.persistence.util.ColumnPosition;

@Table(name = "locomotives")
public class LocomotiveBean implements Serializable {

  private Long id;
  private String name;
  private String previousName;
  private Long uid;
  private Long mfxUid;
  private Integer address;
  private String icon;
  private String decoderTypeString;
  private String mfxSid;
  private Integer tachoMax;
  private Integer vMin;
  private Integer accelerationDelay;
  private Integer brakeDelay;
  private Integer volume;
  private String spm;
  private Integer velocity;
  private Integer richtung;
  private String mfxType;
  private boolean commuter;
  private Integer length;
  private String block;
  private boolean show;
  private String imported;

  private Image locIcon;

  private final Map<Integer, FunctionBean> functions;

  public LocomotiveBean() {
    functions = new HashMap<>();
  }

  public LocomotiveBean(Long id, String name, Long uid, Long mfxUid, Integer address, String icon, String decoderTypeString,
          String mfxSid, Integer tachoMax, Integer vMin, Integer velocity, Integer direction, boolean commuter, Integer length, boolean show) {

    this(id, name, null, uid, mfxUid, address, icon, decoderTypeString, mfxSid, tachoMax, vMin, null, null, null, null, velocity,
            direction, null, null, commuter, length, show);
  }

  public LocomotiveBean(Long id, String name, String previousName, Long uid,
          Long mfxUid, Integer address, String icon, String decoderTypeString,
          String mfxSid, Integer tachoMax, Integer vMin, Integer accelerationDelay,
          Integer brakeDelay, Integer volume, String spm, Integer velocity,
          Integer direction, String mfxType, String block, boolean commuter, Integer length, boolean show) {

    this.id = id;
    this.name = name;
    this.previousName = previousName;
    this.uid = uid;
    this.mfxUid = mfxUid;
    this.address = address;
    this.icon = icon;
    this.decoderTypeString = decoderTypeString;
    this.mfxSid = mfxSid;
    this.tachoMax = tachoMax;
    this.vMin = vMin;
    this.accelerationDelay = accelerationDelay;
    this.brakeDelay = brakeDelay;
    this.volume = volume;
    this.spm = spm;
    this.velocity = velocity;
    this.richtung = direction;
    this.mfxType = mfxType;
    this.block = block;
    this.commuter = commuter;
    this.length = length;
    this.show = show;

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
  @ColumnPosition(position = 1)
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Transient
  public String getPreviousName() {
    return previousName;
  }

  public void setPreviousName(String previousName) {
    this.previousName = previousName;
  }

  @Column(name = "uid")
  @ColumnPosition(position = 2)
  public Long getUid() {
    return uid;
  }

  public void setUid(Long uid) {
    this.uid = uid;
  }

  @Column(name = "mfx_uid")
  @ColumnPosition(position = 14)
  public Long getMfxUid() {
    return mfxUid;
  }

  public void setMfxUid(Long mfxUid) {
    this.mfxUid = mfxUid;
  }

  @Column(name = "address", nullable = false)
  @ColumnPosition(position = 4)
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
  @ColumnPosition(position = 3)
  public String getDecoderTypeString() {
    return decoderTypeString;
  }

  public void setDecoderTypeString(String decoderTypeString) {
    this.decoderTypeString = decoderTypeString;
  }

  @Transient
  public DecoderType getDecoderType() {
    return DecoderType.get(this.decoderTypeString);
  }

  @Column(name = "mfx_sid")
  @ColumnPosition(position = 13)
  public String getMfxSid() {
    return mfxSid;
  }

  public void setMfxSid(String mfxSid) {
    this.mfxSid = mfxSid;
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

  @Transient
  public Integer getAccelerationDelay() {
    return accelerationDelay;
  }

  public void setAccelerationDelay(Integer accelerationDelay) {
    this.accelerationDelay = accelerationDelay;
  }

  @Transient
  public Integer getBrakeDelay() {
    return brakeDelay;
  }

  public void setBrakeDelay(Integer brakeDelay) {
    this.brakeDelay = brakeDelay;
  }

  @Transient
  public Integer getVolume() {
    return volume;
  }

  public void setVolume(Integer volume) {
    this.volume = volume;
  }

  @Transient
  public String getSpm() {
    return spm;
  }

  @Transient
  public void setSpm(String spm) {
    this.spm = spm;
  }

  @Column(name = "velocity")
  @ColumnPosition(position = 7)
  public Integer getVelocity() {
    return velocity;
  }

  public void setVelocity(Integer velocity) {
    this.velocity = velocity;
  }

  @Column(name = "richtung")
  @ColumnPosition(position = 8)
  public Integer getRichtung() {
    return richtung;
  }

  public void setRichtung(Integer richtung) {
    this.richtung = richtung;
  }

  @Transient
  public Direction getDirection() {
    if (this.richtung != null) {
      return Direction.getDirectionMarkin(this.richtung);
    } else {
      return Direction.FORWARDS;
    }
  }

  public void setDirection(Direction direction) {
    this.richtung = direction.getMarklinValue();
  }

  @Transient
  public String getMfxType() {
    return mfxType;
  }

  public void setMfxType(String mfxType) {
    this.mfxType = mfxType;
  }

  @Transient
  public String getBlock() {
    return block;
  }

  public void setBlock(String block) {
    this.block = block;
  }

  @Column(name = "commuter", columnDefinition = "commuter bool default '0'")
  @ColumnPosition(position = 9)
  public boolean isCommuter() {
    return commuter;
  }

  public void setCommuter(boolean commuter) {
    this.commuter = commuter;
  }

  @Column(name = "length")
  @ColumnPosition(position = 11)
  public Integer getLength() {
    return length;
  }

  public void setLength(Integer locLength) {
    this.length = locLength;
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
  public Image getLocIcon() {
    return locIcon;
  }

  public void setLocIcon(Image locIcon) {
    this.locIcon = locIcon;
  }

  @Column(name = "imported", length = 255)
  public String getImported() {
    return imported;
  }

  public void setImported(String imported) {
    this.imported = imported;
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
      this.functions.put(function.getNumber(), function);
    }
  }

  @Transient
  public FunctionBean getFunctionBean(Integer functionNumber) {
    return this.functions.get(functionNumber);
  }

  @Override
  public String toString() {
    return this.name;
  }

  public String toLogString() {
    return "LocomotiveBean{" + "id=" + id + ", name=" + name + ", uid=" + uid + ", mfxUid=" + mfxUid + ", address=" + address + ", icon=" + icon + ", decoderType=" + decoderTypeString + ", mfxSid=" + mfxSid + ", tachoMax=" + tachoMax + ", vMin=" + vMin + ", accelerationDelay=" + accelerationDelay + ", brakeDelay=" + brakeDelay + ", volume=" + volume + ", spm=" + spm + ", velocity=" + velocity + ", richtung=" + richtung + ", blocks=" + block + "}";
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
    hash = 53 * hash + Objects.hashCode(this.previousName);
    hash = 53 * hash + Objects.hashCode(this.uid);
    hash = 53 * hash + Objects.hashCode(this.mfxUid);
    hash = 53 * hash + Objects.hashCode(this.address);
    hash = 53 * hash + Objects.hashCode(this.icon);
    hash = 53 * hash + Objects.hashCode(this.decoderTypeString);
    hash = 53 * hash + Objects.hashCode(this.mfxSid);
    hash = 53 * hash + Objects.hashCode(this.tachoMax);
    hash = 53 * hash + Objects.hashCode(this.vMin);
    hash = 53 * hash + Objects.hashCode(this.accelerationDelay);
    hash = 53 * hash + Objects.hashCode(this.brakeDelay);
    hash = 53 * hash + Objects.hashCode(this.volume);
    hash = 53 * hash + Objects.hashCode(this.spm);
    hash = 53 * hash + Objects.hashCode(this.velocity);
    hash = 53 * hash + Objects.hashCode(this.richtung);
    hash = 53 * hash + Objects.hashCode(this.mfxType);
    hash = 53 * hash + Objects.hashCode(this.block);
    hash = 53 * hash + Objects.hashCode(this.locIcon);
    hash = 53 * hash + Objects.hashCode(this.show);
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
    if (!Objects.equals(this.mfxSid, other.mfxSid)) {
      return false;
    }
    if (!Objects.equals(this.mfxType, other.mfxType)) {
      return false;
    }
    if (!Objects.equals(this.id, other.id)) {
      return false;
    }
    if (!Objects.equals(this.uid, other.uid)) {
      return false;
    }
    if (!Objects.equals(this.mfxUid, other.mfxUid)) {
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
    return Objects.equals(this.show, other.show);
    //return Objects.equals(this.locIcon, other.locIcon);
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

}
