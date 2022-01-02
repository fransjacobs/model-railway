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
package jcs.trackservice;

import java.io.Serializable;
import java.util.Objects;
import jcs.entities.SignalBean;
import jcs.entities.SolenoidAccessory;
import jcs.entities.enums.AccessoryType;
import jcs.entities.enums.AccessoryValue;
import jcs.entities.enums.SignalValue;

/**
 *
 * @author Frans Jacobs
 */
public class AccessoryEvent implements Serializable {

  private final AccessoryType accessoryType;

  //Turnout / SignalBean
  private final Integer address;
  private final AccessoryValue value;

  //Signal  
  private final Integer address2;
  private final AccessoryValue value2;
  private final SignalValue signalValue;

  public AccessoryEvent(SolenoidAccessory accessoiry) {
    if (accessoiry.isTurnout()) {
      this.accessoryType = AccessoryType.TURNOUT;
      this.address = accessoiry.getAddress();
      this.value = accessoiry.getValue();
      this.value2 = null;
      this.address2 = null;
      this.signalValue = null;
    } else if (accessoiry.isSignal()) {
      this.accessoryType = AccessoryType.SIGNAL;
      this.address = accessoiry.getAddress();
      this.address2 = ((SignalBean) accessoiry).getAddress2();
      this.value = accessoiry.getValue();
      this.value2 = ((SignalBean) accessoiry).getValue2();
      this.signalValue = ((SignalBean) accessoiry).getSignalValue();
    } else {
      this.accessoryType = AccessoryType.GENERAL;
      this.address = accessoiry.getAddress();
      this.value = accessoiry.getValue();
      this.value2 = null;
      this.address2 = null;
      this.signalValue = null;
    }
  }

  public boolean isSignal() {
    return AccessoryType.SIGNAL.equals(this.accessoryType);
  }

  public boolean isTurnout() {
    return AccessoryType.TURNOUT.equals(this.accessoryType);
  }

  public boolean isEventFor(SolenoidAccessory accessoiry) {
    if (accessoiry.isTurnout()) {
      return Objects.equals(this.address, accessoiry.getAddress());
    } else if (accessoiry.isSignal()) {
      if (!Objects.equals(this.address, accessoiry.getAddress())) {
        return false;
      }
      return Objects.equals(this.address2, ((SignalBean) accessoiry).getAddress2());
    } else {
      return Objects.equals(this.address, accessoiry.getAddress());
    }
  }

  public AccessoryType getAccessoryType() {
    return accessoryType;
  }

  public Integer getAddress() {
    return address;
  }

  public AccessoryValue getValue() {
    return value;
  }

  public Integer getAddress2() {
    return address2;
  }

  public AccessoryValue getValue2() {
    return value2;
  }

  public SignalValue getSignalValue() {
    return signalValue;
  }


}
