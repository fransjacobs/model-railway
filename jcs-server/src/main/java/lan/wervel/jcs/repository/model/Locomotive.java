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
package lan.wervel.jcs.repository.model;

import java.util.Objects;

public class Locomotive extends ControllableItem {
  
  private static final long serialVersionUID = -2022628399529393484L;
  
  public enum Direction {
    Forwards, Backwards
  };
  
  protected boolean specialFuctions;
  protected boolean f0;
  protected boolean f1;
  protected boolean f2;
  protected boolean f3;
  protected boolean f4;
  
  protected Direction defaultDirection;
  protected Direction direction = Direction.Forwards;
  protected Integer speed;
  protected Integer throttle;
  protected Integer minSpeed;
  protected Integer speedSteps = 14;
  
  private boolean selected = false;
  private String type;
  protected String f0Type;
  protected String f1Type;
  protected String f2Type;
  protected String f3Type;
  protected String f4Type;
  
  public Locomotive() {
    this(null, "Locomotive ?");
  }
  
  public Locomotive(Integer address, String name) {
    this(address, name, null, null, 0, null);
  }
  
  public Locomotive(Integer address, String name, String description, String catalogNumber, Integer minSpeed, String type) {
    this(address, name, description, catalogNumber, minSpeed, type, false, false, false, false, false);
  }
  
  public Locomotive(Integer address, String name, String description, String catalogNumber, Integer minSpeed, String type, boolean f0, boolean f1, boolean f2, boolean f3, boolean f4) {
    this(address, name, description, catalogNumber, minSpeed, type, f0, f1, f2, f3, f4, null, null, null);
  }
  
  protected Locomotive(Integer address, String name, String description, String catalogNumber, Integer minSpeed, String type, boolean f0, boolean f1, boolean f2, boolean f3, boolean f4, Integer speed, Integer throttle, Direction direction) {
    super(address, name, description, catalogNumber);
    
    this.f0 = f0;
    this.f1 = f1;
    this.f2 = f2;
    this.f3 = f3;
    this.f4 = f4;
    
    this.type = type;
    this.minSpeed = minSpeed;
    this.speed = speed;
    this.throttle = throttle;
    this.direction = direction;
  }
  
  public void changeDirection() {
    Direction oldDirection = this.direction;
    if (Direction.Forwards.equals(direction)) {
      this.direction = Direction.Backwards;
    } else {
      this.direction = Direction.Forwards;
    }
    this.speed = 0;
    this.throttle = 0;
    this.handleAttributeChange("changeDirection", oldDirection, this.direction);
  }
  
  public Direction getDirection() {
    return this.direction;
  }
  
  public void setDirection(Direction direction) {
    if (direction != null && !direction.equals(this.direction)) {
      Direction oldDirection = this.direction;
      this.direction = direction;
      this.speed = 0;
      this.throttle = 0;
      this.handleAttributeChange("setDirection", oldDirection, this.direction);
    }
  }
  
  public Integer getMinSpeed() {
    return minSpeed;
  }
  
  public Integer getSpeed() {
    return speed;
  }
  
  public void setSpeed(Integer speed) {
    Integer oldSpeed = this.speed;
    this.speed = speed;
    this.handleAttributeChange("setSpeed", oldSpeed, this.speed);
  }
  
  public Integer getThrottle() {
    return throttle;
  }
  
  public void setThrottle(Integer throttle) {
    Integer oldThrottle = this.throttle;
    this.throttle = throttle;
    this.handleAttributeChange("setThrottle", oldThrottle, this.throttle);
  }
  
  public void stop() {
    Integer oldSpeed = this.speed;
    this.speed = 0;
    this.handleAttributeChange("stop", oldSpeed, this.speed);
  }
  
  public boolean isF0() {
    return f0;
  }
  
  public void setF0(boolean f0) {
    boolean oldF0 = this.f0;
    this.f0 = f0;
    this.handleAttributeChange("setF0", oldF0, this.f0);
  }
  
  public boolean isF1() {
    return f1;
  }
  
  public void setF1(boolean f1) {
    boolean oldF1 = this.f1;
    this.f1 = f1;
    this.handleAttributeChange("setF1", oldF1, this.f1);
  }
  
  public boolean isF2() {
    return f2;
  }
  
  public void setF2(boolean f2) {
    boolean oldF2 = this.f2;
    this.f2 = f2;
    this.handleAttributeChange("setF2", oldF2, this.f2);
  }
  
