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

import java.util.Date;

public class FeedbackModule extends ControllableItem {

  private static final long serialVersionUID = 5051810045476494800L;

  private final int ports;

  private boolean port1;
  private boolean port2;
  private boolean port3;
  private boolean port4;
  private boolean port5;
  private boolean port6;
  private boolean port7;
  private boolean port8;
  private boolean port9;
  private boolean port10;
  private boolean port11;
  private boolean port12;
  private boolean port13;
  private boolean port14;
  private boolean port15;
  private boolean port16;

  private Date lastUpdated;
  private Integer[] response;

  public FeedbackModule(Integer moduleNumber, String catalogNumber, Integer ports) {
    this(moduleNumber, catalogNumber, ports, null, null);
  }

  private FeedbackModule(Integer moduleNumber, String catalogNumber, Integer ports, Integer[] response, Date lastUpdated) {
    super(moduleNumber, catalogNumber);
    this.ports = ports;
    this.response = response;
    this.lastUpdated = lastUpdated;
    setPortStatuses();
  }

  public int getModuleNumber() {
    return this.address;
  }

  public void requestFeedback() {
    this.handleAttributeChange("requestFeedback", null, null);
  }

  private void setPortStatuses() {

    if (response == null) {
      return;
    }
    int lsb = response[0];
    int msb = response[1];
    
    port1 = (lsb & 128) != 0;
    port2 = (lsb & 64) != 0;
    port3 = (lsb & 32) != 0;
    port4 = (lsb & 16) != 0;
    port5 = (lsb & 8) != 0;
    port6 = (lsb & 4) != 0;
    port7 = (lsb & 2) != 0;
    port8 = (lsb & 1) != 0;

    port9 = (msb & 128) != 0;
    port10 = (msb & 64) != 0;
    port11 = (msb & 32) != 0;
    port12 = (msb & 16) != 0;
    port13 = (msb & 8) != 0;
    port14 = (msb & 4) != 0;
    port15 = (msb & 2) != 0;
    port16 = (msb & 1) != 0;

    this.lastUpdated = new Date();
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("Module: ");
    sb.append(getModuleNumber());
    sb.append(" P1: ");
    sb.append(toInt(port1));
    sb.append(" P2: ");
    sb.append(toInt(port2));
    sb.append(" P3: ");
    sb.append(toInt(port3));
    sb.append(" P3: ");
    sb.append(toInt(port2));
    sb.append(" P4: ");
    sb.append(toInt(port4));
    sb.append(" P5: ");
    sb.append(toInt(port5));
    sb.append(" P6: ");
    sb.append(toInt(port6));
    sb.append(" P7: ");
    sb.append(toInt(port7));
    sb.append(" P8: ");
    sb.append(toInt(port8));
    sb.append(" P9: ");
    sb.append(toInt(port9));
    sb.append(" P10: ");
    sb.append(toInt(port10));
    sb.append(" P11: ");
    sb.append(toInt(port11));
    sb.append(" P12: ");
    sb.append(toInt(port12));
    sb.append(" P13: ");
    sb.append(toInt(port13));
    sb.append(" P14: ");
    sb.append(toInt(port14));
    sb.append(" P15: ");
    sb.append(toInt(port15));
    sb.append(" P16: ");
    sb.append(toInt(port16));
    sb.append(" Last Updated: ");
    sb.append(this.lastUpdated);

    return sb.toString();
  }

  private int toInt(boolean b) {
    if (b) {
      return 1;
    }
    return 0;
  }

  public boolean isPort1() {
    return port1;
  }

  public boolean isPort2() {
    return port2;
  }

  public boolean isPort3() {
    return port3;
  }

  public boolean isPort4() {
    return port4;
  }

  public boolean isPort5() {
    return port5;
  }

  public boolean isPort6() {
    return port6;
  }

  public boolean isPort7() {
    return port7;
  }

  public boolean isPort8() {
    return port8;
  }

  public boolean isPort9() {
    return port9;
  }

  public boolean isPort10() {
    return port10;
  }

  public boolean isPort11() {
    return port11;
  }

  public boolean isPort12() {
    return port12;
  }

  public boolean isPort13() {
    return port13;
  }

  public boolean isPort14() {
    return port14;
  }

  public boolean isPort15() {
    return port15;
  }

  public boolean isPort16() {
    return port16;
  }

  public int getPorts() {
    return ports;
  }

  public Date getLastUpdated() {
    return lastUpdated;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 53 * hash + this.ports;
    hash = 53 * hash + (this.port1 ? 1 : 0);
    hash = 53 * hash + (this.port2 ? 1 : 0);
    hash = 53 * hash + (this.port3 ? 1 : 0);
    hash = 53 * hash + (this.port4 ? 1 : 0);
    hash = 53 * hash + (this.port5 ? 1 : 0);
    hash = 53 * hash + (this.port6 ? 1 : 0);
    hash = 53 * hash + (this.port7 ? 1 : 0);
    hash = 53 * hash + (this.port8 ? 1 : 0);
    hash = 53 * hash + (this.port9 ? 1 : 0);
    hash = 53 * hash + (this.port10 ? 1 : 0);
    hash = 53 * hash + (this.port11 ? 1 : 0);
    hash = 53 * hash + (this.port12 ? 1 : 0);
    hash = 53 * hash + (this.port13 ? 1 : 0);
    hash = 53 * hash + (this.port14 ? 1 : 0);
    hash = 53 * hash + (this.port15 ? 1 : 0);
    hash = 53 * hash + (this.port16 ? 1 : 0);
    //hash = 53 * hash + Objects.hashCode(this.lastUpdated);
    hash = 53 * hash + super.hashCode();
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
    final FeedbackModule other = (FeedbackModule) obj;
    if (this.ports != other.ports) {
      return false;
    }
    if (this.port1 != other.port1) {
      return false;
    }
    if (this.port2 != other.port2) {
      return false;
    }
    if (this.port3 != other.port3) {
      return false;
    }
    if (this.port4 != other.port4) {
      return false;
    }
    if (this.port5 != other.port5) {
      return false;
    }
    if (this.port6 != other.port6) {
      return false;
    }
    if (this.port7 != other.port7) {
      return false;
    }
    if (this.port8 != other.port8) {
      return false;
    }
    if (this.port9 != other.port9) {
      return false;
    }
    if (this.port10 != other.port10) {
      return false;
    }
    if (this.port11 != other.port11) {
      return false;
    }
    if (this.port12 != other.port12) {
      return false;
    }
    if (this.port13 != other.port13) {
      return false;
    }
    if (this.port14 != other.port14) {
      return false;
    }
    if (this.port15 != other.port15) {
      return false;
    }
    if (this.port16 != other.port16) {
      return false;
    }
    //if (!Objects.equals(this.lastUpdated, other.lastUpdated)) {
    //  return false;
    //}
    return super.equals(other);
  }

  public Integer[] getResponse() {
    return response;
  }

  public void setResponse(Integer[] response) {
    this.response = response;
    setPortStatuses();
  }

  @Override
  public FeedbackModule copy() {
    return new FeedbackModule(this.address, this.catalogNumber, this.ports, this.response, this.lastUpdated);
  }
}
