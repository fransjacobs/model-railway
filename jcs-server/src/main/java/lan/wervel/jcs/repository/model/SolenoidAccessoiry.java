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

public class SolenoidAccessoiry extends ControllableItem {

  private static final long serialVersionUID = 1493722970971519039L;

  public enum Type {
    SIGNAL, TURNOUT, GENERAL
  }

  public enum StatusType {
    RED, GREEN, OFF
  }

  private StatusType status;
  private Type type;

  public SolenoidAccessoiry() {
    this(null, "?", null, Type.GENERAL, null);
  }

  public SolenoidAccessoiry(Integer address, String description, String catalogNumber, Type type) {
    this(address, description, catalogNumber, type, null);
  }

  private SolenoidAccessoiry(Integer address, String description, String catalogNumber, Type type, StatusType status) {
    super(address, (type != null ? type.toString() : null), description, catalogNumber);
    this.type = type;
    this.status = status;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("[");
    if (this.address != null && this.address > 0) {
      sb.append(this.address);
    }
    sb.append("] ");
    if (null == this.type) {
      sb.append("G: ");
    } else {
      sb.append("");
      switch (this.type) {
        case SIGNAL:
          sb.append("S: ");
          break;
        case TURNOUT:
          sb.append("T: ");
          break;
        default:
          sb.append("G: ");
          break;
      }
    }
    sb.append(this.description);
    sb.append(" {");
    sb.append(this.status);
    sb.append("}");
    return sb.toString();
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 41 * hash + Objects.hashCode(this.status);
    hash = 41 * hash + Objects.hashCode(this.type);
    hash = 41 * hash + super.hashCode();
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
    final SolenoidAccessoiry other = (SolenoidAccessoiry) obj;
    if (this.status != other.status) {
      return false;
    }
    if (this.type != other.type) {
      return false;
    }
    return super.equals(other);
  }

  public Type getType() {
    return type;
  }

  public void setType(Type type) {
    this.type = type;
  }

  public boolean isSignal() {
    return Type.SIGNAL.equals(this.type);
  }

  public boolean isTurnout() {
    return Type.TURNOUT.equals(this.type);
  }

  public void setRed() {
    setStatus(StatusType.RED);
  }

  public boolean isRed() {
    return StatusType.RED.equals(this.status);
  }

  public void setGreen() {
    setStatus(StatusType.GREEN);
  }

  public boolean isGreen() {
    return StatusType.GREEN.equals(this.status);
  }

  public void setCurved() {
    setStatus(StatusType.RED);
  }

  public boolean isCurved() {
    return StatusType.RED.equals(this.status);
  }

  public void setStraight() {
    setStatus(StatusType.GREEN);
  }

  public boolean isStraight() {
    return StatusType.GREEN.equals(this.status);
  }

  public void setStatus(String status) {
    switch (status) {
      case "RED":
        setStatus(StatusType.RED);
        break;
      case "GREEN":
        setStatus(StatusType.GREEN);
        break;
      default:
        setStatus(StatusType.OFF);
        break;
    }
  }

  public void setStatus(StatusType status) {
    StatusType oldStatus = this.status;
    this.status = status;
    this.handleAttributeChange("setStatus", oldStatus, status);
  }

  public StatusType getStatus() {
    return status;
  }

  @Override
  public SolenoidAccessoiry copy() {
    return new SolenoidAccessoiry(this.address, this.description, this.catalogNumber, this.type, this.status);
  }

  public static StatusType getStatusType(String status) {
    switch (status) {
      case "RED":
        return StatusType.RED;
      case "GREEN":
        return StatusType.GREEN;
      case "CURVED":
        return StatusType.RED;
      case "STRAIGHT":
        return StatusType.GREEN;
      default:
        return StatusType.OFF;
    }
  }
}
