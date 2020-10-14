/*
 * Copyright (C) 2020 Frans Jacobs.
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
package lan.wervel.jcs.controller.cs2;

import java.util.Objects;
import lan.wervel.jcs.controller.cs2.can.CanMessage;
import lan.wervel.jcs.entities.enums.Direction;

/**
 *
 * @author Frans Jacobs
 */
public class DirectionInfo {

  private Direction direction;

  public DirectionInfo(CanMessage statusRequest) {
    parseMessage(statusRequest);
  }

  private void parseMessage(CanMessage statusRequest) {
    int[] data = statusRequest.getResponse(0).getData();
    int dir = data[4];

    this.direction = Direction.cs2Get(dir);
  }

  @Override
  public String toString() {
    return "DirectionInfo{" + "direction=" + direction + '}';
  }

  public Direction getDirection() {
    return direction;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 47 * hash + Objects.hashCode(this.direction);
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
    final DirectionInfo other = (DirectionInfo) obj;
    return this.direction == other.direction;
  }

}
