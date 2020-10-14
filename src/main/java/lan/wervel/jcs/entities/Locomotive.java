/*
 * Copyright (C) 2018 Frans Jacobs.
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
import java.util.Arrays;
import java.util.Objects;
import lan.wervel.jcs.entities.enums.Direction;
import lan.wervel.jcs.entities.enums.DecoderType;

public class Locomotive extends ControllableDevice {

  protected Direction defaultDirection = Direction.FORWARDS;
  protected Direction direction = Direction.FORWARDS;
  protected Integer speed;
  protected Integer speedSteps;

  protected Integer functionCount;
  //Can be 1 (only light) 5 (light plus 1-4) 8, 15, 31 so use B0[0], B0[0-4], B0[0-8] B0 and B1 or all
  protected int[] functionValues;
  protected String functionTypes;
  protected DecoderType decoderType;
  protected String iconName;
  protected Integer tachoMax;
  protected Integer vMax;
  protected Integer vMin;

  private static Integer MM_SPEED_STEPS = 14;

  public Locomotive() {
    this(null, "Locomotive ?", null, null, DecoderType.MM, Direction.FORWARDS, 0, MM_SPEED_STEPS, null, 0, null, 1, "0", null, Direction.FORWARDS, null, null);
  }

  public Locomotive(Integer address, String name) {
    this(address, name, null, null, DecoderType.MM, Direction.FORWARDS, 0, MM_SPEED_STEPS, null, 0, null, 1, "0", null, Direction.FORWARDS, null, null);
  }

  //Compatibility
  public Locomotive(Integer address, String name, String description, String catalogNumber, Integer vMin, boolean f0, boolean f1, boolean f2, boolean f3, boolean f4) {
    this(address, name, description, catalogNumber, DecoderType.MM, Direction.FORWARDS, 0, MM_SPEED_STEPS, null, vMin, null, 5, (f0 ? "1" : "0") + (f1 ? "1" : "0") + (f2 ? "1" : "0") + (f3 ? "1" : "0") + (f4 ? "1" : "0"), null, Direction.FORWARDS, null, null);
  }

  public Locomotive(Integer address, String name, String description, String catalogNumber, DecoderType decoderType, Direction direction, Integer speed, Integer speedSteps,
          Integer tachoMax, Integer vMin, Integer vMax, Integer functionCount, String functionValues, String functionTypes, Direction defaultDirection, String iconName, BigDecimal id) {

    this(address, name, description, catalogNumber, decoderType, direction, speed, speedSteps, tachoMax, vMin, vMax, functionCount, toFunctionValueArray(functionValues, functionCount), functionTypes, defaultDirection, iconName, id);
  }

  protected Locomotive(Integer address, String name, String description, String catalogNumber, DecoderType decoderType, Direction direction, Integer speed, Integer speedSteps,
          Integer tachoMax, Integer vMin, Integer vMax, Integer functionCount, int[] functionValues, String functionTypes, Direction defaultDirection, String iconName, BigDecimal id) {
    super(address, name, description, catalogNumber);

    if (decoderType == null) {
      this.decoderType = DecoderType.MM;
    } else {
      this.decoderType = decoderType;
    }

    if (direction == null) {
      this.direction = Direction.FORWARDS;
    } else {
      this.direction = direction;
    }

    this.speed = speed;
    this.tachoMax = tachoMax;

    if (speedSteps == null) {
      this.speedSteps = MM_SPEED_STEPS;
    } else {
      this.speedSteps = speedSteps;
    }

    this.vMin = vMin;
    this.vMax = vMax;

    if (functionCount == null) {
      this.functionCount = 1;
    } else {
      this.functionCount = functionCount;
    }

    this.functionValues = new int[32];
    //Preset all functions to false
    for (int i = 0; i < this.functionValues.length; i++) {
      this.functionValues[i] = 0;
    }

    if (functionValues != null) {
      System.arraycopy(functionValues, 0, this.functionValues, 0, functionValues.length);
    }

    this.functionTypes = functionTypes;

    if (defaultDirection == null) {
      this.defaultDirection = Direction.FORWARDS;
    } else {
      this.defaultDirection = defaultDirection;
    }

    this.iconName = iconName;
    this.id = id;
  }

  private static int[] toFunctionValueArray(String functionValues, Integer functionCount) {
    int cnt;
    if (functionCount == null) {
      cnt = 1;
    } else {
      cnt = functionCount;
    }

    String fvals;
    if (functionValues == null) {
      StringBuilder sb = new StringBuilder();
      for (int i = 0; i < cnt; i++) {
        sb.append("0");
      }
      fvals = sb.toString();
    } else {
      fvals = functionValues;
    }

    int[] fv = new int[32];
    //Preset all functions to false
    for (int i = 0; i < fv.length; i++) {
      fv[i] = 0;
    }

    //parse the string
    for (int i = 0; i < cnt; i++) {
      String v = fvals.substring(i, i + 1);
      if ("1".equals(v)) {
        fv[i] = 1;
      }
    }

    return fv;
  }

  public void changeDirection() {
    setDirection(this.direction.toggle());
  }

  public Direction getDirection() {
    return this.direction;
  }

  public void setDirection(Direction direction) {
    if (direction != null && !direction.equals(this.direction)) {
      this.direction = direction;
      this.speed = 0;
    }
  }

  public Integer getSpeed() {
    return speed;
  }

  public void setSpeed(Integer speed) {
    this.speed = speed;
  }

  public boolean getFunctionValue(int functionNumber) {
    if (functionNumber >= 0 && functionNumber < 32) {
      return this.functionValues[functionNumber] == 1;
    } else {
      return false;
    }
  }

  public void setFunctionValue(int functionNumber, boolean value) {
    if (functionNumber >= 0 && functionNumber < 32) {
      this.functionValues[functionNumber] = value ? 1 : 0;
    }
  }

  public boolean isF0() {
    return getFunctionValue(0);
  }

  public final void setF0(boolean f0) {
    setFunctionValue(0, f0);
  }

  public boolean isF1() {
    return getFunctionValue(1);
  }

  public final void setF1(boolean f1) {
    setFunctionValue(1, f1);
  }

  public boolean isF2() {
    return getFunctionValue(2);
  }

  public final void setF2(boolean f2) {
    setFunctionValue(2, f2);
  }

  public boolean isF3() {
    return getFunctionValue(3);
  }

  public final void setF3(boolean f3) {
    setFunctionValue(3, f3);
  }

  public boolean isF4() {
    return getFunctionValue(4);
  }

  public final void setF4(boolean f4) {
    setFunctionValue(4, f4);
  }

  public Direction getDefaultDirection() {
    return defaultDirection;
  }

  public void setDefaultDirection(Direction defaultDirection) {
    this.defaultDirection = defaultDirection;
  }

  public Integer getSpeedSteps() {
    return speedSteps;
  }

  public void setSpeedSteps(Integer speedSteps) {
    this.speedSteps = speedSteps;
  }

  public DecoderType getDecoderType() {
    return decoderType;
  }

  public void setDecoderType(DecoderType decodeType) {
    this.decoderType = decodeType;
  }

  public Integer getFunctionCount() {
    return functionCount;
  }

  public void setFunctionCount(Integer functionCount) {
    this.functionCount = functionCount;
  }

  public int[] getFunctionValues() {
    return functionValues;
  }

  public void setFunctionValues(int[] functionValues) {
    this.functionValues = functionValues;
  }

  public String getFunctionTypes() {
    return functionTypes;
  }

  public void setFunctionTypes(String functionTypes) {
    this.functionTypes = functionTypes;
  }

  public String getIconName() {
    return iconName;
  }

  public void setIconName(String iconName) {
    this.iconName = iconName;
  }

  public Integer getTachoMax() {
    return tachoMax;
  }

  public void setTachoMax(Integer tachoMax) {
    this.tachoMax = tachoMax;
  }

  public Integer getvMax() {
    return vMax;
  }

  public void setvMax(Integer vMax) {
    this.vMax = vMax;
  }

  public Integer getvMin() {
    return vMin;
  }

  public void setvMin(Integer vMin) {
    this.vMin = vMin;
  }

  @Override
  public String toString() {
    //if (this.address != null && this.address > 0) {
    //  return address + " " + name;
    //} else {
    return name;
    //}
  }

  @Override
  public String toLogString() {
    if (this.address != null && this.address > 0) {
      return name + " [" + address + "][" + decoderType + "] Speed: " + this.speed + ", " + this.direction;
    } else {
      return name;
    }
  }

  public boolean isChanged(Locomotive other) {
    if (this == other) {
      return false;
    }
    if (!Objects.equals(this.id, other.id)) {
      return false;
    }
    return !(this.isF0() == other.isF0() && this.isF1() == other.isF1() && this.isF2() == other.isF2() && this.isF3() == other.isF3() && this.isF4() == other.isF4()
            && Objects.deepEquals(this.direction, other.direction) && Objects.deepEquals(this.speed, other.speed));
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
    final Locomotive other = (Locomotive) obj;
    if (!Objects.deepEquals(this.functionValues, other.functionValues)) {
      return false;
    }
    if (!Objects.equals(this.decoderType, other.decoderType)) {
      return false;
    }
    if (!Objects.equals(this.direction, other.direction)) {
      return false;
    }
    if (!Objects.equals(this.speed, other.speed)) {
      return false;
    }
    if (!Objects.equals(this.tachoMax, other.tachoMax)) {
      return false;
    }
    if (!Objects.equals(this.vMax, other.vMax)) {
      return false;
    }
    if (!Objects.equals(this.vMin, other.vMin)) {
      return false;
    }
    if (!Objects.equals(this.functionCount, other.functionCount)) {
      return false;
    }
    if (!Objects.equals(this.defaultDirection, other.defaultDirection)) {
      return false;
    }
    if (!Objects.equals(this.speedSteps, other.speedSteps)) {
      return false;
    }

    return super.equals(other);
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 47 * hash + Objects.hashCode(this.defaultDirection);
    hash = 47 * hash + Objects.hashCode(this.direction);
    hash = 47 * hash + Objects.hashCode(this.speed);
    hash = 47 * hash + Objects.hashCode(this.speedSteps);
    hash = 47 * hash + Objects.hashCode(this.functionCount);
    hash = 47 * hash + Arrays.hashCode(this.functionValues);
    hash = 47 * hash + Objects.hashCode(this.decoderType);
    hash = 47 * hash + Objects.hashCode(this.iconName);
    hash = 47 * hash + Objects.hashCode(this.tachoMax);
    hash = 47 * hash + Objects.hashCode(this.vMax);
    hash = 47 * hash + Objects.hashCode(this.vMin);
    hash = 47 * hash + Objects.hashCode(super.hashCode());
    return hash;
  }

}