  public boolean isF3() {
    return f3;
  }
  
  public void setF3(boolean f3) {
    boolean oldF3 = this.f3;
    this.f3 = f3;
    this.handleAttributeChange("setF3", oldF3, this.f3);
  }
  
  public boolean isF4() {
    return f4;
  }
  
  public void setF4(boolean f4) {
    boolean oldF4 = this.f4;
    this.f4 = f4;
    this.handleAttributeChange("setF4", oldF4, this.f4);
  }
  
  public boolean isSelected() {
    return selected;
  }
  
  public void setSelected(boolean selected) {
    boolean oldSelected = this.selected;
    this.selected = selected;
    this.handleAttributeChange("setSelected", oldSelected, this.selected);
  }
  
  public String getType() {
    return type;
  }
  
  public void setType(String type) {
    this.type = type;
  }
  
  public boolean isSpecialFuctions() {
    return specialFuctions;
  }
  
  public void setSpecialFuctions(boolean specialFuctions) {
    this.specialFuctions = specialFuctions;
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
  
  public String getF0Type() {
    return f0Type;
  }
  
  public void setF0Type(String f0Type) {
    this.f0Type = f0Type;
  }
  
  public String getF1Type() {
    return f1Type;
  }
  
  public void setF1Type(String f1Type) {
    this.f1Type = f1Type;
  }
  
  public String getF2Type() {
    return f2Type;
  }
  
  public void setF2Type(String f2Type) {
    this.f2Type = f2Type;
  }
  
  public String getF3Type() {
    return f3Type;
  }
  
  public void setF3Type(String f3Type) {
    this.f3Type = f3Type;
  }
  
  public String getF4Type() {
    return f4Type;
  }
  
  public void setF4Type(String f4Type) {
    this.f4Type = f4Type;
  }
  
  @Override
  public String toString() {
    if (this.address != null && this.address > 0) {
      return name + " [" + address + "]";
    } else {
      return name;
    }
  }
  
  @Override
  public int hashCode() {
    int hash = 3;
    hash = 97 * hash + (this.f0 ? 1 : 0);
    hash = 97 * hash + (this.f1 ? 1 : 0);
    hash = 97 * hash + (this.f2 ? 1 : 0);
    hash = 97 * hash + (this.f3 ? 1 : 0);
    hash = 97 * hash + (this.f4 ? 1 : 0);
    hash = 97 * hash + Objects.hashCode(this.direction);
    hash = 97 * hash + Objects.hashCode(this.speed);
    hash = 97 * hash + Objects.hashCode(this.throttle);
    hash = 97 * hash + (this.selected ? 1 : 0);
    hash = 97 * hash + Objects.hashCode(this.type);
    hash = 97 * hash + Objects.hashCode(this.defaultDirection);
    hash = 97 * hash + Objects.hashCode(this.specialFuctions);
    hash = 97 * hash + Objects.hashCode(this.defaultDirection);
    hash = 97 * hash + Objects.hashCode(this.speedSteps);
    hash = 97 * hash + super.hashCode();
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
    final Locomotive other = (Locomotive) obj;
    if (this.f0 != other.f0) {
      return false;
    }
    if (this.f1 != other.f1) {
      return false;
    }
    if (this.f2 != other.f2) {
      return false;
    }
    if (this.f3 != other.f3) {
      return false;
    }
    if (this.f4 != other.f4) {
      return false;
    }
    if (this.specialFuctions != other.specialFuctions) {
      return false;
    }
    if (this.selected != other.selected) {
      return false;
    }
    if (!Objects.equals(this.type, other.type)) {
      return false;
    }
    if (this.direction != other.direction) {
      return false;
    }
    if (!Objects.equals(this.speed, other.speed)) {
      return false;
    }
    if (!Objects.equals(this.throttle, other.throttle)) {
      return false;
    }
    if (!Objects.equals(this.minSpeed, other.minSpeed)) {
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
  public Locomotive copy() {
    Locomotive l = new Locomotive(this.address, this.name, this.description, this.catalogNumber, this.minSpeed, this.type, this.f0, this.f1, this.f2, this.f3, this.f4, this.speed, this.throttle, this.direction);
    l.setSpecialFuctions(this.specialFuctions);
    l.setSelected(this.selected);
    return l;
  }
}
